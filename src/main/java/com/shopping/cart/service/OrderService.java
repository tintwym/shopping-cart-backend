package com.shopping.cart.service;

import com.shopping.cart.entity.Order;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IOrderService;
import com.shopping.cart.repository.OrderRepository;
import com.shopping.cart.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    @Override
    public List<Order> getOrderHistory(String token) {
        User user = userService.getUserFromToken(token);
        return orderRepository.findByUser(user);
    }
}
