package koding_muda_nusantara.koding_muda_belajar.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import koding_muda_nusantara.koding_muda_belajar.dto.AdminCourseDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AdminCourseService;

@Controller
@RequestMapping("/admin")
public class AdminCourseController {
    
    @Autowired
    private AdminCourseService adminCourseService;
    
    private static final int DEFAULT_PAGE_SIZE = 10;
    
    /**
     * Halaman daftar kursus untuk admin
     * URL: /admin/courses
     */
    @GetMapping("/courses")
    public String coursesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search,
            Model model,
            HttpSession session) {
        
        // Cek session admin
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (user == null || !(user instanceof Admin)) {
            return "redirect:/login";
        }
        
        // Dapatkan data kursus dengan statistik
        Page<AdminCourseDTO> coursesPage = adminCourseService.getAllCoursesWithFullStatsPaged(page, size);
        List<AdminCourseDTO> courses = coursesPage.getContent();
        
        // Filter berdasarkan status jika ada
        if (status != null && !status.isEmpty()) {
            try {
                CourseStatus courseStatus = CourseStatus.valueOf(status);
                courses = adminCourseService.getCoursesByStatus(courseStatus);
            } catch (IllegalArgumentException e) {
                // Status tidak valid, abaikan filter
            }
        }
        
        // Filter berdasarkan kategori jika ada
        if (categoryId != null) {
            courses = adminCourseService.getCoursesByCategory(categoryId);
        }
        
        // Search jika ada
        if (search != null && !search.trim().isEmpty()) {
            courses = adminCourseService.searchCourses(search.trim());
        }
        
        // Dapatkan semua kategori untuk filter dropdown
        List<Category> categories = adminCourseService.getAllActiveCategories();
        
        // Statistik untuk header
        long totalCourses = adminCourseService.countAllCourses();
        long publishedCourses = adminCourseService.countByStatus(CourseStatus.published);
        long draftCourses = adminCourseService.countByStatus(CourseStatus.draft);
        long suspendedCourses = adminCourseService.countByStatus(CourseStatus.suspended);
        
        // Add to model
        model.addAttribute("courses", courses);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursesPage.getTotalPages());
        model.addAttribute("totalElements", coursesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        // Filter values untuk maintain state
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("searchKeyword", search);
        
        // Statistics
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("publishedCourses", publishedCourses);
        model.addAttribute("draftCourses", draftCourses);
        model.addAttribute("suspendedCourses", suspendedCourses);
        
        // User info
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        
        return "admin/admin-courses";
    }
    
    /**
     * Halaman detail kursus untuk admin
     * URL: /admin/courses/{id}
     */
    @GetMapping("/courses/{id}")
    public String courseDetailPage(
            @PathVariable("id") Integer courseId,
            Model model,
            HttpSession session) {
        
        // Cek session admin
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (user == null || !(user instanceof Admin)) {
            return "redirect:/login";
        }
        
        // Dapatkan detail kursus
        Course course = adminCourseService.getCourseById(courseId)
                .orElse(null);
        
        if (course == null) {
            return "redirect:/admin/courses?error=notfound";
        }
        
        model.addAttribute("course", course);
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        
        return "admin/course-detail";
    }
}
