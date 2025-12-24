package koding_muda_nusantara.koding_muda_belajar.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AdminCourseService;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseApiController {
    
    @Autowired
    private AdminCourseService adminCourseService;
    
    /**
     * Update status kursus
     * PUT /api/admin/courses/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCourseStatus(
            @PathVariable("id") Integer courseId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        // Validasi session admin
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Unauthorized access"));
        }
        
        String statusStr = request.get("status");
        if (statusStr == null || statusStr.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Status is required"));
        }
        
        try {
            CourseStatus status = CourseStatus.valueOf(statusStr);
            boolean success = adminCourseService.updateCourseStatus(courseId, status);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Status berhasil diubah");
                response.put("courseId", courseId);
                response.put("status", status.name());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Kursus tidak ditemukan"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Status tidak valid: " + statusStr));
        }
    }
    
    /**
     * Update featured status
     * PUT /api/admin/courses/{id}/featured
     */
    @PutMapping("/{id}/featured")
    public ResponseEntity<?> updateFeaturedStatus(
            @PathVariable("id") Integer courseId,
            @RequestBody Map<String, Boolean> request,
            HttpSession session) {
        
        // Validasi session admin
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Unauthorized access"));
        }
        
        Boolean featured = request.get("featured");
        if (featured == null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Featured status is required"));
        }
        
        boolean success = adminCourseService.updateFeaturedStatus(courseId, featured);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Featured status berhasil diubah");
            response.put("courseId", courseId);
            response.put("featured", featured);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Kursus tidak ditemukan"));
        }
    }
    
    /**
     * Hapus kursus
     * DELETE /api/admin/courses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(
            @PathVariable("id") Integer courseId,
            HttpSession session) {
        
        // Validasi session admin
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Unauthorized access"));
        }
        
        boolean success = adminCourseService.deleteCourse(courseId);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Kursus berhasil dihapus");
            response.put("courseId", courseId);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Kursus tidak ditemukan"));
        }
    }
    
    // ======================= HELPER METHODS =======================
    
    /**
     * Cek apakah session adalah admin
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        return user != null && "Admin".equals(role);
    }
    
    /**
     * Buat response error
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
}
