package backend.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Document upload result")
public record DocumentUploadResponse(
        @Schema(description = "Generated document UUID", example = "2a41aa2f-5482-4b59-a6f0-61006b500ea3")
        UUID documentId,
        @Schema(description = "Original file name", example = "lecture-ml.pdf")
        String fileName,
        @Schema(description = "File size in bytes", example = "1048576")
        long size,
        @Schema(description = "Number of indexed chunks", example = "14")
        int chunkCount,
        @Schema(description = "Current processing status", example = "READY")
        String status,
        @Schema(description = "Upload timestamp", example = "2026-07-01T12:00:00Z")
        Instant uploadedAt
) {
}
