/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.controller;

/**
 *
 * @author hanif
 */
import koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.ReviewDTO;
import koding_muda_nusantara.koding_muda_belajar.model.Lecturer;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.LecturerCourseService;
import koding_muda_nusantara.koding_muda_belajar.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LecturerController {

    @Autowired
    private LecturerCourseService lecturerCourseService;
    
    @Autowired
    private ReviewService reviewService;

    // ==================== DASHBOARD ====================

    @GetMapping("/lecturer/dashboard")
    public String showLecturerDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("userRole");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!role.equals("Lecturer")) {
            return "redirect:/";
        }
        
        Lecturer lecturer = (Lecturer) user;
        Integer lecturerId = lecturer.getUserId();
        
        // Get statistics
        long totalCourses = lecturerCourseService.getTotalCourses(lecturerId);
        long publishedCourses = lecturerCourseService.getPublishedCourses(lecturerId);
        long draftCourses = lecturerCourseService.getDraftCourses(lecturerId);
        
        // Get recent courses (max 5)
        Page<CourseWithStatsDTO> coursesPage = lecturerCourseService.getLecturerCourses(
            lecturerId, null, null, null, 0, 5
        );
        List<CourseWithStatsDTO> courses = coursesPage.getContent();
        
        // Calculate total students from courses
        long totalStudents = courses.stream()
            .mapToLong(CourseWithStatsDTO::getTotalStudents)
            .sum();
        
        // Calculate average rating
        double totalRating = courses.stream()
            .filter(c -> c.getAverageRating() != null && c.getAverageRating() > 0)
            .mapToDouble(CourseWithStatsDTO::getAverageRating)
            .average()
            .orElse(0.0);
        String averageRating = totalRating > 0 ? String.format("%.1f", totalRating) : "-";
        
        // Get recent reviews for this lecturer's courses
        List<ReviewDTO> recentReviews = reviewService.getRecentReviewDTOsByLecturerId(lecturerId, 5);
        
        // Add attributes to model
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("publishedCourses", publishedCourses);
        model.addAttribute("draftCourses", draftCourses);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("courses", courses);
        model.addAttribute("recentReviews", recentReviews);
        
        return "lecturer/dashboard";
    }
}