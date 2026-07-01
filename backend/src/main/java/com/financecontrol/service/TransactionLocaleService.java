package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionLocaleRequest;
import com.financecontrol.dto.response.TransactionLocaleResponse;
import com.financecontrol.entity.TransactionLocale;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.TransactionLocaleRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionLocaleService {

    private final TransactionLocaleRepository transactionLocaleRepository;

    @Cacheable(value = "transactionLocales", key = "#userId")
    public List<TransactionLocaleResponse> findAllByUser(Long userId) {
        return transactionLocaleRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(TransactionLocaleResponse::from).toList();
    }

    public TransactionLocaleResponse findById(@NonNull Long id,
                                              @NonNull Long userId) {
        return TransactionLocaleResponse.from(getOrThrow(id, userId));
    }

    @Transactional
    @CacheEvict(value = "transactionLocales", key = "#userId")
    public TransactionLocaleResponse create(Long userId, TransactionLocaleRequest req, boolean force) {
        if (!force && transactionLocaleRepository.existsByUserIdAndNameIgnoreCase(userId, req.name()))
            throw new BusinessException("error.duplicate.name");

        TransactionLocale tl = new TransactionLocale(null, userId, req.name(), req.address(), req.iconKey());
        return TransactionLocaleResponse.from(transactionLocaleRepository.save(tl));
    }

    @Transactional
    @CacheEvict(value = "transactionLocales", allEntries = true)
    public TransactionLocaleResponse update(@NonNull Long id,
                                            @NonNull Long userId,
                                            TransactionLocaleRequest req) {
        TransactionLocale tl = getOrThrow(id, userId);

        tl.setName(req.name());
        tl.setAddress(req.address());
        tl.setIconKey(req.iconKey());

        return TransactionLocaleResponse.from(transactionLocaleRepository.save(tl));
    }

    @Transactional
    @CacheEvict(value = "transactionLocales", allEntries = true)
    public void delete(@NonNull Long id,
                       @NonNull Long userId) {
        getOrThrow(id, userId);
        transactionLocaleRepository.deleteById(id);
    }

    private TransactionLocale getOrThrow(@NonNull Long id,
                                         @NonNull Long userId) {
        TransactionLocale tl = transactionLocaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.transactionLocale"));
        if (!userId.equals(tl.getUserId()))
            throw new ResourceNotFoundException("error.notFound.transactionLocale");
        return tl;
    }
}
