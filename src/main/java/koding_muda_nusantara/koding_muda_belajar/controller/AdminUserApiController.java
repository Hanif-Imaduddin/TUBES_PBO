package koding_muda_nusantara.koding_muda_belajar.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import koding_muda_nusantara.koding_muda_belajar.dto.ApiErrorResponse;
import koding_muda_nusantara.koding_muda_belajar.dto.UserRequestDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserResponseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.exception.DuplicateResourceException;
import koding_muda_nusantara.koding_muda_belajar.exception.ResourceNotFoundException;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * REST API Controller untuk manajemen user oleh Admin
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserApiController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * Helper method untuk mengecek apakah user adalah admin
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user instanceof Admin;
    }

    /**
     * GET /api/admin/users - Mengambil semua user dengan pagination dan filter
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
     * GET /api/admin/users/stats - Mengambil statistik user
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(403, "Forbidden", "Akses ditolak"));
        }

        try {
            UserStatsDTO stats = adminUserService.getUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(500, "Internal Server Error", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/users/{id} - Mengambil user berdasarkan ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable("id") Integer userId,
            HttpSession session) {

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
            Map<String, String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value",
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
     * 
     * PERBAIKAN: Tidak menggunakan removeIf() pada unmodifiable list
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

        // PERBAIKAN: Filter validation errors tanpa memodifikasi list asli
        // Skip password validation untuk update jika tidak diisi
        boolean skipPasswordValidation = request.getPassword() == null || request.getPassword().isEmpty();

        // Ambil field errors dan filter menggunakan stream (tidak memodifikasi list asli)
        List<FieldError> filteredErrors = bindingResult.getFieldErrors()
                .stream()
                .filter(error -> {
                    // Jika password kosong, skip error validasi password
                    if (skipPasswordValidation && "password".equals(error.getField())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Cek apakah ada error setelah filtering
        if (!filteredErrors.isEmpty()) {
            Map<String, String> errorMap = filteredErrors.stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value",
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
}