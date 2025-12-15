package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.model.Lecturer;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.FileStorageService;
import koding_muda_nusantara.koding_muda_belajar.service.VideoStreamingService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private VideoStreamingService videoStreamingService;

    // ==================== VIDEO STREAMING ====================

    /**
     * Streaming video dengan Range Request support
     * URL: /files/stream/videos/{filename}
     */
    @GetMapping("/stream/videos/**")
    public ResponseEntity<Resource> streamVideo(
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        // Ekstrak path dari URL
        String requestUri = request.getRequestURI();
        String relativePath = requestUri.replace("/files/stream/", "");
        
        // Tambahkan prefix uploads jika belum ada
        if (!relativePath.startsWith("uploads/")) {
            relativePath = "uploads/" + relativePath;
        }

        return videoStreamingService.streamVideo(relativePath, rangeHeader);
    }

    /**
     * Streaming video berdasarkan path lengkap dari database
     * URL: /files/stream?path=/uploads/videos/xxx.mp4
     */
    @GetMapping("/stream")
    public ResponseEntity<Resource> streamVideoByPath(
            @RequestParam String path,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) {
        return videoStreamingService.streamVideo(path, rangeHeader);
    }

    // ==================== FILE DOWNLOAD ====================

    /**
     * Download file (thumbnail, dokumen, dll)
     * URL: /files/download?path=/uploads/documents/xxx.pdf
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        try {
            Path filePath = fileStorageService.getFilePath(path);

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String filename = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Serve file secara inline (untuk thumbnail, image, dll)
     * URL: /files/view?path=/uploads/thumbnails/xxx.jpg
     */
    @GetMapping("/view")
    public ResponseEntity<Resource> viewFile(@RequestParam String path) {
        try {
            Path filePath = fileStorageService.getFilePath(path);

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Streaming PDF untuk PDF.js viewer
     * URL: /files/stream/pdf?path=/uploads/documents/xxx.pdf
     * 
     * Endpoint ini khusus untuk PDF dengan header yang tepat agar
     * PDF.js bisa membaca file tanpa CORS issues
     */
    @GetMapping("/stream/pdf")
    public ResponseEntity<Resource> streamPdf(@RequestParam String path) {
        try {
            // Log untuk debugging
            System.out.println("PDF Stream request for path: " + path);
            
            Path filePath = fileStorageService.getFilePath(path);
            System.out.println("Resolved file path: " + filePath.toAbsolutePath());

            if (!Files.exists(filePath)) {
                System.out.println("PDF file not found: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            if (!Files.isReadable(filePath)) {
                System.out.println("PDF file not readable: " + filePath.toAbsolutePath());
                return ResponseEntity.status(403).build();
            }

            // Validasi bahwa file adalah PDF
            String filename = filePath.getFileName().toString().toLowerCase();
            if (!filename.endsWith(".pdf")) {
                System.out.println("File is not PDF: " + filename);
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            long fileSize = Files.size(filePath);
            
            System.out.println("Serving PDF, size: " + fileSize + " bytes");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(fileSize)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName().toString() + "\"")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(resource);

        } catch (IOException e) {
            System.out.println("Error streaming PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Streaming PDF dengan Range Request support (untuk file PDF besar)
     * URL: /files/stream/pdf/range?path=/uploads/documents/xxx.pdf
     */
    @GetMapping("/stream/pdf/range")
    public ResponseEntity<Resource> streamPdfWithRange(
            @RequestParam String path,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) {
        try {
            Path filePath = fileStorageService.getFilePath(path);

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // Validasi bahwa file adalah PDF
            String filename = filePath.getFileName().toString().toLowerCase();
            if (!filename.endsWith(".pdf")) {
                return ResponseEntity.badRequest().build();
            }

            long fileSize = Files.size(filePath);
            Resource resource = new UrlResource(filePath.toUri());

            // Jika tidak ada Range header, kirim seluruh file
            if (rangeHeader == null || rangeHeader.isEmpty()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .contentLength(fileSize)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName().toString() + "\"")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(resource);
            }

            // Parse Range header
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty() 
                    ? Long.parseLong(ranges[1]) 
                    : fileSize - 1;

            if (start >= fileSize) {
                return ResponseEntity.status(416).build(); // Range Not Satisfiable
            }

            long contentLength = end - start + 1;

            // Buat partial resource
            byte[] data = Files.readAllBytes(filePath);
            byte[] partialData = new byte[(int) contentLength];
            System.arraycopy(data, (int) start, partialData, 0, (int) contentLength);

            return ResponseEntity.status(206)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(contentLength)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(new org.springframework.core.io.ByteArrayResource(partialData));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== FILE UPLOAD ====================

    /**
     * Upload video
     * Endpoint khusus lecturer
     */
    @PostMapping("/upload/video")
    @ResponseBody
    public Map<String, Object> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        // Cek authentication
        if (!isLecturer(session)) {
            response.put("success", false);
            response.put("message", "Unauthorized: Hanya lecturer yang dapat upload video");
            return response;
        }

        try {
            String filePath;
            if (courseId != null) {
                filePath = fileStorageService.storeVideo(file, courseId);
            } else {
                filePath = fileStorageService.storeVideo(file);
            }

            response.put("success", true);
            response.put("message", "Video berhasil diupload");
            response.put("data", Map.of(
                "path", filePath,
                "streamUrl", "/files/stream?path=" + filePath,
                "filename", file.getOriginalFilename(),
                "size", file.getSize()
            ));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal upload video: " + e.getMessage());
        }

        return response;
    }

    /**
     * Upload thumbnail
     */
    @PostMapping("/upload/thumbnail")
    @ResponseBody
    public Map<String, Object> uploadThumbnail(
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        if (!isLecturer(session)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            String filePath = fileStorageService.storeThumbnail(file);

            response.put("success", true);
            response.put("message", "Thumbnail berhasil diupload");
            response.put("data", Map.of(
                "path", filePath,
                "viewUrl", "/files/view?path=" + filePath
            ));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal upload thumbnail: " + e.getMessage());
        }

        return response;
    }

    /**
     * Upload dokumen (PDF, dll)
     */
    @PostMapping("/upload/document")
    @ResponseBody
    public Map<String, Object> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        if (!isLecturer(session)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            String filePath;
            if (courseId != null) {
                filePath = fileStorageService.storeDocument(file, courseId);
            } else {
                filePath = fileStorageService.storeDocument(file);
            }

            response.put("success", true);
            response.put("message", "Dokumen berhasil diupload");
            response.put("data", Map.of(
                "path", filePath,
                "downloadUrl", "/files/download?path=" + filePath,
                "viewUrl", "/files/view?path=" + filePath,
                "filename", file.getOriginalFilename(),
                "size", file.getSize()
            ));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal upload dokumen: " + e.getMessage());
        }

        return response;
    }

    /**
     * Upload file berdasarkan tipe konten
     * Endpoint generic untuk berbagai tipe file
     */
    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            HttpSession session
    ) {
        return switch (type.toLowerCase()) {
            case "video" -> uploadVideo(file, courseId, session);
            case "thumbnail" -> uploadThumbnail(file, session);
            case "document", "pdf" -> uploadDocument(file, courseId, session);
            default -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Tipe file tidak valid");
                yield response;
            }
        };
    }

    // ==================== TEMP UPLOAD (untuk lesson content) ====================

    @Autowired
    private koding_muda_nusantara.koding_muda_belajar.service.TempFileService tempFileService;

    /**
     * Upload file ke folder temp lecturer
     * URL: /files/upload/temp/{type}
     * Type: video, pdf
     */
    @PostMapping("/upload/temp/{type}")
    @ResponseBody
    public Map<String, Object> uploadToTemp(
            @PathVariable String type,
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        // Cek authentication
        User user = (User) session.getAttribute("user");
        if (!(user instanceof Lecturer)) {
            response.put("success", false);
            response.put("message", "Unauthorized: Hanya lecturer yang dapat upload");
            return response;
        }

        Lecturer lecturer = (Lecturer) user;

        try {
            // Validasi type
            if (!type.equals("video") && !type.equals("pdf")) {
                response.put("success", false);
                response.put("message", "Tipe file tidak valid. Gunakan: video atau pdf");
                return response;
            }

            // Validasi file type
            if (type.equals("video") && !file.getContentType().startsWith("video/")) {
                response.put("success", false);
                response.put("message", "File harus berupa video");
                return response;
            }
            if (type.equals("pdf") && !file.getContentType().equals("application/pdf")) {
                response.put("success", false);
                response.put("message", "File harus berupa PDF");
                return response;
            }

            // Validasi ukuran
            long maxSize = type.equals("video") ? 500 * 1024 * 1024 : 50 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                response.put("success", false);
                response.put("message", "Ukuran file melebihi batas maksimal");
                return response;
            }

            // Upload ke temp folder
            String filePath = tempFileService.uploadToTemp(file, lecturer.getUserId(), type);

            response.put("success", true);
            response.put("message", "File berhasil diupload ke temp");
            response.put("data", Map.of(
                "path", filePath,
                "filename", file.getOriginalFilename(),
                "size", file.getSize(),
                "type", type
            ));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal upload: " + e.getMessage());
        }

        return response;
    }

    // ==================== DELETE FILE ====================

    /**
     * Hapus file
     */
    @DeleteMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteFile(
            @RequestParam String path,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        if (!isLecturer(session)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            boolean deleted = fileStorageService.deleteFile(path);

            if (deleted) {
                response.put("success", true);
                response.put("message", "File berhasil dihapus");
            } else {
                response.put("success", false);
                response.put("message", "File tidak ditemukan");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal menghapus file: " + e.getMessage());
        }

        return response;
    }

    // ==================== HELPER METHODS ====================

    private boolean isLecturer(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user instanceof Lecturer;
    }
}