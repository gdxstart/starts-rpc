package com.yupi.example.common.model;

import java.io.Serializable;

/**
 * 用户
 * Serializable:对象实现序列化接口，方便网络传输数据
 */
public class User implements Serializable {

    private String name;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
}
