package koding_muda_nusantara.koding_muda_belajar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import koding_muda_nusantara.koding_muda_belajar.dto.AccountInfoDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.ChangePasswordDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UpdateProfileDTO;
import koding_muda_nusantara.koding_muda_belajar.exception.BadRequestException;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AccountService;

@Controller
@RequestMapping("/my-account")
public class MyAccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Halaman My Account - menampilkan dan edit profil
     */
    @GetMapping
    public String myAccountPage(HttpSession session, Model model) {
        // Cek autentikasi
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("userRole");
        System.out.println("========== Debug Role ==========");
        System.out.println(role);
        System.out.println(session.getAttribute("userRole"));
        System.out.println("========== Debug Role ==========");

        if (user == null) {
            return "redirect:/login?redirect=/my-account";
        }

        // Ambil informasi akun lengkap
        AccountInfoDTO accountInfo = accountService.getAccountInfo(user.getUserId(), role);

        // Siapkan DTO untuk form
        UpdateProfileDTO profileDTO = new UpdateProfileDTO(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail()
        );

        // Set attributes ke model
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("profileDTO", profileDTO);
        model.addAttribute("passwordDTO", new ChangePasswordDTO());
        model.addAttribute("pageTitle", "My Account");

        return "my-account";
    }

    /**
     * Handle update profil
     */
    @PostMapping("/update-profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileDTO") UpdateProfileDTO profileDTO,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("userRole");

        if (user == null) {
            return "redirect:/login";
        }

        // Jika ada error validasi
        if (bindingResult.hasErrors()) {
            AccountInfoDTO accountInfo = accountService.getAccountInfo(user.getUserId(), role);
            model.addAttribute("user", user);
            model.addAttribute("role", role);
            model.addAttribute("accountInfo", accountInfo);
            model.addAttribute("passwordDTO", new ChangePasswordDTO());
            model.addAttribute("activeTab", "profile");
            return "my-account";
        }

        try {
            // Update profil
            User updatedUser = accountService.updateProfile(user.getUserId(), profileDTO);
            
            // Update session dengan data terbaru
            session.setAttribute("user", updatedUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Terjadi kesalahan saat memperbarui profil");
        }

        return "redirect:/my-account";
    }

    /**
     * Handle ubah password
     */
    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordDTO") ChangePasswordDTO passwordDTO,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("userRole");

        if (user == null) {
            return "redirect:/login";
        }

        // Jika ada error validasi
        if (bindingResult.hasErrors()) {
            AccountInfoDTO accountInfo = accountService.getAccountInfo(user.getUserId(), role);
            UpdateProfileDTO profileDTO = new UpdateProfileDTO(
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUsername(),
                    user.getEmail()
            );
            model.addAttribute("user", user);
            model.addAttribute("role", role);
            model.addAttribute("accountInfo", accountInfo);
            model.addAttribute("profileDTO", profileDTO);
            model.addAttribute("activeTab", "security");
            return "my-account";
        }

        try {
            // Ubah password
            accountService.changePassword(user.getUserId(), passwordDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Password berhasil diubah");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "security");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Terjadi kesalahan saat mengubah password");
            redirectAttributes.addFlashAttribute("activeTab", "security");
        }

        return "redirect:/my-account";
    }
}
