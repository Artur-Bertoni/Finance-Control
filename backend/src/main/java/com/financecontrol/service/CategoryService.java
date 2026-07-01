package com.financecontrol.service;

import com.financecontrol.dto.request.CategoryRequest;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.CategoryAlias;
import com.financecontrol.exception.BusinessException;
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
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.financecontrol.service.HistoryService.*;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CategoryService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

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
    public CategoryResponse findById(@NonNull Long id,
                                     @NonNull Long userId) {
        return CategoryResponse.from(getOrThrow(id, userId));
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#userId")
    public CategoryResponse create(Long userId,
                                   CategoryRequest req,
                                   boolean force) {
        if (!force && categoryRepository.existsByUserIdAndNameIgnoreCase(userId, req.name()))
            throw new BusinessException("error.duplicate.name");

        Category c = new Category();

        c.setUserId(userId);
        c.setName(req.name());
        c.setDescription(req.description());
        c.setIconKey(req.iconKey());
        c.setCreatedAt(LocalDateTime.now(ZONE));
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
                                   @NonNull Long userId,
                                   CategoryRequest req) {
        Category c = getOrThrow(id, userId);

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
    public List<Category> findByAlias(Long userId,
                                      String aliasName) {
        return categoryAliasRepository.findAllByCategoryUserIdAndAliasName(userId, aliasName)
                .stream().map(CategoryAlias::getCategory).toList();
    }

    @Transactional
    public void learnAlias(Long userId,
                           String aliasName,
                           @NonNull Long categoryId) {
        if (aliasName == null || aliasName.isBlank()) return;

        boolean alreadyMapped = categoryAliasRepository
                .findAllByCategoryUserIdAndAliasName(userId, aliasName)
                .stream()
                .anyMatch(a -> a.getCategory().getId().equals(categoryId));

        if (!alreadyMapped) {
            Category target = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
            if (!userId.equals(target.getUserId()))
                throw new ResourceNotFoundException(CATEGORY_NOT_FOUND);
            categoryAliasRepository.save(new CategoryAlias(target, aliasName));
        }
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(@NonNull Long id,
                       @NonNull Long userId) {
        getOrThrow(id, userId);
        categoryRepository.deleteById(id);
    }

    private Category getOrThrow(@NonNull Long id,
                               @NonNull Long userId) {
        Category c = categoryRepository.findByIdWithAliases(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
        if (!userId.equals(c.getUserId()))
            throw new ResourceNotFoundException(CATEGORY_NOT_FOUND);
        return c;
    }
}
