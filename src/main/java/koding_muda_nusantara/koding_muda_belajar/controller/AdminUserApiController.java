package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.dto.ApiErrorResponse;
import koding_muda_nusantara.koding_muda_belajar.dto.UserRequestDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserResponseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.exception.DuplicateResourceException;
import koding_muda_nusantara.koding_muda_belajar.exception.ResourceNotFoundException;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AdminUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;

/**
 * REST API Controller untuk manajemen pengguna di admin panel
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserApiController {

    private final AdminUserService adminUserService;

    @Autowired
    public AdminUserApiController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * GET /api/admin/users - Mendapatkan semua user dengan pagination
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinDate"));
        Page<UserResponseDTO> users;

        if (search != null && !search.trim().isEmpty()) {
            users = adminUserService.searchUsers(search, pageable);
        } else if (role != null && !role.trim().isEmpty()) {
            users = adminUserService.getUsersByRole(role, pageable);
        } else {
            users = adminUserService.getAllUsers(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/users/all - Mendapatkan semua user tanpa pagination
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsersNoPagination(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        List<UserResponseDTO> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/admin/users/{id} - Mendapatkan user berdasarkan ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Integer userId, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        try {
            UserResponseDTO user = adminUserService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/users - Membuat user baru
     */
    @PostMapping
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserRequestDTO request,
            BindingResult bindingResult,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        // Validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            e -> e.getField(),
                            e -> e.getDefaultMessage(),
                            (e1, e2) -> e1
                    ));

            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    400, "Bad Request", "Data tidak valid"
            );
            errorResponse.setFieldErrors(errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            UserResponseDTO createdUser = adminUserService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiErrorResponse(409, "Conflict", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/users/{id} - Mengupdate user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable("id") Integer userId,
            @Valid @RequestBody UserRequestDTO request,
            BindingResult bindingResult,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        // Skip password validation for update if not provided
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            bindingResult.getAllErrors().removeIf(error -> 
                "password".equals(((org.springframework.validation.FieldError) error).getField())
            );
        }

        // Validation errors (excluding password if empty)
        List<org.springframework.validation.FieldError> errors = bindingResult.getFieldErrors()
                .stream()
                .filter(e -> !("password".equals(e.getField()) && 
                             (request.getPassword() == null || request.getPassword().isEmpty())))
                .collect(Collectors.toList());

        if (!errors.isEmpty()) {
            Map<String, String> errorMap = errors.stream()
                    .collect(Collectors.toMap(
                            e -> e.getField(),
                            e -> e.getDefaultMessage(),
                            (e1, e2) -> e1
                    ));

            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    400, "Bad Request", "Data tidak valid"
            );
            errorResponse.setFieldErrors(errorMap);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            UserResponseDTO updatedUser = adminUserService.updateUser(userId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse(404, "Not Found", e.getMessage()));
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiErrorResponse(409, "Conflict", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/users/{id} - Menghapus user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable("id") Integer userId,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        // Prevent self-deletion
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && currentUser.getUserId().equals(userId)) {
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse(400, "Bad Request", "Tidak dapat menghapus akun sendiri"));
        }

        try {
            adminUserService.deleteUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User berhasil dihapus");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/users/stats - Mendapatkan statistik user
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        UserStatsDTO stats = adminUserService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * PUT /api/admin/users/{id}/role - Mengubah role user
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeUserRole(
            @PathVariable("id") Integer userId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        String newRole = request.get("role");
        if (newRole == null || newRole.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse(400, "Bad Request", "Role wajib diisi"));
        }

        // Prevent self role change
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && currentUser.getUserId().equals(userId)) {
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse(400, "Bad Request", "Tidak dapat mengubah role akun sendiri"));
        }

        try {
            UserResponseDTO updatedUser = adminUserService.changeUserRole(userId, newRole);
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse(404, "Not Found", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/users/check-username - Cek ketersediaan username
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(
            @RequestParam String username,
            @RequestParam(required = false) Integer excludeId,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        boolean exists;
        if (excludeId != null) {
            exists = adminUserService.isUsernameExists(username, excludeId);
        } else {
            exists = adminUserService.isUsernameExists(username);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("available", !exists);
        response.put("username", username);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/users/check-email - Cek ketersediaan email
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Integer excludeId,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        boolean exists;
        if (excludeId != null) {
            exists = adminUserService.isEmailExists(email, excludeId);
        } else {
            exists = adminUserService.isEmailExists(email);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("available", !exists);
        response.put("email", email);
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method untuk cek apakah user adalah admin
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user instanceof Admin;
    }
}
