/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.repository;

import java.util.List;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.model.CartItem;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hanif
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionCode(String transactionCode);
}
