package koding_muda_nusantara.koding_muda_belajar.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * DTO untuk menampilkan informasi akun user
 */
public class AccountInfoDTO {

    private Integer userId;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Timestamp joinDate;
    private String role;
    
    // Balance info (untuk Student dan Lecturer)
    private BigDecimal balance;
    
    // Stats untuk Student
    private Long enrolledCourses;
    private Long completedCourses;
    
    // Stats untuk Lecturer
    private Long totalCourses;
    private Long totalStudents;
    private BigDecimal totalEarnings;

    // Default constructor
    public AccountInfoDTO() {
    }

    // Constructor untuk basic info
    public AccountInfoDTO(Integer userId, String firstName, String lastName, 
            String username, String email, Timestamp joinDate, String role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.joinDate = joinDate;
        this.role = role;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(Long enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public Long getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(Long completedCourses) {
        this.completedCourses = completedCourses;
    }

    public Long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(Long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    // Helper methods
    public String getFullName() {
        if (lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }

    public String getInitials() {
        String initials = "";
        if (firstName != null && !firstName.isEmpty()) {
            initials += firstName.charAt(0);
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials += lastName.charAt(0);
        }
        return initials.toUpperCase();
    }

    public String getRoleDisplay() {
        if (role == null) return "";
        switch (role) {
            case "Student":
                return "Pelajar";
            case "Lecturer":
                return "Pengajar";
            case "Admin":
                return "Administrator";
            default:
                return role;
        }
    }

    public String getFormattedBalance() {
        if (balance == null) return "Rp 0";
        return String.format("Rp %,.0f", balance);
    }
}
