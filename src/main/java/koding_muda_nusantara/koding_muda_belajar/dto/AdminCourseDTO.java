package koding_muda_nusantara.koding_muda_belajar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseLevel;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.model.Lecturer;

/**
 * DTO untuk menampilkan data kursus di halaman admin
 * Termasuk informasi enrollment count
 */
public class AdminCourseDTO {
    
    private Integer courseId;
    private String title;
    private String slug;
    private String shortDescription;
    private String thumbnailUrl;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private CourseLevel level;
    private CourseStatus status;
    private boolean isFeatured;
    private Integer totalLessons;
    private Integer totalDuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    
    // Related entities info
    private Category category;
    private Lecturer lecturer;
    
    // Statistics
    private Long enrollmentCount;
    private Double averageRating;
    private Long reviewCount;
    
    // Constructor kosong
    public AdminCourseDTO() {}
    
    // Constructor untuk JPQL query
    public AdminCourseDTO(Integer courseId, String title, String slug, 
                          String shortDescription, String thumbnailUrl,
                          BigDecimal price, BigDecimal discountPrice,
                          CourseLevel level, CourseStatus status, boolean isFeatured,
                          Integer totalLessons, Integer totalDuration,
                          LocalDateTime createdAt, LocalDateTime updatedAt,
                          LocalDateTime publishedAt,
                          Category category, Lecturer lecturer,
                          Long enrollmentCount) {
        this.courseId = courseId;
        this.title = title;
        this.slug = slug;
        this.shortDescription = shortDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.discountPrice = discountPrice;
        this.level = level;
        this.status = status;
        this.isFeatured = isFeatured;
        this.totalLessons = totalLessons;
        this.totalDuration = totalDuration;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.publishedAt = publishedAt;
        this.category = category;
        this.lecturer = lecturer;
        this.enrollmentCount = enrollmentCount != null ? enrollmentCount : 0L;
    }
    
    // Constructor dengan rating
    public AdminCourseDTO(Integer courseId, String title, String slug, 
                          String shortDescription, String thumbnailUrl,
                          BigDecimal price, BigDecimal discountPrice,
                          CourseLevel level, CourseStatus status, boolean isFeatured,
                          Integer totalLessons, Integer totalDuration,
                          LocalDateTime createdAt, LocalDateTime updatedAt,
                          LocalDateTime publishedAt,
                          Category category, Lecturer lecturer,
                          Long enrollmentCount, Double averageRating, Long reviewCount) {
        this(courseId, title, slug, shortDescription, thumbnailUrl, price, discountPrice,
             level, status, isFeatured, totalLessons, totalDuration, createdAt, updatedAt,
             publishedAt, category, lecturer, enrollmentCount);
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
    }
    
    // Getters and Setters
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    
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
    
    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    
    public Integer getTotalLessons() { return totalLessons; }
    public void setTotalLessons(Integer totalLessons) { this.totalLessons = totalLessons; }
    
    public Integer getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Integer totalDuration) { this.totalDuration = totalDuration; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public Lecturer getLecturer() { return lecturer; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }
    
    public Long getEnrollmentCount() { return enrollmentCount; }
    public void setEnrollmentCount(Long enrollmentCount) { this.enrollmentCount = enrollmentCount; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }
    
    // Helper methods
    public String getFormattedPrice() {
        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return "Gratis";
        }
        return "Rp " + String.format("%,.0f", price);
    }
    
    public String getFormattedRating() {
        if (averageRating == null || averageRating == 0) return "-";
        return String.format("%.1f", averageRating);
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
}
