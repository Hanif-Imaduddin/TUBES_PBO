package koding_muda_nusantara.koding_muda_belajar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionItemDTO {
    
    private Long id;
    private BigDecimal price;
    private LocalDateTime createdAt;
    
    // Course info
    private Integer courseId;
    private String courseTitle;
    private String courseSlug;
    private String courseThumbnailUrl;

    // Default constructor
    public TransactionItemDTO() {}

    // Constructor untuk query
    public TransactionItemDTO(Long id, BigDecimal price, LocalDateTime createdAt,
                              Integer courseId, String courseTitle, String courseSlug, 
                              String courseThumbnailUrl) {
        this.id = id;
        this.price = price;
        this.createdAt = createdAt;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseSlug = courseSlug;
        this.courseThumbnailUrl = courseThumbnailUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public String getCourseThumbnailUrl() {
        return courseThumbnailUrl;
    }

    public void setCourseThumbnailUrl(String courseThumbnailUrl) {
        this.courseThumbnailUrl = courseThumbnailUrl;
    }
}
