/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.dto;

/**
 *
 * @author hanif
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO untuk Lesson dalam Section
 */
public class LessonDTO {

    private Integer lessonId;

    @NotBlank(message = "Judul materi wajib diisi")
    @Size(min = 3, max = 255, message = "Judul materi harus antara 3-255 karakter")
    private String title;

    @NotBlank(message = "Tipe konten wajib dipilih")
    private String contentType; // video, text, pdf, quiz

    private String contentUrl;

    private String contentText;

    private Integer duration = 0; // dalam menit

    private Integer sortOrder = 0;

    private Boolean isPreview = false;

    private Boolean isLocked = true;

    private MultipartFile contentFile;

    // Constructors
    public LessonDTO() {}

    public LessonDTO(String title, String contentType) {
        this.title = title;
        this.contentType = contentType;
    }

    // Getters and Setters
    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
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

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public MultipartFile getContentFile() {
        return contentFile;
    }

    public void setContentFile(MultipartFile contentFile) {
        this.contentFile = contentFile;
    }

    // Helper method untuk mendapatkan tipe display
    public String getContentTypeDisplay() {
        return switch (contentType) {
            case "video" -> "Video";
            case "text" -> "Teks";
            case "pdf" -> "PDF";
            case "quiz" -> "Quiz";
            default -> contentType;
        };
    }
}
