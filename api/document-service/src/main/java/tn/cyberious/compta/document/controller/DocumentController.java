package tn.cyberious.compta.document.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import tn.cyberious.compta.document.dto.DocumentResponse;
import tn.cyberious.compta.document.dto.DocumentSearchRequest;
import tn.cyberious.compta.document.dto.DocumentUpdateRequest;
import tn.cyberious.compta.document.dto.DocumentUploadRequest;
import tn.cyberious.compta.document.dto.MetadataRequest;
import tn.cyberious.compta.document.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management endpoints")
public class DocumentController {

  private final DocumentService documentService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload a document", description = "Uploads a new document to the storage")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<DocumentResponse> upload(
      @Parameter(description = "File to upload") @RequestPart("file") MultipartFile file,
      @Parameter(description = "Document metadata") @RequestPart("data") @Valid DocumentUploadRequest request,
      @Parameter(description = "User ID", hidden = true) @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId) {
    DocumentResponse response = documentService.upload(file, request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update document metadata", description = "Updates document metadata (not the file)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Document updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<DocumentResponse> update(
      @Parameter(description = "Document ID") @PathVariable Long id,
      @Valid @RequestBody DocumentUpdateRequest request) {
    DocumentResponse response = documentService.update(id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a document", description = "Deletes a document and its file from storage")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<Void> delete(@Parameter(description = "Document ID") @PathVariable Long id) {
    documentService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get document by ID", description = "Returns document details including tags and metadata")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Document found"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<DocumentResponse> getById(
      @Parameter(description = "Document ID") @PathVariable Long id) {
    DocumentResponse response = documentService.getById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "Get all documents", description = "Returns all documents")
  @ApiResponse(responseCode = "200", description = "List of documents")
  public ResponseEntity<List<DocumentResponse>> getAll() {
    List<DocumentResponse> response = documentService.getAll();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/my")
  @Operation(summary = "Get my documents", description = "Returns documents uploaded by the current user")
  @ApiResponse(responseCode = "200", description = "List of user's documents")
  public ResponseEntity<List<DocumentResponse>> getMyDocuments(
      @Parameter(description = "User ID", hidden = true) @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId) {
    List<DocumentResponse> response = documentService.getByUploadedBy(userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get documents by category", description = "Returns all documents in a category")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of documents"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<List<DocumentResponse>> getByCategory(
      @Parameter(description = "Category ID") @PathVariable Long categoryId) {
    List<DocumentResponse> response = documentService.getByCategoryId(categoryId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/public")
  @Operation(summary = "Get public documents", description = "Returns all public documents")
  @ApiResponse(responseCode = "200", description = "List of public documents")
  public ResponseEntity<List<DocumentResponse>> getPublic() {
    List<DocumentResponse> response = documentService.getPublic();
    return ResponseEntity.ok(response);
  }

  @PostMapping("/search")
  @Operation(summary = "Search documents", description = "Searches documents by various criteria")
  @ApiResponse(responseCode = "200", description = "Search results")
  public ResponseEntity<List<DocumentResponse>> search(@RequestBody DocumentSearchRequest request) {
    List<DocumentResponse> response = documentService.search(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/search")
  @Operation(summary = "Search documents (GET)", description = "Searches documents by query parameters")
  @ApiResponse(responseCode = "200", description = "Search results")
  public ResponseEntity<List<DocumentResponse>> searchGet(
      @Parameter(description = "Search query") @RequestParam(required = false) String query,
      @Parameter(description = "Category ID") @RequestParam(required = false) Long categoryId,
      @Parameter(description = "Uploader user ID") @RequestParam(required = false) String uploadedBy,
      @Parameter(description = "Tag name") @RequestParam(required = false) String tag) {
    DocumentSearchRequest request = new DocumentSearchRequest();
    request.setQuery(query);
    request.setCategoryId(categoryId);
    request.setUploadedBy(uploadedBy);
    request.setTag(tag);
    List<DocumentResponse> response = documentService.search(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/download")
  @Operation(summary = "Download document", description = "Downloads the document file")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "File content", content = @Content(schema = @Schema(type = "string", format = "binary"))),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<byte[]> download(@Parameter(description = "Document ID") @PathVariable Long id) {
    DocumentResponse doc = documentService.getById(id);
    byte[] content = documentService.download(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(doc.getMimeType()));
    headers.setContentDispositionFormData("attachment", doc.getFileName());
    headers.setContentLength(content.length);

    return ResponseEntity.ok().headers(headers).body(content);
  }

  @GetMapping("/{id}/download-url")
  @Operation(summary = "Get download URL", description = "Returns a presigned URL for downloading the document")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Download URL"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<Map<String, String>> getDownloadUrl(
      @Parameter(description = "Document ID") @PathVariable Long id) {
    String url = documentService.getDownloadUrl(id);
    return ResponseEntity.ok(Map.of("url", url));
  }

  @PutMapping("/{id}/metadata")
  @Operation(summary = "Set document metadata", description = "Sets custom metadata key-value pairs")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Metadata updated"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<Map<String, String>> setMetadata(
      @Parameter(description = "Document ID") @PathVariable Long id,
      @Valid @RequestBody MetadataRequest request) {
    documentService.setMetadata(id, request.getMetadata());
    Map<String, String> metadata = documentService.getMetadata(id);
    return ResponseEntity.ok(metadata);
  }

  @GetMapping("/{id}/metadata")
  @Operation(summary = "Get document metadata", description = "Returns custom metadata for a document")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Metadata"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<Map<String, String>> getMetadata(
      @Parameter(description = "Document ID") @PathVariable Long id) {
    Map<String, String> metadata = documentService.getMetadata(id);
    return ResponseEntity.ok(metadata);
  }

  @DeleteMapping("/{id}/metadata/{key}")
  @Operation(summary = "Delete metadata key", description = "Deletes a specific metadata key")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Metadata key deleted"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<Void> deleteMetadataKey(
      @Parameter(description = "Document ID") @PathVariable Long id,
      @Parameter(description = "Metadata key") @PathVariable String key) {
    documentService.deleteMetadataKey(id, key);
    return ResponseEntity.noContent().build();
  }
}
