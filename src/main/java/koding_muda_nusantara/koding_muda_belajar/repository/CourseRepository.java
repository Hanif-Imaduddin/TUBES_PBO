package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    // Find by slug
    Optional<Course> findBySlug(String slug);

    // Check if slug exists
    boolean existsBySlug(String slug);

    // Check if slug exists (excluding specific course)
    boolean existsBySlugAndCourseIdNot(String slug, Integer courseId);

    // Find by lecturer
    List<Course> findByLecturerUserId(Integer lecturerId);

    // Count by lecturer
    long countByLecturerUserId(Integer lecturerId);

    // Count by lecturer and status
    long countByLecturerUserIdAndStatus(Integer lecturerId, String status);

    // Check ownership
    boolean existsByCourseIdAndLecturerUserId(Integer courseId, Integer lecturerId);

    // Find published courses
    List<Course> findByStatus(String status);

    // Find by category
    List<Course> findByCategoryCategoryId(Integer categoryId);

    // Search courses
    @Query("SELECT c FROM Course c WHERE c.status = 'published' AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchCourses(@Param("keyword") String keyword);

    // Find featured courses
    List<Course> findByIsFeaturedTrueAndStatus(String status);

    // Find courses by lecturer with filters
    @Query("SELECT c FROM Course c WHERE c.lecturer.userId = :lecturerId " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:categoryId IS NULL OR c.category.categoryId = :categoryId) " +
           "ORDER BY c.createdAt DESC")
    List<Course> findByLecturerWithFilters(
            @Param("lecturerId") Integer lecturerId,
            @Param("status") String status,
            @Param("categoryId") Integer categoryId
    );
}