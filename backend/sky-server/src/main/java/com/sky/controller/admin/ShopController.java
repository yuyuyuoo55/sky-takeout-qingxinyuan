package com.sky.controller.admin;

import com.sky.config.RedisConfiguration;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
//店铺状态设置的controller
public class ShopController {

    public final String KEY="SHOP_STATUS";

    @Autowired
    public RedisTemplate redisTemplate;

    //设置营业状态
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status)
    {
        log.info("设置营业状态:{}",status==1 ? "营业中" : "已打烊");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    //获取营业状态
    @GetMapping("/status")
    public Result<Integer> getStatus()
    {
        Integer status =(Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取营业状态:{}",status==1? "营业中" : "已打烊" );
        return Result.success(status);
    }
}
