package koding_muda_nusantara.koding_muda_belajar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

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

            logger.info("Storage directories initialized at: {}", rootLocation.toAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("Tidak dapat membuat direktori upload", e);
        }
    }

    // ========== UPLOAD METHODS (yang sudah ada) ==========

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
            String storedPath = "/" + uploadDir + "/" + relativePath + "/" + newFilename;
            logger.info("File stored: {} -> {}", originalFilename, storedPath);
            
            return storedPath;

        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage(), e);
        }
    }

    // ========== DELETE METHODS (baru ditambahkan) ==========

    /**
     * Hapus file berdasarkan path yang tersimpan di database
     * 
     * @param relativePath path file seperti "/uploads/videos/course-1/uuid.mp4"
     * @return true jika berhasil, false jika file tidak ditemukan
     */
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            logger.warn("File path kosong, tidak ada yang dihapus");
            return false;
        }

        try {
            Path filePath = getFilePath(relativePath);
            
            // Security check: pastikan file berada di dalam direktori upload
            Path absoluteFilePath = filePath.toAbsolutePath().normalize();
            Path absoluteRootPath = rootLocation.toAbsolutePath().normalize();
            
            if (!absoluteFilePath.startsWith(absoluteRootPath)) {
                logger.error("Percobaan menghapus file di luar direktori upload: {}", relativePath);
                throw new SecurityException("Tidak diizinkan menghapus file di luar direktori upload");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("File berhasil dihapus: {}", relativePath);
                return true;
            } else {
                logger.warn("File tidak ditemukan: {}", relativePath);
                return false;
            }

        } catch (IOException e) {
            logger.error("Gagal menghapus file {}: {}", relativePath, e.getMessage());
            throw new RuntimeException("Gagal menghapus file: " + e.getMessage(), e);
        }
    }

    /**
     * Hapus folder course beserta semua isinya (video dan dokumen)
     * 
     * @param courseId ID course
     */
    public void deleteCourseFolder(Integer courseId) {
        String courseFolderName = "course-" + courseId;
        
        // Hapus folder video course
        Path videoFolder = videosLocation.resolve(courseFolderName);
        deleteDirectoryRecursively(videoFolder);
        
        // Hapus folder dokumen course
        Path docFolder = documentsLocation.resolve(courseFolderName);
        deleteDirectoryRecursively(docFolder);
        
        logger.info("Folder course {} berhasil dihapus", courseId);
    }

    /**
     * Hapus direktori beserta semua isinya secara rekursif
     */
    private void deleteDirectoryRecursively(Path directory) {
        if (!Files.exists(directory)) {
            logger.debug("Direktori tidak ada, skip: {}", directory);
            return;
        }

        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.debug("Deleted: {}", path);
                    } catch (IOException e) {
                        logger.warn("Gagal menghapus: {} - {}", path, e.getMessage());
                    }
                });
        } catch (IOException e) {
            logger.error("Gagal menghapus direktori {}: {}", directory, e.getMessage());
        }
    }

    /**
     * Hapus semua file dalam list
     * 
     * @param filePaths list path file
     * @return jumlah file yang berhasil dihapus
     */
    public int deleteFiles(List<String> filePaths) {
        int deletedCount = 0;
        for (String filePath : filePaths) {
            try {
                if (deleteFile(filePath)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                logger.warn("Gagal menghapus file {}: {}", filePath, e.getMessage());
            }
        }
        logger.info("Berhasil menghapus {} dari {} file", deletedCount, filePaths.size());
        return deletedCount;
    }

    // ========== UTILITY METHODS ==========

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
     * Dapatkan ukuran file dalam bytes
     */
    public long getFileSize(String relativePath) {
        try {
            Path filePath = getFilePath(relativePath);
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
        } catch (IOException e) {
            logger.warn("Gagal mendapatkan ukuran file: {}", relativePath);
        }
        return 0;
    }

    /**
     * Hitung total ukuran folder course
     */
    public long getCourseFolderSize(Integer courseId) {
        String courseFolderName = "course-" + courseId;
        long totalSize = 0;
        
        totalSize += getFolderSize(videosLocation.resolve(courseFolderName));
        totalSize += getFolderSize(documentsLocation.resolve(courseFolderName));
        
        return totalSize;
    }

    private long getFolderSize(Path folder) {
        if (!Files.exists(folder)) {
            return 0;
        }

        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.filter(Files::isRegularFile)
                      .mapToLong(path -> {
                          try {
                              return Files.size(path);
                          } catch (IOException e) {
                              return 0;
                          }
                      })
                      .sum();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Hitung jumlah file dalam folder course
     */
    public int getCourseFileCount(Integer courseId) {
        String courseFolderName = "course-" + courseId;
        int count = 0;
        
        count += getFileCount(videosLocation.resolve(courseFolderName));
        count += getFileCount(documentsLocation.resolve(courseFolderName));
        
        return count;
    }

    private int getFileCount(Path folder) {
        if (!Files.exists(folder)) {
            return 0;
        }

        try (Stream<Path> walk = Files.walk(folder)) {
            return (int) walk.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            return 0;
        }
    }

    // ========== VALIDATION METHODS ==========

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

    // ========== GETTERS ==========

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