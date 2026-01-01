package koding_muda_nusantara.koding_muda_belajar.dto;

/**
 * DTO untuk statistik user di admin dashboard
 */
public class UserStatsDTO {

    private Long totalStudents;
    private Long totalLecturers;
    private Long totalAdmins;
    private Long totalUsers;

    public UserStatsDTO() {
        this.totalStudents = 0L;
        this.totalLecturers = 0L;
        this.totalAdmins = 0L;
        this.totalUsers = 0L;
    }

    public UserStatsDTO(Long totalStudents, Long totalLecturers, Long totalAdmins) {
        this.totalStudents = totalStudents != null ? totalStudents : 0L;
        this.totalLecturers = totalLecturers != null ? totalLecturers : 0L;
        this.totalAdmins = totalAdmins != null ? totalAdmins : 0L;
        this.totalUsers = this.totalStudents + this.totalLecturers + this.totalAdmins;
    }

    // Getters and Setters
    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
        updateTotalUsers();
    }

    public Long getTotalLecturers() {
        return totalLecturers;
    }

    public void setTotalLecturers(Long totalLecturers) {
        this.totalLecturers = totalLecturers;
        updateTotalUsers();
    }

    public Long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(Long totalAdmins) {
        this.totalAdmins = totalAdmins;
        updateTotalUsers();
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    private void updateTotalUsers() {
        this.totalUsers = (totalStudents != null ? totalStudents : 0L) 
                        + (totalLecturers != null ? totalLecturers : 0L) 
                        + (totalAdmins != null ? totalAdmins : 0L);
    }
}
