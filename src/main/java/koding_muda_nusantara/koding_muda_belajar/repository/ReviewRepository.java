/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.repository;

import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.model.Review;
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
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId AND r.isApproved = true")
    Double getAverageRatingByCourseId(@Param("courseId") int courseId);
    int countByCourseCourseId(int courseId);
    List<Review> findByCourseCourseId(int courseId);
    
    boolean existsByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);
}
