package koding_muda_nusantara.koding_muda_belajar.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import koding_muda_nusantara.koding_muda_belajar.dto.AdminCourseDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Lesson;
import koding_muda_nusantara.koding_muda_belajar.model.Section;
import koding_muda_nusantara.koding_muda_belajar.repository.CartItemRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.CategoryRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.CourseRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.EnrollmentRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.LessonRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.ReviewRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.SectionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class AdminCourseService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminCourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private CategoryRepository categoryRepository;

    // Repository tambahan untuk membersihkan data terkait
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CartItemRepository cartItemRepository;
    
    /**
     * Mendapatkan semua kursus dengan enrollment count untuk halaman admin
     */
    public List<AdminCourseDTO> getAllCoursesWithStats() {
        List<Object[]> results = courseRepository.findAllWithEnrollmentCount();
        return mapToAdminCourseDTO(results);
    }
    
    /**
     * Mendapatkan kursus dengan pagination
     */
    public Page<AdminCourseDTO> getAllCoursesWithStatsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Object[]> results = courseRepository.findAllWithEnrollmentCountPaged(pageable);
        
        List<AdminCourseDTO> dtoList = mapToAdminCourseDTO(results.getContent());
        return new PageImpl<>(dtoList, pageable, results.getTotalElements());
    }
    
    /**
     * Mendapatkan kursus dengan statistik lengkap (enrollment, rating, reviews)
     */
    public Page<AdminCourseDTO> getAllCoursesWithFullStatsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Object[]> results = courseRepository.findAllWithFullStatsPaged(pageable);
        
        List<AdminCourseDTO> dtoList = mapToAdminCourseDTOWithRating(results.getContent());
        return new PageImpl<>(dtoList, pageable, results.getTotalElements());
    }
    
    /**
     * Filter kursus berdasarkan status
     */
    public List<AdminCourseDTO> getCoursesByStatus(CourseStatus status) {
        List<Object[]> results = courseRepository.findByStatusWithEnrollmentCount(status);
        return mapToAdminCourseDTO(results);
    }
    
    /**
     * Filter kursus berdasarkan kategori
     */
    public List<AdminCourseDTO> getCoursesByCategory(Integer categoryId) {
        List<Object[]> results = courseRepository.findByCategoryWithEnrollmentCount(categoryId);
        return mapToAdminCourseDTO(results);
    }
    
    /**
     * Cari kursus berdasarkan keyword
     */
    public List<AdminCourseDTO> searchCourses(String keyword) {
        List<Object[]> results = courseRepository.searchWithEnrollmentCount(keyword);
        return mapToAdminCourseDTO(results);
    }
    
    /**
     * Mendapatkan detail kursus berdasarkan ID
     */
    @Transactional(readOnly = true)
    public Optional<Course> getCourseById(Integer courseId) {
        return courseRepository.findById(courseId);
    }
    
    /**
     * Update status kursus
     */
    public boolean updateCourseStatus(Integer courseId, CourseStatus status) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setStatus(status);
            
            // Set publishedAt jika status berubah ke published
            if (status == CourseStatus.published && course.getPublishedAt() == null) {
                course.setPublishedAt(LocalDateTime.now());
            }
            
            courseRepository.save(course);
            return true;
        }
        return false;
    }
    
    /**
     * Update featured status
     */
    public boolean updateFeaturedStatus(Integer courseId, boolean featured) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setFeatured(featured);
            courseRepository.save(course);
            return true;
        }
        return false;
    }
    
    /**
     * Hapus kursus
     */
    /**
     * Hapus course beserta semua file terkait (thumbnail, video, dokumen)
     * 
     * @param courseId ID course yang akan dihapus
     * @return true jika berhasil, false jika gagal
     */
    @Transactional
    public boolean deleteCourse(Integer courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        
        if (courseOpt.isEmpty()) {
            logger.warn("Course dengan ID {} tidak ditemukan", courseId);
            return false;
        }

        Course course = courseOpt.get();
        
        try {
            // 1. Hapus semua file terkait course
            deleteAllCourseFiles(course);
            
            // 2. Hapus data terkait di tabel lain (jika tidak cascade)
            deleteRelatedData(courseId);
            
            // 3. Hapus course dari database
            // Karena cascade sudah diset, sections dan lessons akan terhapus otomatis
            courseRepository.delete(course);
            
            logger.info("Course '{}' (ID: {}) berhasil dihapus beserta semua file", 
                       course.getTitle(), courseId);
            return true;
            
        } catch (Exception e) {
            logger.error("Gagal menghapus course ID {}: {}", courseId, e.getMessage(), e);
            throw new RuntimeException("Gagal menghapus course: " + e.getMessage(), e);
        }
    }

    /**
     * Hapus semua file yang terkait dengan course
     */
    private void deleteAllCourseFiles(Course course) {
        // 1. Hapus thumbnail course
        if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(course.getThumbnailUrl());
                logger.debug("Thumbnail course dihapus: {}", course.getThumbnailUrl());
            } catch (Exception e) {
                logger.warn("Gagal menghapus thumbnail course: {}", e.getMessage());
                // Lanjutkan meskipun gagal hapus thumbnail
            }
        }

        // 2. Hapus preview video course (jika ada)
        if (course.getPreviewVideoUrl() != null && !course.getPreviewVideoUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(course.getPreviewVideoUrl());
                logger.debug("Preview video course dihapus: {}", course.getPreviewVideoUrl());
            } catch (Exception e) {
                logger.warn("Gagal menghapus preview video: {}", e.getMessage());
            }
        }

        // 3. Hapus semua file lesson (video, pdf, dll)
        List<Section> sections = sectionRepository.findByCourse_CourseIdOrderBySortOrderAsc(course.getCourseId());
        
        for (Section section : sections) {
            List<Lesson> lessons = lessonRepository.findBySection_SectionIdOrderBySortOrderAsc(section.getSectionId());
            
            for (Lesson lesson : lessons) {
                deleteLessonFiles(lesson);
            }
        }

        // 4. Hapus folder course jika menggunakan struktur folder per course
        try {
            fileStorageService.deleteCourseFolder(course.getCourseId());
            logger.debug("Folder course {} dihapus", course.getCourseId());
        } catch (Exception e) {
            logger.warn("Gagal menghapus folder course: {}", e.getMessage());
        }
    }

    /**
     * Hapus file yang terkait dengan lesson
     */
    private void deleteLessonFiles(Lesson lesson) {
        // Hapus content URL (video/pdf)
        if (lesson.getContentUrl() != null && !lesson.getContentUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(lesson.getContentUrl());
                logger.debug("File lesson '{}' dihapus: {}", lesson.getTitle(), lesson.getContentUrl());
            } catch (Exception e) {
                logger.warn("Gagal menghapus file lesson {}: {}", lesson.getLessonId(), e.getMessage());
            }
        }
    }

    /**
     * Hapus data terkait di tabel lain yang mungkin tidak cascade
     */
    private void deleteRelatedData(Integer courseId) {
        try {
            // Hapus cart items yang berisi course ini
            cartItemRepository.deleteByCourse_CourseId(courseId);
            logger.debug("Cart items untuk course {} dihapus", courseId);
        } catch (Exception e) {
            logger.warn("Gagal menghapus cart items: {}", e.getMessage());
        }

        // Enrollment dan Review biasanya sudah cascade, tapi bisa ditambahkan jika perlu
        // enrollmentRepository.deleteByCourse_CourseId(courseId);
        // reviewRepository.deleteByCourse_CourseId(courseId);
    }

    /**
     * Hapus course dengan validasi tambahan
     * Gunakan method ini untuk penghapusan yang lebih aman
     */
    @Transactional
    public CourseDeleteResult deleteCourseWithValidation(Integer courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        
        if (courseOpt.isEmpty()) {
            return new CourseDeleteResult(false, "Course tidak ditemukan", null);
        }

        Course course = courseOpt.get();

        // Hitung statistik sebelum dihapus (untuk logging/audit)
        long enrollmentCount = enrollmentRepository.countByCourse_CourseId(courseId);
        long reviewCount = reviewRepository.countByCourse_CourseId(courseId);
        int sectionCount = course.getSections() != null ? course.getSections().size() : 0;

        // Simpan info untuk response
        String courseTitle = course.getTitle();
        String lecturerName = course.getLecturer().getFirstName() + " " + course.getLecturer().getLastName();

        // Lakukan penghapusan
        boolean deleted = deleteCourse(courseId);

        if (deleted) {
            String message = String.format(
                "Course '%s' berhasil dihapus. Statistik: %d enrollments, %d reviews, %d sections",
                courseTitle, enrollmentCount, reviewCount, sectionCount
            );
            
            CourseDeleteInfo info = new CourseDeleteInfo(
                courseId, courseTitle, lecturerName, 
                enrollmentCount, reviewCount, sectionCount
            );
            
            return new CourseDeleteResult(true, message, info);
        } else {
            return new CourseDeleteResult(false, "Gagal menghapus course", null);
        }
    }

    /**
     * Bulk delete courses
     */
    @Transactional
    public BulkDeleteResult deleteMultipleCourses(List<Integer> courseIds) {
        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();

        for (Integer courseId : courseIds) {
            try {
                if (deleteCourse(courseId)) {
                    successCount++;
                } else {
                    failCount++;
                    errors.append("Course ID ").append(courseId).append(" tidak ditemukan. ");
                }
            } catch (Exception e) {
                failCount++;
                errors.append("Course ID ").append(courseId).append(": ").append(e.getMessage()).append(". ");
            }
        }

        return new BulkDeleteResult(successCount, failCount, errors.toString());
    }

    // ========== Inner Classes untuk Result ==========

    public static class CourseDeleteResult {
        private final boolean success;
        private final String message;
        private final CourseDeleteInfo info;

        public CourseDeleteResult(boolean success, String message, CourseDeleteInfo info) {
            this.success = success;
            this.message = message;
            this.info = info;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public CourseDeleteInfo getInfo() { return info; }
    }

    public static class CourseDeleteInfo {
        private final Integer courseId;
        private final String courseTitle;
        private final String lecturerName;
        private final long enrollmentCount;
        private final long reviewCount;
        private final int sectionCount;

        public CourseDeleteInfo(Integer courseId, String courseTitle, String lecturerName,
                               long enrollmentCount, long reviewCount, int sectionCount) {
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.lecturerName = lecturerName;
            this.enrollmentCount = enrollmentCount;
            this.reviewCount = reviewCount;
            this.sectionCount = sectionCount;
        }

        // Getters
        public Integer getCourseId() { return courseId; }
        public String getCourseTitle() { return courseTitle; }
        public String getLecturerName() { return lecturerName; }
        public long getEnrollmentCount() { return enrollmentCount; }
        public long getReviewCount() { return reviewCount; }
        public int getSectionCount() { return sectionCount; }
    }

    public static class BulkDeleteResult {
        private final int successCount;
        private final int failCount;
        private final String errors;

        public BulkDeleteResult(int successCount, int failCount, String errors) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.errors = errors;
        }

        public int getSuccessCount() { return successCount; }
        public int getFailCount() { return failCount; }
        public String getErrors() { return errors; }
        public boolean hasErrors() { return failCount > 0; }
    }
    
    /**
     * Mendapatkan semua kategori aktif
     */
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc();
    }
    
    /**
     * Mendapatkan semua kategori
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
    
    /**
     * Menghitung total kursus
     */
    @Transactional(readOnly = true)
    public long countAllCourses() {
        return courseRepository.count();
    }
    
    /**
     * Menghitung kursus berdasarkan status
     */
    @Transactional(readOnly = true)
    public long countByStatus(CourseStatus status) {
        return courseRepository.countByStatus(status);
    }
    
    // ======================= HELPER METHODS =======================
    
    /**
     * Mapping dari Object[] ke AdminCourseDTO
     */
    private List<AdminCourseDTO> mapToAdminCourseDTO(List<Object[]> results) {
        List<AdminCourseDTO> dtoList = new ArrayList<>();
        
        for (Object[] row : results) {
            Course course = (Course) row[0];
            Long enrollmentCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            
            AdminCourseDTO dto = new AdminCourseDTO();
            dto.setCourseId(course.getCourseId());
            dto.setTitle(course.getTitle());
            dto.setSlug(course.getSlug());
            dto.setShortDescription(course.getShortDescription());
            dto.setThumbnailUrl(course.getThumbnailUrl());
            dto.setPrice(course.getPrice());
            dto.setDiscountPrice(course.getDiscountPrice());
            dto.setLevel(course.getLevel());
            dto.setStatus(course.getStatus());
            dto.setFeatured(course.isFeatured());
            dto.setTotalLessons(course.getTotalLessons());
            dto.setTotalDuration(course.getTotalDuration());
            dto.setCreatedAt(course.getCreatedAt());
            dto.setUpdatedAt(course.getUpdatedAt());
            dto.setPublishedAt(course.getPublishedAt());
            dto.setCategory(course.getCategory());
            dto.setLecturer(course.getLecturer());
            dto.setEnrollmentCount(enrollmentCount);
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
    
    /**
     * Mapping dari Object[] ke AdminCourseDTO dengan rating
     */
    private List<AdminCourseDTO> mapToAdminCourseDTOWithRating(List<Object[]> results) {
        List<AdminCourseDTO> dtoList = new ArrayList<>();
        
        for (Object[] row : results) {
            Course course = (Course) row[0];
            Long enrollmentCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Double avgRating = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            Long reviewCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            
            AdminCourseDTO dto = new AdminCourseDTO();
            dto.setCourseId(course.getCourseId());
            dto.setTitle(course.getTitle());
            dto.setSlug(course.getSlug());
            dto.setShortDescription(course.getShortDescription());
            dto.setThumbnailUrl(course.getThumbnailUrl());
            dto.setPrice(course.getPrice());
            dto.setDiscountPrice(course.getDiscountPrice());
            dto.setLevel(course.getLevel());
            dto.setStatus(course.getStatus());
            dto.setFeatured(course.isFeatured());
            dto.setTotalLessons(course.getTotalLessons());
            dto.setTotalDuration(course.getTotalDuration());
            dto.setCreatedAt(course.getCreatedAt());
            dto.setUpdatedAt(course.getUpdatedAt());
            dto.setPublishedAt(course.getPublishedAt());
            dto.setCategory(course.getCategory());
            dto.setLecturer(course.getLecturer());
            dto.setEnrollmentCount(enrollmentCount);
            dto.setAverageRating(avgRating);
            dto.setReviewCount(reviewCount);
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
}
