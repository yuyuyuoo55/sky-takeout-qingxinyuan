package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.SalesTop10ReportVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
//订单管理的Mapper
public interface OrderMapper {
    //插入订单数据
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 用于替换微信支付更新数据库状态的问题
     *
     * @param orderStatus
     * @param orderPaidStatus
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} " +
            "where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, String orderNumber);


    //根据用户id查询订单
    @Select("select * from orders where user_id=#{userId} order by order_time desc ")
    List<Orders> selectOrders(Long userId);

    //根据订单id查询订单
    @Select("select * from orders where id=#{id}")
    Orders selectOrderById(Integer id);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Integer id);

    //查询订单
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    //查询各个订单状态数量
    @Select("select status,count(*) num from orders  group by status")
    List<Map<Object,Object>> getOrderStatusNum();

    //查询订单的状态与下单时间
    @Select("select * from orders where status=#{status} and order_time<#{time}")
    List<Orders> selectOrderStatusAndOrderTime(Integer status, LocalDateTime time);

    //根据订单号查询订单id
    @Select("select * from orders where number=#{orderNumber}")
    Long selectOrderIdByOrderNumber(String orderNumber);

    //根据日期时间获取订单实收金额
    @Select("select sum(amount) from orders where status=5 and checkout_time>#{begin_time} and checkout_time <#{end_time}")
    Integer getFinishOrdersByCheckoutTime(LocalDateTime begin_time,LocalDateTime end_time);

    //获取订单数量
    Integer countByMap(Map map);

    //查询销售top10
    List<GoodsSalesDTO> getTop10(LocalDateTime beginTime, LocalDateTime endTime);

    //查询营业额
    Double sumByMap(Map map);
}
