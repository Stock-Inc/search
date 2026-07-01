package backend.app.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentTextExtractorTests {

    @Test
    void extractsDocxChunksWithPageOne() throws IOException {
        var chunks = DocumentTextExtractor.extractChunks(Path.of("src/test/resources/fixtures/correct.docx"));

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.stream().allMatch(chunk -> chunk.pageNumber() == 1));
    }

    @Test
    void extractsPdfChunksWithPageNumbers() throws IOException {
        var chunks = DocumentTextExtractor.extractChunks(Path.of("src/test/resources/fixtures/correct.pdf"));

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.stream().allMatch(chunk -> chunk.pageNumber() >= 1));
    }
}
