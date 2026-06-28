package backend.app.api.controller;

import backend.app.api.DocumentTextExtractor;
import backend.app.api.DocumentUploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class UploadController {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("user.home"), "uploads");

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file);
            Files.createDirectories(UPLOAD_DIR);

            UUID documentId = UUID.randomUUID();
            String originalName = sanitizeFileName(file.getOriginalFilename());
            Path savedFile = UPLOAD_DIR.resolve(documentId + getExtension(originalName));
            file.transferTo(savedFile);

            String text = DocumentTextExtractor.extractText(savedFile);
            List<String> chunks = DocumentTextExtractor.splitIntoChunks(text);

            return ResponseEntity.ok(new DocumentUploadResponse(
                    documentId,
                    originalName,
                    file.getSize(),
                    chunks.size(),
                    chunks
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    private static void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 20 MB");
        }

        String fileName = sanitizeFileName(file.getOriginalFilename());
        String extension = getExtension(fileName);
        if (!extension.equals(".pdf") && !extension.equals(".docx")) {
            throw new IllegalArgumentException("Supported file formats: PDF, DOCX");
        }
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "upload";
        }
        return Paths.get(fileName).getFileName().toString();
    }

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
