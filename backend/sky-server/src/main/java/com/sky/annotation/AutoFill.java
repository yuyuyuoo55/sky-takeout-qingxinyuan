package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//自定义注解
@Target(ElementType.METHOD)//在什么地方使用该注解
@Retention(RetentionPolicy.RUNTIME)//什么时候运行该注解

//运行该注解时需要选择操作类型
public @interface AutoFill {
    //数据库操作类型   update,insert
    OperationType type();
}
