package com.financecontrol.controller;

import com.financecontrol.dto.request.BudgetRequest;
import com.financecontrol.dto.response.BudgetResponse;
import com.financecontrol.service.BudgetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/budgets")
public class BudgetController extends BaseController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(budgetService.findAllByUser(requireUserId(session)));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> upsert(@RequestBody BudgetRequest req,
                                                 HttpSession session) {
        return ResponseEntity.ok(budgetService.upsert(requireUserId(session), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id,
                                       HttpSession session) {
        budgetService.delete(requireUserId(session), id);
        return ResponseEntity.noContent().build();
    }
}
