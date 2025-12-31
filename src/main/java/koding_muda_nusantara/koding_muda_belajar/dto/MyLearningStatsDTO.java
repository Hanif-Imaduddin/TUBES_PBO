package koding_muda_nusantara.koding_muda_belajar.dto;

/**
 * DTO untuk menampilkan statistik pembelajaran student
 */
public class MyLearningStatsDTO {

    private Long totalEnrolledCourses;
    private Long inProgressCourses;
    private Long completedCourses;
    private Long totalLessonsCompleted;
    private Long totalWatchTimeMinutes;

    // Default constructor
    public MyLearningStatsDTO() {
        this.totalEnrolledCourses = 0L;
        this.inProgressCourses = 0L;
        this.completedCourses = 0L;
        this.totalLessonsCompleted = 0L;
        this.totalWatchTimeMinutes = 0L;
    }

    // Constructor untuk aggregate query
    public MyLearningStatsDTO(Long totalEnrolledCourses, Long inProgressCourses, 
            Long completedCourses) {
        this.totalEnrolledCourses = totalEnrolledCourses != null ? totalEnrolledCourses : 0L;
        this.inProgressCourses = inProgressCourses != null ? inProgressCourses : 0L;
        this.completedCourses = completedCourses != null ? completedCourses : 0L;
        this.totalLessonsCompleted = 0L;
        this.totalWatchTimeMinutes = 0L;
    }

    // Full constructor
    public MyLearningStatsDTO(Long totalEnrolledCourses, Long inProgressCourses, 
            Long completedCourses, Long totalLessonsCompleted, Long totalWatchTimeMinutes) {
        this.totalEnrolledCourses = totalEnrolledCourses != null ? totalEnrolledCourses : 0L;
        this.inProgressCourses = inProgressCourses != null ? inProgressCourses : 0L;
        this.completedCourses = completedCourses != null ? completedCourses : 0L;
        this.totalLessonsCompleted = totalLessonsCompleted != null ? totalLessonsCompleted : 0L;
        this.totalWatchTimeMinutes = totalWatchTimeMinutes != null ? totalWatchTimeMinutes : 0L;
    }

    // Getters and Setters
    public Long getTotalEnrolledCourses() {
        return totalEnrolledCourses;
    }

    public void setTotalEnrolledCourses(Long totalEnrolledCourses) {
        this.totalEnrolledCourses = totalEnrolledCourses;
    }

    public Long getInProgressCourses() {
        return inProgressCourses;
    }

    public void setInProgressCourses(Long inProgressCourses) {
        this.inProgressCourses = inProgressCourses;
    }

    public Long getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(Long completedCourses) {
        this.completedCourses = completedCourses;
    }

    public Long getTotalLessonsCompleted() {
        return totalLessonsCompleted;
    }

    public void setTotalLessonsCompleted(Long totalLessonsCompleted) {
        this.totalLessonsCompleted = totalLessonsCompleted;
    }

    public Long getTotalWatchTimeMinutes() {
        return totalWatchTimeMinutes;
    }

    public void setTotalWatchTimeMinutes(Long totalWatchTimeMinutes) {
        this.totalWatchTimeMinutes = totalWatchTimeMinutes;
    }

    // Helper methods
    public String getFormattedWatchTime() {
        if (totalWatchTimeMinutes == null || totalWatchTimeMinutes == 0) {
            return "0 menit";
        }
        long hours = totalWatchTimeMinutes / 60;
        long minutes = totalWatchTimeMinutes % 60;
        if (hours > 0) {
            return hours + " jam " + (minutes > 0 ? minutes + " menit" : "");
        }
        return minutes + " menit";
    }

    public int getCompletionRate() {
        if (totalEnrolledCourses == null || totalEnrolledCourses == 0) {
            return 0;
        }
        return (int) ((completedCourses * 100) / totalEnrolledCourses);
    }
}
