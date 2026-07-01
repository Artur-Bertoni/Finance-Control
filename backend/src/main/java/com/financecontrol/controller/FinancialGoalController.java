package com.financecontrol.controller;

import com.financecontrol.dto.request.GoalRequest;
import com.financecontrol.dto.response.GoalResponse;
import com.financecontrol.service.GoalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class FinancialGoalController extends BaseController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<List<GoalResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(goalService.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> findById(@PathVariable @NonNull Long id,
                                                          HttpSession session) {
        return ResponseEntity.ok(goalService.findById(id, requireUserId(session)));
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(@RequestBody GoalRequest req,
                                               @RequestParam(defaultValue = "false") boolean force,
                                               HttpSession session) {
        return ResponseEntity.ok(goalService.create(requireUserId(session), req, force));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(@PathVariable @NonNull Long id,
                                                        @RequestBody GoalRequest req,
                                                        HttpSession session) {
        return ResponseEntity.ok(goalService.update(id, requireUserId(session), req));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archive(@PathVariable @NonNull Long id,
                                        HttpSession session) {
        goalService.archive(id, requireUserId(session));

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id,
                                       HttpSession session) {
        goalService.delete(id, requireUserId(session));

        return ResponseEntity.noContent().build();
    }
}
