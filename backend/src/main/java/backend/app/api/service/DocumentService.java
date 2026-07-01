package backend.app.api.service;

import backend.app.api.DocumentChunk;
import backend.app.api.dto.DocumentResponse;
import backend.app.api.DocumentTextExtractor;
import backend.app.api.dto.DocumentUploadResponse;
import backend.app.api.model.StoredDocument;
import backend.app.api.repository.StoredDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private final StoredDocumentRepository documentRepository;
    private final SearchService searchService;
    private final Path uploadDir;

    public DocumentService(
            StoredDocumentRepository documentRepository,
            SearchService searchService,
            @Value("${storage.upload-dir}") String uploadDir
    ) {
        this.documentRepository = documentRepository;
        this.searchService = searchService;
        this.uploadDir = Paths.get(uploadDir);
    }

    /**
     * Validates, stores and indexes an uploaded document.
     *
     * @param file multipart document (PDF/DOCX)
     * @return upload metadata with generated document UUID
     * @throws IOException when storing or parsing the file fails
     */
    public DocumentUploadResponse upload(MultipartFile file) throws IOException {
        long startedAt = System.nanoTime();
        validateFile(file);
        Files.createDirectories(uploadDir);

        UUID documentId = UUID.randomUUID();
        String originalName = sanitizeFileName(file.getOriginalFilename());
        log.info("Uploading document id={} fileName={} size={}", documentId, originalName, file.getSize());
        Path savedFile = uploadDir.resolve(documentId + getExtension(originalName));
        file.transferTo(savedFile);

        List<DocumentChunk> chunks = DocumentTextExtractor.extractChunks(savedFile);
        if (chunks.isEmpty()) {
            log.warn("No text chunks extracted id={} fileName={}", documentId, originalName);
        }
        StoredDocument document = new StoredDocument(
                documentId,
                originalName,
                file.getSize(),
                chunks.size(),
                "INDEXING",
                Instant.now()
        );
        documentRepository.save(document);

        try {
            searchService.indexChunks(documentId, originalName, chunks);
            document.setStatus("READY");
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("Indexed document id={} fileName={} chunks={} elapsedMs={}", documentId, originalName, chunks.size(), elapsedMillis);
        } catch (RuntimeException e) {
            document.setStatus("ERROR");
            log.error("Document indexing failed id={} fileName={}", documentId, originalName, e);
            throw e;
        } finally {
            documentRepository.save(document);
        }

        return toUploadResponse(document);
    }

    /**
     * Returns uploaded documents sorted by upload date in descending order.
     */
    public List<DocumentResponse> findAll() {
        return documentRepository.findAll().stream()
                .sorted(Comparator.comparing(StoredDocument::getUploadedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns one uploaded document by UUID.
     *
     * @param documentId uploaded document id
     */
    public DocumentResponse findById(UUID documentId) {
        return documentRepository.findById(documentId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    private DocumentUploadResponse toUploadResponse(StoredDocument document) {
        return new DocumentUploadResponse(
                document.getId(),
                document.getFileName(),
                document.getSize(),
                document.getChunkCount(),
                document.getStatus(),
                document.getUploadedAt()
        );
    }

    private DocumentResponse toResponse(StoredDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getSize(),
                document.getChunkCount(),
                document.getStatus(),
                document.getUploadedAt()
        );
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
