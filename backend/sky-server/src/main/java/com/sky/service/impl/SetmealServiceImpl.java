package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

//套餐管理的service
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    //新增套餐
    @Override
    @Transactional
    public void addMeal(SetmealDTO setmealDTO) {
        //1.将请求值封装到实体类中
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //2.1把套餐基础属性交给mapper
        setmealMapper.addMeal(setmeal);

        //2.2把套餐相关的菜品属性交给mapper
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));

        setmealDishMapper.addMealDish(setmealDishes);
    }

    //套餐的分页查询
    @Override
    public PageResult pageSelect(SetmealPageQueryDTO setmealPageQueryDTO) {
        //1.使用分页插件PageHelper
        //a.获取当前页码
        int nowPage = setmealPageQueryDTO.getPage();
        //b.获取每页记录数
        int pageSize = setmealPageQueryDTO.getPageSize();
        PageHelper.startPage(nowPage,pageSize);

        //调用持久层方法
        Page<Setmeal> page=setmealMapper.pageSelect(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    //批量删除套餐
    @Override
    @Transactional
    public void deleteSetmeal(List<Long> ids) {
        //删除套餐
        setmealMapper.deletemeal(ids);

        //删除套餐包含的菜品
        setmealDishMapper.deletemealDish(ids);
    }

    //根据id查询套餐
    @Override
    @Transactional
    //套餐id
    public SetmealVO selectById(Long id) {
        //查询套餐的基本信息
        Setmeal setmeal = setmealMapper.selectById(id);

        //查询套餐包含的菜品
        List<SetmealDish> setmealDish= setmealDishMapper.selectByid(id);

        //创建返回值对象
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);

        setmealVO.setSetmealDishes(setmealDish);

        return setmealVO;
    }

    //修改套餐
    @Override
    @Transactional
    public void updateSetmeal(SetmealVO setmealVO) {
        //1.修改套餐的基本信息
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealVO,setmeal);
        setmealMapper.updateSetmeal(setmeal);

        //2.修改套餐包含的相关菜品
        List<SetmealDish> setmealDishes = setmealVO.getSetmealDishes();

        //先删除原有套餐菜品关系
        setmealDishMapper.deletemealDish(Arrays.asList(setmealVO.getId()));
        
        //重新添加套餐菜品关系
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealVO.getId());
            });
            setmealDishMapper.addMealDish(setmealDishes);
        }
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    //套餐的起售和停售
    @Override
    public void startOrStop(Integer status, Integer id) {
        setmealMapper.startOrStop(status,id);
    }
}