package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//套餐管理的controller
@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    //新增套餐
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache" ,key = "#setmealDTO.categoryId")
    public Result addMeal(@RequestBody SetmealDTO setmealDTO)
    {
        log.info("新增套餐:{}",setmealDTO);
        setmealService.addMeal(setmealDTO);
        return Result.success();
    }

    //套餐的分页查询
    @GetMapping("/page")
    public Result<PageResult> pageSelect(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        log.info("套餐的分页查询:{}",setmealPageQueryDTO);
        PageResult pageResult=setmealService.pageSelect(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    //删除套餐
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache" ,allEntries = true)
    public Result deleteSetmeal(@RequestParam List<Long> ids)
    {
        log.info("批量删除套餐:{}",ids);
        if(!ids.isEmpty()) {
            setmealService.deleteSetmeal(ids);
            return Result.success();
        }
        log.error("没有选择套餐");
        return Result.error("没有选择套餐");
    }

    //根据id查询套餐
    @GetMapping("/{id}")
    public Result<SetmealVO> selectById(@PathVariable Long id)
    {
        log.info("根据id查询套餐:{}",id);
        SetmealVO setmealVo=setmealService.selectById(id);
        return Result.success(setmealVo);
    }

    //修改套餐
    @PutMapping
    @CacheEvict(cacheNames = "setmealCache" ,key = "#setmealVO.categoryId")
    public Result updateSetmeal(@RequestBody SetmealVO setmealVO){
        log.info("修改套餐信息:{}",setmealVO);
        setmealService.updateSetmeal(setmealVO);
        return Result.success();
    }

    //套餐的起售停售
    @PostMapping("/status/{status}")
   public Result StartOrStopSetmeal(@PathVariable Integer status,Integer id)
   {
        log.info("套餐的起售和停售:,{}" ,status);
        log.info("套餐id:{}",id);
        setmealService.startOrStop(status,id);
        return Result.success();
   }
}
