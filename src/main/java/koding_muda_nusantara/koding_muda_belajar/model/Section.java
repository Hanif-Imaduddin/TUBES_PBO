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
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sections", indexes = {
    @Index(name = "idx_section_course", columnList = "course_id"),
    @Index(name = "idx_section_order", columnList = "course_id, sort_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Integer sectionId;

    // Relasi Many-to-One ke Course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude // Hindari loop saat logging
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_preview", nullable = false)
    private boolean isPreview = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========================================================================
    // RELASI KE LESSONS (One-to-Many)
    // ========================================================================
    
    // Satu Section memiliki banyak Lesson
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC") // Otomatis urutkan lesson berdasarkan sort_order saat di-fetch
    @ToString.Exclude
    private List<Lesson> lessons;
}
