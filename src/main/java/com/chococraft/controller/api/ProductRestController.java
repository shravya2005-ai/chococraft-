package com.chococraft.controller.api;

import com.chococraft.dto.ApiResponse;
import com.chococraft.entity.Category;
import com.chococraft.entity.Product;
import com.chococraft.service.CategoryService;
import com.chococraft.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductRestController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<Product>>> getProducts(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "sortBy", defaultValue = "newest") String sortBy,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<Product> products = productService.searchAndFilterProducts(categoryId, minPrice, maxPrice, query, sortBy, page, size);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable("id") Long id) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", product));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        List<Category> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", categories));
    }
}
