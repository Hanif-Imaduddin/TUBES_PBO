/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.model;

/**
 *
 * @author hanif
 */
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentMethod;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;
import koding_muda_nusantara.koding_muda_belajar.enums.TransactionType;

@Entity
@Table(name = "transactions")
@Data
@EntityListeners(AuditingEntityListener.class) // Untuk otomatis update created_at/updated_at
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private int id;

    // Relasi ke Student (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "transaction_code", nullable = false, unique = true, length = 50)
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType = TransactionType.purchase; // Default

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.pending;

    @Column(name = "payment_proof_url", length = 500)
    private String paymentProofUrl;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relasi ke Item Transaksi (One Transaction -> Many Items)
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionItem> items = new ArrayList<>();
    
    public void addItem(TransactionItem item) {
        items.add(item);
        item.setTransaction(this);
    }

    public void removeItem(TransactionItem item) {
        items.remove(item);
        item.setTransaction(null);
    }

}
