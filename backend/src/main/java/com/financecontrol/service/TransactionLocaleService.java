package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionLocaleRequest;
import com.financecontrol.dto.response.TransactionLocaleResponse;
import com.financecontrol.entity.TransactionLocale;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.TransactionLocaleRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionLocaleService {

    private final TransactionLocaleRepository repository;

    public List<TransactionLocaleResponse> findAllByUser(Long userId) {
        return repository.findByUserIdOrderByIdDesc(userId).stream()
                .map(TransactionLocaleResponse::from).toList();
    }

    public TransactionLocaleResponse findById(@NonNull Long id) {
        return TransactionLocaleResponse.from(getOrThrow(id));
    }

    @Transactional
    public TransactionLocaleResponse create(Long userId, TransactionLocaleRequest req) {
        TransactionLocale tl = new TransactionLocale(null, userId, req.name(), req.address());
        return TransactionLocaleResponse.from(repository.save(tl));
    }

    @Transactional
    public TransactionLocaleResponse update(@NonNull Long id, TransactionLocaleRequest req) {
        TransactionLocale tl = getOrThrow(id);
        tl.setName(req.name());
        tl.setAddress(req.address());
        return TransactionLocaleResponse.from(repository.save(tl));
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    private TransactionLocale getOrThrow(@NonNull Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Local de transação não encontrado"));
    }
}
