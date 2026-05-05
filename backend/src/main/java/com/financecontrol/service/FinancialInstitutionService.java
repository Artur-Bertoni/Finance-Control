package com.financecontrol.service;

import com.financecontrol.dto.request.FinancialInstitutionRequest;
import com.financecontrol.dto.response.FinancialInstitutionResponse;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.FinancialInstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialInstitutionService {

    private final FinancialInstitutionRepository repository;

    public List<FinancialInstitutionResponse> findAllByUser(Long userId) {
        return repository.findByUserIdOrderByIdDesc(userId).stream()
                .map(FinancialInstitutionResponse::from).toList();
    }

    public FinancialInstitutionResponse findById(Long id) {
        return FinancialInstitutionResponse.from(getOrThrow(id));
    }

    @Transactional
    public FinancialInstitutionResponse create(Long userId, FinancialInstitutionRequest req) {
        FinancialInstitution fi = new FinancialInstitution(null, userId, req.name(), req.address(), req.contact());
        return FinancialInstitutionResponse.from(repository.save(fi));
    }

    @Transactional
    public FinancialInstitutionResponse update(Long id, Long userId, FinancialInstitutionRequest req) {
        FinancialInstitution fi = getOrThrow(id);
        fi.setName(req.name());
        fi.setAddress(req.address());
        fi.setContact(req.contact());
        return FinancialInstitutionResponse.from(repository.save(fi));
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    private FinancialInstitution getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição financeira não encontrada"));
    }
}
