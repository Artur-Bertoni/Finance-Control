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

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.financecontrol.service.ChangeHistoryService.*;

@Service
@RequiredArgsConstructor
public class FinancialInstitutionService {

    private final FinancialInstitutionRepository financialInstitutionRepository;
    private final ChangeHistoryService changeHistoryService;

    public List<FinancialInstitutionResponse> findAllByUser(Long userId) {
        return financialInstitutionRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(FinancialInstitutionResponse::from).toList();
    }

    public FinancialInstitutionResponse findById(@NonNull Long id) {
        return FinancialInstitutionResponse.from(getOrThrow(id));
    }

    @Transactional
    public FinancialInstitutionResponse create(Long userId, FinancialInstitutionRequest req) {
        FinancialInstitution fi = new FinancialInstitution(null, userId, req.name(), req.address(), req.contact(), req.iconKey(), LocalDateTime.now());
        FinancialInstitutionResponse result = FinancialInstitutionResponse.from(financialInstitutionRepository.save(fi));
        changeHistoryService.recordCreation(ENTITY_INSTITUTION, result.id(), userId);
        return result;
    }

    @Transactional
    public FinancialInstitutionResponse update(@NonNull Long id, FinancialInstitutionRequest req) {
        FinancialInstitution fi = getOrThrow(id);

        Map<String, String[]> diff = new LinkedHashMap<>();
        if (differs(fi.getName(), req.name()))
            diff.put("name", diff(fi.getName(), req.name()));
        if (differs(fi.getAddress(), req.address()))
            diff.put("address", diff(fi.getAddress(), req.address()));
        if (differs(fi.getContact(), req.contact()))
            diff.put("contact", diff(fi.getContact(), req.contact()));
        if (differs(fi.getIconKey(), req.iconKey()))
            diff.put("iconKey", diff(fi.getIconKey(), req.iconKey()));

        fi.setName(req.name());
        fi.setAddress(req.address());
        fi.setContact(req.contact());
        fi.setIconKey(req.iconKey());

        FinancialInstitutionResponse result = FinancialInstitutionResponse.from(financialInstitutionRepository.save(fi));
        changeHistoryService.recordChanges(ENTITY_INSTITUTION, id, fi.getUserId(), diff);
        return result;
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
