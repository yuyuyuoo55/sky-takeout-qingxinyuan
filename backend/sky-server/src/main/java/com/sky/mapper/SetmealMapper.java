package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    //新增套餐
    @AutoFill(type= OperationType.INSERT)
    void addMeal(Setmeal setmeal);

    //套餐的分页查询
    Page<Setmeal> pageSelect(SetmealPageQueryDTO setmealPageQueryDTO);

    //批量删除套餐
    void deletemeal(List<Long> ids);

    //根据id查询套餐
    @Select("select * from setmeal where id=#{id}")
    Setmeal selectById(Long id);

    //修改套餐的基本信息
    @AutoFill(type = OperationType.UPDATE)
    void updateSetmeal(Setmeal setmeal);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    //套餐的起售和停售
    @Update("update setmeal set status=#{status} where id=#{id};")
    void startOrStop(Integer status, Integer id);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}