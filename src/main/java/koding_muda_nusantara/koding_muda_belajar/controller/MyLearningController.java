package koding_muda_nusantara.koding_muda_belajar.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.MyLearningStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.repository.CategoryRepository;
import koding_muda_nusantara.koding_muda_belajar.service.MyLearningService;

@Controller
@RequestMapping("/my-courses")
public class MyLearningController {

    @Autowired
    private MyLearningService myLearningService;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Halaman My Learning - menampilkan semua kursus yang di-enroll
     */
    @GetMapping
    public String myLearningPage(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            HttpSession session,
            Model model) {

        // Cek autentikasi
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("userRole");
        System.out.println("========== Debug Role ==========");
        System.out.println(role);
        System.out.println("========== Debug Role ==========");

        if (user == null) {
            return "redirect:/login?redirect=/my-courses";
        }

        // Pastikan user adalah Student
        if (!(user instanceof Student)) {
            return "redirect:/";
        }

        Integer studentId = user.getUserId();

        // Ambil data kursus berdasarkan filter
        List<MyLearningCourseDTO> courses;

        if (search != null && !search.trim().isEmpty()) {
            // Search mode
            courses = myLearningService.searchMyLearningCourses(studentId, search);
            model.addAttribute("searchKeyword", search);
        } else if (category != null && category > 0) {
            // Filter by category
            courses = myLearningService.getMyLearningCoursesByCategory(studentId, category);
            model.addAttribute("selectedCategory", category);
        } else if (status != null && !status.isEmpty() && !status.equals("all")) {
            // Filter by status
            courses = myLearningService.getMyLearningCoursesByStatus(studentId, status);
        } else {
            // Default: semua kursus
            courses = myLearningService.getMyLearningCourses(studentId);
        }

        // Ambil statistik
        MyLearningStatsDTO stats = myLearningService.getMyLearningStats(studentId);

        // Ambil kursus yang baru diakses untuk "Continue Learning"
        List<MyLearningCourseDTO> continueLearning = myLearningService.getRecentlyAccessedCourses(studentId, 3);

        // Ambil kursus yang hampir selesai
        List<MyLearningCourseDTO> almostCompleted = myLearningService.getAlmostCompletedCourses(studentId);

        // Ambil daftar kategori untuk filter
        List<Category> categories = categoryRepository.findByIsActiveTrue();

        // Set attributes ke model
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        model.addAttribute("courses", courses);
        model.addAttribute("stats", stats);
        model.addAttribute("continueLearning", continueLearning);
        model.addAttribute("almostCompleted", almostCompleted);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedStatus", status != null ? status : "all");
        model.addAttribute("selectedSort", sort);
        model.addAttribute("pageTitle", "My Learning");

        return "student/my-learning";
    }
}
