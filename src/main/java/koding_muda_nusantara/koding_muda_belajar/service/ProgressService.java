package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.model.Enrollment;
import koding_muda_nusantara.koding_muda_belajar.model.Lesson;
import koding_muda_nusantara.koding_muda_belajar.model.LessonProgress;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.repository.EnrollmentRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.LessonProgressRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.LessonRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressService {

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // ==================== GET PROGRESS ====================

    /**
     * Dapatkan progress lesson untuk student
     */
    public Optional<LessonProgress> getLessonProgress(Integer studentId, Integer lessonId) {
        return lessonProgressRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId);
    }

    /**
     * Dapatkan semua progress student di course
     */
    public List<LessonProgress> getCourseProgress(Integer studentId, Integer courseId) {
        return lessonProgressRepository.findByStudentAndCourse(studentId, courseId);
    }

    /**
     * Dapatkan daftar lesson ID yang sudah selesai
     */
    public List<Integer> getCompletedLessonIds(Integer studentId, Integer courseId) {
        return lessonProgressRepository.findCompletedLessonIds(studentId, courseId);
    }

    /**
     * Cek apakah lesson sudah selesai
     */
    public boolean isLessonCompleted(Integer studentId, Integer lessonId) {
        return lessonProgressRepository.isLessonCompleted(studentId, lessonId);
    }

    // ==================== UPDATE PROGRESS ====================

    /**
     * Update atau buat progress lesson
     */
    @Transactional
    public LessonProgress updateLessonProgress(Integer studentId, Integer lessonId, boolean completed) {
        // Dapatkan atau buat progress
        LessonProgress progress = getOrCreateProgress(studentId, lessonId);

        if (completed && !progress.isCompleted()) {
            progress.markAsCompleted();
        } else if (!completed && progress.isCompleted()) {
            progress.markAsIncomplete();
        }

        progress = lessonProgressRepository.save(progress);

        // Update course progress
        Lesson lesson = progress.getLesson();
        Integer courseId = lesson.getSection().getCourse().getCourseId();
        updateCourseProgress(studentId, courseId);

        return progress;
    }

    /**
     * Toggle status completed lesson
     */
    @Transactional
    public LessonProgress toggleLessonComplete(Integer studentId, Integer lessonId) {
        LessonProgress progress = getOrCreateProgress(studentId, lessonId);
        
        if (progress.isCompleted()) {
            progress.markAsIncomplete();
        } else {
            progress.markAsCompleted();
        }

        progress = lessonProgressRepository.save(progress);

        // Update course progress
        Lesson lesson = progress.getLesson();
        Integer courseId = lesson.getSection().getCourse().getCourseId();
        updateCourseProgress(studentId, courseId);

        return progress;
    }

    /**
     * Update posisi video terakhir
     */
    @Transactional
    public LessonProgress updateVideoPosition(Integer studentId, Integer lessonId, int position, int duration) {
        LessonProgress progress = getOrCreateProgress(studentId, lessonId);
        
        progress.setLastPosition(position);
        progress.setUpdatedAt(LocalDateTime.now());

        // Auto complete jika sudah menonton 90%
        if (duration > 0 && position >= (duration * 0.9) && !progress.isCompleted()) {
            progress.markAsCompleted();
            
            // Update course progress
            Lesson lesson = progress.getLesson();
            Integer courseId = lesson.getSection().getCourse().getCourseId();
            updateCourseProgress(studentId, courseId);
        }

        return lessonProgressRepository.save(progress);
    }

    /**
     * Tambah watch time
     */
    @Transactional
    public LessonProgress addWatchTime(Integer studentId, Integer lessonId, int seconds) {
        LessonProgress progress = getOrCreateProgress(studentId, lessonId);
        progress.addWatchTime(seconds);
        return lessonProgressRepository.save(progress);
    }

    // ==================== CALCULATE PROGRESS ====================

    /**
     * Hitung persentase progress course
     */
    public int calculateCourseProgress(Integer studentId, Integer courseId) {
        // Hitung total lessons di course
        int totalLessons = lessonRepository.countByCourseId(courseId);
        
        if (totalLessons == 0) {
            return 0;
        }

        // Hitung lessons yang sudah selesai
        int completedLessons = lessonProgressRepository.countCompletedLessons(studentId, courseId);

        // Hitung persentase
        return Math.round((float) completedLessons / totalLessons * 100);
    }

    /**
     * Update progress di enrollment
     */
    @Transactional
    public void updateCourseProgress(Integer studentId, Integer courseId) {
        int progressPercentage = calculateCourseProgress(studentId, courseId);
        
        Optional<Enrollment> enrollmentOpt = enrollmentRepository
                .findByStudentUserIdAndCourseCourseId(studentId, courseId);

        if (enrollmentOpt.isPresent()) {
            Enrollment enrollment = enrollmentOpt.get();
            enrollment.setProgressPercentage(new BigDecimal(progressPercentage));
            enrollment.setLastAccessedAt(LocalDateTime.now());

            // Jika 100%, tandai sebagai selesai
            if (progressPercentage >= 100 && enrollment.getStatus() != Enrollment.EnrollmentStatus.completed) {
                enrollment.setStatus(Enrollment.EnrollmentStatus.completed);
                enrollment.setCompletedAt(LocalDateTime.now());
            }

            enrollmentRepository.save(enrollment);
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Hitung jumlah lesson yang selesai
     */
    public int countCompletedLessons(Integer studentId, Integer courseId) {
        return lessonProgressRepository.countCompletedLessons(studentId, courseId);
    }

    /**
     * Dapatkan total watch time di course (dalam detik)
     */
    public int getTotalWatchTime(Integer studentId, Integer courseId) {
        return lessonProgressRepository.getTotalWatchTime(studentId, courseId);
    }

    /**
     * Format watch time ke string (HH:MM:SS)
     */
    public String formatWatchTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d jam %d menit", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d menit", minutes);
        } else {
            return String.format("%d detik", seconds);
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Dapatkan atau buat progress baru
     */
    private LessonProgress getOrCreateProgress(Integer studentId, Integer lessonId) {
        Optional<LessonProgress> existingProgress = lessonProgressRepository
                .findByStudentUserIdAndLessonLessonId(studentId, lessonId);

        if (existingProgress.isPresent()) {
            return existingProgress.get();
        }

        // Buat progress baru
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student tidak ditemukan"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson tidak ditemukan"));

        LessonProgress newProgress = new LessonProgress(student, lesson);
        return lessonProgressRepository.save(newProgress);
    }

    /**
     * Reset progress course
     */
    @Transactional
    public void resetCourseProgress(Integer studentId, Integer courseId) {
        // Hapus semua lesson progress
        lessonProgressRepository.deleteByStudentAndCourse(studentId, courseId);

        // Reset enrollment progress
        Optional<Enrollment> enrollmentOpt = enrollmentRepository
                .findByStudentUserIdAndCourseCourseId(studentId, courseId);

        if (enrollmentOpt.isPresent()) {
            Enrollment enrollment = enrollmentOpt.get();
            enrollment.setProgressPercentage(BigDecimal.ZERO);
            enrollment.setStatus(Enrollment.EnrollmentStatus.active);
            enrollment.setCompletedAt(null);
            enrollmentRepository.save(enrollment);
        }
    }
}
