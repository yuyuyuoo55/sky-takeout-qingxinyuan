package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetmealService {

    //新增套餐
    void addMeal(SetmealDTO setmealDTO);

    //套餐的分页查询
    PageResult pageSelect(SetmealPageQueryDTO setmealPageQueryDTO);

    //批量删除套餐
    void deleteSetmeal(List<Long> ids);

    //根据id查询套餐
    SetmealVO selectById(Long id);

    //修改套餐
    void updateSetmeal(SetmealVO setmealVO);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

    //套餐的起售和停售
    void startOrStop(Integer status, Integer id);
}
