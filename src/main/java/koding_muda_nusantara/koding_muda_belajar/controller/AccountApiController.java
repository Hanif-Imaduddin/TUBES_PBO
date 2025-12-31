package koding_muda_nusantara.koding_muda_belajar.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import koding_muda_nusantara.koding_muda_belajar.dto.AccountInfoDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.ChangePasswordDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UpdateProfileDTO;
import koding_muda_nusantara.koding_muda_belajar.exception.BadRequestException;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AccountService;

@RestController
@RequestMapping("/api/account")
public class AccountApiController {

    @Autowired
    private AccountService accountService;

    /**
     * GET /api/account - Mendapatkan informasi akun
     */
    @GetMapping
    public ResponseEntity<?> getAccountInfo(HttpSession session) {
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("userRole");

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        try {
            AccountInfoDTO accountInfo = accountService.getAccountInfo(user.getUserId(), role);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", accountInfo
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * PUT /api/account/profile - Update profil
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateProfileDTO dto,
            BindingResult bindingResult,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        // Validasi error
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Validasi gagal",
                    "errors", errors
            ));
        }

        try {
            User updatedUser = accountService.updateProfile(user.getUserId(), dto);
            
            // Update session
            session.setAttribute("user", updatedUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profil berhasil diperbarui"
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan server"
            ));
        }
    }

    /**
     * POST /api/account/change-password - Ubah password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordDTO dto,
            BindingResult bindingResult,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        // Validasi error
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Validasi gagal",
                    "errors", errors
            ));
        }

        try {
            accountService.changePassword(user.getUserId(), dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password berhasil diubah"
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan server"
            ));
        }
    }

    /**
     * GET /api/account/check-username - Cek ketersediaan username
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(String username, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("available", false));
        }

        try {
            boolean exists = accountService.getUserById(user.getUserId())
                    .map(u -> !u.getUsername().equals(username))
                    .orElse(true);
            
            // Jika username berbeda, cek apakah sudah dipakai user lain
            // Logic ini bisa dikembangkan lebih lanjut
            return ResponseEntity.ok(Map.of("available", !exists));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("available", false));
        }
    }

    /**
     * GET /api/account/check-email - Cek ketersediaan email
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(String email, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("available", false));
        }

        try {
            boolean isSameEmail = accountService.getUserById(user.getUserId())
                    .map(u -> u.getEmail().equals(email))
                    .orElse(false);
            
            return ResponseEntity.ok(Map.of("available", isSameEmail));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("available", false));
        }
    }
}
