package tn.cyberious.compta.document.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.document.dto.DocumentVersionResponse;
import tn.cyberious.compta.document.dto.DocumentVersionUploadRequest;
import tn.cyberious.compta.document.service.DocumentVersionService;

@RestController
@RequestMapping("/api/documents/{documentId}/versions")
@RequiredArgsConstructor
@Tag(name = "Document Versions", description = "Document version management endpoints")
public class DocumentVersionController {

  private final DocumentVersionService documentVersionService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload new version", description = "Uploads a new version of a document")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Version uploaded successfully"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<DocumentVersionResponse> uploadVersion(
      @Parameter(description = "Document ID") @PathVariable Long documentId,
      @Parameter(description = "New file version") @RequestPart("file") MultipartFile file,
      @Parameter(description = "Version metadata")
          @RequestPart(value = "data", required = false)
          @Valid
          DocumentVersionUploadRequest request,
      @Parameter(description = "User ID", hidden = true)
          @RequestHeader(value = "X-User-Id", defaultValue = "anonymous")
          String userId) {
    if (request == null) {
      request = new DocumentVersionUploadRequest();
    }
    DocumentVersionResponse response =
        documentVersionService.uploadNewVersion(documentId, file, request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @Operation(summary = "Get all versions", description = "Returns all versions of a document")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of versions"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<List<DocumentVersionResponse>> getVersions(
      @Parameter(description = "Document ID") @PathVariable Long documentId) {
    List<DocumentVersionResponse> response = documentVersionService.getVersions(documentId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{versionNumber}")
  @Operation(
      summary = "Get specific version",
      description = "Returns details of a specific version")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Version details"),
    @ApiResponse(responseCode = "404", description = "Document or version not found")
  })
  public ResponseEntity<DocumentVersionResponse> getVersion(
      @Parameter(description = "Document ID") @PathVariable Long documentId,
      @Parameter(description = "Version number") @PathVariable Integer versionNumber) {
    DocumentVersionResponse response = documentVersionService.getVersion(documentId, versionNumber);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{versionNumber}/download")
  @Operation(
      summary = "Download version",
      description = "Downloads a specific version of the document")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "File content",
        content = @Content(schema = @Schema(type = "string", format = "binary"))),
    @ApiResponse(responseCode = "404", description = "Document or version not found")
  })
  public ResponseEntity<byte[]> downloadVersion(
      @Parameter(description = "Document ID") @PathVariable Long documentId,
      @Parameter(description = "Version number") @PathVariable Integer versionNumber) {
    DocumentVersionResponse version = documentVersionService.getVersion(documentId, versionNumber);
    byte[] content = documentVersionService.downloadVersion(documentId, versionNumber);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", version.getFileName());
    headers.setContentLength(content.length);

    return ResponseEntity.ok().headers(headers).body(content);
  }

  @GetMapping("/{versionNumber}/download-url")
  @Operation(
      summary = "Get version download URL",
      description = "Returns a presigned URL for downloading a specific version")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Download URL"),
    @ApiResponse(responseCode = "404", description = "Document or version not found")
  })
  public ResponseEntity<Map<String, String>> getVersionDownloadUrl(
      @Parameter(description = "Document ID") @PathVariable Long documentId,
      @Parameter(description = "Version number") @PathVariable Integer versionNumber) {
    String url = documentVersionService.getVersionDownloadUrl(documentId, versionNumber);
    return ResponseEntity.ok(Map.of("url", url));
  }
}
