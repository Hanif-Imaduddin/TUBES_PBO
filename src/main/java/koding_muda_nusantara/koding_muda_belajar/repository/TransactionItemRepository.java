/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.repository;

import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionItemDTO;
import koding_muda_nusantara.koding_muda_belajar.model.CartItem;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.model.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hanif
 */
@Repository
public interface TransactionItemRepository extends JpaRepository<TransactionItem, Integer> {
    List<TransactionItem> findByTransactionId(Integer transactionId);


    // Find items by transaction ID with course info (DTO)
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.TransactionItemDTO(" +
           "ti.id, ti.price, ti.createdAt, " +
           "c.courseId, c.title, c.slug, c.thumbnailUrl) " +
           "FROM TransactionItem ti " +
           "JOIN ti.course c " +
           "WHERE ti.transaction.id = :transactionId")
    List<TransactionItemDTO> findItemDTOsByTransactionId(@Param("transactionId") Integer transactionId);

    // Check if course is in any paid transaction for a student
    @Query("SELECT COUNT(ti) > 0 FROM TransactionItem ti " +
           "JOIN ti.transaction t " +
           "WHERE ti.course.courseId = :courseId " +
           "AND t.student.userId = :studentId " +
           "AND t.paymentStatus = 'paid'")
    boolean existsPaidTransactionForCourse(@Param("courseId") Integer courseId, 
                                           @Param("studentId") Integer studentId);

    // Delete all items by transaction ID
    void deleteByTransactionId(Integer transactionId);
}
