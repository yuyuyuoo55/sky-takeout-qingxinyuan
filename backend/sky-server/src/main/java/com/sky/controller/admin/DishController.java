package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
//菜品管理的controller
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate; 
    
    //菜品添加
    @PostMapping
    public Result addDish(@RequestBody DishDTO dishDTO)
    {
        log.info("菜品添加:{}",dishDTO);
        dishService.addDish(dishDTO);
        
        //当菜品添加时，清理缓存数据
        String key="dish_"+dishDTO.getCategoryId().toString();
        cleanCache(key);
        
        return Result.success();
    }

    //菜品分页查询
    @GetMapping("/page")
    public Result<PageResult> pageSelect(DishPageQueryDTO dishPageQueryDTO)
    {
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult=dishService.pageSelect(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    //批量删除菜品
    @DeleteMapping
    public Result deleteDish(@RequestParam List<Long> ids)
    {
        log.info("批量删除菜品:{}",ids);
        if(!ids.isEmpty()) {
            dishService.deleteDish(ids);

            cleanCache("dish_*");

            return Result.success();
        }
        log.error("没有选择菜品");
        return Result.error("没有选择菜品");
    }

    //根据id查询菜品
    @GetMapping("/{id}")
    public Result<DishVO> selectById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.selectById(id);//后绪步骤实现
        return Result.success(dishVO);
    }

    //修改菜品
    @PutMapping
    public Result updateDish(@RequestBody DishDTO dishDTO)
    {
        log.info("修改菜品:{}",dishDTO);
        dishService.updateDish(dishDTO);

        cleanCache("dish_*");
        return Result.success();
    }

    //根据分类id查询菜品
    @GetMapping("/list")
    public Result<List<Dish>> selectByCategoryId(Integer categoryId)
    {
        log.info("根据分类id查询菜品:{}",categoryId);
        List<Dish> dish=dishService.selectByCategoryId(categoryId);
        return Result.success(dish);
    }

    //菜品起售和停售
    @PostMapping("/status/{status}")
    public Result DishStartOrStop(@PathVariable Integer status,Long id)
    {
        log.info("菜品起售和停售:{},{}",status,id);
        dishService.DishStartOrStop(status,id);

        cleanCache("dish_*");
        return Result.success();
    }

    private void cleanCache(String pattern) {
        //删除菜品时,清理缓存数据
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
