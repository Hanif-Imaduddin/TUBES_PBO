package koding_muda_nusantara.koding_muda_belajar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseLevel;
import koding_muda_nusantara.koding_muda_belajar.enums.EnrollmentStatus;

/**
 * DTO untuk menampilkan data kursus yang di-enroll oleh student
 * di halaman My Learning
 */
public class MyLearningCourseDTO {

    // Enrollment data
    private Integer enrollmentId;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private BigDecimal progressPercentage;
    private LocalDateTime lastAccessedAt;
    private EnrollmentStatus enrollmentStatus;

    // Course data
    private Integer courseId;
    private String courseTitle;
    private String courseSlug;
    private String shortDescription;
    private String thumbnailUrl;
    private CourseLevel level;
    private Integer totalDuration;
    private Integer totalLessons;

    // Category data
    private String categoryName;

    // Lecturer data
    private Integer lecturerId;
    private String lecturerFirstName;
    private String lecturerLastName;

    // Calculated fields
    private Integer completedLessons;
    private Integer remainingLessons;

    // Default constructor
    public MyLearningCourseDTO() {
    }

    // Constructor untuk JPQL query
    public MyLearningCourseDTO(Integer enrollmentId, LocalDateTime enrolledAt, 
            LocalDateTime completedAt, BigDecimal progressPercentage,
            LocalDateTime lastAccessedAt, EnrollmentStatus enrollmentStatus,
            Integer courseId, String courseTitle, String courseSlug,
            String shortDescription, String thumbnailUrl, CourseLevel level,
            Integer totalDuration, Integer totalLessons,
            String categoryName,
            Integer lecturerId, String lecturerFirstName, String lecturerLastName) {
        this.enrollmentId = enrollmentId;
        this.enrolledAt = enrolledAt;
        this.completedAt = completedAt;
        this.progressPercentage = progressPercentage;
        this.lastAccessedAt = lastAccessedAt;
        this.enrollmentStatus = enrollmentStatus;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseSlug = courseSlug;
        this.shortDescription = shortDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.level = level;
        this.totalDuration = totalDuration;
        this.totalLessons = totalLessons;
        this.categoryName = categoryName;
        this.lecturerId = lecturerId;
        this.lecturerFirstName = lecturerFirstName;
        this.lecturerLastName = lecturerLastName;
    }

    // Getters and Setters
    public Integer getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Integer enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public EnrollmentStatus getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(EnrollmentStatus enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseSlug() {
        return courseSlug;
    }

    public void setCourseSlug(String courseSlug) {
        this.courseSlug = courseSlug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public CourseLevel getLevel() {
        return level;
    }

    public void setLevel(CourseLevel level) {
        this.level = level;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Integer lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getLecturerFirstName() {
        return lecturerFirstName;
    }

    public void setLecturerFirstName(String lecturerFirstName) {
        this.lecturerFirstName = lecturerFirstName;
    }

    public String getLecturerLastName() {
        return lecturerLastName;
    }

    public void setLecturerLastName(String lecturerLastName) {
        this.lecturerLastName = lecturerLastName;
    }

    public Integer getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(Integer completedLessons) {
        this.completedLessons = completedLessons;
    }

    public Integer getRemainingLessons() {
        return remainingLessons;
    }

    public void setRemainingLessons(Integer remainingLessons) {
        this.remainingLessons = remainingLessons;
    }

    // Helper methods
    public String getLecturerFullName() {
        if (lecturerLastName != null && !lecturerLastName.isEmpty()) {
            return lecturerFirstName + " " + lecturerLastName;
        }
        return lecturerFirstName;
    }

    public int getProgressPercentageAsInt() {
        return progressPercentage != null ? progressPercentage.intValue() : 0;
    }

    public boolean isCompleted() {
        return enrollmentStatus == EnrollmentStatus.completed;
    }

    public boolean isActive() {
        return enrollmentStatus == EnrollmentStatus.active;
    }

    public String getLevelDisplay() {
        if (level == null) return "Pemula";
        switch (level) {
            case beginner:
                return "Pemula";
            case intermediate:
                return "Menengah";
            case advanced:
                return "Mahir";
            default:
                return "Pemula";
        }
    }

    public String getFormattedDuration() {
        if (totalDuration == null || totalDuration == 0) {
            return "0 menit";
        }
        int hours = totalDuration / 60;
        int minutes = totalDuration % 60;
        if (hours > 0) {
            return hours + " jam " + (minutes > 0 ? minutes + " menit" : "");
        }
        return minutes + " menit";
    }
}
