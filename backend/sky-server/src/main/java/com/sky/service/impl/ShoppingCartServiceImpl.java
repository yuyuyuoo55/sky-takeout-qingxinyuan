package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    //添加商品
    @Override

    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前商品是否在购物车中,如果在商品数量+1
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long id = BaseContext.getCurrentId();//用户id
        shoppingCart.setUserId(id);

        List<ShoppingCart> shoppingCartlist =shoppingCartMapper.selectCart(shoppingCart);//查询商品是否在购物车中
        if(shoppingCartlist.size()==1&& shoppingCartlist !=null)//每次添加商品就是一次请求
        {
            shoppingCart= shoppingCartlist.get(0);//所以只有一条数据
            shoppingCart.setNumber(shoppingCart.getNumber()+1);//新增商品数量
            //更改数据
            shoppingCartMapper.updateData(shoppingCart);
            return;
        }

        //如果不在将当前商品添加到购物车中
        //判断商品是菜品还是套餐
        Long dishId = shoppingCart.getDishId();//菜品id
        Long setmealId = shoppingCart.getSetmealId();//套餐id
            if(dishId !=null) //商品为菜品
            {
                //查询菜品基本信息
                Dish dish = dishMapper.selectById(dishId);

                shoppingCart.setAmount(dish.getPrice());//设置商品价格
                shoppingCart.setImage(dish.getImage());//设置图像
                shoppingCart.setName(dish.getName());//设置商品名称

                //插入数据到数据库中
            }
            else //商品为套餐
            {
                //查询套餐基本信息
                Setmeal setmeal = setmealMapper.selectById(setmealId);

                shoppingCart.setAmount(setmeal.getPrice());//设置商品价格
                shoppingCart.setCreateTime(LocalDateTime.now());//设置创建商品时间
                shoppingCart.setImage(setmeal.getImage());//设置图像
                shoppingCart.setName(setmeal.getName());//设置商品名称

                //插入数据到数据库中
            }

            shoppingCart.setNumber(1);//设置数量
            shoppingCart.setCreateTime(LocalDateTime.now());//设置创建商品时间
            shoppingCartMapper.insert(shoppingCart);
    }

    //查看购物车
    @Override
    public List<ShoppingCart> showCart() {
        Long user_id = BaseContext.getCurrentId();
        ShoppingCart shoppingCart=ShoppingCart.builder()
                        .userId(user_id)
                        .build();

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectCart(shoppingCart);

        return shoppingCartList;
    }

    //清空购物车
    @Override
    public void clean() {
        Long user_id = BaseContext.getCurrentId();
        shoppingCartMapper.clean(user_id);
    }

    //删除某一个商品
    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);

        shoppingCart.setUserId(BaseContext.getCurrentId());//设置用户id

        //判断商品是否在购物车中
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectCart(shoppingCart);
        if(shoppingCartList!=null && shoppingCartList.size()==1)//商品在购物车中
        {
            shoppingCart = shoppingCartList.get(0);
            //如果商品数量大于1，将数量减1
            if(shoppingCart.getNumber() > 1) {
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateData(shoppingCart);
            } else {
                //如果商品数量为1，直接删除整个商品
                shoppingCartMapper.delete(shoppingCart);
            }
        }
    }
}
