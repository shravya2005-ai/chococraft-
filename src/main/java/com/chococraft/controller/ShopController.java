package com.chococraft.controller;

import com.chococraft.entity.Product;
import com.chococraft.service.CategoryService;
import com.chococraft.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class ShopController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/shop")
    public String shop(@RequestParam(name = "categoryId", required = false) Long categoryId,
                       @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
                       @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
                       @RequestParam(name = "query", required = false) String query,
                       @RequestParam(name = "sortBy", defaultValue = "newest") String sortBy,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {

        Page<Product> productPage = productService.searchAndFilterProducts(categoryId, minPrice, maxPrice, query, sortBy, page, 9);

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("searchQuery", query);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        return "shop";
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable("id") Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", productService.getRelatedProducts(
                product.getCategory() != null ? product.getCategory().getId() : null, product.getId()));
        return "product-details";
    }
}
