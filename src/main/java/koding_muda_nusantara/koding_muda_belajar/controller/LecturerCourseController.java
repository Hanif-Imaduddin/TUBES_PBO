package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.dto.CourseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.LessonDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.SectionDTO;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Lecturer;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.CategoryService;
import koding_muda_nusantara.koding_muda_belajar.service.CourseService;
import koding_muda_nusantara.koding_muda_belajar.service.FileStorageService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.repository.CategoryRepository;
import koding_muda_nusantara.koding_muda_belajar.service.LecturerCourseService;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/lecturer/courses")
public class LecturerCourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private LecturerCourseService lecturerCourseService;
    
    @Autowired
    private CategoryRepository categoryRepository;

    // ==================== HELPER METHODS ====================

    /**
     * Mendapatkan lecturer dari session
     * Return null jika user bukan lecturer atau belum login
     */
    private Lecturer getLecturerFromSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user instanceof Lecturer) {
            return (Lecturer) user;
        }
        return null;
    }

    /**
     * Cek apakah user adalah lecturer
     */
    private boolean isLecturer(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        return "Lecturer".equals(role);
    }

    // ==================== LIST COURSES ====================

    /**
     * Menampilkan daftar kursus milik lecturer
     * @param page
     * @param size
     * @param status
     * @param category
     * @param search
     * @param model
     * @param session
     * @return 
     */
    @GetMapping
    public String courses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Model model,
            HttpSession session) {
        
        // Cek session
        Object userObj = session.getAttribute("user");
        if (userObj == null || !(userObj instanceof Lecturer)) {
            return "redirect:/login";
        }
        
        Lecturer lecturer = (Lecturer) userObj;
        
        // Ambil data courses dengan pagination
        Page<CourseWithStatsDTO> coursePage = lecturerCourseService.getLecturerCourses(
                lecturer.getUserId(), status, category, search, page, size);
        
        // Ambil semua kategori untuk filter
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        
        // Statistik
        long totalCourses = lecturerCourseService.getTotalCourses(lecturer.getUserId());
        long publishedCourses = lecturerCourseService.getPublishedCourses(lecturer.getUserId());
        long draftCourses = lecturerCourseService.getDraftCourses(lecturer.getUserId());
        
        // Add to model
        model.addAttribute("user", lecturer);
        model.addAttribute("role", "Lecturer");
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("totalItems", coursePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("categories", categories);
        
        // Filter values untuk maintain state
        model.addAttribute("selectedStatus", status != null ? status : "");
        model.addAttribute("selectedCategory", category != null ? category : "");
        model.addAttribute("searchQuery", search != null ? search : "");
        
        // Statistik
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("publishedCourses", publishedCourses);
        model.addAttribute("draftCourses", draftCourses);
        
        // Pagination info
        int startItem = page * size + 1;
        int endItem = Math.min((page + 1) * size, (int) coursePage.getTotalElements());
        model.addAttribute("startItem", coursePage.getTotalElements() > 0 ? startItem : 0);
        model.addAttribute("endItem", endItem);
        
        return "lecturer/courses";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Integer id, HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (userObj == null || !(userObj instanceof Lecturer)) {
            return "redirect:/login";
        }
        
        // Implement delete logic dengan validasi ownership
        // courseService.deleteCourse(id, ((Lecturer) userObj).getUserId());
        
        return "redirect:/lecturer/courses";
    }

    // ==================== CREATE COURSE ====================

    /**
     * Menampilkan form untuk membuat kursus baru
     */
    @GetMapping("/create")
    public String showCreateForm(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // Cek authentication
        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            redirectAttributes.addFlashAttribute("error", "Silakan login sebagai lecturer terlebih dahulu");
            return "redirect:/login";
        }

        CourseDTO courseDTO = new CourseDTO();

        // Tambahkan section default
        SectionDTO defaultSection = new SectionDTO();
        defaultSection.setTitle("Section 1: Pendahuluan");
        defaultSection.setSortOrder(0);
        courseDTO.addSection(defaultSection);

        List<Category> categories = categoryService.getAllActiveCategories();

        model.addAttribute("user", lecturer);
        model.addAttribute("courseDTO", courseDTO);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Buat Kursus Baru");

        return "lecturer/course-form";
    }

    // ==================== EDIT COURSE ====================

    /**
     * Menampilkan form untuk edit kursus
     */
    @GetMapping("/{courseId}/edit")
    public String showEditForm(
            @PathVariable Integer courseId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // Cek authentication
        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            redirectAttributes.addFlashAttribute("error", "Silakan login sebagai lecturer terlebih dahulu");
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);

        // Validasi kepemilikan kursus
        if (course == null || !course.getLecturer().getUserId().equals(lecturer.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "Kursus tidak ditemukan atau Anda tidak memiliki akses");
            return "redirect:/lecturer/courses";
        }

        // Convert entity ke DTO
        CourseDTO courseDTO = courseService.convertToDTO(course);
        List<Category> categories = categoryService.getAllActiveCategories();

        model.addAttribute("user", lecturer);
        model.addAttribute("courseDTO", courseDTO);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Edit Kursus");

        return "lecturer/course-form";
    }

    // ==================== SAVE COURSE ====================

    /**
     * Menyimpan kursus baru atau update kursus existing
     * Thumbnail sudah diupload via AJAX terpisah, URL-nya ada di existingThumbnailUrl
     */
    @PostMapping("/save")
    public String saveCourse(
            @Valid @ModelAttribute("courseDTO") CourseDTO courseDTO,
            BindingResult bindingResult,
            HttpSession session,
            @RequestParam(value = "sectionsJson", required = false) String sectionsJson,
            @RequestParam(value = "action", defaultValue = "draft") String action,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // Cek authentication
        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            redirectAttributes.addFlashAttribute("error", "Silakan login sebagai lecturer terlebih dahulu");
            return "redirect:/login";
        }

        // Validasi error
        if (bindingResult.hasErrors()) {
            List<Category> categories = categoryService.getAllActiveCategories();
            model.addAttribute("user", lecturer);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", courseDTO.getCourseId() != null);
            model.addAttribute("pageTitle", courseDTO.getCourseId() != null ? "Edit Kursus" : "Buat Kursus Baru");
            return "lecturer/course-form";
        }

        try {
            // SET STATUS BERDASARKAN ACTION BUTTON YANG DIKLIK
            // Ini adalah perbaikan utama - status ditentukan oleh tombol yang diklik
            String status = "publish".equals(action) ? "published" : "draft";
            courseDTO.setStatus(status);
            
            // Debug log
            System.out.println("=== SAVE COURSE ===");
            System.out.println("Action clicked: " + action);
            System.out.println("Status to save: " + status);
            System.out.println("Course title: " + courseDTO.getTitle());

            // Parse sections dari JSON jika ada
            if (sectionsJson != null && !sectionsJson.isEmpty()) {
                List<SectionDTO> sections = courseService.parseSectionsFromJson(sectionsJson);
                courseDTO.setSections(sections);
            }

            // Simpan kursus
            Course savedCourse;
            if (courseDTO.getCourseId() != null) {
                // Update existing course - validasi kepemilikan
                Course existingCourse = courseService.getCourseById(courseDTO.getCourseId());
                if (existingCourse == null || !existingCourse.getLecturer().getUserId().equals(lecturer.getUserId())) {
                    redirectAttributes.addFlashAttribute("error", "Anda tidak memiliki akses ke kursus ini");
                    return "redirect:/lecturer/courses";
                }
                savedCourse = courseService.updateCourse(courseDTO, lecturer.getUserId());
                
                String successMsg = "publish".equals(action) ? "Kursus berhasil dipublish" : "Draft berhasil disimpan";
                redirectAttributes.addFlashAttribute("success", successMsg);
            } else {
                // Create new course
                savedCourse = courseService.createCourse(courseDTO, lecturer.getUserId());
                
                String successMsg = "publish".equals(action) ? "Kursus berhasil dibuat dan dipublish" : "Draft kursus berhasil disimpan";
                redirectAttributes.addFlashAttribute("success", successMsg);
            }

            // Cleanup temp folder lecturer setelah berhasil simpan
            courseService.cleanupLecturerTempFolder(lecturer.getUserId());

            // Redirect ke list courses
            return "redirect:/lecturer/courses";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal menyimpan kursus: " + e.getMessage());
            return "redirect:/lecturer/courses/create";
        }
    }

    // ==================== DELETE COURSE ====================

    /**
     * Hapus kursus
     */
    @PostMapping("/{courseId}/delete")
    public String deleteCourse(
            @PathVariable Integer courseId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        // Cek authentication
        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            redirectAttributes.addFlashAttribute("error", "Silakan login sebagai lecturer terlebih dahulu");
            return "redirect:/login";
        }

        try {
            // Validasi kepemilikan
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "Anda tidak memiliki akses ke kursus ini");
                return "redirect:/lecturer/courses";
            }

            courseService.deleteCourse(courseId);
            redirectAttributes.addFlashAttribute("success", "Kursus berhasil dihapus");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus kursus: " + e.getMessage());
        }

        return "redirect:/lecturer/courses";
    }

    // ==================== AJAX ENDPOINTS ====================

    /**
     * Menyimpan kursus sebagai draft via AJAX
     */
    @PostMapping("/save-draft")
    @ResponseBody
    public Map<String, Object> saveDraft(
            @RequestBody CourseDTO courseDTO,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        // Cek authentication
        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            courseDTO.setStatus("draft");

            Course savedCourse;
            if (courseDTO.getCourseId() != null) {
                // Validasi kepemilikan
                if (!courseService.isOwnedByLecturer(courseDTO.getCourseId(), lecturer.getUserId())) {
                    response.put("success", false);
                    response.put("message", "Anda tidak memiliki akses ke kursus ini");
                    return response;
                }
                savedCourse = courseService.updateCourse(courseDTO, lecturer.getUserId());
            } else {
                savedCourse = courseService.createCourse(courseDTO, lecturer.getUserId());
            }

            response.put("success", true);
            response.put("message", "Draft berhasil disimpan");
            response.put("data", savedCourse.getCourseId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal menyimpan draft: " + e.getMessage());
        }

        return response;
    }

    /**
     * Menambah section baru via AJAX
     */
    @PostMapping("/{courseId}/sections")
    @ResponseBody
    public Map<String, Object> addSection(
            @PathVariable Integer courseId,
            @RequestBody SectionDTO sectionDTO,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            SectionDTO savedSection = courseService.addSection(courseId, sectionDTO);
            response.put("success", true);
            response.put("message", "Section berhasil ditambahkan");
            response.put("data", savedSection);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal menambah section: " + e.getMessage());
        }

        return response;
    }

    /**
     * Update section via AJAX
     */
    @PutMapping("/{courseId}/sections/{sectionId}")
    @ResponseBody
    public Map<String, Object> updateSection(
            @PathVariable Integer courseId,
            @PathVariable Integer sectionId,
            @RequestBody SectionDTO sectionDTO,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            SectionDTO updatedSection = courseService.updateSection(sectionId, sectionDTO);
            response.put("success", true);
            response.put("message", "Section berhasil diperbarui");
            response.put("data", updatedSection);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal memperbarui section: " + e.getMessage());
        }

        return response;
    }

    /**
     * Hapus section via AJAX
     */
    @DeleteMapping("/{courseId}/sections/{sectionId}")
    @ResponseBody
    public Map<String, Object> deleteSection(
            @PathVariable Integer courseId,
            @PathVariable Integer sectionId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            courseService.deleteSection(sectionId);
            response.put("success", true);
            response.put("message", "Section berhasil dihapus");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal menghapus section: " + e.getMessage());
        }

        return response;
    }

    /**
     * Menambah lesson ke section via AJAX
     */
    @PostMapping("/{courseId}/sections/{sectionId}/lessons")
    @ResponseBody
    public Map<String, Object> addLesson(
            @PathVariable Integer courseId,
            @PathVariable Integer sectionId,
            @RequestBody LessonDTO lessonDTO,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            LessonDTO savedLesson = courseService.addLesson(sectionId, lessonDTO);
            response.put("success", true);
            response.put("message", "Materi berhasil ditambahkan");
            response.put("data", savedLesson);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal menambah materi: " + e.getMessage());
        }

        return response;
    }

    /**
     * Update lesson via AJAX
     */
    @PutMapping("/{courseId}/lessons/{lessonId}")
    @ResponseBody
    public Map<String, Object> updateLesson(
            @PathVariable Integer courseId,
            @PathVariable Integer lessonId,
            @RequestBody LessonDTO lessonDTO,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            LessonDTO updatedLesson = courseService.updateLesson(lessonId, lessonDTO);
            response.put("success", true);
            response.put("message", "Materi berhasil diperbarui");
            response.put("data", updatedLesson);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal memperbarui materi: " + e.getMessage());
        }

        return response;
    }

    /**
     * Hapus lesson via AJAX
     */
    @DeleteMapping("/{courseId}/lessons/{lessonId}")
    @ResponseBody
    public Map<String, Object> deleteLesson(
            @PathVariable Integer courseId,
            @PathVariable Integer lessonId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            courseService.deleteLesson(lessonId);
            response.put("success", true);
            response.put("message", "Materi berhasil dihapus");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal menghapus materi: " + e.getMessage());
        }

        return response;
    }

    /**
     * Upload file materi (video/pdf) via AJAX
     */
    @PostMapping("/{courseId}/lessons/{lessonId}/upload")
    @ResponseBody
    public Map<String, Object> uploadLessonContent(
            @PathVariable Integer courseId,
            @PathVariable Integer lessonId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("contentType") String contentType,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            String fileUrl = fileStorageService.storeContent(file, contentType);
            courseService.updateLessonContent(lessonId, fileUrl, contentType);

            response.put("success", true);
            response.put("message", "File berhasil diupload");
            response.put("data", fileUrl);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal mengupload file: " + e.getMessage());
        }

        return response;
    }

    /**
     * Update urutan sections via AJAX (drag & drop)
     */
    @PostMapping("/{courseId}/sections/reorder")
    @ResponseBody
    public Map<String, Object> reorderSections(
            @PathVariable Integer courseId,
            @RequestBody List<Integer> sectionIds,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            courseService.reorderSections(courseId, sectionIds);
            response.put("success", true);
            response.put("message", "Urutan section berhasil diperbarui");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal mengubah urutan: " + e.getMessage());
        }

        return response;
    }

    /**
     * Update urutan lessons dalam section via AJAX (drag & drop)
     */
    @PostMapping("/{courseId}/sections/{sectionId}/lessons/reorder")
    @ResponseBody
    public Map<String, Object> reorderLessons(
            @PathVariable Integer courseId,
            @PathVariable Integer sectionId,
            @RequestBody List<Integer> lessonIds,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Lecturer lecturer = getLecturerFromSession(session);
        if (lecturer == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            if (!courseService.isOwnedByLecturer(courseId, lecturer.getUserId())) {
                response.put("success", false);
                response.put("message", "Anda tidak memiliki akses ke kursus ini");
                return response;
            }

            courseService.reorderLessons(sectionId, lessonIds);
            response.put("success", true);
            response.put("message", "Urutan materi berhasil diperbarui");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal mengubah urutan: " + e.getMessage());
        }

        return response;
    }
}