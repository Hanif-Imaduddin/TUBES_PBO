package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    public List<Wishlist> getAllByStudentId(Integer studentId) {
        return wishlistRepository.findByStudentUserIdOrderByAddedAtDesc(studentId);
    }
    
    public boolean isInWishlist(Integer studentId, Integer courseId) {
        return wishlistRepository.existsByStudentUserIdAndCourseCourseId(studentId, courseId);
    }
    
    public int getWishlistCount(Integer studentId) {
        return wishlistRepository.countByStudentUserId(studentId);
    }
    
    @Transactional
    public Wishlist addToWishlist(Student student, Course course) {
        // Cek apakah sudah ada di wishlist
        if (isInWishlist(student.getUserId(), course.getCourseId())) {
            throw new RuntimeException("Kursus sudah ada di wishlist");
        }
        
        Wishlist wishlist = new Wishlist();
        wishlist.setStudent(student);
        wishlist.setCourse(course);
        return wishlistRepository.save(wishlist);
    }
    
    @Transactional
    public void removeFromWishlist(Integer wishlistId) {
        wishlistRepository.deleteById(wishlistId);
    }
    
    @Transactional
    public void removeFromWishlistByStudentAndCourse(Integer studentId, Integer courseId) {
        wishlistRepository.deleteByStudentUserIdAndCourseCourseId(studentId, courseId);
    }
    
    @Transactional
    public boolean toggleWishlist(Student student, Course course) {
        Optional<Wishlist> existing = wishlistRepository.findByStudentUserIdAndCourseCourseId(
                student.getUserId(), course.getCourseId());
        
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false; // Removed from wishlist
        } else {
            Wishlist wishlist = new Wishlist();
            wishlist.setStudent(student);
            wishlist.setCourse(course);
            wishlistRepository.save(wishlist);
            return true; // Added to wishlist
        }
    }
}
