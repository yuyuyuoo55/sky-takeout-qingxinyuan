package com.sky.controller.admin;

import com.sky.dto.OrdersAcceptDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderHistoryVo;
import com.sky.vo.OrderReportVO;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单管理
 */
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> selectOrder( OrdersPageQueryDTO ordersPageQueryDTO)
    {
        log.info("查询订单:{}",ordersPageQueryDTO);
        PageResult pageResult = orderService.findOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("各个订单状态数量")
    public Result<OrderStatisticsVO> getOrderStatusNum()
    {
        log.info("查询各个订单状态数量");
        OrderStatisticsVO orderStatisticsVO=orderService.getOrderStatusNum();
        return Result.success(orderStatisticsVO);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderHistoryVo> selectOrder(@PathVariable Integer id)
    {
        log.info("查询订单:{}",id);
        OrderHistoryVo orderHistoryVo=orderService.selectOrderById(id);
        return Result.success(orderHistoryVo);
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result acceptOrder(@RequestBody OrdersAcceptDTO ordersAcceptDTO) {
        log.info("接收订单id:{}",ordersAcceptDTO.getId());
        orderService.acceptOrder(ordersAcceptDTO.getId().intValue());
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO)
    {
        log.info("拒单:{}",ordersRejectionDTO);
        orderService.rejectOrderById(ordersRejectionDTO);
        return Result.success();
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO)
    {
        log.info("取消订单;{}",ordersCancelDTO);
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }

    @ApiOperation("派送订单")
    @PutMapping("/delivery/{id}")
    public Result deliveryOrder(@PathVariable Integer id)
    {
        log.info("根据订单id派送:{}",id);
        orderService.deliverOrderById(id);
        return Result.success();
    }

    @ApiOperation("完成订单")
    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Integer id)
    {
        log.info("根据订单id完成:{}",id);
        orderService.compeleteOrderById(id);
        return Result.success();
    }
}
