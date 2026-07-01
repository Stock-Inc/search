package backend.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API error response")
public record ErrorResponse(
        @Schema(example = "2026-07-01T12:00:00Z") Instant timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Supported file formats: PDF, DOCX") String message,
        @Schema(example = "/api/v1/documents/upload") String path
) {
}
