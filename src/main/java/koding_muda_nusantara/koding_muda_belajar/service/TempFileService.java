package koding_muda_nusantara.koding_muda_belajar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TempFileService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final String TEMP_FOLDER = "temp";

    /**
     * Upload file ke folder temp lecturer
     * Path: uploads/temp/lecturer-{id}/{uuid}.{ext}
     */
    public String uploadToTemp(MultipartFile file, Integer lecturerId, String type) throws IOException {
        // Validasi file
        if (file.isEmpty()) {
            throw new IOException("File kosong");
        }

        // Buat folder temp untuk lecturer
        String tempPath = TEMP_FOLDER + "/lecturer-" + lecturerId;
        Path tempDir = Paths.get(uploadDir, tempPath);
        
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;

        // Simpan file
        Path filePath = tempDir.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path
        return "/" + uploadDir + "/" + tempPath + "/" + newFilename;
    }

    /**
     * Pindahkan file dari temp ke folder permanent
     * Dari: uploads/temp/lecturer-{id}/{filename}
     * Ke: uploads/{type}/course-{courseId}/{filename}
     */
    public String moveFromTemp(String tempPath, Integer courseId, String type) throws IOException {
        if (tempPath == null || tempPath.isEmpty()) {
            return null;
        }
//        if (!(tempPath.contains("temp"))){
//            return tempPath;
//        }
        // Parse temp path
        Path sourcePath = Paths.get(tempPath.startsWith("/") ? tempPath.substring(1) : tempPath);
        
        if (!Files.exists(sourcePath)) {
            throw new IOException("File tidak ditemukan: " + tempPath);
        }

        // Tentukan folder tujuan berdasarkan type
        String targetFolder;
        switch (type) {
            case "video":
                targetFolder = "videos";
                break;
            case "pdf":
                targetFolder = "documents";
                break;
            default:
                targetFolder = "files";
        }

        // Buat folder tujuan
        String targetPath = targetFolder + "/course-" + courseId;
        Path targetDir = Paths.get(uploadDir, targetPath);
        
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Pindahkan file
        String filename = sourcePath.getFileName().toString();
        Path destinationPath = targetDir.resolve(filename);
        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Return new path
        return "/" + uploadDir + "/" + targetPath + "/" + filename;
    }

    /**
     * Pindahkan semua file dari temp lecturer ke folder course
     * Return list of new paths
     */
    public void moveAllFromTemp(Integer lecturerId, Integer courseId, List<String> tempPaths) throws IOException {
        for (String tempPath : tempPaths) {
            if (tempPath != null && !tempPath.isEmpty() && tempPath.contains("/temp/")) {
                // Detect type from path or extension
                String type = detectTypeFromPath(tempPath);
                moveFromTemp(tempPath, courseId, type);
            }
        }
    }

    /**
     * Hapus folder temp lecturer
     */
    public void cleanupTempFolder(Integer lecturerId) throws IOException {
        Path tempDir = Paths.get(uploadDir, TEMP_FOLDER, "lecturer-" + lecturerId);
        
        if (Files.exists(tempDir)) {
            // Hapus semua file di folder
            try (Stream<Path> paths = Files.walk(tempDir)) {
                paths.sorted((a, b) -> b.compareTo(a)) // Reverse order untuk hapus file dulu
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             // Log error tapi lanjutkan
                             System.err.println("Gagal hapus: " + path);
                         }
                     });
            }
        }
    }

    /**
     * Dapatkan semua file di temp folder lecturer
     */
    public List<String> getTempFiles(Integer lecturerId) throws IOException {
        Path tempDir = Paths.get(uploadDir, TEMP_FOLDER, "lecturer-" + lecturerId);
        
        if (!Files.exists(tempDir)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(tempDir)) {
            return paths.filter(Files::isRegularFile)
                       .map(path -> "/" + path.toString().replace("\\", "/"))
                       .collect(Collectors.toList());
        }
    }

    /**
     * Detect file type from path/extension
     */
    private String detectTypeFromPath(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mkv") || 
            lower.endsWith(".avi") || lower.endsWith(".mov")) {
            return "video";
        } else if (lower.endsWith(".pdf")) {
            return "pdf";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
                   lower.endsWith(".png") || lower.endsWith(".gif")) {
            return "image";
        }
        return "file";
    }

    /**
     * Cek apakah path adalah temp path
     */
    public boolean isTempPath(String path) {
        return path != null && path.contains("/temp/");
    }

    /**
     * Convert temp path ke permanent path
     */
    public String convertTempToPermanentPath(String tempPath, Integer courseId, String type) {
        if (tempPath == null || !isTempPath(tempPath)) {
            return tempPath;
        }

        String filename = Paths.get(tempPath).getFileName().toString();
        String targetFolder;
        
        switch (type) {
            case "video":
                targetFolder = "videos";
                break;
            case "pdf":
                targetFolder = "documents";
                break;
            default:
                targetFolder = "files";
        }

        return "/" + uploadDir + "/" + targetFolder + "/course-" + courseId + "/" + filename;
    }
}
