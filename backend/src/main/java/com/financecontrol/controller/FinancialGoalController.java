package com.financecontrol.controller;

import com.financecontrol.dto.request.FinancialGoalRequest;
import com.financecontrol.dto.response.FinancialGoalResponse;
import com.financecontrol.service.FinancialGoalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class FinancialGoalController extends BaseController {

    private final FinancialGoalService goalService;

    @GetMapping
    public ResponseEntity<List<FinancialGoalResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(goalService.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialGoalResponse> findById(@PathVariable @NonNull Long id, HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(goalService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FinancialGoalResponse> create(@RequestBody FinancialGoalRequest req, HttpSession session) {
        return ResponseEntity.ok(goalService.create(requireUserId(session), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialGoalResponse> update(@PathVariable @NonNull Long id,
                                                        @RequestBody FinancialGoalRequest req,
                                                        HttpSession session) {
        return ResponseEntity.ok(goalService.update(id, requireUserId(session), req));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archive(@PathVariable @NonNull Long id, HttpSession session) {
        requireUserId(session);
        goalService.archive(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id, HttpSession session) {
        requireUserId(session);
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
