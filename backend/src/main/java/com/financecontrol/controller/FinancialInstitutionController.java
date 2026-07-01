package com.financecontrol.controller;

import com.financecontrol.dto.request.FinancialInstitutionRequest;
import com.financecontrol.dto.response.FinancialInstitutionResponse;
import com.financecontrol.service.FinancialInstitutionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/financial-institutions")
public class FinancialInstitutionController extends BaseController {

    private final FinancialInstitutionService financialInstitutionService;

    @GetMapping
    public ResponseEntity<List<FinancialInstitutionResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(financialInstitutionService.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialInstitutionResponse> findById(@PathVariable @NonNull Long id,
                                                                 HttpSession session) {
        return ResponseEntity.ok(financialInstitutionService.findById(id, requireUserId(session)));
    }

    @PostMapping
    public ResponseEntity<FinancialInstitutionResponse> create(@RequestBody FinancialInstitutionRequest req,
                                                               @RequestParam(defaultValue = "false") boolean force,
                                                               HttpSession session) {
        return ResponseEntity.ok(financialInstitutionService.create(requireUserId(session), req, force));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialInstitutionResponse> update(@PathVariable @NonNull Long id,
                                                               @RequestBody FinancialInstitutionRequest req,
                                                               HttpSession session) {
        return ResponseEntity.ok(financialInstitutionService.update(id, requireUserId(session), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id,
                                       HttpSession session) {
        financialInstitutionService.delete(id, requireUserId(session));

        return ResponseEntity.noContent().build();
    }
}
