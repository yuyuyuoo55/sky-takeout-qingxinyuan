package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    //查询商品是否在购物车中
    List<ShoppingCart> selectCart(ShoppingCart shoppingCart);

    //更新商品数据
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateData(ShoppingCart shoppingCart);

    //保存商品到购物车中
    @Insert("insert into shopping_cart (name, image, dish_id, setmeal_id, dish_flavor, number, amount, create_time, user_id)" +
            "values (#{name},#{image},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime},#{userId})")
    void insert(ShoppingCart shoppingCart);

    //清空购物车
    @Delete("delete from shopping_cart where user_id=#{userId}")
    void clean(Long user_id);
    
    //删除购物车中某个商品
    void delete(ShoppingCart shoppingCart);
}