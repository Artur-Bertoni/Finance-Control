package com.financecontrol.controller;

import com.financecontrol.dto.response.DashboardResponse;
import com.financecontrol.service.ReportExportService;
import com.financecontrol.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController extends BaseController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                          @RequestParam(required = false) Long accountId,
                                                          HttpSession session) {
        return ResponseEntity.ok(reportService.getDashboard(requireUserId(session), startDate, endDate, accountId));
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                            @RequestParam(required = false) Long accountId,
                                            @RequestParam(defaultValue = "pt") String lang,
                                            HttpSession session) {
        byte[] body = reportExportService.exportPdf(requireUserId(session), startDate, endDate, accountId, lang);
        return fileResponse(body, MediaType.APPLICATION_PDF, "relatorio-financeiro.pdf");
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                              @RequestParam(required = false) Long accountId,
                                              @RequestParam(defaultValue = "pt") String lang,
                                              HttpSession session) {
        byte[] body = reportExportService.exportExcel(requireUserId(session), startDate, endDate, accountId, lang);
        return fileResponse(body, XLSX, "relatorio-financeiro.xlsx");
    }

    @SuppressWarnings("null")
    private ResponseEntity<byte[]> fileResponse(byte[] body, MediaType type, String filename) {
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
