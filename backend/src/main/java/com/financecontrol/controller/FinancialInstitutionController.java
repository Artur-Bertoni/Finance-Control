package com.financecontrol.controller;

import com.financecontrol.dto.request.FinancialInstitutionRequest;
import com.financecontrol.dto.response.FinancialInstitutionResponse;
import com.financecontrol.service.FinancialInstitutionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/financial-institutions")
@RequiredArgsConstructor
public class FinancialInstitutionController extends BaseController {

    private final FinancialInstitutionService service;

    @GetMapping
    public ResponseEntity<List<FinancialInstitutionResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(service.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialInstitutionResponse> findById(@PathVariable Long id, HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<FinancialInstitutionResponse> create(@RequestBody FinancialInstitutionRequest req,
                                                               HttpSession session) {
        return ResponseEntity.ok(service.create(requireUserId(session), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialInstitutionResponse> update(@PathVariable Long id,
                                                               @RequestBody FinancialInstitutionRequest req,
                                                               HttpSession session) {
        return ResponseEntity.ok(service.update(id, requireUserId(session), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        requireUserId(session);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
