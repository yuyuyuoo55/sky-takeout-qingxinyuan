package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    //新增员工
    void save(EmployeeDTO employeeDTO);

    //员工信息分页查询
    PageResult pageSelect(EmployeePageQueryDTO employeePageQueryDTO);

    //启用禁用员工账号
    void startOrStop(Integer status, Long id);

    //根据id查询员工信息
    Employee selectById(Integer id);

    //修改员工信息
    void updateEmployee(EmployeeDTO employeeDTO);
}
