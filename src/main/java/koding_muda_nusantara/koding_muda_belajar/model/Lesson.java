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

import java.time.LocalDateTime;
import koding_muda_nusantara.koding_muda_belajar.enums.LessonContentType;

@Entity
@Table(name = "lessons", indexes = {
    @Index(name = "idx_lesson_section", columnList = "section_id"),
    @Index(name = "idx_lesson_order", columnList = "section_id, sort_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Integer lessonId;

    // Relasi Many-to-One ke Section
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private LessonContentType contentType = LessonContentType.video;

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    // Menggunakan LONGTEXT agar bisa menampung artikel panjang
    @Column(name = "content_text", columnDefinition = "LONGTEXT")
    private String contentText;
    
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration = 0; // Dalam detik atau menit

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_preview", nullable = false)
    private boolean isPreview = false;

    // Default locked = true (dikunci kecuali sudah beli/enroll)
    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
