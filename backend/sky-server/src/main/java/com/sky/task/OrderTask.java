package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    //处理超时未支付订单
    @Scheduled(cron = "0 */15 * * * *")
//    @Scheduled(cron = "0/5 * * * * ?")//corn表达式
    public void processTimeOutPayOrder()
    {
        log.info("处理超时订单:{}", LocalDateTime.now());

        //查询当前订单处于待支付状态，且下单时间超过15分钟的订单 当前时间-15>下单时间
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList=orderMapper.selectOrderStatusAndOrderTime(Orders.PENDING_PAYMENT,time);

        if(ordersList != null && ordersList.size()>0)
        {
            for (Orders order : ordersList) {
                order.setCancelReason("订单超时,取消订单");
                order.setCancelTime(LocalDateTime.now());
                order.setStatus(Orders.CANCELLED);
                orderMapper.update(order);
            }
        }

    }

    //处理一直未完成订单
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0/5 * * * * ?")//corn表达式
//    @Scheduled(cron = "0 0/1 * * * *")
    public void processUnCompeletedOrder()
    {
        log.info("处理超时未完成订单:{}",LocalDateTime.now());

        //先查询哪些订单状态派送中，且下单时间>8h
        LocalDateTime time = LocalDateTime.now().plusHours(-8);
        List<Orders> ordersList = orderMapper.selectOrderStatusAndOrderTime(Orders.TO_BE_CONFIRMED, time);


        if(ordersList != null && ordersList.size()>0)
        {
            for (Orders order : ordersList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }

    //处理状态为已接单的订单
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0/5 * * * * ?")//corn表达式
    public void processReceivedOrder()
    {
        log.info("处理超时未完成订单:{}",LocalDateTime.now());

        //先查询哪些订单状态派送中，且下单时间>8h
        LocalDateTime time = LocalDateTime.now().plusHours(-8);
        List<Orders> ordersList = orderMapper.selectOrderStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);


        if(ordersList != null && ordersList.size()>0)
        {
            for (Orders order : ordersList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
