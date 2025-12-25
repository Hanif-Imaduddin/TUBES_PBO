package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.dto.UserResponseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AdminUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;

/**
 * Controller untuk halaman manajemen pengguna di admin panel
 */
@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Autowired
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Halaman daftar pengguna
     */
    @GetMapping("/users")
    public String usersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            HttpSession session,
            Model model) {

        // Check authentication
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("role");
        if (!(currentUser instanceof Admin)) {
            return "redirect:/";
        }

        // Get user stats
        UserStatsDTO stats = adminUserService.getUserStats();
        model.addAttribute("totalStudents", stats.getTotalStudents());
        model.addAttribute("totalLecturers", stats.getTotalLecturers());
        model.addAttribute("totalAdmins", stats.getTotalAdmins());

        // Get users with pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinDate"));
        Page<UserResponseDTO> usersPage;

        if (search != null && !search.trim().isEmpty()) {
            usersPage = adminUserService.searchUsers(search, pageable);
            model.addAttribute("search", search);
        } else if (role != null && !role.trim().isEmpty()) {
            usersPage = adminUserService.getUsersByRole(role, pageable);
            model.addAttribute("selectedRole", role);
        } else {
            usersPage = adminUserService.getAllUsers(pageable);
        }

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalElements", usersPage.getTotalElements());
        model.addAttribute("size", size);

        // User info for header
        model.addAttribute("user", currentUser);
        model.addAttribute("role", userRole);

        return "admin/admin-users";
    }
}
