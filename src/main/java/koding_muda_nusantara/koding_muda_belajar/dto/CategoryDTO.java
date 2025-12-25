/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    
    private Integer categoryId;
    private String name;
    private String slug;
    private String icon;
    private Long courseCount;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    // Constructor untuk JPQL (lengkap)
    public CategoryDTO(Integer categoryId, String name, String slug, String icon, Long courseCount) {
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.icon = icon;
        this.courseCount = courseCount != null ? courseCount : 0L;
    }
    
    // Constructor minimal (hanya name dan courseCount)
    public CategoryDTO(String name, Long courseCount) {
        this.name = name;
        this.courseCount = courseCount != null ? courseCount : 0L;
    }
    
    // Static method untuk konversi dari Entity ke DTO
    public static CategoryDTO fromEntity(koding_muda_nusantara.koding_muda_belajar.model.Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
    
    // Static method dengan course count
    public static CategoryDTO fromEntity(koding_muda_nusantara.koding_muda_belajar.model.Category category, Long courseCount) {
        CategoryDTO dto = fromEntity(category);
        dto.setCourseCount(courseCount);
        return dto;
    }
    
    // Method untuk konversi ke Entity
    public koding_muda_nusantara.koding_muda_belajar.model.Category toEntity() {
        koding_muda_nusantara.koding_muda_belajar.model.Category category = new koding_muda_nusantara.koding_muda_belajar.model.Category();
        category.setCategoryId(this.categoryId);
        category.setName(this.name);
        category.setSlug(this.slug);
        category.setDescription(this.description);
        category.setIcon(this.icon);
        category.setActive(this.isActive);
        return category;
    }
}
