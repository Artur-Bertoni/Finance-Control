package com.financecontrol.controller;

import com.financecontrol.dto.request.BulkEditRequest;
import com.financecontrol.dto.response.BulkEditResponse;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.service.BulkEditService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bulk-edit")
public class BulkEditController extends BaseController {

    private final BulkEditService bulkEditService;

    @PostMapping
    public ResponseEntity<BulkEditResponse> edit(@RequestBody BulkEditRequest req,
                                                 HttpSession session) {
        return ResponseEntity.ok(bulkEditService.edit(
                requireType(req), req.ids(), req.fields(), req.values(), requireUserId(session)));
    }

    @NonNull
    private BulkEntityType requireType(BulkEditRequest req) {
        if (req == null || req.type() == null)
            throw new BusinessException("error.bulk.invalidType");
        return req.type();
    }
}
