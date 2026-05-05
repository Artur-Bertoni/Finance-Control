package com.financecontrol.controller;

import com.financecontrol.dto.request.CategoryRequest;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController extends BaseController {

    private final CategoryService service;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(service.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id, HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest req, HttpSession session) {
        return ResponseEntity.ok(service.create(requireUserId(session), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @RequestBody CategoryRequest req,
                                                   HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        requireUserId(session);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
