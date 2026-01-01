package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    
    List<Wishlist> findByStudentUserIdOrderByAddedAtDesc(Integer studentId);
    
    Optional<Wishlist> findByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);
    
    boolean existsByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);
    
    void deleteByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);
    
    int countByStudentUserId(Integer studentId);
}
