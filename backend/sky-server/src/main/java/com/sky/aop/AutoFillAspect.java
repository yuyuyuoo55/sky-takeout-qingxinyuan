package com.sky.aop;

//自定义切面类

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Slf4j
@Aspect
public class AutoFillAspect {

    //1.定义切入点
    //2.定义切入点表达式
//    切入点表达式: 1.execution : 根据方法的签名来匹配  "execution(权限修饰符(可省略) 返回值 包名.类名.方法名(形参) throws 异常(可省略))"
//                2.@annotation : 根据注解来匹配

    //该注解的作用是将公共的切入点表达式抽取出来，需要用到时引用该切点表达式即可。
    @Pointcut("execution(* com.sky.mapper.*.*(..))  && @annotation(com.sky.annotation.AutoFill)")
    public void AutoFillAspectCut() {
    }

    //前置通知，在通知中进行公共字段的赋值
    @Before("AutoFillAspectCut()")
    public void AutoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的赋值...");

        //1.确定使用该注解的参数类型    update insert
        //a.获取到方法的签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //b.从方法的签名获取传递的注解
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //c.从注解中获取对应的值
        OperationType type = autoFill.type();

        //2.获取方法的形参 ---实体类对象
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || args == null) {
            return;
        }
        Object o = args[0];//实体类对象
        //3.根据操作类型的不同为实体类对象的属性赋值
        //a.如果为插入数据
        if (type == OperationType.INSERT) {
            //为四个公共字段赋值
            try {
                Method setCreateTime = o.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = o.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = o.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = o.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);


                //通过反射为对象的属性赋值
                setCreateTime.invoke(o, LocalDateTime.now());
                setCreateUser.invoke(o, BaseContext.getCurrentId());

                setUpdateTime.invoke(o, LocalDateTime.now());
                setUpdateUser.invoke(o, BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //b.如果为更新数据
        else if (type == OperationType.UPDATE) {
            try {
                Method setUpdateTime = o.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = o.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(o, LocalDateTime.now());
                setUpdateUser.invoke(o, BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}