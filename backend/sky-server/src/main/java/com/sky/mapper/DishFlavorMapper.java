package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    //添加菜品口味
    void save(List<DishFlavor> flavors);

    //根据id查询菜品口味
    @Select("select * from dish_flavor where dish_id=#{dishId}")
    List<DishFlavor> selectById(Long dishId);
    
    //根据菜品id删除口味数据
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
    
    //批量插入菜品口味
    void insertBatch(List<DishFlavor> flavors);

    //更新菜品的口味信息
    void updateFlaveorDish(List<DishFlavor> dishFlavors);
}