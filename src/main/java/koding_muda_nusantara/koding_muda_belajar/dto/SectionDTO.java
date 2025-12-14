/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.dto;

/**
 *
 * @author hanif
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO untuk Section dalam Course
 */
public class SectionDTO {

    private Integer sectionId;

    @NotBlank(message = "Judul section wajib diisi")
    @Size(min = 3, max = 255, message = "Judul section harus antara 3-255 karakter")
    private String title;

    private String description;

    private Integer sortOrder = 0;

    private Boolean isPreview = false;

    @Valid
    private List<LessonDTO> lessons = new ArrayList<>();

    // Constructors
    public SectionDTO() {}

    public SectionDTO(String title) {
        this.title = title;
    }

    // Getters and Setters
    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsPreview() {
        return isPreview;
    }

    public void setIsPreview(Boolean isPreview) {
        this.isPreview = isPreview;
    }

    public List<LessonDTO> getLessons() {
        return lessons;
    }

    public void setLessons(List<LessonDTO> lessons) {
        this.lessons = lessons;
    }

    // Helper method untuk menambah lesson
    public void addLesson(LessonDTO lesson) {
        this.lessons.add(lesson);
    }
}
