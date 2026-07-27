package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    //订单明细表插入数据
    void insert(List<OrderDetail> orderDetails);

    //根据订单id查询详细数据
    @Select("select * from order_detail where order_id=#{orderId}")
    List<OrderDetail> selectById(Long orderId);

}
