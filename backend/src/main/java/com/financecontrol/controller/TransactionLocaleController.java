package com.financecontrol.controller;

import com.financecontrol.dto.request.TransactionLocaleRequest;
import com.financecontrol.dto.response.TransactionLocaleResponse;
import com.financecontrol.service.TransactionLocaleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction-locales")
public class TransactionLocaleController extends BaseController {

    private final TransactionLocaleService transactionLocaleService;

    @GetMapping
    public ResponseEntity<List<TransactionLocaleResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(transactionLocaleService.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionLocaleResponse> findById(@PathVariable @NonNull Long id,
                                                              HttpSession session) {
        return ResponseEntity.ok(transactionLocaleService.findById(id, requireUserId(session)));
    }

    @PostMapping
    public ResponseEntity<TransactionLocaleResponse> create(@RequestBody TransactionLocaleRequest req,
                                                            @RequestParam(defaultValue = "false") boolean force,
                                                            HttpSession session) {
        return ResponseEntity.ok(transactionLocaleService.create(requireUserId(session), req, force));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionLocaleResponse> update(@PathVariable @NonNull Long id,
                                                            @RequestBody TransactionLocaleRequest req,
                                                            HttpSession session) {
        return ResponseEntity.ok(transactionLocaleService.update(id, requireUserId(session), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id,
                                       HttpSession session) {
        transactionLocaleService.delete(id, requireUserId(session));
        return ResponseEntity.noContent().build();
    }
}
