package com.financetracker.pro.service;

import com.financetracker.pro.document.Transaction;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    ByteArrayInputStream exportTransactionsToCsv(String userId, LocalDate start, LocalDate end);
    byte[] generatePdfReport(String userId, int month, int year);
    Map<String, Object> importTransactionsFromCsv(String userId, InputStream inputStream);
}
