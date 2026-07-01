package backend.app.api;

public record DocumentChunk(
        int chunkIndex,
        int pageNumber,
        String text
) {
}
