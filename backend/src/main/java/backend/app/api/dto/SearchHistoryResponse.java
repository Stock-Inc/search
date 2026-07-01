package backend.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Saved search query entry")
public record SearchHistoryResponse(
        @Schema(description = "History entry ID", example = "15")
        Long id,
        @Schema(description = "Original query string", example = "машинное обучение")
        String query,
        @Schema(description = "Number of results returned for the query", example = "8")
        int resultCount,
        @Schema(description = "Query timestamp", example = "2026-07-01T12:00:00Z")
        Instant createdAt
) {
}
