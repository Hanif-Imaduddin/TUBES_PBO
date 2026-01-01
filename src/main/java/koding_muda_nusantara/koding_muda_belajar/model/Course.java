/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.model;

/**
 *
 * @author hanif
 */

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseLevel;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;
import lombok.ToString;

@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_course_status", columnList = "status"),
    @Index(name = "idx_course_category", columnList = "category_id"),
    @Index(name = "idx_course_lecturer", columnList = "lecturer_id"),
    @Index(name = "idx_course_featured", columnList = "is_featured")
})
@Data // Lombok untuk Getter, Setter, toString, dll
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer courseId;

    // Relasi ke Lecturer (Many-to-One)
    // Asumsi: Anda sudah membuat Entity Lecturer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Relasi ke Category (Many-to-One)
    // Asumsi: Anda sudah membuat Entity Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "preview_video_url", length = 500)
    private String previewVideoUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseLevel level = CourseLevel.beginner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.draft;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    @Column(name = "total_duration", nullable = false)
    private Integer totalDuration = 0; // Dalam menit atau detik (sesuai logika bisnis)

    @Column(name = "total_lessons", nullable = false)
    private Integer totalLessons = 0;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "what_you_learn", columnDefinition = "TEXT")
    private String whatYouLearn;

    // Otomatis diisi saat insert
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Otomatis diupdate saat ada perubahan
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Section> sections = new ArrayList<>();
}
