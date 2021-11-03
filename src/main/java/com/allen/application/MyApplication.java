package com.allen.application;

import com.allen.application.service.UserService;
import com.allen.spring.AllenApplicationContext;


public class MyApplication {

    public static void main(String[] args) {

        AllenApplicationContext applicationContext = new AllenApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.getOrder();
    }
}
