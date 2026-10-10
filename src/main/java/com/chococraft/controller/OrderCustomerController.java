package com.chococraft.controller;

import com.chococraft.entity.CustomerOrder;
import com.chococraft.entity.User;
import com.chococraft.security.CustomUserDetails;
import com.chococraft.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderCustomerController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listUserOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        List<CustomerOrder> orders = orderService.getOrdersForUser(user);
        model.addAttribute("orders", orders);
        return "order-history";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable("id") Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        User user = userDetails.getUser();
        CustomerOrder order = orderService.getOrderByIdAndUser(id, user);
        model.addAttribute("order", order);
        return "order-details";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long id,
                              @RequestParam(name = "reason", required = false, defaultValue = "Cancelled by customer") String reason,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        try {
            CustomerOrder order = orderService.getOrderByIdAndUser(id, user);
            orderService.cancelOrder(order.getId(), user, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + order.getOrderNumber() + " has been cancelled and product inventory restored.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
