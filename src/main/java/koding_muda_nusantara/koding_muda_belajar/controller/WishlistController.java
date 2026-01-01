package koding_muda_nusantara.koding_muda_belajar.controller;

import jakarta.servlet.http.HttpSession;
import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    
    @Autowired
    private WishlistService wishlistService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private CartService cartService;
    
    /**
     * GET /wishlist - Tampilkan halaman wishlist
     */
    @GetMapping("")
    public String showWishlist(HttpSession session, Model model) {
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            return "redirect:/login?redirect=/wishlist";
        }
        
        List<Wishlist> wishlistItems = wishlistService.getAllByStudentId(student.getUserId());
        int cartCount = userService.getCartCount(student);
        
        model.addAttribute("wishlistItems", wishlistItems);
        model.addAttribute("student", student);
        model.addAttribute("cartCount", cartCount);
        
        return "student/wishlist";
    }
    
    /**
     * POST /wishlist/add - Tambah ke wishlist
     */
    @PostMapping("/add")
    public String addToWishlist(
            @RequestParam Integer courseId,
            @RequestParam(required = false) String redirect,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            return "redirect:/login";
        }
        
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("error", "Kursus tidak ditemukan");
            return "redirect:/courses";
        }
        
        // Cek apakah sudah enrolled
        if (enrollmentService.isEnrolled(student.getUserId(), courseId)) {
            redirectAttributes.addFlashAttribute("error", "Anda sudah terdaftar di kursus ini");
            return "redirect:/courses/" + course.getSlug();
        }
        
        // Cek apakah sudah ada di wishlist
        if (wishlistService.isInWishlist(student.getUserId(), courseId)) {
            redirectAttributes.addFlashAttribute("info", "Kursus sudah ada di wishlist");
            return redirect != null ? "redirect:" + redirect : "redirect:/courses/" + course.getSlug();
        }
        
        try {
            wishlistService.addToWishlist(student, course);
            redirectAttributes.addFlashAttribute("success", "Berhasil ditambahkan ke wishlist!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return redirect != null ? "redirect:" + redirect : "redirect:/courses/" + course.getSlug();
    }
    
    /**
     * POST /wishlist/remove - Hapus dari wishlist
     */
    @PostMapping("/remove")
    public String removeFromWishlist(
            @RequestParam Integer wishlistId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            return "redirect:/login";
        }
        
        try {
            wishlistService.removeFromWishlist(wishlistId);
            redirectAttributes.addFlashAttribute("success", "Berhasil dihapus dari wishlist");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/wishlist";
    }
    
    /**
     * POST /wishlist/toggle - Toggle wishlist (AJAX)
     */
    @PostMapping("/toggle")
    @ResponseBody
    public Map<String, Object> toggleWishlist(
            @RequestParam Integer courseId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            response.put("success", false);
            response.put("message", "Silakan login terlebih dahulu");
            return response;
        }
        
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            response.put("success", false);
            response.put("message", "Kursus tidak ditemukan");
            return response;
        }
        
        try {
            boolean isInWishlist = wishlistService.toggleWishlist(student, course);
            response.put("success", true);
            response.put("isInWishlist", isInWishlist);
            response.put("message", isInWishlist ? "Ditambahkan ke wishlist" : "Dihapus dari wishlist");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * POST /wishlist/move-to-cart - Pindahkan ke keranjang
     */
    @PostMapping("/move-to-cart")
    public String moveToCart(
            @RequestParam Integer wishlistId,
            @RequestParam Integer courseId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            return "redirect:/login";
        }
        
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("error", "Kursus tidak ditemukan");
            return "redirect:/wishlist";
        }
        
        try {
            // Tambah ke cart jika belum ada
            if (!cartService.alreadyExist(student.getUserId(), courseId)) {
                cartService.addItem(student, course);
            }
            
            // Hapus dari wishlist
            wishlistService.removeFromWishlist(wishlistId);
            
            redirectAttributes.addFlashAttribute("success", "Kursus dipindahkan ke keranjang");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/wishlist";
    }
}
