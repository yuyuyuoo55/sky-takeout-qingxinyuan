package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderHistoryVo;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    private Orders orders;

    //商家地址
    @Value("${sky.shop.address}")
    private String shopAddress;

    //用户申请注册的key
    @Value("${sky.baidu.ak}")
    private String ak;

    //导入WebSocket
    @Autowired
    private WebSocketServer webSocketServer;


    //用户下单
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常,判断当前用户地址铺是否为空,购物车是否为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();//获取地址铺id

        //查询地址簿表
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        //如果地址为空
        if(addressBook==null)
        {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //检查用户下单是否超出收货范围
        checkOutOfRange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());

        //查询当前用户的购物车信息
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectCart(shoppingCart);

        //如果购物车为空
        if(shoppingCartList.isEmpty() || shoppingCartList ==null)
        {
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //构造订单数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);

        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setUserId(userId);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());

        this.orders=orders;

        //往订单表插入1条数据
        orderMapper.insert(orders);
        //往订单详细表插入多条数据

        List<OrderDetail> orderDetails=new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insert(orderDetails);
        //清空当前购物车数据
        shoppingCartMapper.clean(userId);

        //返回vo对象
        OrderSubmitVO orderSubmitVO= OrderSubmitVO.builder()
                .orderAmount(orders.getAmount())
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception{
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*/

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        //获取订单号码
        String orderNumber = ordersPaymentDTO.getOrderNumber();

        log.info("调用updateStatus，用于替换微信支付更新数据库状态的问题");
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderNumber);

        //根据订单号码查询订单id
        Long orderId=orderMapper.selectOrderIdByOrderNumber(orderNumber);

        //来单提醒
        Map map=new HashMap();
        map.put("type",1);//消息提示,1:来当提醒,2:用户催单
        map.put("orderId",orderId);
        map.put("content","订单号: "+orderNumber);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

        return vo;
    }



    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();


        orderMapper.update(orders);
    }

    //查询历史订单
    @Transactional
    public PageResult selectOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        int page = ordersPageQueryDTO.getPage();//起始页码
        int pageSize = ordersPageQueryDTO.getPageSize();//每页展示数据
        PageHelper.startPage(page,pageSize);

        //根据用户id查询历史订单
        Long userId = BaseContext.getCurrentId();

        //查询订单表
        List<Orders> ordersList = orderMapper.selectOrders(userId);

        //将每个订单和订单的详细数据保存到新的集合中
        List<OrderHistoryVo> historyList = new ArrayList<>();

        //查询订单明细表
        for (Orders order : ordersList) {
            //根据订单id查询每个订单的详细数据
            Long orderId = order.getId();
            List<OrderDetail> orderDetailList = orderDetailMapper.selectById(orderId);

            //创建OrderHistoryVo对象并复制订单信息
            OrderHistoryVo orderHistoryVo = new OrderHistoryVo();
            BeanUtils.copyProperties(order, orderHistoryVo);
            //设置订单详情
            orderHistoryVo.setOrderDetailList(orderDetailList);

            //将OrderHistoryVo对象添加到列表中
            historyList.add(orderHistoryVo);
        }

        //转换为Page对象并返回结果
        Page<OrderHistoryVo> pages = new Page<>();
        pages.addAll(historyList);
        pages.setTotal(historyList.size());
        
        return new PageResult(pages.getTotal(), pages.getResult());
    }


    //查询订单详情
    @Transactional
    public OrderHistoryVo selectOrderById(Integer id) {
        Orders order = selectOrder(id);

        //查询订单明细表
        List<OrderDetail> orderDetailList=orderDetailMapper.selectById(id.longValue());

        OrderHistoryVo orderHistoryVo=new OrderHistoryVo();
        BeanUtils.copyProperties(order,orderHistoryVo);
        orderHistoryVo.setOrderDetailList(orderDetailList);

        return orderHistoryVo;
    }

    //查询订单表
    private Orders selectOrder(Integer id) {
        //不存在则抛出异常
        Orders order = orderMapper.selectOrderById(id);
        if(order ==null)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        return order;

    }

    //取消订单
    public void cancelOrderById(Integer id) {
        Orders order = selectOrder(id);

        order.setPayStatus(Orders.REFUND);

        //设置取消订单时间
        order.setCancelTime(LocalDateTime.now());


        //设置订单状态:
        order.setStatus(Orders.CANCELLED);
        orderMapper.update(order);
    }

    //再来一单
    //点击再来一单进入【点餐主页】，且该订单的所有餐品信息已加入至购物车
    public void orderOneMore(Integer id) {
        //查询订单是否存在
        Orders order = selectOrder(id);

        //查询原订单的详细信息
        List<OrderDetail> orderDetailList = orderDetailMapper.selectById(Long.valueOf(id));
        
        //将原订单的菜品信息添加到购物车
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCartList.add(shoppingCart);
        }
        
        //批量插入购物车
        for (ShoppingCart shoppingCart : shoppingCartList) {
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    //订单搜索
    public PageResult findOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        int page = ordersPageQueryDTO.getPage();
        int pageSize = ordersPageQueryDTO.getPageSize();

        PageHelper.startPage(page,pageSize);//分页操作
        Orders order=new Orders();
        BeanUtils.copyProperties(ordersPageQueryDTO,order);

        Page<Orders> pages= orderMapper.pageQuery(ordersPageQueryDTO);

        return new PageResult(pages.getTotal(),pages.getResult());
    }

    //查询各个订单状态数量
