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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.financecontrol.service.HistoryService.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryAliasRepository categoryAliasRepository;
    private final HistoryService historyService;

    private static final String CATEGORY_NOT_FOUND = "error.notFound.category";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#userId")
    public List<CategoryResponse> findAllByUser(Long userId) {
        return categoryRepository.findByUserIdWithAliases(userId).stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(@NonNull Long id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#userId")
    public CategoryResponse create(Long userId,
                                   CategoryRequest req) {
        Category c = new Category();

        c.setUserId(userId);
        c.setName(req.name());
        c.setDescription(req.description());
        c.setIconKey(req.iconKey());
        c.setCreatedAt(LocalDateTime.now());
        categoryRepository.save(c);

        List<String> aliasNames = (req.aliases() != null && !req.aliases().isEmpty())
                ? req.aliases().stream().filter(a -> a != null && !a.isBlank()).toList()
                : List.of(req.name());

        aliasNames.forEach(a -> c.getAliases().add(new CategoryAlias(c, a)));

        CategoryResponse result = CategoryResponse.from(categoryRepository.save(c));
        historyService.recordCreation(ENTITY_CATEGORY, result.id(), userId);
        return result;
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse update(@NonNull Long id,
                                   CategoryRequest req) {
        Category c = getOrThrow(id);

        String oldAliases = c.getAliases().stream()
                .map(CategoryAlias::getAliasName).sorted().collect(Collectors.joining(", "));

        Map<String, String[]> diff = new LinkedHashMap<>();

        if (differs(c.getName(), req.name()))
            diff.put("name", diff(c.getName(), req.name()));
        if (differs(c.getDescription(), req.description()))
            diff.put("description", diff(c.getDescription(), req.description()));
        if (differs(c.getIconKey(), req.iconKey()))
            diff.put("iconKey", diff(c.getIconKey(), req.iconKey()));

        c.setName(req.name());
        c.setDescription(req.description());
        c.setIconKey(req.iconKey());

        c.getAliases().clear();
        entityManager.flush();

        if (req.aliases() != null && !req.aliases().isEmpty()) {
            req.aliases().stream()
                    .filter(a -> a != null && !a.isBlank())
                    .forEach(a -> c.getAliases().add(new CategoryAlias(c, a)));
        }

        CategoryResponse result = CategoryResponse.from(categoryRepository.save(c));

        String newAliases = result.aliases().stream().sorted().collect(Collectors.joining(", "));
        if (differs(oldAliases, newAliases))
            diff.put("aliases", diff(oldAliases, newAliases));

        historyService.recordChanges(ENTITY_CATEGORY, id, c.getUserId(), diff);
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByAlias(Long userId,
                                          String aliasName) {
        return categoryAliasRepository.findFirstByCategoryUserIdAndAliasName(userId, aliasName).map(CategoryAlias::getCategory);
    }

    @Transactional
    public void learnAlias(Long userId,
                           String aliasName,
                           @NonNull Long categoryId) {
        if (aliasName == null || aliasName.isBlank()) return;

        categoryAliasRepository.findFirstByCategoryUserIdAndAliasName(userId, aliasName).ifPresentOrElse(
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
    public Category findOrCreateByInternalName(Long userId,
                                               String internalName) {
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
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        categoryRepository.deleteById(id);
    }

    private Category getOrThrow(@NonNull Long id) {
        return categoryRepository.findByIdWithAliases(id).orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
    }
}
