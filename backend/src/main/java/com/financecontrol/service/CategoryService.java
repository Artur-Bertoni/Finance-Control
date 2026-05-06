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

    private final CategoryRepository repository;

    public List<CategoryResponse> findAllByUser(Long userId) {
        return repository.findByUserIdOrderByIdDesc(userId).stream()
                .map(CategoryResponse::from).toList();
    }

    public CategoryResponse findById(@NonNull Long id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest req) {
        Category c = new Category(null, userId, req.name(), req.description());
        return CategoryResponse.from(repository.save(c));
    }

    @Transactional
    public CategoryResponse update(@NonNull Long id, CategoryRequest req) {
        Category c = getOrThrow(id);
        c.setName(req.name());
        c.setDescription(req.description());
        return CategoryResponse.from(repository.save(c));
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    private Category getOrThrow(@NonNull Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}
