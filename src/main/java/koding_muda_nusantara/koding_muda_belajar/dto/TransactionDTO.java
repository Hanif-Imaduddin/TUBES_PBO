package koding_muda_nusantara.koding_muda_belajar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import koding_muda_nusantara.koding_muda_belajar.enums.PaymentMethod;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;
import koding_muda_nusantara.koding_muda_belajar.enums.TransactionType;

public class TransactionDTO {
    
    private Integer id;
    private String transactionCode;
    private TransactionType transactionType;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String paymentProofUrl;
    private LocalDateTime paidAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Student info
    private Integer studentId;
    private String studentName;
    private String studentEmail;
    private String studentInitials;
    
    // Items
    private List<TransactionItemDTO> items;
    private int itemCount;

    // Default constructor
    public TransactionDTO() {}

    // Constructor untuk list view (tanpa items detail)
    public TransactionDTO(Integer id, String transactionCode, TransactionType transactionType,
                          BigDecimal amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                          LocalDateTime paidAt, String notes, LocalDateTime createdAt,
                          Integer studentId, String firstName, String lastName, String email) {
        this.id = id;
        this.transactionCode = transactionCode;
        this.transactionType = transactionType;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paidAt = paidAt;
        this.notes = notes;
        this.createdAt = createdAt;
        this.studentId = studentId;
        this.studentName = firstName + (lastName != null ? " " + lastName : "");
        this.studentEmail = email;
        this.studentInitials = getInitials(firstName, lastName);
    }

    // Helper method untuk generate initials
    private String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentProofUrl() {
        return paymentProofUrl;
    }

    public void setPaymentProofUrl(String paymentProofUrl) {
        this.paymentProofUrl = paymentProofUrl;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentInitials() {
        return studentInitials;
    }

    public void setStudentInitials(String studentInitials) {
        this.studentInitials = studentInitials;
    }

    public List<TransactionItemDTO> getItems() {
        return items;
    }

    public void setItems(List<TransactionItemDTO> items) {
        this.items = items;
        this.itemCount = items != null ? items.size() : 0;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
