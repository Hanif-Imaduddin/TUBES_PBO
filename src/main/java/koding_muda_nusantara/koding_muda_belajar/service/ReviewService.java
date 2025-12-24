/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.service;

/**
 *
 * @author hanif
 */

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.dto.ReviewDTO;
import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Review;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.repository.CourseRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.ReviewRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.StudentRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentRepository studentRepository;
    
    public boolean isReviewedBy(Integer courseId,Integer studentId){
        return reviewRepository.existsByStudentUserIdAndCourseCourseId(studentId, courseId);
    }
    
    public Review createReview(Integer courseId, Integer rating, String comment, Integer studentId){
        Review review = new Review();
        Course course = courseRepository.getReferenceById(courseId);
        Student student = studentRepository.getReferenceById(studentId);
        
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(rating);
        review.setReviewText(comment);
        
        return reviewRepository.save(review);
    }
    
    public List<ReviewDTO> getRecentReviewDTOs(int n) {
        return reviewRepository.findRecentReviewDTOs(PageRequest.of(0, n));
    }
}
