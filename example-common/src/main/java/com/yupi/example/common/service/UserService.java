package com.yupi.example.common.service;

import com.yupi.example.common.model.User;

/**
 * 用户服务
 */
public interface UserService {
    /**
     * 获取用户接口
     * @param user 用户
     * @return 用户
     */
    User getUser(User user);

    /**
     * 新方法：获取数字，用于测试Mock
     * @return
     */
    default int getNumber(){
        return 1;
    }
}
