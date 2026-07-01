package backend.app.api.controller;

import backend.app.api.dto.ErrorResponse;
import backend.app.api.dto.SearchHistoryResponse;
import backend.app.api.dto.SearchResult;
import backend.app.api.service.SearchService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text search in indexed document chunks")
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Search document chunks", description = "Performs full-text search over indexed chunks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed"),
            @ApiResponse(responseCode = "400", description = "Invalid search request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @Timed(value = "app.search.requests", description = "Search request latency and count")
    public List<SearchResult> search(
            @Parameter(description = "Search query", example = "искусственный интеллект")
            @RequestParam("q") @NotBlank String query
    ) throws Exception {
        return searchService.search(query, 0, 10);
    }

    @Operation(summary = "List search query history", description = "Returns latest 50 queries with result counts")
    @ApiResponse(responseCode = "200", description = "History returned")
    @GetMapping("/history")
    @Timed(value = "app.search.history.requests", description = "Search history request latency and count")
    public List<SearchHistoryResponse> history() {
        return searchService.history();
    }
}
