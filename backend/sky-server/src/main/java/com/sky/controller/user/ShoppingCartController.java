package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端- 购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    //添加商品到购物车
    @PostMapping("/add")
    public Result addCart(@RequestBody ShoppingCartDTO shoppingCartDTO)
    {
        log.info("商品添加:{}",shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    //查看购物车
    @GetMapping("/list")
    public Result<List<ShoppingCart>> showCart()
    {
        log.info("查看购物车:");
        List<ShoppingCart> shoppingCart=shoppingCartService.showCart();
        return Result.success(shoppingCart);
    }

    //清空购物车
    @DeleteMapping("/clean")
    public Result cleanCart()
    {
        log.info("清空购物车: ");
        shoppingCartService.clean();
        return Result.success();
    }

    //删除某一个商品
    @PostMapping("/sub")
    public Result delete(@RequestBody ShoppingCartDTO shoppingCartDTO)
    {
        log.info("删除某一个商品:{}",shoppingCartDTO);
        shoppingCartService.delete(shoppingCartDTO);
        return Result.success();
    }
}
