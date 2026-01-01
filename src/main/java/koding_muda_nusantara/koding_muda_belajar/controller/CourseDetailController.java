/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Review;
import koding_muda_nusantara.koding_muda_belajar.model.Section;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.CourseService;
import koding_muda_nusantara.koding_muda_belajar.service.EnrollmentService;
import koding_muda_nusantara.koding_muda_belajar.service.ReviewService;
import koding_muda_nusantara.koding_muda_belajar.service.UserService;
import koding_muda_nusantara.koding_muda_belajar.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author hanif
 */

@Controller
@RequestMapping("/courses")
public class CourseDetailController {
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private ReviewService reviewService;
    private Object isInWishlist;
    
    @GetMapping("/{courseSlug}")
    public String showCourseDetail(
            @PathVariable String courseSlug,
            @RequestParam(name="error",required = false) String err,
            HttpSession session,
            Model model
    ){
        Student student = getStudentFromSession(session);
        if (student == null) {
            System.out.println("Student == null");
            return "redirect:/login?redirect=/courses/" + courseSlug;
        }
        Course course = courseService.getCourseBySlug(courseSlug);
        if (course == null) {
            System.out.println("Course == null");
            return "redirect:/courses?error=notfound";
        }        
        Boolean isEnrolled = enrollmentService.canAccessCourse(student.getUserId(), course.getCourseId());
        int cartCount = userService.getCartCount(student);
        double courseAverageRating = courseService.getAverageRating(course.getCourseId());
        int courseTotalReviews = courseService.getTotalReviews(course.getCourseId());
        long courseTotalEnrollments = courseService.getTotalEnrollments(course.getCourseId());
        List<Review> reviews = courseService.getAllReviews(course.getCourseId());
        List<Section> sections = courseService.getAllSections(course.getCourseId());
        
        boolean hasReviewed = reviewService.isReviewedBy(course.getCourseId(), student.getUserId());
        
        model.addAttribute("error", err);
        model.addAttribute("course", course);
        model.addAttribute("sections", sections);
        model.addAttribute("user", student);
        model.addAttribute("isEnrolled",isEnrolled);
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("courseAverageRating", courseAverageRating);
        model.addAttribute("courseTotalReviews", courseTotalReviews);
        model.addAttribute("courseTotalEnrollments",courseTotalEnrollments);
        model.addAttribute("reviews", reviews);
        model.addAttribute("isInWishlist", isInWishlist);
        
        return "student/course-detail";
    }
    
    @PostMapping("/{courseSlug}/review")
    public String postReview(
            @RequestParam(name = "courseId") Integer courseId,
            @RequestParam(name = "rating") Integer rating,
            @RequestParam(name = "comment") String comment,
            @PathVariable String courseSlug,
            HttpSession session
    ){
        Student student = getStudentFromSession(session);
        if (student == null) {
            System.out.println("Student == null");
            return "redirect:/login?redirect=/courses/" + courseSlug;
        }
        
        Course course = courseService.getCourseById(courseId);
        
        // Cek apakah sudah enroll
        Boolean isEnrolled = enrollmentService.canAccessCourse(student.getUserId(), course.getCourseId());
        if (!isEnrolled) {
            return "redirect:/courses/" + courseSlug;
        }
        // Cek apakah sudah pernah review
        boolean hasReviewed = reviewService.isReviewedBy(course.getCourseId(), student.getUserId());
        if (hasReviewed) {
            return "redirect:/courses/" + courseSlug;
        }
        Review review = reviewService.createReview(courseId,rating,comment,student.getUserId());
        System.out.println(review);
        
        return "redirect:/courses/" + courseSlug;
    }
    
    // ==================== HELPER METHODS ====================
    private Student getStudentFromSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user instanceof Student) {
            return (Student) user;
        }
        return null;
    }
}
