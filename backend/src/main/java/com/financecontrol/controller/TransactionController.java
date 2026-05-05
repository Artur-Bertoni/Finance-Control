package com.financecontrol.controller;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController extends BaseController {

    private final TransactionService service;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long accountId,
            HttpSession session) {
        return ResponseEntity.ok(service.findAllByUser(requireUserId(session), startDate, endDate, categoryId, accountId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable Long id, HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody TransactionRequest req, HttpSession session) {
        return ResponseEntity.ok(service.create(requireUserId(session), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id,
                                                      @RequestBody TransactionRequest req,
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
