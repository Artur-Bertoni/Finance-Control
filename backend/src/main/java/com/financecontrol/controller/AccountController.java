package com.financecontrol.controller;

import com.financecontrol.dto.request.AccountRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.service.AccountService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController extends BaseController {

    private final AccountService service;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(service.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable Long id, HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/total-value")
    public ResponseEntity<Double> totalValue(@RequestParam(required = false) Long accountId,
                                             HttpSession session) {
        return ResponseEntity.ok(service.totalValue(requireUserId(session), accountId));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody AccountRequest req, HttpSession session) {
        return ResponseEntity.ok(service.create(requireUserId(session), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable Long id,
                                                  @RequestBody AccountRequest req,
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
