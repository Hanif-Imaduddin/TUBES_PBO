/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentMethod;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;
import koding_muda_nusantara.koding_muda_belajar.enums.TransactionType;
import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.model.Transaction;
import koding_muda_nusantara.koding_muda_belajar.model.TransactionItem;
import koding_muda_nusantara.koding_muda_belajar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hanif
 */
@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TransactionItemRepository transactionItemRepository;
    
    @Autowired
    private CourseRepository couresRepository;
    
    @Transactional
    public Transaction createTransaction(
            List<Integer> courseIds,
            String paymentMethod,
            Student student
    ){
        Transaction transaction = new Transaction();
        
        transaction.setStudent(student);
        
        transaction.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
        
        LocalDateTime padeAt = LocalDateTime.now();
        
        transaction.setPaidAt(padeAt);
        transaction.setPaymentStatus(PaymentStatus.paid);
        
        String transaction_code = "TRX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"-"+UUID.randomUUID().toString().substring(0, 6);
        
        transaction.setTransactionCode(transaction_code);
        
        transaction.setTransactionType(TransactionType.purchase);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Integer courseId: courseIds){
            Course course = couresRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course tidak ditemukan: " + courseId));
            TransactionItem transactionItem = new TransactionItem();
            transactionItem.setCourse(course);
            transactionItem.setPrice(course.getPrice());
            totalAmount = totalAmount.add(course.getPrice());
            
            transaction.addItem(transactionItem);
        }
        transaction.setAmount(totalAmount);
        return transactionRepository.save(transaction);
    }
    
    public Transaction getByTransactionId(String transactionCode){
        return transactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan"));
    }
    
    public List<TransactionItem> getAllItemsById(int transactionId){
        return transactionItemRepository.findByTransactionId(transactionId);
    }
    
    public long countPendingTransactions(){
        return transactionRepository.countByPaymentStatus(PaymentStatus.pending);
    }
}
