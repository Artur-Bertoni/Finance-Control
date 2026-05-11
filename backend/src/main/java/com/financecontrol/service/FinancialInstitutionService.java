package com.financecontrol.service;

import com.financecontrol.dto.request.FinancialInstitutionRequest;
import com.financecontrol.dto.response.FinancialInstitutionResponse;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.FinancialInstitutionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialInstitutionService {

    private final FinancialInstitutionRepository financialInstitutionRepository;

    public List<FinancialInstitutionResponse> findAllByUser(Long userId) {
        return financialInstitutionRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(FinancialInstitutionResponse::from).toList();
    }

    public FinancialInstitutionResponse findById(@NonNull Long id) {
        return FinancialInstitutionResponse.from(getOrThrow(id));
    }

    @Transactional
    public FinancialInstitutionResponse create(Long userId, FinancialInstitutionRequest req) {
        FinancialInstitution fi = new FinancialInstitution(null, userId, req.name(), req.address(), req.contact());
        return FinancialInstitutionResponse.from(financialInstitutionRepository.save(fi));
    }

    @Transactional
    public FinancialInstitutionResponse update(@NonNull Long id, FinancialInstitutionRequest req) {
        FinancialInstitution fi = getOrThrow(id);

        fi.setName(req.name());
        fi.setAddress(req.address());
        fi.setContact(req.contact());

        return FinancialInstitutionResponse.from(financialInstitutionRepository.save(fi));
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        financialInstitutionRepository.deleteById(id);
    }

    private FinancialInstitution getOrThrow(@NonNull Long id) {
        return financialInstitutionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.notFound.financialInstitution"));
    }
}
