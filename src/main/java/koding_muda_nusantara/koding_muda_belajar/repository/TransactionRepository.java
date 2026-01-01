/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;
import koding_muda_nusantara.koding_muda_belajar.enums.TransactionType;
import koding_muda_nusantara.koding_muda_belajar.model.CartItem;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hanif
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Optional<Transaction> findByTransactionCode(String transactionCode);
    
    long countByPaymentStatus(PaymentStatus paymentStatus);
    // Find all transactions with student info (paginated)
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO(" +
           "t.id, t.transactionCode, t.transactionType, t.amount, t.paymentMethod, " +
           "t.paymentStatus, t.paidAt, t.notes, t.createdAt, " +
           "u.userId, u.firstName, u.lastName, u.email) " +
           "FROM Transaction t " +
           "JOIN t.student s " +
           "JOIN User u ON u.userId = s.userId " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionDTO> findAllTransactionDTOs(Pageable pageable);

    // Find transactions by status (paginated)
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO(" +
           "t.id, t.transactionCode, t.transactionType, t.amount, t.paymentMethod, " +
           "t.paymentStatus, t.paidAt, t.notes, t.createdAt, " +
           "u.userId, u.firstName, u.lastName, u.email) " +
           "FROM Transaction t " +
           "JOIN t.student s " +
           "JOIN User u ON u.userId = s.userId " +
           "WHERE t.paymentStatus = :status " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionDTO> findByPaymentStatus(@Param("status") PaymentStatus status, Pageable pageable);

    // Find transactions by type (paginated)
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO(" +
           "t.id, t.transactionCode, t.transactionType, t.amount, t.paymentMethod, " +
           "t.paymentStatus, t.paidAt, t.notes, t.createdAt, " +
           "u.userId, u.firstName, u.lastName, u.email) " +
           "FROM Transaction t " +
           "JOIN t.student s " +
           "JOIN User u ON u.userId = s.userId " +
           "WHERE t.transactionType = :type " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionDTO> findByTransactionType(@Param("type") TransactionType type, Pageable pageable);

    // Search by transaction code or student name
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO(" +
           "t.id, t.transactionCode, t.transactionType, t.amount, t.paymentMethod, " +
           "t.paymentStatus, t.paidAt, t.notes, t.createdAt, " +
           "u.userId, u.firstName, u.lastName, u.email) " +
           "FROM Transaction t " +
           "JOIN t.student s " +
           "JOIN User u ON u.userId = s.userId " +
           "WHERE LOWER(t.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionDTO> searchTransactions(@Param("keyword") String keyword, Pageable pageable);


    // Count all transactions
    @Query("SELECT COUNT(t) FROM Transaction t")
    long countAll();

    // Total revenue (paid transactions)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.paymentStatus = 'paid'")
    BigDecimal getTotalRevenue();

    // Revenue this month
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.paymentStatus = 'paid' " +
           "AND t.paidAt >= :startOfMonth")
    BigDecimal getRevenueThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    // Transactions this month
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.createdAt >= :startOfMonth")
    long countTransactionsThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    // Find transactions by student ID
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO(" +
           "t.id, t.transactionCode, t.transactionType, t.amount, t.paymentMethod, " +
           "t.paymentStatus, t.paidAt, t.notes, t.createdAt, " +
           "u.userId, u.firstName, u.lastName, u.email) " +
           "FROM Transaction t " +
           "JOIN t.student s " +
           "JOIN User u ON u.userId = s.userId " +
           "WHERE s.userId = :studentId " +
           "ORDER BY t.createdAt DESC")
    List<TransactionDTO> findByStudentId(@Param("studentId") Integer studentId);

    // Recent transactions (for dashboard)
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO(" +
           "t.id, t.transactionCode, t.transactionType, t.amount, t.paymentMethod, " +
           "t.paymentStatus, t.paidAt, t.notes, t.createdAt, " +
           "u.userId, u.firstName, u.lastName, u.email) " +
           "FROM Transaction t " +
           "JOIN t.student s " +
           "JOIN User u ON u.userId = s.userId " +
           "ORDER BY t.createdAt DESC")
    List<TransactionDTO> findRecentTransactions(Pageable pageable);

    // Find transactions within date range
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO(" +
           "t.id, t.transactionCode, t.transactionType, t.amount, t.paymentMethod, " +
           "t.paymentStatus, t.paidAt, t.notes, t.createdAt, " +
           "u.userId, u.firstName, u.lastName, u.email) " +
           "FROM Transaction t " +
           "JOIN t.student s " +
           "JOIN User u ON u.userId = s.userId " +
           "WHERE t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    List<TransactionDTO> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
}
