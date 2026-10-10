package com.chococraft.service;

import com.chococraft.entity.Category;
import com.chococraft.entity.Product;
import com.chococraft.exception.ResourceNotFoundException;
import com.chococraft.repository.CategoryRepository;
import com.chococraft.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    public List<Product> getBestsellerProducts() {
        return productRepository.findByBestsellerTrueAndActiveTrue();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    public Page<Product> searchAndFilterProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                                String query, String sortBy, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        } else if ("name_asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else if ("rating_desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "rating");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.filterProducts(categoryId, minPrice, maxPrice, query, pageable);
    }

    public List<Product> getRelatedProducts(Long categoryId, Long currentProductId) {
        return productRepository.findRelatedProducts(categoryId, currentProductId, PageRequest.of(0, 4));
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThanEqual(threshold);
    }

    public Page<Product> getAllProductsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return productRepository.findAll(pageable);
    }

    @Transactional
    public Product saveProduct(Product product, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product details, Long categoryId) {
        Product existing = getById(id);
        existing.setName(details.getName());
        existing.setDescription(details.getDescription());
        existing.setPrice(details.getPrice());
        existing.setWeight(details.getWeight());
        existing.setImageUrl(details.getImageUrl());
        existing.setIngredients(details.getIngredients());
        existing.setAllergenInformation(details.getAllergenInformation());
        existing.setStorageInstructions(details.getStorageInstructions());
        existing.setStockQuantity(details.getStockQuantity());
        existing.setActive(details.isActive());
        existing.setFeatured(details.isFeatured());
        existing.setBestseller(details.isBestseller());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            existing.setCategory(category);
        }
        return productRepository.save(existing);
    }

    @Transactional
    public void updateStockQuantity(Long productId, Integer newQuantity) {
        Product product = getById(productId);
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        product.setStockQuantity(newQuantity);
        productRepository.save(product);
    }

    @Transactional
    public void toggleActiveStatus(Long id) {
        Product product = getById(id);
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        // Safe soft deletion to preserve order history integrity
        Product product = getById(id);
        product.setActive(false);
        productRepository.save(product);
    }
}
