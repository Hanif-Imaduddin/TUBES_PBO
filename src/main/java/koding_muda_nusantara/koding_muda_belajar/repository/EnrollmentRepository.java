package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    // Cek apakah student sudah enroll di course
    boolean existsByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);

    // Cari enrollment berdasarkan student dan course
    Optional<Enrollment> findByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);

    // Daftar enrollment student
    List<Enrollment> findByStudentUserIdOrderByEnrolledAtDesc(Integer studentId);

    // Daftar enrollment student berdasarkan status
    List<Enrollment> findByStudentUserIdAndStatusOrderByEnrolledAtDesc(Integer studentId, EnrollmentStatus status);

    // Daftar enrollment untuk course tertentu
    List<Enrollment> findByCourseCourseIdOrderByEnrolledAtDesc(Integer courseId);

    // Hitung jumlah student enrolled di course
    long countByCourseCourseId(Integer courseId);

    // Hitung jumlah course yang diikuti student
    long countByStudentUserId(Integer studentId);

    // Hitung jumlah course yang sudah selesai oleh student
    long countByStudentUserIdAndStatus(Integer studentId, EnrollmentStatus status);

    // Cari enrollment aktif student
    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId AND e.status = 'active' ORDER BY e.lastAccessedAt DESC")
    List<Enrollment> findActiveEnrollments(@Param("studentId") Integer studentId);

    // Cari enrollment yang terakhir diakses
    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId ORDER BY e.lastAccessedAt DESC NULLS LAST")
    List<Enrollment> findRecentlyAccessed(@Param("studentId") Integer studentId);

    // Cari enrollment yang sudah selesai
    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId AND e.status = 'completed' ORDER BY e.completedAt DESC")
    List<Enrollment> findCompletedEnrollments(@Param("studentId") Integer studentId);

    /**
     * Hitung jumlah enrollment untuk course tertentu
     */
    long countByCourse_CourseId(Integer courseId);
    
    /**
     * Hapus semua enrollment untuk course tertentu (jika tidak cascade)
     */
    @Modifying
    @Query("DELETE FROM Enrollment e WHERE e.course.courseId = :courseId")
    void deleteByCourse_CourseId(@Param("courseId") Integer courseId);
    
    /**
     * Cek apakah student sudah enroll di course tertentu
     */
    boolean existsByStudent_UserIdAndCourse_CourseId(Integer studentId, Integer courseId);

    /**
     * Mendapatkan enrollment berdasarkan student dan course
     */
    Optional<Enrollment> findByStudent_UserIdAndCourse_CourseId(Integer studentId, Integer courseId);

    /**
     * Mendapatkan semua enrollment student dengan status tertentu
     */
    List<Enrollment> findByStudent_UserIdAndStatus(Integer studentId, EnrollmentStatus status);

    /**
     * Mendapatkan semua enrollment student
     */
    List<Enrollment> findByStudent_UserId(Integer studentId);

    /**
     * Menghitung total enrollment student
     */
    long countByStudent_UserId(Integer studentId);

    /**
     * Menghitung enrollment student berdasarkan status
     */
    long countByStudent_UserIdAndStatus(Integer studentId, EnrollmentStatus status);

    /**
     * Query untuk mendapatkan daftar kursus yang di-enroll dengan detail lengkap
     * untuk halaman My Learning
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO(" +
           "e.enrollmentId, e.enrolledAt, e.completedAt, e.progressPercentage, " +
           "e.lastAccessedAt, e.status, " +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.level, c.totalDuration, c.totalLessons, " +
           "cat.name, " +
           "l.userId, u.firstName, u.lastName) " +
           "FROM Enrollment e " +
           "JOIN e.course c " +
           "JOIN c.category cat " +
           "JOIN c.lecturer l " +
           "JOIN User u ON l.userId = u.userId " +
           "WHERE e.student.userId = :studentId " +
           "ORDER BY e.lastAccessedAt DESC NULLS LAST, e.enrolledAt DESC")
    List<MyLearningCourseDTO> findMyLearningCourses(@Param("studentId") Integer studentId);

    /**
     * Query untuk mendapatkan kursus dengan filter status
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO(" +
           "e.enrollmentId, e.enrolledAt, e.completedAt, e.progressPercentage, " +
           "e.lastAccessedAt, e.status, " +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.level, c.totalDuration, c.totalLessons, " +
           "cat.name, " +
           "l.userId, u.firstName, u.lastName) " +
           "FROM Enrollment e " +
           "JOIN e.course c " +
           "JOIN c.category cat " +
           "JOIN c.lecturer l " +
           "JOIN User u ON l.userId = u.userId " +
           "WHERE e.student.userId = :studentId " +
           "AND e.status = :status " +
           "ORDER BY e.lastAccessedAt DESC NULLS LAST, e.enrolledAt DESC")
    List<MyLearningCourseDTO> findMyLearningCoursesByStatus(
            @Param("studentId") Integer studentId, 
            @Param("status") EnrollmentStatus status);

    /**
     * Query untuk mendapatkan kursus dengan filter kategori
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO(" +
           "e.enrollmentId, e.enrolledAt, e.completedAt, e.progressPercentage, " +
           "e.lastAccessedAt, e.status, " +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.level, c.totalDuration, c.totalLessons, " +
           "cat.name, " +
           "l.userId, u.firstName, u.lastName) " +
           "FROM Enrollment e " +
           "JOIN e.course c " +
           "JOIN c.category cat " +
           "JOIN c.lecturer l " +
           "JOIN User u ON l.userId = u.userId " +
           "WHERE e.student.userId = :studentId " +
           "AND cat.categoryId = :categoryId " +
           "ORDER BY e.lastAccessedAt DESC NULLS LAST, e.enrolledAt DESC")
    List<MyLearningCourseDTO> findMyLearningCoursesByCategory(
            @Param("studentId") Integer studentId, 
            @Param("categoryId") Integer categoryId);

    /**
     * Query untuk search kursus berdasarkan judul
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO(" +
           "e.enrollmentId, e.enrolledAt, e.completedAt, e.progressPercentage, " +
           "e.lastAccessedAt, e.status, " +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.level, c.totalDuration, c.totalLessons, " +
           "cat.name, " +
           "l.userId, u.firstName, u.lastName) " +
           "FROM Enrollment e " +
           "JOIN e.course c " +
           "JOIN c.category cat " +
           "JOIN c.lecturer l " +
           "JOIN User u ON l.userId = u.userId " +
           "WHERE e.student.userId = :studentId " +
           "AND LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY e.lastAccessedAt DESC NULLS LAST, e.enrolledAt DESC")
    List<MyLearningCourseDTO> searchMyLearningCourses(
            @Param("studentId") Integer studentId, 
            @Param("keyword") String keyword);

    /**
     * Query dengan pagination untuk My Learning
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO(" +
           "e.enrollmentId, e.enrolledAt, e.completedAt, e.progressPercentage, " +
           "e.lastAccessedAt, e.status, " +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.level, c.totalDuration, c.totalLessons, " +
           "cat.name, " +
           "l.userId, u.firstName, u.lastName) " +
           "FROM Enrollment e " +
           "JOIN e.course c " +
           "JOIN c.category cat " +
           "JOIN c.lecturer l " +
           "JOIN User u ON l.userId = u.userId " +
           "WHERE e.student.userId = :studentId")
    Page<MyLearningCourseDTO> findMyLearningCoursesPageable(
            @Param("studentId") Integer studentId, 
            Pageable pageable);

    /**
     * Mendapatkan kursus yang baru diakses (untuk continue learning)
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO(" +
           "e.enrollmentId, e.enrolledAt, e.completedAt, e.progressPercentage, " +
           "e.lastAccessedAt, e.status, " +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.level, c.totalDuration, c.totalLessons, " +
           "cat.name, " +
           "l.userId, u.firstName, u.lastName) " +
           "FROM Enrollment e " +
           "JOIN e.course c " +
           "JOIN c.category cat " +
           "JOIN c.lecturer l " +
           "JOIN User u ON l.userId = u.userId " +
           "WHERE e.student.userId = :studentId " +
           "AND e.status = 'active' " +
           "AND e.lastAccessedAt IS NOT NULL " +
           "ORDER BY e.lastAccessedAt DESC")
    List<MyLearningCourseDTO> findRecentlyAccessedCourses(
            @Param("studentId") Integer studentId, 
            Pageable pageable);

    /**
     * Mendapatkan kursus yang hampir selesai (progress > 75%)
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO(" +
           "e.enrollmentId, e.enrolledAt, e.completedAt, e.progressPercentage, " +
           "e.lastAccessedAt, e.status, " +
           "c.courseId, c.title, c.slug, c.shortDescription, c.thumbnailUrl, " +
           "c.level, c.totalDuration, c.totalLessons, " +
           "cat.name, " +
           "l.userId, u.firstName, u.lastName) " +
           "FROM Enrollment e " +
           "JOIN e.course c " +
           "JOIN c.category cat " +
           "JOIN c.lecturer l " +
           "JOIN User u ON l.userId = u.userId " +
           "WHERE e.student.userId = :studentId " +
           "AND e.status = 'active' " +
           "AND e.progressPercentage >= 75 " +
           "ORDER BY e.progressPercentage DESC")
    List<MyLearningCourseDTO> findAlmostCompletedCourses(@Param("studentId") Integer studentId);
}
