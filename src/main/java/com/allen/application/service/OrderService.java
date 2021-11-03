package com.allen.application.service;

import com.allen.application.pojos.Order;
import com.allen.spring.annotations.Autowired;
import com.allen.spring.annotations.Component;

@Component
public class OrderService {

    @Autowired
    Order order;

    public OrderService() {
        System.out.println("OrderService初始化...");
    }

    public String getOrderInfo() {
        return order.toString();
    }
}
