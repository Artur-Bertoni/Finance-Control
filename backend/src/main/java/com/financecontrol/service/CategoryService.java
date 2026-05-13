package com.financecontrol.service;

import com.financecontrol.dto.request.CategoryRequest;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAllByUser(Long userId) {
        return categoryRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(CategoryResponse::from).toList();
    }

    public CategoryResponse findById(@NonNull Long id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest req) {
        String internalName = (req.internalName() != null && !req.internalName().isBlank())
                ? req.internalName() : req.name();
        Category c = new Category(null, userId, req.name(), req.description(), internalName);
        return CategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(@NonNull Long id, CategoryRequest req) {
        Category c = getOrThrow(id);
        c.setName(req.name());
        c.setDescription(req.description());
        if (req.internalName() != null && !req.internalName().isBlank()) {
            c.setInternalName(req.internalName());
        }
        return CategoryResponse.from(categoryRepository.save(c));
    }

    // Used by the statement import: finds an existing category by its internal name or
    // creates a new one (with name = internalName) if none exists yet.
    @Transactional
    public Category findOrCreateByInternalName(Long userId, String internalName) {
        return categoryRepository.findByUserIdAndInternalName(userId, internalName)
                .orElseGet(() -> {
                    String name = internalName.length() > 500 ? internalName.substring(0, 500) : internalName;
                    Category c = new Category(null, userId, name, null, internalName);
                    return categoryRepository.save(c);
                });
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        categoryRepository.deleteById(id);
    }

    private Category getOrThrow(@NonNull Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.notFound.category"));
    }
}
