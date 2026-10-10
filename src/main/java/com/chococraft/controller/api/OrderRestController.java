package com.chococraft.controller.api;

import com.chococraft.dto.ApiResponse;
import com.chococraft.entity.CustomerOrder;
import com.chococraft.entity.User;
import com.chococraft.security.CustomUserDetails;
import com.chococraft.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerOrder>>> getUserOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<CustomerOrder> orders = orderService.getOrdersForUser(user);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerOrder>> getOrderDetails(@PathVariable("id") Long id,
                                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        CustomerOrder order = orderService.getOrderByIdAndUser(id, user);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved", order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<CustomerOrder>> cancelOrder(@PathVariable("id") Long id,
                                                                   @RequestParam(name = "reason", required = false) String reason,
                                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        CustomerOrder order = orderService.cancelOrder(id, user, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
