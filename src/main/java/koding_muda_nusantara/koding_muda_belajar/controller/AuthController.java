/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.controller;

/**
 *
 * @author hanif
 */
import koding_muda_nusantara.koding_muda_belajar.dto.LoginRequest;
import koding_muda_nusantara.koding_muda_belajar.dto.RegisterRequest;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.service.CategoryService;
import koding_muda_nusantara.koding_muda_belajar.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private CategoryService categoryService;

    // ==================== HOME ====================
    
    @GetMapping("/")
    public String home(Model model,HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        long totalStudents = userService.getTotalStudents();
        long totalLecturers = userService.getTotalLecturers();
        long totalCourses = courseService.getTotalCourses();
        
        CategoryDTO webCat = categoryService.getCategoryWithPublishedCourseCount("web-development");
        CategoryDTO dataCat = categoryService.getCategoryWithPublishedCourseCount("data-science");
        CategoryDTO securityCat = categoryService.getCategoryWithPublishedCourseCount("cyber-security");
        CategoryDTO dataBaseCat = categoryService.getCategoryWithPublishedCourseCount("database");

        List<CourseWithStatsDTO> popularCourse = courseService.getPopularCourse();
        List<CourseWithStatsDTO> recentCourse = courseService.getRecentCourse();
        
        model.addAttribute("user", user);
        model.addAttribute("role", session.getAttribute("userRole"));
        model.addAttribute("totalStudents",totalStudents);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalLecturers", totalLecturers);
        model.addAttribute("webCat", webCat);
        model.addAttribute("dataCat",dataCat );
        model.addAttribute("securityCat", securityCat);
        model.addAttribute("dataBaseCat", dataBaseCat);
        model.addAttribute("popularCourse", popularCourse);
        model.addAttribute("recentCourse", recentCourse);
        
        return "index";
    }

    // ==================== LOGIN ====================

    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        // Jika sudah login, redirect ke dashboard
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "redirect:/";
        }
        
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @ModelAttribute LoginRequest loginRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        try {
            User user = userService.login(loginRequest);
            
            // Simpan user di session
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userRole", userService.getUserRole(user));
            
            return "redirect:/";
            
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loginRequest", loginRequest);
            System.out.println("=============DEBUG LOGIN=================");
            System.out.println(e.getMessage());
            System.out.println("=============DEBUG LOGIN=================");
            return "login";
        }
    }

    // ==================== REGISTER ====================

    @GetMapping("/register")
    public String showRegisterPage(Model model, HttpSession session) {
        // Jika sudah login, redirect ke dashboard
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "redirect:/";
        }
        
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @ModelAttribute RegisterRequest registerRequest,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        try {
            userService.register(registerRequest);
            redirectAttributes.addFlashAttribute("success", "Registrasi berhasil! Silakan login.");
            return "redirect:/login";
            
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            return "register";
        }
    }

    // ==================== LOGOUT ====================

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Anda telah logout.");
        return "redirect:/login";
    }
}
