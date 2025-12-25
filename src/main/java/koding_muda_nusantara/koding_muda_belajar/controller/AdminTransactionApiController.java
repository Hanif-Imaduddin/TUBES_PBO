package koding_muda_nusantara.koding_muda_belajar.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionStatusUpdateDTO;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AdminTransactionService;

@RestController
@RequestMapping("/api/admin/transactions")
public class AdminTransactionApiController {

    @Autowired
    private AdminTransactionService adminTransactionService;

    /**
     * Check if user is admin
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user instanceof Admin;
    }

    /**
     * Get transaction detail by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionDetail(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        TransactionDTO transaction = adminTransactionService.getTransactionDetail(id);
        
        if (transaction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Transaction not found"));
        }
        
        return ResponseEntity.ok(transaction);
    }

    /**
     * Update transaction status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTransactionStatus(
            @PathVariable Integer id,
            @RequestBody TransactionStatusUpdateDTO updateDTO,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        boolean success = adminTransactionService.updateTransactionStatus(id, updateDTO);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Transaction not found"));
        }
    }

    /**
     * Export transactions to CSV
     */
    @GetMapping("/export")
    public void exportTransactions(HttpServletResponse response, HttpSession session) throws IOException {
        if (!isAdmin(session)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized");
            return;
        }

        // Set response headers for CSV download
        String filename = "transactions_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setCharacterEncoding("UTF-8");

        // Get all transactions
        List<TransactionDTO> transactions = adminTransactionService.getAllTransactionsForExport();

        // Write CSV
        PrintWriter writer = response.getWriter();
        
        // Header
        writer.println("Kode Transaksi,Nama Pengguna,Email,Tipe,Metode Pembayaran,Jumlah,Status,Tanggal");
        
        // Data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (TransactionDTO trx : transactions) {
            StringBuilder row = new StringBuilder();
            row.append(escapeCSV(trx.getTransactionCode())).append(",");
            row.append(escapeCSV(trx.getStudentName())).append(",");
            row.append(escapeCSV(trx.getStudentEmail())).append(",");
            row.append(escapeCSV(trx.getTransactionType().toString())).append(",");
            row.append(escapeCSV(trx.getPaymentMethod().toString())).append(",");
            row.append(trx.getAmount()).append(",");
            row.append(escapeCSV(trx.getPaymentStatus().toString())).append(",");
            row.append(trx.getCreatedAt() != null ? trx.getCreatedAt().format(formatter) : "");
            writer.println(row.toString());
        }
        
        writer.flush();
    }

    /**
     * Get transaction statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTransactionStats(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        return ResponseEntity.ok(adminTransactionService.getTransactionStats());
    }

    /**
     * Get recent transactions
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentTransactions(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        List<TransactionDTO> transactions = adminTransactionService.getRecentTransactions(10);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Helper method to escape CSV values
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, newline, or quote, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
