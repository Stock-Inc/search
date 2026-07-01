package backend.app.api.controller;

import backend.app.api.dto.DocumentResponse;
import backend.app.api.dto.DocumentUploadResponse;
import backend.app.api.dto.ErrorResponse;
import backend.app.api.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Upload and list indexed documents")
@RequestMapping("/api/v1/documents")
public class UploadController {

    private final DocumentService documentService;

    @Operation(summary = "Upload PDF or DOCX document")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document uploaded and indexed"),
            @ApiResponse(responseCode = "400", description = "Invalid upload request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentUploadResponse handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        return documentService.upload(file);
    }

    @Operation(summary = "List uploaded documents")
    @ApiResponse(responseCode = "200", description = "List of uploaded documents")
    @GetMapping
    public List<DocumentResponse> documents() {
        return documentService.findAll();
    }

    @Operation(summary = "Get uploaded document by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document returned"),
            @ApiResponse(responseCode = "404", description = "Document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{documentId}")
    public DocumentResponse document(@PathVariable UUID documentId) {
        return documentService.findById(documentId);
    }
}
