package koding_muda_nusantara.koding_muda_belajar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.thumbnails:thumbnails}")
    private String thumbnailsDir;

    @Value("${app.upload.videos:videos}")
    private String videosDir;

    @Value("${app.upload.documents:documents}")
    private String documentsDir;

    private Path rootLocation;
    private Path thumbnailsLocation;
    private Path videosLocation;
    private Path documentsLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(uploadDir);
            thumbnailsLocation = rootLocation.resolve(thumbnailsDir);
            videosLocation = rootLocation.resolve(videosDir);
            documentsLocation = rootLocation.resolve(documentsDir);

            // Buat direktori jika belum ada
            Files.createDirectories(rootLocation);
            Files.createDirectories(thumbnailsLocation);
            Files.createDirectories(videosLocation);
            Files.createDirectories(documentsLocation);

        } catch (IOException e) {
            throw new RuntimeException("Tidak dapat membuat direktori upload", e);
        }
    }

    /**
     * Simpan thumbnail kursus
     * @return URL/path relatif untuk disimpan di database
     */
    public String storeThumbnail(MultipartFile file) {
        return storeFile(file, thumbnailsLocation, "thumbnails");
    }

    /**
     * Simpan video lesson
     * @return URL/path relatif untuk disimpan di database
     */
    public String storeVideo(MultipartFile file) {
        validateVideoFile(file);
        return storeFile(file, videosLocation, "videos");
    }

    /**
     * Simpan video lesson dalam subfolder kursus
     * @param courseId ID kursus untuk organisasi folder
     * @return URL/path relatif untuk disimpan di database
     */
    public String storeVideo(MultipartFile file, Integer courseId) {
        validateVideoFile(file);
        
        try {
            // Buat subfolder untuk kursus
            Path courseVideoPath = videosLocation.resolve("course-" + courseId);
            Files.createDirectories(courseVideoPath);
            
            return storeFile(file, courseVideoPath, "videos/course-" + courseId);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori kursus", e);
        }
    }

    /**
     * Simpan dokumen (PDF, dll)
     * @return URL/path relatif untuk disimpan di database
     */
    public String storeDocument(MultipartFile file) {
        validateDocumentFile(file);
        return storeFile(file, documentsLocation, "documents");
    }

    /**
     * Simpan dokumen dalam subfolder kursus
     */
    public String storeDocument(MultipartFile file, Integer courseId) {
        validateDocumentFile(file);
        
        try {
            Path courseDocPath = documentsLocation.resolve("course-" + courseId);
            Files.createDirectories(courseDocPath);
            
            return storeFile(file, courseDocPath, "documents/course-" + courseId);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori kursus", e);
        }
    }

    /**
     * Simpan content berdasarkan tipe
     */
    public String storeContent(MultipartFile file, String contentType) {
        return switch (contentType.toLowerCase()) {
            case "video" -> storeVideo(file);
            case "pdf", "document" -> storeDocument(file);
            case "thumbnail" -> storeThumbnail(file);
            default -> throw new RuntimeException("Tipe konten tidak valid: " + contentType);
        };
    }

    /**
     * Simpan content berdasarkan tipe dengan courseId
     */
    public String storeContent(MultipartFile file, String contentType, Integer courseId) {
        return switch (contentType.toLowerCase()) {
            case "video" -> storeVideo(file, courseId);
            case "pdf", "document" -> storeDocument(file, courseId);
            case "thumbnail" -> storeThumbnail(file);
            default -> throw new RuntimeException("Tipe konten tidak valid: " + contentType);
        };
    }

    /**
     * Method utama untuk menyimpan file
     */
    private String storeFile(MultipartFile file, Path destinationDir, String relativePath) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File tidak boleh kosong");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            
            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path destinationFile = destinationDir.resolve(newFilename).normalize();
            
            // Security check - pastikan file disimpan dalam direktori yang benar
            if (!destinationFile.getParent().equals(destinationDir.normalize())) {
                throw new RuntimeException("Tidak dapat menyimpan file di luar direktori yang ditentukan");
            }

            // Simpan file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return relative path untuk disimpan di database
            return "/" + uploadDir + "/" + relativePath + "/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage(), e);
        }
    }

    /**
     * Dapatkan Path absolut dari relative path
     */
    public Path getFilePath(String relativePath) {
        // Remove leading slash jika ada
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return Paths.get(relativePath).normalize();
    }

    /**
     * Dapatkan Path absolut untuk video
     */
    public Path getVideoPath(String filename) {
        return videosLocation.resolve(filename).normalize();
    }

    /**
     * Cek apakah file exists
     */
    public boolean fileExists(String relativePath) {
        Path filePath = getFilePath(relativePath);
        return Files.exists(filePath) && Files.isReadable(filePath);
    }

    /**
     * Hapus file
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = getFilePath(relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Gagal menghapus file: " + e.getMessage(), e);
        }
    }

    /**
     * Validasi file video
     */
    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File video tidak boleh kosong");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new RuntimeException("File harus berupa video");
        }

        // Validasi ekstensi
        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename).toLowerCase();
        if (!isValidVideoExtension(extension)) {
            throw new RuntimeException("Format video tidak didukung. Gunakan: mp4, webm, ogg, mov, avi");
        }

        // Validasi ukuran (max 500MB)
        long maxSize = 500 * 1024 * 1024; // 500MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Ukuran video maksimal 500MB");
        }
    }

    /**
     * Validasi file dokumen
     */
    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File dokumen tidak boleh kosong");
        }

        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename).toLowerCase();
        if (!isValidDocumentExtension(extension)) {
            throw new RuntimeException("Format dokumen tidak didukung. Gunakan: pdf, doc, docx, txt");
        }

        // Validasi ukuran (max 50MB)
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Ukuran dokumen maksimal 50MB");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex);
    }

    private boolean isValidVideoExtension(String extension) {
        return extension.equals(".mp4") || 
               extension.equals(".webm") || 
               extension.equals(".ogg") || 
               extension.equals(".mov") || 
               extension.equals(".avi") ||
               extension.equals(".mkv");
    }

    private boolean isValidDocumentExtension(String extension) {
        return extension.equals(".pdf") || 
               extension.equals(".doc") || 
               extension.equals(".docx") || 
               extension.equals(".txt") ||
               extension.equals(".ppt") ||
               extension.equals(".pptx");
    }

    // Getters untuk path
    public Path getRootLocation() {
        return rootLocation;
    }

    public Path getVideosLocation() {
        return videosLocation;
    }

    public Path getThumbnailsLocation() {
        return thumbnailsLocation;
    }

    public Path getDocumentsLocation() {
        return documentsLocation;
    }
}
