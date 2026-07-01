package com.financecontrol.controller;

import com.financecontrol.dto.request.AccountRequest;
import com.financecontrol.dto.request.PayInvoiceRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.dto.response.InvoiceResponse;
import com.financecontrol.service.AccountService;
import com.financecontrol.service.CreditCardInvoiceService;
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
    private final CreditCardInvoiceService creditCardInvoiceService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(accountService.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable @NonNull Long id,
                                                    HttpSession session) {
        return ResponseEntity.ok(accountService.findById(id, requireUserId(session)));
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
        accountService.delete(id, requireUserId(session));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceResponse>> invoices(@PathVariable @NonNull Long id,
                                                          HttpSession session) {
        return ResponseEntity.ok(creditCardInvoiceService.listInvoices(requireUserId(session), id));
    }

    @PostMapping("/{id}/invoices/{reference}/pay")
    public ResponseEntity<InvoiceResponse> payInvoice(@PathVariable @NonNull Long id,
                                                      @PathVariable @NonNull String reference,
                                                      @RequestBody PayInvoiceRequest req,
                                                      HttpSession session) {
        Long userId = requireUserId(session);
        return ResponseEntity.ok(creditCardInvoiceService.pay(userId, id, reference, req));
    }
}
