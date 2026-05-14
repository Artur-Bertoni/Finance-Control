package com.financecontrol.service;

import com.financecontrol.dto.request.CategoryRequest;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.CategoryAlias;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.CategoryAliasRepository;
import com.financecontrol.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository      categoryRepository;
    private final CategoryAliasRepository categoryAliasRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllByUser(Long userId) {
        return categoryRepository.findByUserIdWithAliases(userId).stream()
                .map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(@NonNull Long id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest req) {
        Category c = new Category();
        c.setUserId(userId);
        c.setName(req.name());
        c.setDescription(req.description());
        categoryRepository.save(c);

        List<String> aliasNames = (req.aliases() != null && !req.aliases().isEmpty())
                ? req.aliases().stream().filter(a -> a != null && !a.isBlank()).toList()
                : List.of(req.name());

        aliasNames.forEach(a -> c.getAliases().add(new CategoryAlias(c, a)));

        return CategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(@NonNull Long id, CategoryRequest req) {
        Category c = getOrThrow(id);
        c.setName(req.name());
        c.setDescription(req.description());

        c.getAliases().clear();
        if (req.aliases() != null && !req.aliases().isEmpty()) {
            req.aliases().stream()
                    .filter(a -> a != null && !a.isBlank())
                    .forEach(a -> c.getAliases().add(new CategoryAlias(c, a)));
        }

        return CategoryResponse.from(categoryRepository.save(c));
    }

    // Used by statement import preview: finds an existing category by alias name.
    @Transactional(readOnly = true)
    public Optional<Category> findByAlias(Long userId, String aliasName) {
        return categoryAliasRepository.findByCategoryUserIdAndAliasName(userId, aliasName)
                .map(CategoryAlias::getCategory);
    }

    // Used by legacy auto-import flow: finds or creates a category by alias name.
    @Transactional
    public Category findOrCreateByInternalName(Long userId, String internalName) {
        return categoryAliasRepository.findByCategoryUserIdAndAliasName(userId, internalName)
                .map(CategoryAlias::getCategory)
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setUserId(userId);
                    String name = internalName.length() > 500 ? internalName.substring(0, 500) : internalName;
                    c.setName(name);
                    categoryRepository.save(c);
                    c.getAliases().add(new CategoryAlias(c, internalName));
                    return categoryRepository.save(c);
                });
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        categoryRepository.deleteById(id);
    }

    private Category getOrThrow(@NonNull Long id) {
        return categoryRepository.findByIdWithAliases(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.category"));
    }
}