//查询各个订单状态数量
    public OrderStatisticsVO getOrderStatusNum() {
        List<Map<Object,Object>> orderMapList = orderMapper.getOrderStatusNum();
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();

        for (Map<Object, Object> map : orderMapList) {
            // 使用Number类型接收，避免类型转换问题
            Number statusNum = (Number) map.get("status");
            Number countNum = (Number) map.get("num");


            if (statusNum != null && countNum != null) {
             int status = statusNum.intValue();
             int num = countNum.intValue();

                switch (status) {
                    case 2 : orderStatisticsVO.setToBeConfirmed(num);   break;     // 待接单
                    case 3 : orderStatisticsVO.setConfirmed(num);       break;// 待派送
                    case 4 : orderStatisticsVO.setDeliveryInProgress(num); break;   // 派送中
                }
            }
        }
        return orderStatisticsVO;
    }

    //接单
    public void acceptOrder(Integer id) {
        //先查询订单是否存在
        Orders order = selectOrder(id);

        order.setStatus(3);
        orderMapper.update(order);
    }

    //拒单
    public void rejectOrderById(OrdersRejectionDTO ordersRejectionDTO) {
        //先查询订单受否存在
        Orders order = selectOrder(ordersRejectionDTO.getId().intValue());

        order.setStatus(6);
        order.setCancelTime(LocalDateTime.now());
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        order.setAmount(BigDecimal.ZERO);

        orderMapper.update(order);
    }

    //取消订单
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        //先查询订单受否存在
        Orders order = selectOrder(ordersCancelDTO.getId().intValue());

        order.setStatus(6);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setAmount(BigDecimal.ZERO);

        orderMapper.update(order);
    }

    //派送订单
    public void deliverOrderById(Integer id) {
        //先查询订单受否存在
        Orders order = selectOrder(id);

        order.setStatus(4);
        orderMapper.update(order);
    }

    //完成订单
    public void compeleteOrderById(Integer id) {
        //先查询订单受否存在
        Orders order = selectOrder(id);

        order.setStatus(5);
        order.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(order);
    }

    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

    //催单
    public void dealUrgeOrder(Integer id) {
        //先查询订单是否存在
        Orders order = selectOrder(id);

        //查询订单是否状态为已支付
        Integer status = order.getStatus();

        if(status == Orders.REFUND)
        {
            //用户催单
            Map map=new HashMap();
            map.put("type",2);//消息提示,1:来当提醒,2:用户催单
            map.put("orderId",id);
            map.put("content","订单号: "+order.getNumber());

            String json = JSON.toJSONString(map);
            webSocketServer.sendToAllClient(json);
        }

    }

}
