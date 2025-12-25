package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;

/**
 * Controller untuk halaman web manajemen kategori (Admin)
 * Menangani request untuk menampilkan halaman HTML
 */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Menampilkan halaman daftar kategori
     * @param model
     * @param session
     * @return 
     */
    @GetMapping
    public String showCategoriesPage(Model model, HttpSession session) {
        // Cek autentikasi admin
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (user == null || !(user instanceof Admin)) {
            return "redirect:/login";
        }
        
        // Ambil semua kategori dengan jumlah kursus
        List<CategoryDTO> categories = categoryService.findAllWithCourseCount();
        
        // Tambahkan data ke model
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        model.addAttribute("pageTitle", "Manajemen Kategori");
        
        return "admin/admin-categories";
    }
}
