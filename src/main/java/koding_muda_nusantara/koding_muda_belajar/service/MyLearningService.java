package koding_muda_nusantara.koding_muda_belajar.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import koding_muda_nusantara.koding_muda_belajar.dto.MyLearningCourseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.MyLearningStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.EnrollmentStatus;
import koding_muda_nusantara.koding_muda_belajar.repository.EnrollmentRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.LessonProgressRepository;

@Service
@Transactional(readOnly = true)
public class MyLearningService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired(required = false)
    private LessonProgressRepository lessonProgressRepository;

    /**
     * Mendapatkan semua kursus yang di-enroll student
     */
    public List<MyLearningCourseDTO> getMyLearningCourses(Integer studentId) {
        List<MyLearningCourseDTO> courses = enrollmentRepository.findMyLearningCourses(studentId);
        enrichWithLessonProgress(courses, studentId);
        return courses;
    }

    /**
     * Mendapatkan kursus dengan filter status (all, active, completed)
     */
    public List<MyLearningCourseDTO> getMyLearningCoursesByStatus(Integer studentId, String status) {
        List<MyLearningCourseDTO> courses;
        
        if (status == null || status.isEmpty() || status.equalsIgnoreCase("all")) {
            courses = enrollmentRepository.findMyLearningCourses(studentId);
        } else {
            try {
                EnrollmentStatus enrollmentStatus = EnrollmentStatus.valueOf(status.toLowerCase());
                courses = enrollmentRepository.findMyLearningCoursesByStatus(studentId, enrollmentStatus);
            } catch (IllegalArgumentException e) {
                courses = enrollmentRepository.findMyLearningCourses(studentId);
            }
        }
        
        enrichWithLessonProgress(courses, studentId);
        return courses;
    }

    /**
     * Mendapatkan kursus dengan filter kategori
     */
    public List<MyLearningCourseDTO> getMyLearningCoursesByCategory(Integer studentId, Integer categoryId) {
        List<MyLearningCourseDTO> courses = enrollmentRepository.findMyLearningCoursesByCategory(studentId, categoryId);
        enrichWithLessonProgress(courses, studentId);
        return courses;
    }

    /**
     * Search kursus berdasarkan keyword
     */
    public List<MyLearningCourseDTO> searchMyLearningCourses(Integer studentId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getMyLearningCourses(studentId);
        }
        List<MyLearningCourseDTO> courses = enrollmentRepository.searchMyLearningCourses(studentId, keyword.trim());
        enrichWithLessonProgress(courses, studentId);
        return courses;
    }

    /**
     * Mendapatkan kursus dengan pagination
     */
    public Page<MyLearningCourseDTO> getMyLearningCoursesPageable(Integer studentId, int page, int size, String sortBy) {
        Sort sort;
        switch (sortBy != null ? sortBy.toLowerCase() : "recent") {
            case "title":
                sort = Sort.by("course.title").ascending();
                break;
            case "progress":
                sort = Sort.by("progressPercentage").descending();
                break;
            case "enrolled":
                sort = Sort.by("enrolledAt").descending();
                break;
            default:
                sort = Sort.by("lastAccessedAt").descending();
        }
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return enrollmentRepository.findMyLearningCoursesPageable(studentId, pageable);
    }

    /**
     * Mendapatkan kursus yang baru diakses untuk "Continue Learning"
     */
    public List<MyLearningCourseDTO> getRecentlyAccessedCourses(Integer studentId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<MyLearningCourseDTO> courses = enrollmentRepository.findRecentlyAccessedCourses(studentId, pageable);
        enrichWithLessonProgress(courses, studentId);
        return courses;
    }

    /**
     * Mendapatkan kursus yang hampir selesai
     */
    public List<MyLearningCourseDTO> getAlmostCompletedCourses(Integer studentId) {
        List<MyLearningCourseDTO> courses = enrollmentRepository.findAlmostCompletedCourses(studentId);
        enrichWithLessonProgress(courses, studentId);
        return courses;
    }

    /**
     * Mendapatkan statistik pembelajaran student
     */
    public MyLearningStatsDTO getMyLearningStats(Integer studentId) {
        long totalEnrolled = enrollmentRepository.countByStudent_UserId(studentId);
        long completed = enrollmentRepository.countByStudent_UserIdAndStatus(studentId, EnrollmentStatus.completed);
        long inProgress = enrollmentRepository.countByStudent_UserIdAndStatus(studentId, EnrollmentStatus.active);
        
        MyLearningStatsDTO stats = new MyLearningStatsDTO(totalEnrolled, inProgress, completed);
        
        // Jika LessonProgressRepository tersedia, tambahkan statistik lesson
        if (lessonProgressRepository != null) {
            try {
                Long totalLessonsCompleted = lessonProgressRepository.countCompletedLessonsByStudent(studentId);
                Long totalWatchTime = lessonProgressRepository.getTotalWatchTimeByStudent(studentId);
                stats.setTotalLessonsCompleted(totalLessonsCompleted != null ? totalLessonsCompleted : 0L);
                stats.setTotalWatchTimeMinutes(totalWatchTime != null ? totalWatchTime / 60 : 0L);
            } catch (Exception e) {
                // Jika ada error, gunakan default value
            }
        }
        
        return stats;
    }

    /**
     * Cek apakah student sudah enroll di course
     */
    public boolean isEnrolled(Integer studentId, Integer courseId) {
        return enrollmentRepository.existsByStudent_UserIdAndCourse_CourseId(studentId, courseId);
    }

    /**
     * Menambahkan informasi progress lesson ke setiap course
     */
    private void enrichWithLessonProgress(List<MyLearningCourseDTO> courses, Integer studentId) {
        if (lessonProgressRepository == null) {
            return;
        }
        
        for (MyLearningCourseDTO course : courses) {
            try {
                Long completedLessons = lessonProgressRepository
                        .countCompletedLessonsByCourse(studentId, course.getCourseId());
                course.setCompletedLessons(completedLessons != null ? completedLessons.intValue() : 0);
                
                Integer totalLessons = course.getTotalLessons();
                Integer completed = course.getCompletedLessons();
                course.setRemainingLessons(totalLessons - completed);
            } catch (Exception e) {
                course.setCompletedLessons(0);
                course.setRemainingLessons(course.getTotalLessons());
            }
        }
    }
}
