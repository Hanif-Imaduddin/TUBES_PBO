/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryDTO {
    
    private Integer categoryId;
    private String name;
    private String slug;
    private String icon;
    private Long courseCount;
    
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
}
