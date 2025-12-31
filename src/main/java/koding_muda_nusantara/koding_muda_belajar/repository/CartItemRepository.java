/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.repository;

/**
 *
 * @author hanif
 */
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hanif
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    int countByStudentUserId(Integer studentId);
    
    List<CartItem> findByStudentUserId(Integer studentId);
    
    @Query("SELECT SUM(ci.course.price) FROM CartItem ci WHERE ci.student.userId = :studentId")
    BigDecimal sumTotalPriceByStudentId(@Param("studentId") Integer studentId);
    
    boolean existsByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);
    
    Optional<CartItem> findByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);
     /**
     * Hapus semua cart item yang berisi course tertentu
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.course.courseId = :courseId")
    void deleteByCourse_CourseId(@Param("courseId") Integer courseId);
    
    /**
     * Hitung jumlah cart item untuk course tertentu
     */
    long countByCourse_CourseId(Integer courseId);
}
