package koding_muda_nusantara.koding_muda_belajar.dto;

import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;

public class TransactionStatusUpdateDTO {
    
    private PaymentStatus status;
    private String notes;

    // Default constructor
    public TransactionStatusUpdateDTO() {}

    public TransactionStatusUpdateDTO(PaymentStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
