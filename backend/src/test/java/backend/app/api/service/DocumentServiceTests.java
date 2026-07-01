package backend.app.api.service;

import backend.app.api.dto.DocumentUploadResponse;
import backend.app.api.repository.StoredDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentServiceTests {

    @TempDir
    Path tempDir;

    @Test
    void uploadRejectsUnsupportedExtension() {
        DocumentService service = service();
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "test".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.upload(file));

        assertEquals("Supported file formats: PDF, DOCX", exception.getMessage());
    }

    @Test
    void uploadRejectsEmptyFile() {
        DocumentService service = service();
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.upload(file));

        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void uploadRejectsTooLargeFile() {
        DocumentService service = service();
        byte[] oversized = new byte[20 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", oversized);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.upload(file));

        assertEquals("File size must not exceed 20 MB", exception.getMessage());
    }

    @Test
    void uploadDocxIndexesChunksAndReturnsReadyStatus() throws Exception {
        StoredDocumentRepository repository = mock(StoredDocumentRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        SearchService searchService = mock(SearchService.class);
        DocumentService service = new DocumentService(repository, searchService, tempDir.toString());

        byte[] content = Files.readAllBytes(Path.of("src", "test", "resources", "fixtures", "correct.docx"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "correct.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                content
        );

        DocumentUploadResponse response = service.upload(file);

        assertEquals("READY", response.status());
        assertTrue(response.chunkCount() > 0);
        verify(searchService).indexChunks(eq(response.documentId()), eq("correct.docx"), any());
    }

    private DocumentService service() {
        StoredDocumentRepository repository = mock(StoredDocumentRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        SearchService searchService = mock(SearchService.class);
        return new DocumentService(repository, searchService, tempDir.toString());
    }
}
