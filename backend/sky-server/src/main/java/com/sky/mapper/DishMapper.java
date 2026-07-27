package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    //添加菜品
    @AutoFill(type = OperationType.INSERT)
    void save(Dish dish);

    //分页查询菜品
    Page<Dish> pageSelect(Dish dish);

    //删除菜品
    void deleteDish(List<Long> ids);

    //根据id查询菜品数据
    @Select("select * from dish where id=#{id}")
    Dish selectById(Long id);

    //更新菜品信息
    @AutoFill(type = OperationType.UPDATE)
    void updateDish(Dish dish);

    //根据分类id查询菜品
    @Select("select * from dish where category_id=#{categoryId}")
    List<Dish> selectByCategoryId(Integer categoryId);

    //菜品起售或停售
    @Select("update dish set status=#{status} where id=#{id}")
    void DishStartOrStop(Integer status, Long id);
    
    //删除菜品口味
    void deleteByDishId(Long dishId);
    
    //保存菜品口味
    void saveFlavors(List<DishFlavor> flavors);

        /**
         * 根据条件统计菜品数量
         * @param map
         * @return
         */
        Integer countByMap(Map map);
}