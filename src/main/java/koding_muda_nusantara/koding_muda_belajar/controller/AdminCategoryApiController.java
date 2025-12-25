package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;

/**
 * REST API Controller untuk operasi CRUD kategori
 * Digunakan oleh JavaScript di halaman admin
 */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryApiController {

    private final CategoryService categoryService;

    @Autowired
    public AdminCategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Cek apakah user adalah admin
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && (user instanceof Admin);
    }

    /**
     * Response untuk unauthorized access
     */
    private ResponseEntity<Map<String, Object>> unauthorizedResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Akses ditolak. Silakan login sebagai admin.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Response untuk error
     */
    private ResponseEntity<Map<String, Object>> errorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Response untuk sukses
     */
    private ResponseEntity<Map<String, Object>> successResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // API ENDPOINTS
    // ========================================================================

    /**
     * GET /api/admin/categories
     * Mendapatkan semua kategori
     */
    @GetMapping
    public ResponseEntity<?> getAllCategories(HttpSession session) {
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        List<CategoryDTO> categories = categoryService.findAllWithCourseCount();
        return ResponseEntity.ok(categories);
    }

    /**
     * GET /api/admin/categories/{id}
     * Mendapatkan kategori berdasarkan ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        Optional<CategoryDTO> category = categoryService.findById(id);
        
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        } else {
            return errorResponse("Kategori tidak ditemukan", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * POST /api/admin/categories
     * Membuat kategori baru
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO, 
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        try {
            // Validasi slug unik
            if (categoryService.isSlugExists(categoryDTO.getSlug())) {
                return errorResponse("Slug sudah digunakan", HttpStatus.BAD_REQUEST);
            }
            
            // Validasi nama unik
            if (categoryService.isNameExists(categoryDTO.getName())) {
                return errorResponse("Nama kategori sudah ada", HttpStatus.BAD_REQUEST);
            }
            
            CategoryDTO savedCategory = categoryService.save(categoryDTO);
            return successResponse("Kategori berhasil ditambahkan", savedCategory);
            
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Terjadi kesalahan saat menyimpan kategori", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /api/admin/categories/{id}
     * Update kategori yang sudah ada
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryDTO categoryDTO,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        try {
            // Cek apakah kategori ada
            if (categoryService.findById(id).isEmpty()) {
                return errorResponse("Kategori tidak ditemukan", HttpStatus.NOT_FOUND);
            }
            
            // Validasi slug unik (kecuali untuk kategori ini)
            if (categoryService.isSlugExistsExcludingId(categoryDTO.getSlug(), id)) {
                return errorResponse("Slug sudah digunakan", HttpStatus.BAD_REQUEST);
            }
            
            // Validasi nama unik (kecuali untuk kategori ini)
            if (categoryService.isNameExistsExcludingId(categoryDTO.getName(), id)) {
                return errorResponse("Nama kategori sudah ada", HttpStatus.BAD_REQUEST);
            }
            
            CategoryDTO updatedCategory = categoryService.update(id, categoryDTO);
            return successResponse("Kategori berhasil diperbarui", updatedCategory);
            
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Terjadi kesalahan saat memperbarui kategori", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /api/admin/categories/{id}
     * Menghapus kategori
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(
            @PathVariable Integer id,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        try {
            // Cek apakah kategori ada
            Optional<CategoryDTO> category = categoryService.findById(id);
            if (category.isEmpty()) {
                return errorResponse("Kategori tidak ditemukan", HttpStatus.NOT_FOUND);
            }
            
            // Cek apakah kategori bisa dihapus
            if (!categoryService.canDelete(id)) {
                return errorResponse("Tidak dapat menghapus kategori yang masih memiliki kursus", HttpStatus.BAD_REQUEST);
            }
            
            categoryService.deleteById(id);
            return successResponse("Kategori berhasil dihapus", null);
            
        } catch (IllegalStateException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("Terjadi kesalahan saat menghapus kategori", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PATCH /api/admin/categories/{id}/toggle-status
     * Toggle status aktif kategori
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleCategoryStatus(
            @PathVariable Integer id,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        try {
            // Cek apakah kategori ada
            if (categoryService.findById(id).isEmpty()) {
                return errorResponse("Kategori tidak ditemukan", HttpStatus.NOT_FOUND);
            }
            
            CategoryDTO updatedCategory = categoryService.toggleActiveStatus(id);
            String status = updatedCategory.isActive() ? "diaktifkan" : "dinonaktifkan";
            return successResponse("Kategori berhasil " + status, updatedCategory);
            
        } catch (Exception e) {
            return errorResponse("Terjadi kesalahan saat mengubah status kategori", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/admin/categories/check-slug
     * Cek apakah slug sudah ada
     */
    @GetMapping("/check-slug")
    public ResponseEntity<Map<String, Object>> checkSlug(
            @RequestParam String slug,
            @RequestParam(required = false) Integer excludeId,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        boolean exists;
        if (excludeId != null) {
            exists = categoryService.isSlugExistsExcludingId(slug, excludeId);
        } else {
            exists = categoryService.isSlugExists(slug);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/categories/check-name
     * Cek apakah nama sudah ada
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkName(
            @RequestParam String name,
            @RequestParam(required = false) Integer excludeId,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        boolean exists;
        if (excludeId != null) {
            exists = categoryService.isNameExistsExcludingId(name, excludeId);
        } else {
            exists = categoryService.isNameExists(name);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/categories/search
     * Mencari kategori berdasarkan nama
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchCategories(
            @RequestParam String q,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        List<CategoryDTO> categories = categoryService.searchByName(q);
        return ResponseEntity.ok(categories);
    }

    /**
     * GET /api/admin/categories/active
     * Mendapatkan semua kategori yang aktif
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveCategories(HttpSession session) {
        if (!isAdmin(session)) {
            return unauthorizedResponse();
        }
        
        List<CategoryDTO> categories = categoryService.findAllActive();
        return ResponseEntity.ok(categories);
    }
}
