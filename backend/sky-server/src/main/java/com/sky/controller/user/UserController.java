package com.sky.controller.user;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtService;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 controller
 */
@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public Result<UserLoginVO> wxLogin(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信登录用户的授权码:{}", userLoginDTO.getCode());

        // 微信登录，返回用户信息
        User user = userService.wxLogin(userLoginDTO);

        // 为微信用户生成 jwt 令牌
        String token = jwtService.createUserToken(user.getId());

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }
}
