package com.financecontrol.controller;

import com.financecontrol.dto.request.AccountRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.service.AccountService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController extends BaseController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(accountService.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable @NonNull Long id, 
                                                    HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(accountService.findById(id));
    }

    @GetMapping("/total-value")
    public ResponseEntity<Double> totalValue(@RequestParam(required = false) Long accountId, 
                                             HttpSession session) {
        return ResponseEntity.ok(accountService.totalValue(requireUserId(session), accountId));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody AccountRequest req,
                                                  @RequestParam(defaultValue = "false") boolean force,
                                                  HttpSession session) {
        return ResponseEntity.ok(accountService.create(requireUserId(session), req, force));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable @NonNull Long id, 
                                                  @RequestBody AccountRequest req, 
                                                  HttpSession session) {
        return ResponseEntity.ok(accountService.update(id, requireUserId(session), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id, 
                                       HttpSession session) {
        requireUserId(session);
        accountService.delete(id);
        
        return ResponseEntity.noContent().build();
    }
}
