package com.financecontrol.controller;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.AppNotificationResponse;
import com.financecontrol.dto.response.TransactionCreateResponse;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.service.AppNotificationService;
import com.financecontrol.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController extends BaseController {

    private final TransactionService        transactionService;
    private final AppNotificationService    notificationService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long accountId,
            HttpSession session) {
        return ResponseEntity.ok(transactionService.findAllByUser(requireUserId(session), startDate, endDate, categoryId, accountId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable @NonNull Long id, HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(transactionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TransactionCreateResponse> create(@RequestBody TransactionRequest req, HttpSession session) {
        Long userId = requireUserId(session);
        TransactionResponse tx = transactionService.create(userId, req);
        List<AppNotificationResponse> notifications = notificationService.checkGoalImpact(userId, tx.id());
        return ResponseEntity.ok(new TransactionCreateResponse(tx, notifications));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable @NonNull Long id,
                                                      @RequestBody TransactionRequest req,
                                                      HttpSession session) {
        return ResponseEntity.ok(transactionService.update(id, requireUserId(session), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id, HttpSession session) {
        requireUserId(session);
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
