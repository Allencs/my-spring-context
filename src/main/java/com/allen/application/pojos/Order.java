package com.allen.application.pojos;

import com.allen.spring.annotations.Component;
import com.allen.spring.annotations.Value;

@Component
public class Order {

    @Value("123456")
    String orderNum;
    @Value("测试商品")
    String product;
    @Value("99.99")
    Float price;
    @Value("100")
    Integer size;
    @Value("this is test product")
    String message;

    public Order() {
        System.out.println("Order订单初始化...");
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNum='" + orderNum + '\'' +
                ", product='" + product + '\'' +
                ", price=" + price +
                ", size=" + size +
                ", message='" + message + '\'' +
                '}';
    }
}
