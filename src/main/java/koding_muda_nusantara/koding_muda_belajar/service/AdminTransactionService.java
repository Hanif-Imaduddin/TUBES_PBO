package koding_muda_nusantara.koding_muda_belajar.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionItemDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionStatusUpdateDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;
import koding_muda_nusantara.koding_muda_belajar.enums.TransactionType;
import koding_muda_nusantara.koding_muda_belajar.model.Transaction;
import koding_muda_nusantara.koding_muda_belajar.repository.TransactionItemRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.TransactionRepository;

@Service
public class AdminTransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionItemRepository transactionItemRepository;

    // ==================== STATISTICS ====================

    /**
     * Get transaction statistics for admin dashboard
     */
    public TransactionStatsDTO getTransactionStats() {
        TransactionStatsDTO stats = new TransactionStatsDTO();
        
        // Count by status
        stats.setTotalTransactions(transactionRepository.countAll());
        stats.setPaidTransactions(transactionRepository.countByPaymentStatus(PaymentStatus.paid));
        stats.setPendingTransactions(transactionRepository.countByPaymentStatus(PaymentStatus.pending));
        stats.setFailedTransactions(transactionRepository.countByPaymentStatus(PaymentStatus.failed));
        stats.setRefundedTransactions(transactionRepository.countByPaymentStatus(PaymentStatus.refunded));
        
        // Revenue
        BigDecimal totalRevenue = transactionRepository.getTotalRevenue();
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // This month stats
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        BigDecimal revenueThisMonth = transactionRepository.getRevenueThisMonth(startOfMonth);
        stats.setRevenueThisMonth(revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO);
        stats.setTransactionsThisMonth(transactionRepository.countTransactionsThisMonth(startOfMonth));
        
        return stats;
    }

    // ==================== LIST & SEARCH ====================

    /**
     * Get all transactions with pagination
     */
    public Page<TransactionDTO> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findAllTransactionDTOs(pageable);
    }

    /**
     * Get transactions by status with pagination
     */
    public Page<TransactionDTO> getTransactionsByStatus(PaymentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByPaymentStatus(status, pageable);
    }

    /**
     * Get transactions by type with pagination
     */
    public Page<TransactionDTO> getTransactionsByType(TransactionType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByTransactionType(type, pageable);
    }

    /**
     * Search transactions by keyword
     */
    public Page<TransactionDTO> searchTransactions(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.searchTransactions(keyword, pageable);
    }

    /**
     * Get recent transactions for dashboard
     */
    public List<TransactionDTO> getRecentTransactions(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findRecentTransactions(pageable);
    }

    // ==================== DETAIL ====================

    /**
     * Get transaction by ID
     */
    public Optional<Transaction> getTransactionById(Integer id) {
        return transactionRepository.findById(id);
    }

    /**
     * Get transaction detail with items
     */
    public TransactionDTO getTransactionDetail(Integer id) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        
        if (transactionOpt.isEmpty()) {
            return null;
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Build DTO
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionCode(transaction.getTransactionCode());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setPaymentStatus(transaction.getPaymentStatus());
        dto.setPaymentProofUrl(transaction.getPaymentProofUrl());
        dto.setPaidAt(transaction.getPaidAt());
        dto.setNotes(transaction.getNotes());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        
        // Student info
        if (transaction.getStudent() != null) {
            dto.setStudentId(transaction.getStudent().getUserId());
            String firstName = transaction.getStudent().getFirstName();
            String lastName = transaction.getStudent().getLastName();
            dto.setStudentName(firstName + (lastName != null ? " " + lastName : ""));
            dto.setStudentEmail(transaction.getStudent().getEmail());
            
            // Initials
            StringBuilder initials = new StringBuilder();
            if (firstName != null && !firstName.isEmpty()) {
                initials.append(firstName.charAt(0));
            }
            if (lastName != null && !lastName.isEmpty()) {
                initials.append(lastName.charAt(0));
            }
            dto.setStudentInitials(initials.toString().toUpperCase());
        }
        
        // Get items
        List<TransactionItemDTO> items = transactionItemRepository.findItemDTOsByTransactionId(id);
        dto.setItems(items);
        
        return dto;
    }

    // ==================== UPDATE STATUS ====================

    /**
     * Update transaction status
     */
    @Transactional
    public boolean updateTransactionStatus(Integer id, TransactionStatusUpdateDTO updateDTO) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        
        if (transactionOpt.isEmpty()) {
            return false;
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Update status
        transaction.setPaymentStatus(updateDTO.getStatus());
        
        // Update notes if provided
        if (updateDTO.getNotes() != null && !updateDTO.getNotes().isEmpty()) {
            String existingNotes = transaction.getNotes();
            String newNote = "[Admin] " + updateDTO.getNotes();
            
            if (existingNotes != null && !existingNotes.isEmpty()) {
                transaction.setNotes(existingNotes + "\n" + newNote);
            } else {
                transaction.setNotes(newNote);
            }
        }
        
        // If status changed to paid, set paidAt
        if (updateDTO.getStatus() == PaymentStatus.paid && transaction.getPaidAt() == null) {
            transaction.setPaidAt(LocalDateTime.now());
        }
        
        // Update timestamp
        transaction.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        return true;
    }

    // ==================== EXPORT ====================

    /**
     * Get all transactions for export (no pagination)
     */
    public List<TransactionDTO> getAllTransactionsForExport() {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        return transactionRepository.findRecentTransactions(pageable);
    }

    /**
     * Get transactions by date range for export
     */
    public List<TransactionDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByDateRange(startDate, endDate);
    }

    // ==================== STUDENT TRANSACTIONS ====================

    /**
     * Get transactions by student ID
     */
    public List<TransactionDTO> getStudentTransactions(Integer studentId) {
        return transactionRepository.findByStudentId(studentId);
    }
}
