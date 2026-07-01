package backend.app.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BackendApplicationTests {

    @Test
    void splitIntoChunksUsesExpectedOverlap() {
        String text = "a".repeat(1100);

        var chunks = DocumentTextExtractor.splitIntoChunks(text);

        assertEquals(2, chunks.size());
        assertEquals(1000, chunks.getFirst().length());
        assertEquals(200, chunks.getLast().length());
    }

    @Test
    void splitIntoChunksKeepsHundredCharacterOverlap() {
        String text = "0123456789".repeat(250);

        var chunks = DocumentTextExtractor.splitIntoChunks(text);

        assertEquals(3, chunks.size());
        String firstTail = chunks.get(0).substring(900);
        String secondHead = chunks.get(1).substring(0, 100);
        assertArrayEquals(firstTail.toCharArray(), secondHead.toCharArray());
    }
}
