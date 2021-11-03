package com.allen.application.service;

import com.allen.spring.annotations.Autowired;
import com.allen.spring.annotations.Component;

@Component
public class UserService {

    @Autowired
    OrderService orderService;

    public UserService() {
        System.out.println("UserService初始化...");
    }

    public void getOrder() {
        System.out.println(orderService.getOrderInfo());
    }
}
