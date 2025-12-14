package koding_muda_nusantara.koding_muda_belajar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class VideoStreamingService {

    @Autowired
    private FileStorageService fileStorageService;

    // Default chunk size 1MB
    private static final long DEFAULT_CHUNK_SIZE = 1024 * 1024;

    /**
     * Streaming video dengan support Range Request
     * 
     * @param relativePath Path relatif video (dari database)
     * @param rangeHeader  Header Range dari request (contoh: "bytes=0-1000000")
     * @return ResponseEntity dengan partial content atau full content
     */
    public ResponseEntity<Resource> streamVideo(String relativePath, String rangeHeader) {
        try {
            Path videoPath = fileStorageService.getFilePath(relativePath);

            // Validasi file exists
            if (!Files.exists(videoPath) || !Files.isReadable(videoPath)) {
                return ResponseEntity.notFound().build();
            }

            long fileSize = Files.size(videoPath);
            String contentType = getContentType(videoPath);

            // Jika tidak ada Range header, return full file
            if (rangeHeader == null || rangeHeader.isEmpty()) {
                return getFullVideo(videoPath, fileSize, contentType);
            }

            // Parse Range header dan return partial content
            return getPartialVideo(videoPath, rangeHeader, fileSize, contentType);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Return full video (tanpa Range Request)
     */
    private ResponseEntity<Resource> getFullVideo(Path videoPath, long fileSize, String contentType) 
            throws IOException {
        
        Resource resource = new UrlResource(videoPath.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }

    /**
     * Return partial video (dengan Range Request)
     */
    private ResponseEntity<Resource> getPartialVideo(Path videoPath, String rangeHeader, 
            long fileSize, String contentType) throws IOException {

        // Parse range header: "bytes=start-end"
        RangeInfo range = parseRangeHeader(rangeHeader, fileSize);

        if (range == null || !range.isValid(fileSize)) {
            // Range tidak valid
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                    .build();
        }

        long contentLength = range.end - range.start + 1;

        // Buat resource untuk partial content
        Resource resource = new PartialContentResource(videoPath, range.start, range.end);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .header(HttpHeaders.CONTENT_RANGE, 
                        String.format("bytes %d-%d/%d", range.start, range.end, fileSize))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }

    /**
     * Parse Range header
     * Format: "bytes=start-end" atau "bytes=start-" atau "bytes=-end"
     */
    private RangeInfo parseRangeHeader(String rangeHeader, long fileSize) {
        try {
            if (!rangeHeader.startsWith("bytes=")) {
                return null;
            }

            String range = rangeHeader.substring(6); // Remove "bytes="
            String[] parts = range.split("-");

            long start;
            long end;

            if (range.startsWith("-")) {
                // Format: "-end" (last n bytes)
                long lastBytes = Long.parseLong(parts[1]);
                start = fileSize - lastBytes;
                end = fileSize - 1;
            } else if (range.endsWith("-") || parts.length == 1) {
                // Format: "start-" (from start to end)
                start = Long.parseLong(parts[0]);
                // Batasi chunk size untuk efisiensi
                end = Math.min(start + DEFAULT_CHUNK_SIZE - 1, fileSize - 1);
            } else {
                // Format: "start-end"
                start = Long.parseLong(parts[0]);
                end = Long.parseLong(parts[1]);
                // Batasi chunk size
                end = Math.min(end, start + DEFAULT_CHUNK_SIZE - 1);
            }

            // Pastikan end tidak melebihi file size
            end = Math.min(end, fileSize - 1);

            return new RangeInfo(start, end);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Dapatkan content type berdasarkan file extension
     */
    private String getContentType(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        
        if (filename.endsWith(".mp4")) {
            return "video/mp4";
        } else if (filename.endsWith(".webm")) {
            return "video/webm";
        } else if (filename.endsWith(".ogg") || filename.endsWith(".ogv")) {
            return "video/ogg";
        } else if (filename.endsWith(".mov")) {
            return "video/quicktime";
        } else if (filename.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (filename.endsWith(".mkv")) {
            return "video/x-matroska";
        }
        
        return "application/octet-stream";
    }

    /**
     * Inner class untuk menyimpan informasi range
     */
    private static class RangeInfo {
        final long start;
        final long end;

        RangeInfo(long start, long end) {
            this.start = start;
            this.end = end;
        }

        boolean isValid(long fileSize) {
            return start >= 0 && start < fileSize && end >= start && end < fileSize;
        }
    }

    /**
     * Custom Resource untuk partial content
     */
    private static class PartialContentResource extends UrlResource {
        private final long start;
        private final long end;
        private final Path path;

        public PartialContentResource(Path path, long start, long end) throws IOException {
            super(path.toUri());
            this.path = path;
            this.start = start;
            this.end = end;
        }

        @Override
        public long contentLength() {
            return end - start + 1;
        }

        @Override
        public java.io.InputStream getInputStream() throws IOException {
            return new PartialInputStream(path, start, end);
        }
    }

    /**
     * Custom InputStream untuk membaca partial file
     */
    private static class PartialInputStream extends java.io.InputStream {
        private final RandomAccessFile file;
        private final long end;
        private long position;

        public PartialInputStream(Path path, long start, long end) throws IOException {
            this.file = new RandomAccessFile(path.toFile(), "r");
            this.file.seek(start);
            this.position = start;
            this.end = end;
        }

        @Override
        public int read() throws IOException {
            if (position > end) {
                return -1;
            }
            position++;
            return file.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (position > end) {
                return -1;
            }
            
            // Batasi jumlah bytes yang dibaca
            long remaining = end - position + 1;
            int toRead = (int) Math.min(len, remaining);
            
            int bytesRead = file.read(b, off, toRead);
            if (bytesRead > 0) {
                position += bytesRead;
            }
            return bytesRead;
        }

        @Override
        public void close() throws IOException {
            file.close();
        }

        @Override
        public int available() throws IOException {
            long remaining = end - position + 1;
            return (int) Math.min(remaining, Integer.MAX_VALUE);
        }
    }
}
