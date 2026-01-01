package koding_muda_nusantara.koding_muda_belajar.dto;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Integer reviewId;
    private String studentName;
    private String studentFirstName;
    private String studentLastName;
    private String courseTitle;
    private String courseSlug;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;

    public ReviewDTO(Integer reviewId, String firstName, String lastName, 
                     String courseTitle, String courseSlug, Integer rating, 
                     String reviewText, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.studentName = firstName + (lastName != null ? " " + lastName : "");
        this.courseTitle = courseTitle;
        this.courseSlug = courseSlug;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
        this.studentFirstName = firstName;
        this.studentLastName = lastName;
    }

    // Getters
    public Integer getReviewId() { return reviewId; }
    public String getStudentName() { return studentName; }
    public String getCourseTitle() { return courseTitle; }
    public String getCourseSlug() { return courseSlug; }
    public Integer getRating() { return rating; }
    public String getReviewText() { return reviewText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStudentFirstName(){return studentFirstName;}
    public String getStudentLastName(){return studentLastName;}
}