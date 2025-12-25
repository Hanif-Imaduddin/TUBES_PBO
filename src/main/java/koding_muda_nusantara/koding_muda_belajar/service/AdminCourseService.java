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
import koding_muda_nusantara.koding_muda_belajar.repository.CategoryRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.CourseRepository;

@Service
@Transactional
public class AdminCourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
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
    public boolean deleteCourse(Integer courseId) {
        if (courseRepository.existsById(courseId)) {
            courseRepository.deleteById(courseId);
            return true;
        }
        return false;
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
