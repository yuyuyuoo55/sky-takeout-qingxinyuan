package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderHistoryVo;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//订单管理Controller
@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "订单管理")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //用户下单
    @ApiOperation(value = "用户下单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO)
    {
        log.info("用户下单:{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO=orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }


    //订单支付
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    //查询历史订单
    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result<PageResult> selectHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO)
    {
        log.info("查询历史订单:{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.selectOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    //查询订单详情
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderHistoryVo> selectOrder(@PathVariable Integer id)
    {
        log.info("根据订单id查询订单详细信息:{}",id);
        OrderHistoryVo orderHistoryVo=orderService.selectOrderById(id);
        return Result.success(orderHistoryVo);
    }

    //取消订单
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancelOrder(@PathVariable Integer id)
    {
        log.info("根据订单id取消订单:{}",id);
        orderService.cancelOrderById(id);
        return Result.success();
    }

    //再来一单
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result orderOneMore(@PathVariable Integer id)
    {
        log.info("再来一单订单id:{}",id);
        orderService.orderOneMore(id);
        return Result.success();
    }

    //催单
    @GetMapping("/reminder/{id}")
    @ApiOperation("客户催单")
    public Result urgeOrder(@PathVariable Integer id)
    {
        log.info("客户催单:{}",id);
        orderService.dealUrgeOrder(id);
        return Result.success();
    }
}
