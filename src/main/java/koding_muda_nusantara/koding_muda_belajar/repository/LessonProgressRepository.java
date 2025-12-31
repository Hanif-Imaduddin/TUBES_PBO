package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Integer> {

    // Cari progress berdasarkan student dan lesson
    Optional<LessonProgress> findByStudentUserIdAndLessonLessonId(Integer studentId, Integer lessonId);

    // Cek apakah progress sudah ada
    boolean existsByStudentUserIdAndLessonLessonId(Integer studentId, Integer lessonId);

    // Daftar progress student untuk semua lesson di course
    @Query("SELECT lp FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "JOIN l.section s " +
           "WHERE lp.student.userId = :studentId AND s.course.courseId = :courseId")
    List<LessonProgress> findByStudentAndCourse(
            @Param("studentId") Integer studentId, 
            @Param("courseId") Integer courseId
    );

    // Hitung jumlah lesson yang sudah selesai di course
    @Query("SELECT COUNT(lp) FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "JOIN l.section s " +
           "WHERE lp.student.userId = :studentId " +
           "AND s.course.courseId = :courseId " +
           "AND lp.isCompleted = true")
    int countCompletedLessons(
            @Param("studentId") Integer studentId, 
            @Param("courseId") Integer courseId
    );

    // Cek apakah lesson sudah selesai
    @Query("SELECT CASE WHEN COUNT(lp) > 0 THEN true ELSE false END " +
           "FROM LessonProgress lp " +
           "WHERE lp.student.userId = :studentId " +
           "AND lp.lesson.lessonId = :lessonId " +
           "AND lp.isCompleted = true")
    boolean isLessonCompleted(
            @Param("studentId") Integer studentId, 
            @Param("lessonId") Integer lessonId
    );

    // Daftar lesson yang sudah selesai di course
    @Query("SELECT lp.lesson.lessonId FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "JOIN l.section s " +
           "WHERE lp.student.userId = :studentId " +
           "AND s.course.courseId = :courseId " +
           "AND lp.isCompleted = true")
    List<Integer> findCompletedLessonIds(
            @Param("studentId") Integer studentId, 
            @Param("courseId") Integer courseId
    );

    // Total watch time student di course
    @Query("SELECT COALESCE(SUM(lp.watchTime), 0) FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "JOIN l.section s " +
           "WHERE lp.student.userId = :studentId " +
           "AND s.course.courseId = :courseId")
    int getTotalWatchTime(
            @Param("studentId") Integer studentId, 
            @Param("courseId") Integer courseId
    );

    // Hapus semua progress student di course
    @Query("DELETE FROM LessonProgress lp " +
           "WHERE lp.student.userId = :studentId " +
           "AND lp.lesson.lessonId IN " +
           "(SELECT l.lessonId FROM Lesson l JOIN l.section s WHERE s.course.courseId = :courseId)")
    void deleteByStudentAndCourse(
            @Param("studentId") Integer studentId, 
            @Param("courseId") Integer courseId
    );
    
    /**
     * Mendapatkan progress lesson untuk student dan lesson tertentu
     */
    Optional<LessonProgress> findByStudent_UserIdAndLesson_LessonId(Integer studentId, Integer lessonId);

    /**
     * Cek apakah lesson sudah completed
     */
    boolean existsByStudent_UserIdAndLesson_LessonIdAndIsCompletedTrue(Integer studentId, Integer lessonId);

    /**
     * Mendapatkan semua progress untuk student
     */
    List<LessonProgress> findByStudent_UserId(Integer studentId);

    /**
     * Menghitung jumlah lesson yang sudah diselesaikan oleh student
     */
    @Query("SELECT COUNT(lp) FROM LessonProgress lp " +
           "WHERE lp.student.userId = :studentId " +
           "AND lp.isCompleted = true")
    Long countCompletedLessonsByStudent(@Param("studentId") Integer studentId);

    /**
     * Menghitung jumlah lesson yang sudah diselesaikan untuk course tertentu
     */
    @Query("SELECT COUNT(lp) FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "JOIN l.section s " +
           "WHERE lp.student.userId = :studentId " +
           "AND s.course.courseId = :courseId " +
           "AND lp.isCompleted = true")
    Long countCompletedLessonsByCourse(
            @Param("studentId") Integer studentId, 
            @Param("courseId") Integer courseId);

    /**
     * Mendapatkan total watch time student (dalam detik)
     */
    @Query("SELECT COALESCE(SUM(lp.watchTime), 0) FROM LessonProgress lp " +
           "WHERE lp.student.userId = :studentId")
    Long getTotalWatchTimeByStudent(@Param("studentId") Integer studentId);

    /**
     * Mendapatkan total watch time untuk course tertentu (dalam detik)
     */
    @Query("SELECT COALESCE(SUM(lp.watchTime), 0) FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "JOIN l.section s " +
           "WHERE lp.student.userId = :studentId " +
           "AND s.course.courseId = :courseId")
    Long getTotalWatchTimeByCourse(
            @Param("studentId") Integer studentId, 
            @Param("courseId") Integer courseId);

    /**
     * Mendapatkan lesson terakhir yang diakses untuk course tertentu
     */
    @Query("SELECT lp FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "JOIN l.section s " +
           "WHERE lp.student.userId = :studentId " +
           "AND s.course.courseId = :courseId " +
           "ORDER BY lp.updatedAt DESC")
    List<LessonProgress> findLastAccessedLessonByCourse(
            @Param("studentId") Integer studentId, 
            @Param("courseId") Integer courseId);
}
