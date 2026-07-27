package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    public final String WX_LOGIN="https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    //微信用户登录的service
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //向微信后端服务器发送请求，获取用户唯一标识openid,需要携带appId+appsecret+code三个参数。
        String openid = getOpenid(userLoginDTO);
        //判断openid是否为空,如果为空则说明请求失败
        if(openid == null)
        {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //如果不为空，请求系统后端数据库判断用户是否存在
        User user=userMapper.selectByOpenid(openid);
        //如果不存在，在数据库中创建新用户
        if(user == null)
        {
            user=User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.createUser(user);
        }

        return user;
    }

    private String getOpenid(UserLoginDTO userLoginDTO) {
        Map <String,String>map=new HashMap();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN,map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
