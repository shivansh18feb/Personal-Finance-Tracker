package com.financetracker.pro.controller;

import com.financetracker.pro.document.RecurringTransaction;
import com.financetracker.pro.security.UserPrincipal;
import com.financetracker.pro.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
@Tag(name = "Recurring & Subscriptions", description = "Automated Recurring Bills & Subscription Lifecycle APIs")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;

    @PostMapping
    @Operation(summary = "Create a recurring transaction or subscription rule")
    public ResponseEntity<RecurringTransaction> createRecurring(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody RecurringTransaction recurring) {
        return new ResponseEntity<>(recurringService.createRecurring(userPrincipal.getId(), recurring), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all recurring transactions")
    public ResponseEntity<List<RecurringTransaction>> getAll(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(recurringService.getAllRecurring(userPrincipal.getId()));
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Get active and tracked subscriptions")
    public ResponseEntity<List<RecurringTransaction>> getSubscriptions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(recurringService.getSubscriptions(userPrincipal.getId()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status (ACTIVE, PAUSED, CANCELLED)")
    public ResponseEntity<RecurringTransaction> updateStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        return ResponseEntity.ok(recurringService.updateStatus(userPrincipal.getId(), id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recurring rule")
    public ResponseEntity<Map<String, String>> deleteRecurring(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        recurringService.deleteRecurring(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Recurring transaction rule deleted"));
    }
}
