package koding_muda_nusantara.koding_muda_belajar.dto;

import java.math.BigDecimal;

public class TransactionStatsDTO {
    
    private long totalTransactions;
    private long paidTransactions;
    private long pendingTransactions;
    private long failedTransactions;
    private long refundedTransactions;
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private long transactionsThisMonth;

    // Default constructor
    public TransactionStatsDTO() {
        this.totalTransactions = 0;
        this.paidTransactions = 0;
        this.pendingTransactions = 0;
        this.failedTransactions = 0;
        this.refundedTransactions = 0;
        this.totalRevenue = BigDecimal.ZERO;
        this.revenueThisMonth = BigDecimal.ZERO;
        this.transactionsThisMonth = 0;
    }

    // Getters and Setters
    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getPaidTransactions() {
        return paidTransactions;
    }

    public void setPaidTransactions(long paidTransactions) {
        this.paidTransactions = paidTransactions;
    }

    public long getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(long pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public long getFailedTransactions() {
        return failedTransactions;
    }

    public void setFailedTransactions(long failedTransactions) {
        this.failedTransactions = failedTransactions;
    }

    public long getRefundedTransactions() {
        return refundedTransactions;
    }

    public void setRefundedTransactions(long refundedTransactions) {
        this.refundedTransactions = refundedTransactions;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public void setRevenueThisMonth(BigDecimal revenueThisMonth) {
        this.revenueThisMonth = revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO;
    }

    public long getTransactionsThisMonth() {
        return transactionsThisMonth;
    }

    public void setTransactionsThisMonth(long transactionsThisMonth) {
        this.transactionsThisMonth = transactionsThisMonth;
    }
}
