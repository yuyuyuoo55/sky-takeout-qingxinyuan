package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import org.springframework.stereotype.Service;

public interface UserService {
    //微信用户登录的service
    User wxLogin(UserLoginDTO userLoginDTO);
}
