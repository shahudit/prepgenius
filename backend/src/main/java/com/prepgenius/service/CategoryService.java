package com.prepgenius.service;

import com.prepgenius.dto.CategoryRequest;
import com.prepgenius.dto.CategoryResponse;
import com.prepgenius.model.Category;
import com.prepgenius.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .group(request.getGroup())
                .active(request.isActive())
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::mapToResponse);
    }

    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToResponse(category);
    }

    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getName().equalsIgnoreCase(request.getName()) &&
            categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Another category with this name already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setGroup(request.getGroup());
        category.setActive(request.isActive());

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .group(category.getGroup())
                .active(category.isActive())
                .build();
    }
}
