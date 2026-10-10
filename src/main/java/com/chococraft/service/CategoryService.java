package com.chococraft.service;

import com.chococraft.entity.Category;
import com.chococraft.exception.BadRequestException;
import com.chococraft.exception.ResourceNotFoundException;
import com.chococraft.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    public Category getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    }

    @Transactional
    public Category saveCategory(Category category) {
        if (category.getId() == null && categoryRepository.existsByName(category.getName())) {
            throw new BadRequestException("Category with name '" + category.getName() + "' already exists.");
        }
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(category.getName().toLowerCase().replaceAll("[^a-z0-9]", "-"));
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category details) {
        Category existing = getById(id);
        existing.setName(details.getName());
        existing.setDescription(details.getDescription());
        existing.setImageUrl(details.getImageUrl());
        existing.setActive(details.isActive());
        if (details.getSlug() != null && !details.getSlug().isBlank()) {
            existing.setSlug(details.getSlug().toLowerCase().replaceAll("[^a-z0-9]", "-"));
        }
        return categoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getById(id);
        category.setActive(false);
        categoryRepository.save(category);
    }
}
