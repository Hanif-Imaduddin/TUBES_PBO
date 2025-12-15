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
import java.time.LocalDate;

@Entity
@Table(name = "balances")
public class Balance {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "amount", nullable = false)
    private Double amount = 0.0;

    @Column(name = "last_update_date", nullable = false)
    private LocalDate lastUpdateDate;

    public Balance() {
        this.amount = 0.0;
        this.lastUpdateDate = LocalDate.now();
    }

    public Balance(User user) {
        this.user = user;
        this.userId = user.getUserId();
        this.amount = 0.0;
        this.lastUpdateDate = LocalDate.now();
    }

    public Balance(User user, Double amount) {
        this.user = user;
        this.userId = user.getUserId();
        this.amount = amount;
        this.lastUpdateDate = LocalDate.now();
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
        this.lastUpdateDate = LocalDate.now();
    }

    public LocalDate getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDate lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
