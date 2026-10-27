package com.financetracker.pro.service.impl;

import com.financetracker.pro.document.Transaction;
import com.financetracker.pro.document.TransactionType;
import com.financetracker.pro.document.User;
import com.financetracker.pro.exception.BadRequestException;
import com.financetracker.pro.repository.TransactionRepository;
import com.financetracker.pro.repository.UserRepository;
import com.financetracker.pro.service.ReportService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public ByteArrayInputStream exportTransactionsToCsv(String userId, LocalDate start, LocalDate end) {
        List<Transaction> list;
        if (start != null && end != null) {
            list = transactionRepository.findByUserIdAndTransactionDateBetweenAndDeletedFalse(userId, start, end);
        } else {
            list = transactionRepository.findByUserIdAndDeletedFalse(userId);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(new PrintWriter(out),
                     CSVFormat.DEFAULT.builder().setHeader("ID", "Date", "Type", "Amount", "Currency", "Category", "Description", "Merchant", "PaymentMethod").build())) {

            for (Transaction t : list) {
                printer.printRecord(
                        t.getId(),
                        t.getTransactionDate(),
                        t.getType(),
                        t.getAmount(),
                        t.getCurrency(),
                        t.getCategory(),
                        t.getDescription(),
                        t.getMerchant(),
                        t.getPaymentMethod()
                );
            }
            printer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export CSV: " + e.getMessage());
        }
    }

    @Override
    public byte[] generatePdfReport(String userId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        User user = userRepository.findById(userId).orElse(null);
        String currency = (user != null) ? user.getPreferredCurrency() : "USD";
        String userName = (user != null) ? user.getFullName() : "Valued Member";

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndDeletedFalse(userId, start, end);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD);
            Paragraph title = new Paragraph("Personal Finance Tracker Pro - Monthly Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Paragraph meta = new Paragraph("Statement Period: " + ym.getMonth().name() + " " + year +
                    "\nGenerated for: " + userName + " (" + (user != null ? user.getEmail() : "") + ")" +
                    "\nGenerated On: " + LocalDate.now() + "\n\n", subFont);
            document.add(meta);

            // Summary Table
            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            summaryTable.addCell(new PdfPCell(new Phrase("Total Income (" + currency + ")")));
            summaryTable.addCell(new PdfPCell(new Phrase("Total Expense (" + currency + ")")));
            summaryTable.addCell(new PdfPCell(new Phrase("Net Savings (" + currency + ")")));

            summaryTable.addCell(new PdfPCell(new Phrase("+" + totalIncome.toString())));
            summaryTable.addCell(new PdfPCell(new Phrase("-" + totalExpense.toString())));
            summaryTable.addCell(new PdfPCell(new Phrase(netBalance.toString())));
            summaryTable.setSpacingAfter(20);
            document.add(summaryTable);

            // Transactions Table
            Paragraph transTitle = new Paragraph("Transaction Log", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            transTitle.setSpacingAfter(10);
            document.add(transTitle);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Date")));
            table.addCell(new PdfPCell(new Phrase("Type")));
            table.addCell(new PdfPCell(new Phrase("Category")));
            table.addCell(new PdfPCell(new Phrase("Description")));
            table.addCell(new PdfPCell(new Phrase("Amount")));

            for (Transaction t : transactions) {
                table.addCell(t.getTransactionDate().toString());
                table.addCell(t.getType().name());
                table.addCell(t.getCategory());
                table.addCell(t.getDescription() != null ? t.getDescription() : "");
                table.addCell((t.getType() == TransactionType.INCOME ? "+" : "-") + t.getAmount());
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("PDF Generation Error: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> importTransactionsFromCsv(String userId, InputStream inputStream) {
        int successful = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build())) {

            for (CSVRecord record : csvParser) {
                try {
                    String dateStr = record.get("Date");
                    String typeStr = record.get("Type");
                    String amountStr = record.get("Amount");
                    String category = record.isMapped("Category") ? record.get("Category") : "Other expenses";
                    String description = record.isMapped("Description") ? record.get("Description") : "";
                    String merchant = record.isMapped("Merchant") ? record.get("Merchant") : "";
                    String paymentMethod = record.isMapped("PaymentMethod") ? record.get("PaymentMethod") : "Cash";

                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                    TransactionType type = TransactionType.valueOf(typeStr.toUpperCase().trim());
                    BigDecimal amount = new BigDecimal(amountStr.trim());

                    Transaction transaction = Transaction.builder()
                            .userId(userId)
                            .transactionDate(date)
                            .type(type)
                            .amount(amount)
                            .currency("USD")
                            .category(category)
                            .description(description)
                            .merchant(merchant)
                            .paymentMethod(paymentMethod)
                            .deleted(false)
                            .build();

                    transactionRepository.save(transaction);
                    successful++;
                } catch (Exception ex) {
                    failed++;
                    errors.add("Row " + record.getRecordNumber() + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to read CSV input: " + e.getMessage());
        }

        return Map.of(
                "totalProcessed", successful + failed,
                "successful", successful,
                "failed", failed,
                "errors", errors
        );
    }
}
