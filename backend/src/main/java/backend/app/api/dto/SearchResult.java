package backend.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single search hit from document chunks")
public record SearchResult(
        @Schema(description = "Unique chunk identifier", example = "2a41aa2f-5482-4b59-a6f0-61006b500ea3-3")
        String chunk_id,
        @Schema(description = "Original uploaded file name", example = "lecture-ml.pdf")
        String file_name,
        @Schema(description = "Page number for PDF documents, always 1 for DOCX", example = "7")
        int page,
        @Schema(description = "Matching fragment text (may contain <mark>highlighted</mark> terms)")
        String text,
        @Schema(description = "Relevance score from Elasticsearch", example = "12.734")
        double score
) {
}
