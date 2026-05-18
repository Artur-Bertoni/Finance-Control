package com.financecontrol.service;

import com.financecontrol.dto.request.CategoryRequest;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.CategoryAlias;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.CategoryAliasRepository;
import com.financecontrol.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    private static final String CATEGORY_NOT_FOUND = "error.notFound.category";

    @PersistenceContext
    private EntityManager entityManager;

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
        c.setIconKey(req.iconKey());
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
        c.setIconKey(req.iconKey());

        c.getAliases().clear();
        entityManager.flush(); // garante que os DELETEs são executados antes dos INSERTs para evitar violação de chave única

        if (req.aliases() != null && !req.aliases().isEmpty()) {
            req.aliases().stream()
                    .filter(a -> a != null && !a.isBlank())
                    .forEach(a -> c.getAliases().add(new CategoryAlias(c, a)));
        }

        return CategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByAlias(Long userId, String aliasName) {
        return categoryAliasRepository.findFirstByCategoryUserIdAndAliasName(userId, aliasName)
                .map(CategoryAlias::getCategory);
    }

    @Transactional
    public void learnAlias(Long userId, String aliasName, @NonNull Long categoryId) {
        if (aliasName == null || aliasName.isBlank()) return;

        categoryAliasRepository.findFirstByCategoryUserIdAndAliasName(userId, aliasName)
                .ifPresentOrElse(
                    existing -> {
                        if (!existing.getCategory().getId().equals(categoryId)) {
                            categoryAliasRepository.delete(existing);
                            Category target = categoryRepository.findById(categoryId)
                                    .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
                            categoryAliasRepository.save(new CategoryAlias(target, aliasName));
                        }
                    },
                    () -> {
                        Category target = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
                        categoryAliasRepository.save(new CategoryAlias(target, aliasName));
                    }
                );
    }

    @Transactional
    public Category findOrCreateByInternalName(Long userId, String internalName) {
        return categoryAliasRepository.findFirstByCategoryUserIdAndAliasName(userId, internalName)
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
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
    }
}
