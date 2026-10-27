package com.financetracker.pro.controller;

import com.financetracker.pro.security.UserPrincipal;
import com.financetracker.pro.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Import/Export", description = "CSV Export/Import & PDF Financial Statement Reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/export/csv")
    @Operation(summary = "Export transactions to CSV")
    public ResponseEntity<InputStreamResource> exportCsv(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ByteArrayInputStream in = reportService.exportTransactionsToCsv(userPrincipal.getId(), startDate, endDate);
        String filename = "transactions_" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "Generate and download monthly financial PDF statement")
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        byte[] pdfBytes = reportService.generatePdfReport(userPrincipal.getId(), targetMonth, targetYear);
        String filename = "statement_" + targetYear + "_" + targetMonth + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import transactions from CSV file")
    public ResponseEntity<Map<String, Object>> importCsv(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CSV file cannot be empty"));
        }

        Map<String, Object> result = reportService.importTransactionsFromCsv(userPrincipal.getId(), file.getInputStream());
        return ResponseEntity.ok(result);
    }
}
