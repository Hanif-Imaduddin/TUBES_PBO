package koding_muda_nusantara.koding_muda_belajar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CategoryDTO {
    
    private Integer categoryId;
    
    @NotBlank(message = "Nama kategori tidak boleh kosong")
    @Size(max = 100, message = "Nama kategori maksimal 100 karakter")
    private String name;
    
    @NotBlank(message = "Slug tidak boleh kosong")
    @Size(max = 100, message = "Slug maksimal 100 karakter")
    private String slug;
    
    @Size(max = 65535, message = "Deskripsi terlalu panjang")
    private String description;
    
    @Size(max = 50, message = "Icon maksimal 50 karakter")
    private String icon;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    // Field tambahan untuk statistik
    private Long courseCount;

    // ========================================================================
    // CONSTRUCTORS - Penting untuk JPQL queries
    // ========================================================================
    
    /**
     * Default constructor (diperlukan untuk Jackson deserialization)
     */
    public CategoryDTO() {
    }

    /**
     * Constructor untuk query: SELECT new CategoryDTO(c.categoryId, c.name, c.slug, c.icon, COUNT(co))
     * Digunakan di: findAllWithCourseCount(), findActiveWithCourseCount()
     */
    public CategoryDTO(Integer categoryId, String name, String slug, String icon, Long courseCount) {
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.icon = icon;
        this.courseCount = courseCount != null ? courseCount : 0L;
    }

    /**
     * Constructor minimal untuk statistik sederhana
     * Query: SELECT new CategoryDTO(c.name, COUNT(co))
     */
    public CategoryDTO(String name, Long courseCount) {
        this.name = name;
        this.courseCount = courseCount != null ? courseCount : 0L;
    }

    /**
     * Full constructor untuk semua field
     */
    public CategoryDTO(Integer categoryId, String name, String slug, String description, 
                       String icon, Boolean isActive, LocalDateTime createdAt, Long courseCount) {
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.icon = icon;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.courseCount = courseCount;
    }

    /**
     * Constructor tanpa courseCount
     */
    public CategoryDTO(Integer categoryId, String name, String slug, String description,
                       String icon, Boolean isActive, LocalDateTime createdAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.icon = icon;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // ========================================================================
    // STATIC FACTORY METHODS
    // ========================================================================
    
    /**
     * Konversi dari Entity ke DTO
     */
    public static CategoryDTO fromEntity(koding_muda_nusantara.koding_muda_belajar.model.Category category) {
        if (category == null) return null;
        
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setIcon(category.getIcon());
        dto.setIsActive(category.isActive());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }
    
    /**
     * Konversi dari Entity ke DTO dengan course count
     */
    public static CategoryDTO fromEntity(koding_muda_nusantara.koding_muda_belajar.model.Category category, Long courseCount) {
        CategoryDTO dto = fromEntity(category);
        if (dto != null) {
            dto.setCourseCount(courseCount != null ? courseCount : 0L);
        }
        return dto;
    }
    
    /**
     * Konversi DTO ke Entity
     */
    public koding_muda_nusantara.koding_muda_belajar.model.Category toEntity() {
        koding_muda_nusantara.koding_muda_belajar.model.Category category = 
                new koding_muda_nusantara.koding_muda_belajar.model.Category();
        category.setCategoryId(this.categoryId);
        category.setName(this.name);
        category.setSlug(this.slug);
        category.setDescription(this.description);
        category.setIcon(this.icon);
        category.setActive(this.isActive != null ? this.isActive : true);
        return category;
    }

    // ========================================================================
    // GETTERS AND SETTERS
    // ========================================================================

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @JsonProperty("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    @JsonProperty("isActive")
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Alias untuk kompatibilitas dengan Thymeleaf
    public Boolean isActive() {
        return isActive != null ? isActive : false;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(Long courseCount) {
        this.courseCount = courseCount;
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", isActive=" + isActive +
                ", courseCount=" + courseCount +
                '}';
    }
}