package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.enums.EnrollmentStatus;

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
}
