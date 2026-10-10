package com.chococraft.controller;

import com.chococraft.entity.Category;
import com.chococraft.entity.CustomerOrder;
import com.chococraft.entity.Product;
import com.chococraft.entity.User;
import com.chococraft.enums.OrderStatus;
import com.chococraft.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ContactService contactService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.getAllProductsForAdmin(0, 1000).getTotalElements());
        model.addAttribute("totalOrders", orderService.getAllOrdersForAdmin(null, 0, 1000).getTotalElements());
        model.addAttribute("pendingOrders", orderService.getPendingOrdersCount());
        model.addAttribute("completedOrders", orderService.getCompletedOrdersCount());
        model.addAttribute("totalSales", orderService.getTotalSalesRevenue());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(10));
        model.addAttribute("recentOrders", orderService.getRecentOrders());
        return "admin/dashboard";
    }

    // --- Product Management ---
    @GetMapping("/products")
    public String listProducts(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        Page<Product> productPage = productService.getAllProductsForAdmin(page, 10);
        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult bindingResult,
                              @RequestParam(name = "categoryId", required = false) Long categoryId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/product-form";
        }
        try {
            if (product.getId() == null) {
                productService.saveProduct(product, categoryId);
                redirectAttributes.addFlashAttribute("successMessage", "Product created successfully.");
            } else {
                productService.updateProduct(product.getId(), product, categoryId);
                redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/toggle-status/{id}")
    public String toggleProductStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.toggleActiveStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product active status updated.");
        return "redirect:/admin/products";
    }

    // --- Inventory Management ---
    @GetMapping("/inventory")
    public String inventoryManagement(Model model) {
        model.addAttribute("products", productService.getAllProductsForAdmin(0, 1000).getContent());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(10));
        return "admin/inventory";
    }

    @PostMapping("/inventory/update")
    public String updateStock(@RequestParam("productId") Long productId,
                              @RequestParam("stockQuantity") Integer stockQuantity,
                              RedirectAttributes redirectAttributes) {
        try {
            productService.updateStockQuantity(productId, stockQuantity);
            redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    // --- Category Management ---
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new Category());
        }
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("categoryForm") Category category,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/categories";
        }
        try {
            if (category.getId() == null) {
                categoryService.saveCategory(category);
                redirectAttributes.addFlashAttribute("successMessage", "Category created successfully.");
            } else {
                categoryService.updateCategory(category.getId(), category);
                redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // --- Order Management ---
    @GetMapping("/orders")
    public String listOrders(@RequestParam(name = "status", required = false) OrderStatus status,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             Model model) {
        Page<CustomerOrder> orderPage = orderService.getAllOrdersForAdmin(status, page, 10);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String adminOrderDetails(@PathVariable("id") Long id, Model model) {
        CustomerOrder order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/order-details";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("orderStatus") OrderStatus newStatus,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + newStatus.getLabel());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    // --- Customer Management ---
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<User> customers = userService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "admin/customers";
    }

    // --- Customer Inquiries ---
    @GetMapping("/messages")
    public String listContactMessages(Model model) {
        model.addAttribute("messages", contactService.getAllMessages());
        return "admin/messages";
    }
}
