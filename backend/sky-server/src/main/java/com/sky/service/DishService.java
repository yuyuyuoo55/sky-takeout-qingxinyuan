package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    //添加菜品
    void addDish(DishDTO dishDTO);

    //分页查询菜表
    PageResult pageSelect(DishPageQueryDTO dishPageQueryDTO);

    //删除菜品
    void deleteDish(List<Long> ids);

    //查询菜品
    DishVO selectById(Long id);

    //更新菜品信息
    void updateDish(DishDTO dishDTO);

    //根据分类id查询菜品
    List<Dish> selectByCategoryId(Integer categoryId);

    //菜品起售和停售
    void DishStartOrStop(Integer status, Long id);


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
