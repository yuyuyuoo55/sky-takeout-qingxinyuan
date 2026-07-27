package com.sky.controller.admin;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.UserType;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtService;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录，用户名：{}", employeeLoginDTO.getUsername());

        Employee employee = employeeService.login(employeeLoginDTO);

        // 登录成功后，生成 jwt 令牌
        String token = jwtService.createAdminToken(employee.getId());

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getAdmin().getTokenName());
        if (token != null && !token.isEmpty()) {
            jwtService.invalidateToken(token, UserType.ADMIN);
        }
        return Result.success();
    }

    /**
     * 新增员工
     */
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工:{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工信息分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> pageSelect(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工信息分页查询:{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageSelect(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用员工账号:{},{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据id查询员工信息
     */
    @GetMapping("/{id}")
    public Result<Employee> selectById(@PathVariable Integer id) {
        log.info("根据id查询员工信息:{}", id);
        Employee employee = employeeService.selectById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     */
    @PutMapping
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("修改员工信息:{}", employeeDTO);
        employeeService.updateEmployee(employeeDTO);
        return Result.success();
    }
}
