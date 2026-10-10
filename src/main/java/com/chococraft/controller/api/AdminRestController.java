package com.chococraft.controller.api;

import com.chococraft.dto.ApiResponse;
import com.chococraft.entity.CustomerOrder;
import com.chococraft.entity.Product;
import com.chococraft.enums.OrderStatus;
import com.chococraft.service.OrderService;
import com.chococraft.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.getAllProductsForAdmin(0, 1000).getTotalElements());
        stats.put("totalOrders", orderService.getAllOrdersForAdmin(null, 0, 1000).getTotalElements());
        stats.put("pendingOrders", orderService.getPendingOrdersCount());
        stats.put("completedOrders", orderService.getCompletedOrdersCount());
        stats.put("totalSales", orderService.getTotalSalesRevenue());
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", stats));
    }

    @PostMapping("/products/{id}/stock")
    public ResponseEntity<ApiResponse<Void>> updateProductStock(@PathVariable("id") Long id,
                                                                 @RequestParam("stockQuantity") Integer stockQuantity) {
        productService.updateStockQuantity(id, stockQuantity);
        return ResponseEntity.ok(ApiResponse.success("Stock updated"));
    }

    @PostMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<CustomerOrder>> updateOrderStatus(@PathVariable("id") Long id,
                                                                          @RequestParam("status") OrderStatus status) {
        CustomerOrder order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }
}
