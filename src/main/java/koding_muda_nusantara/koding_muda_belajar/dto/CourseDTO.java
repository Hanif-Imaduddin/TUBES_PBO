package koding_muda_nusantara.koding_muda_belajar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO untuk Create/Update Course
 */
public class CourseDTO {

    private Integer courseId;

    @NotBlank(message = "Judul kursus wajib diisi")
    @Size(min = 5, max = 255, message = "Judul harus antara 5-255 karakter")
    private String title;

    @NotBlank(message = "Deskripsi kursus wajib diisi")
    @Size(min = 20, message = "Deskripsi minimal 20 karakter")
    private String description;

    @Size(max = 500, message = "Deskripsi singkat maksimal 500 karakter")
    private String shortDescription;

    @NotNull(message = "Kategori wajib dipilih")
    private Integer categoryId;

    @NotBlank(message = "Level wajib dipilih")
    private String level; // beginner, intermediate, advanced

    @NotNull(message = "Harga wajib diisi")
    @DecimalMin(value = "0.0", message = "Harga tidak boleh negatif")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Harga diskon tidak boleh negatif")
    private BigDecimal discountPrice;

    private Boolean isFree = false;

    private String status = "draft"; // draft, published

    private MultipartFile thumbnail;

    private String existingThumbnailUrl;

    private String requirements;

    private String whatYouLearn;

    @Valid
    private List<SectionDTO> sections = new ArrayList<>();

    // Constructors
    public CourseDTO() {}

    // Getters and Setters
    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public MultipartFile getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(MultipartFile thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getExistingThumbnailUrl() {
        return existingThumbnailUrl;
    }

    public void setExistingThumbnailUrl(String existingThumbnailUrl) {
        this.existingThumbnailUrl = existingThumbnailUrl;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getWhatYouLearn() {
        return whatYouLearn;
    }

    public void setWhatYouLearn(String whatYouLearn) {
        this.whatYouLearn = whatYouLearn;
    }

    public List<SectionDTO> getSections() {
        return sections;
    }

    public void setSections(List<SectionDTO> sections) {
        this.sections = sections;
    }

    // Helper method untuk menambah section
    public void addSection(SectionDTO section) {
        this.sections.add(section);
    }
}
