package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    //添加商品
    void add(ShoppingCartDTO shoppingCartDTO);

    //查看购物车
    List<ShoppingCart> showCart();

    //清空购物车
    void clean();

    //删除某一个商品
    void delete(ShoppingCartDTO shoppingCartDTO);
}
