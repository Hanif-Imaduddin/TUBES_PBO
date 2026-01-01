/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.dto;

import java.math.BigDecimal;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseLevel;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;

public class CourseWithStatsDTO {
    
    private Integer courseId;
    private String title;
    private String slug;
    private String shortDescription;
    private String thumbnailUrl;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private CourseLevel level;
    private CourseStatus status;
    private Integer totalLessons;
    private Integer totalDuration;
    private String categoryName;
    private String categorySlug;
    private String lecturerName;
    
    // Statistik
    private Long totalStudents;
    private Double averageRating;
    private Long totalReviews;
    private BigDecimal totalEarnings;
    
    // Constructor untuk JPQL
    public CourseWithStatsDTO(Integer courseId, String title, String slug, 
                               String shortDescription, String thumbnailUrl,
                               BigDecimal price, BigDecimal discountPrice,
                               CourseLevel level, CourseStatus status,
                               Integer totalLessons, Integer totalDuration,
                               String categoryName, String categorySlug,
                               Long totalStudents, Double averageRating, 
                               Long totalReviews) {
        this.courseId = courseId;
        this.title = title;
        this.slug = slug;
        this.shortDescription = shortDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.discountPrice = discountPrice;
        this.level = level;
        this.status = status;
        this.totalLessons = totalLessons;
        this.totalDuration = totalDuration;
        this.categoryName = categoryName;
        this.categorySlug = categorySlug;
        this.totalStudents = totalStudents != null ? totalStudents : 0L;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
    }
    
    public CourseWithStatsDTO(Integer courseId, String title, String slug, 
                               String shortDescription, String thumbnailUrl,
                               BigDecimal price, BigDecimal discountPrice,
                               CourseLevel level, CourseStatus status,
                               Integer totalLessons, Integer totalDuration,
                               String categoryName, String categorySlug,
                               String lecturerName,
                               Long totalStudents, Double averageRating, 
                               Long totalReviews) {
        this.courseId = courseId;
        this.title = title;
        this.slug = slug;
        this.shortDescription = shortDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.discountPrice = discountPrice;
        this.level = level;
        this.status = status;
        this.totalLessons = totalLessons;
        this.totalDuration = totalDuration;
        this.categoryName = categoryName;
        this.categorySlug = categorySlug;
        this.lecturerName = lecturerName;
        this.totalStudents = totalStudents != null ? totalStudents : 0L;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
    }
    
    public CourseWithStatsDTO() {}
    
    // Getters and Setters
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    
    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(BigDecimal discountPrice) { this.discountPrice = discountPrice; }
    
    public CourseLevel getLevel() { return level; }
    public void setLevel(CourseLevel level) { this.level = level; }
    
    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
    
    public Integer getTotalLessons() { return totalLessons; }
    public void setTotalLessons(Integer totalLessons) { this.totalLessons = totalLessons; }
    
    public Integer getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Integer totalDuration) { this.totalDuration = totalDuration; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getCategorySlug() { return categorySlug; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }
    
    public Long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Long totalStudents) { this.totalStudents = totalStudents; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }
    
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    
    // Helper methods untuk tampilan
    public String getFormattedRating() {
        if (averageRating == null || averageRating == 0) return "-";
        return String.format("%.1f", averageRating);
    }
    
    public String getFormattedEarnings() {
        if (totalEarnings == null) return "Rp 0";
        return "Rp " + String.format("%,.0f", totalEarnings);
    }
    
    public String getFormattedStudents() {
        if (totalStudents == null) return "0";
        if (totalStudents >= 1000) {
            return String.format("%.1fK", totalStudents / 1000.0);
        }
        return String.format("%,d", totalStudents);
    }
    
    public String getStatusDisplay() {
        if (status == null) return "Draft";
        switch (status) {
            case draft: return "Draft";
            case published: return "Published";
            case suspended: return "Suspended";
            default: return "Draft";
        }
    }
    
    public String getStatusClass() {
        if (status == null) return "status-draft";
        switch (status) {
            case published: return "status-published";
            case draft: return "status-draft";
            case suspended: return "status-suspended";
            default: return "status-draft";
        }
    }
}
