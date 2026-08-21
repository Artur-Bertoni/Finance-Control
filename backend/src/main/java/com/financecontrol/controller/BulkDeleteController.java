package com.financecontrol.controller;

import com.financecontrol.dto.request.BulkDeleteRequest;
import com.financecontrol.dto.response.BulkDeletePreviewResponse;
import com.financecontrol.dto.response.BulkDeleteResponse;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.service.BulkDeleteService;
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
@RequestMapping("/api/bulk-delete")
public class BulkDeleteController extends BaseController {

    private final BulkDeleteService bulkDeleteService;

    @PostMapping("/preview")
    public ResponseEntity<BulkDeletePreviewResponse> preview(@RequestBody BulkDeleteRequest req,
                                                             HttpSession session) {
        return ResponseEntity.ok(bulkDeleteService.preview(requireType(req), req.ids(), requireUserId(session)));
    }

    @PostMapping
    public ResponseEntity<BulkDeleteResponse> delete(@RequestBody BulkDeleteRequest req,
                                                     HttpSession session) {
        return ResponseEntity.ok(bulkDeleteService.delete(requireType(req), req.ids(), requireUserId(session)));
    }

    @NonNull
    private BulkEntityType requireType(BulkDeleteRequest req) {
        if (req == null || req.type() == null)
            throw new BusinessException("error.bulk.invalidType");
        return req.type();
    }
}
