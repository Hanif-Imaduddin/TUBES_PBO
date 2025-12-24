/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.dto.ReviewDTO;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;
import koding_muda_nusantara.koding_muda_belajar.model.Review;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.CourseService;
import koding_muda_nusantara.koding_muda_belajar.service.ReviewService;
import koding_muda_nusantara.koding_muda_belajar.service.TransactionService;
import koding_muda_nusantara.koding_muda_belajar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author hanif
 */

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(
            Model model,
            HttpSession session
    ){
        User user = (User)session.getAttribute("user");
        Admin admin = null;
        if (!userService.isAdmin(user)){
            return "redirect:/logout";
        }else{
            admin = (Admin)user;
        }
        long totalLecturers = userService.getTotalLecturers();
        long totalStudents = userService.getTotalStudents();
        long totalUsers = totalLecturers+userService.getTotalStudents();
        long totalCourses = courseService.getTotalCourses();
        List<ReviewDTO> recentReviews = reviewService.getRecentReviewDTOs(5);
        long publishedCourses = courseService.countPublishedCourse();
        long draftCourses = courseService.countDraftCourse();
        long pendingTransactions = transactionService.countPendingTransactions();
        
        System.out.println("=== DEBUG REVIEWS ===");
        System.out.println("Reviews count: " + recentReviews.size());
        for(ReviewDTO r : recentReviews) {
            System.out.println("Review ID: " + r.getReviewText());
            System.out.println("Student: " + (r.getStudentName()));
            System.out.println("Course: " + (r.getCourseTitle()));
        }
        System.out.println("=====================");
        
        model.addAttribute("user", admin);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalLecturers", totalLecturers);
        model.addAttribute("recentReviews", recentReviews);
        model.addAttribute("totalStudents",totalStudents);
        model.addAttribute("publishedCourses", publishedCourses);
        model.addAttribute("draftCourses", draftCourses);
        model.addAttribute("pendingTransactions",pendingTransactions);
        
        
        
        return "admin/admin-dashboard";
    }
}
