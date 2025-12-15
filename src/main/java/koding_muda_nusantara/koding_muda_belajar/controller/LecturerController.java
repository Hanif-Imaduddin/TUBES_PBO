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
import jakarta.validation.Valid;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LecturerController {

    // ==================== DASHBOARD ====================

    @GetMapping("/lecturer/dashboard")
    public String showLecturerDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("userRole");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        if (role.equals("Lecturer")){
            return "lecturer/dashboard";
        }
        return "redirect:/";
    }
}
