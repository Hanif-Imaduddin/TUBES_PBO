package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseLevel;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    
        // Query dengan pagination - semua kursus lecturer
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.lecturer.userId = :lecturerId " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug")
    Page<CourseWithStatsDTO> findCoursesWithStatsByLecturerId(
            @Param("lecturerId") Integer lecturerId, 
            Pageable pageable);
    
    // Query dengan filter status
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.lecturer.userId = :lecturerId AND c.status = :status " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug")
    Page<CourseWithStatsDTO> findCoursesWithStatsByLecturerIdAndStatus(
            @Param("lecturerId") Integer lecturerId,
            @Param("status") CourseStatus status,
            Pageable pageable);
    
    // Query dengan filter kategori
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.lecturer.userId = :lecturerId AND cat.slug = :categorySlug " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug")
    Page<CourseWithStatsDTO> findCoursesWithStatsByLecturerIdAndCategory(
            @Param("lecturerId") Integer lecturerId,
            @Param("categorySlug") String categorySlug,
            Pageable pageable);
    
    // Query dengan filter status dan kategori
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.lecturer.userId = :lecturerId AND c.status = :status AND cat.slug = :categorySlug " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug")
    Page<CourseWithStatsDTO> findCoursesWithStatsByLecturerIdAndStatusAndCategory(
            @Param("lecturerId") Integer lecturerId,
            @Param("status") CourseStatus status,
            @Param("categorySlug") String categorySlug,
            Pageable pageable);
    
    // Query dengan pencarian judul
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.lecturer.userId = :lecturerId AND LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug")
    Page<CourseWithStatsDTO> findCoursesWithStatsByLecturerIdAndSearch(
            @Param("lecturerId") Integer lecturerId,
            @Param("search") String search,
            Pageable pageable);
    
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
       "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
       "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
       "cat.name, cat.slug, " +
       "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
       "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
       "FROM Course c " +
       "LEFT JOIN c.category cat " +
       "LEFT JOIN c.lecturer l " +
       "LEFT JOIN User u ON l.userId = u.userId " +
       "LEFT JOIN Enrollment e ON e.course = c " +
       "LEFT JOIN Review r ON r.course = c " +
       "WHERE c.status = 'published' " +
       "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
       "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
       "cat.name, cat.slug, u.firstName, u.lastName " +
       "ORDER BY COUNT(DISTINCT e.student) DESC")
    List<CourseWithStatsDTO> findPopularCoursesWithStatsAndLecturer(Pageable pageable);
    
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
       "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
       "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
       "cat.name, cat.slug, " +
       "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
       "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
       "FROM Course c " +
       "LEFT JOIN c.category cat " +
       "LEFT JOIN c.lecturer l " +
       "LEFT JOIN User u ON l.userId = u.userId " +
       "LEFT JOIN Enrollment e ON e.course = c " +
       "LEFT JOIN Review r ON r.course = c " +
       "WHERE c.status = 'published' " +
       "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
       "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
       "cat.name, cat.slug, u.firstName, u.lastName " +
       "ORDER BY c.createdAt DESC")
    List<CourseWithStatsDTO> findRecentCoursesWithStatsAndLecturer(Pageable pageable);
    
    Long countByLecturerUserIdAndStatus(Integer lecturerId, CourseStatus status);
    
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN c.lecturer l " +
           "LEFT JOIN User u ON l.userId = u.userId " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.status = 'published' " +
           "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryIds IS NULL OR cat.categoryId IN :categoryIds) " +
           "AND (:levels IS NULL OR c.level IN :levels) " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, u.firstName, u.lastName " +
           "HAVING (:minRating IS NULL OR COALESCE(AVG(CAST(r.rating AS double)), 0.0) >= :minRating)")
    Page<CourseWithStatsDTO> searchCoursesWithStats(
            @Param("keyword") String keyword,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("levels") List<CourseLevel> levels,
            @Param("minRating") Double minRating,
            Pageable pageable
    );
    
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN c.lecturer l " +
           "LEFT JOIN User u ON l.userId = u.userId " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.status = 'published' " +
           "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryIds IS NULL OR cat.categoryId IN :categoryIds) " +
           "AND (:levels IS NULL OR c.level IN :levels) " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, u.firstName, u.lastName " +
           "HAVING (:minRating IS NULL OR COALESCE(AVG(CAST(r.rating AS double)), 0.0) >= :minRating)"+
           "ORDER BY COUNT(DISTINCT e.student) DESC")
    Page<CourseWithStatsDTO> searchPopularCoursesWithStats(
            @Param("keyword") String keyword,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("levels") List<CourseLevel> levels,
            @Param("minRating") Double minRating,
            Pageable pageable
    );
    
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN c.lecturer l " +
           "LEFT JOIN User u ON l.userId = u.userId " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.status = 'published' " +
           "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryIds IS NULL OR cat.categoryId IN :categoryIds) " +
           "AND (:levels IS NULL OR c.level IN :levels) " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, u.firstName, u.lastName " +
           "HAVING (:minRating IS NULL OR COALESCE(AVG(CAST(r.rating AS double)), 0.0) >= :minRating)"+
           "ORDER BY COALESCE(AVG(CAST(r.rating AS double)), 0.0) DESC")
    Page<CourseWithStatsDTO> searchAvgRatingCoursesWithStats(
            @Param("keyword") String keyword,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("levels") List<CourseLevel> levels,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

    /**
     * Mendapatkan kursus populer (berdasarkan jumlah student)
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN c.lecturer l " +
           "LEFT JOIN User u ON l.userId = u.userId " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.status = 'published' " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, u.firstName, u.lastName " +
           "ORDER BY COUNT(DISTINCT e.student) DESC")
    List<CourseWithStatsDTO> findPopularCoursesWithStats(Pageable pageable);

    /**
     * Mendapatkan kursus terbaru
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN c.lecturer l " +
           "LEFT JOIN User u ON l.userId = u.userId " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.status = 'published' " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, u.firstName, u.lastName " +
           "ORDER BY c.createdAt DESC")
    List<CourseWithStatsDTO> findNewestCoursesWithStats(Pageable pageable);

    /**
     * Mendapatkan detail kursus berdasarkan slug
     * @param slug
     * @return 
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN c.lecturer l " +
           "LEFT JOIN User u ON l.userId = u.userId " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.slug = :slug " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, u.firstName, u.lastName")
    Optional<CourseWithStatsDTO> findCourseWithStatsBySlug(@Param("slug") String slug);

    /**
     * Mendapatkan detail kursus berdasarkan ID
     * @param courseId
     * @return 
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO(" +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, " +
           "CONCAT(u.firstName, ' ', COALESCE(u.lastName, '')), " +
           "COUNT(DISTINCT e.student), COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(DISTINCT r.id)) " +
           "FROM Course c " +
           "LEFT JOIN c.category cat " +
           "LEFT JOIN c.lecturer l " +
           "LEFT JOIN User u ON l.userId = u.userId " +
           "LEFT JOIN Enrollment e ON e.course = c " +
           "LEFT JOIN Review r ON r.course = c " +
           "WHERE c.courseId = :courseId " +
           "GROUP BY c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.price, c.discountPrice, c.level, c.status, c.totalLessons, c.totalDuration, " +
           "cat.name, cat.slug, u.firstName, u.lastName")
    Optional<CourseWithStatsDTO> findCourseWithStatsById(@Param("courseId") Integer courseId);
}