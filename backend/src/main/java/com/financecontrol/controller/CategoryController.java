package com.financecontrol.controller;

import com.financecontrol.dto.request.CategoryRequest;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController extends BaseController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(categoryService.findAllByUser(requireUserId(session)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable @NonNull Long id,
                                                     HttpSession session) {
        return ResponseEntity.ok(categoryService.findById(id, requireUserId(session)));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest req,
                                                   @RequestParam(defaultValue = "false") boolean force,
                                                   HttpSession session) {
        return ResponseEntity.ok(categoryService.create(requireUserId(session), req, force));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable @NonNull Long id, 
                                                   @RequestBody CategoryRequest req,
                                                   HttpSession session) {
        return ResponseEntity.ok(categoryService.update(id, requireUserId(session), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id,
                                       HttpSession session) {
        categoryService.delete(id, requireUserId(session));
        return ResponseEntity.noContent().build();
    }
}
