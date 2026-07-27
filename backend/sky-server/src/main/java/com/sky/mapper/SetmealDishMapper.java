package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

//套餐跟菜品相关的mapper
@Mapper
public interface SetmealDishMapper {

    //新增套餐的菜品的关系
    void addMealDish(List<SetmealDish> setmealDishes);

    //查询套餐包含的菜品
    List<SetmealDish> selectByid(Long setmealId);

    //批量删除套餐相关的菜品
    void deletemealDish(List<Long> ids);
}
