package backend.app.api;

import java.util.List;
import java.util.UUID;

public record DocumentUploadResponse(
        UUID documentId,
        String fileName,
        long size,
        int chunkCount,
        List<String> chunks
) {
}
