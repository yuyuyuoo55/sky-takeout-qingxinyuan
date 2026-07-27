package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderHistoryVo;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;

//订单管理Service
public interface OrderService {
    //用户下单
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    //查询历史订单
    PageResult selectOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    //查询订单详情
    OrderHistoryVo selectOrderById(Integer id);

    //取消订单
    void cancelOrderById(Integer id);

    //再来一单
    void orderOneMore(Integer id);

    //订单搜索
    PageResult findOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    //查询各个订单状态数量
    OrderStatisticsVO getOrderStatusNum();

    //接单
    void acceptOrder(Integer id);

    //拒单
    void rejectOrderById(OrdersRejectionDTO ordersRejectionDTO);

    //取消订单
    void cancelOrder(OrdersCancelDTO ordersCancelDTO);

    //派单
    void deliverOrderById(Integer id);

    //完成订单
    void compeleteOrderById(Integer id);

    //催单
    void dealUrgeOrder(Integer id);

//    //催单
//    String dealUrgeOrder(Integer id);
}
