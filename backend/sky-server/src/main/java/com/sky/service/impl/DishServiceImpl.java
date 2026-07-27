package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addDish(DishDTO dishDTO) {
        // 1. DTO转DO（修正属性拷贝方向）
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish); // 反向拷贝导致字段为空
        
        // 2. 添加菜品
        dishMapper.save(dish);
        
        // 3. 添加口味（假设菜品ID已生成）
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(!flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.save(flavors);
        }
    }

    //分页查询菜表
    @Override
    public PageResult pageSelect(DishPageQueryDTO dishPageQueryDTO) {
        //1.使用分页插件
        int page = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();
        PageHelper.startPage(page,pageSize);

        //2.为实体类的属性赋值
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishPageQueryDTO,dish);
        Page<Dish> dishes=dishMapper.pageSelect(dish);

        return new PageResult(dishes.getTotal(),dishes.getResult());
    }

    //删除菜品
    @Override
    public void deleteDish(List<Long> ids) {
        dishMapper.deleteDish(ids);
    }

    //根据id查询菜品
    @Override
    public DishVO selectById(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.selectById(id);

        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectById(id);//后绪步骤实现

        //将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    //更新菜品信息
    @Override
    @Transactional
    public void updateDish(DishDTO dishDTO) {
        //1.更新菜品信息
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.updateDish(dish);
        
        //2.更新菜品口味信息
        //先删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        
        //再插入新的口味数据
        List<DishFlavor> dishFlavors=dishDTO.getFlavors();
        if(dishFlavors != null && !dishFlavors.isEmpty()){
            //为每个口味对象设置菜品ID
            dishFlavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }

    //根据分类id查询菜品
    @Override
    public List<Dish> selectByCategoryId(Integer categoryId) {
        return dishMapper.selectByCategoryId(categoryId);
    }

    //菜品起售和停售
    @Override
    @Transactional
    public void DishStartOrStop(Integer status, Long id) {
        dishMapper.DishStartOrStop(status,id);

        //需要手动设置菜品的更新时间和更新用户,因为公共字段赋值传递的形参只能是实体类对象
        Dish dish=new Dish();
        dish.setId(id);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());

        dishMapper.updateDish(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.pageSelect(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectById(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}