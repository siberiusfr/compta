package tn.cyberious.compta.document.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.document.dto.DocumentShareRequest;
import tn.cyberious.compta.document.dto.DocumentShareResponse;
import tn.cyberious.compta.document.service.DocumentShareService;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Sharing", description = "Document sharing management endpoints")
public class DocumentShareController {

  private final DocumentShareService documentShareService;

  @PostMapping("/{documentId}/shares")
  @Operation(summary = "Share a document", description = "Shares a document with another user")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Document shared successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request or already shared"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<DocumentShareResponse> share(
      @Parameter(description = "Document ID") @PathVariable Long documentId,
      @Valid @RequestBody DocumentShareRequest request,
      @Parameter(description = "User ID", hidden = true) @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId) {
    DocumentShareResponse response = documentShareService.share(documentId, request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/shares/{shareId}")
  @Operation(summary = "Update a share", description = "Updates share permission or expiration")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Share updated successfully"),
    @ApiResponse(responseCode = "404", description = "Share not found")
  })
  public ResponseEntity<DocumentShareResponse> update(
      @Parameter(description = "Share ID") @PathVariable Long shareId,
      @Valid @RequestBody DocumentShareRequest request) {
    DocumentShareResponse response = documentShareService.update(shareId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/shares/{shareId}")
  @Operation(summary = "Revoke a share", description = "Revokes document sharing")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Share revoked successfully"),
    @ApiResponse(responseCode = "404", description = "Share not found")
  })
  public ResponseEntity<Void> revoke(@Parameter(description = "Share ID") @PathVariable Long shareId) {
    documentShareService.revoke(shareId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{documentId}/shares")
  @Operation(summary = "Get document shares", description = "Returns all shares for a document")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of shares"),
    @ApiResponse(responseCode = "404", description = "Document not found")
  })
  public ResponseEntity<List<DocumentShareResponse>> getSharesByDocument(
      @Parameter(description = "Document ID") @PathVariable Long documentId) {
    List<DocumentShareResponse> response = documentShareService.getSharesByDocument(documentId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/shared-with-me")
  @Operation(summary = "Get documents shared with me", description = "Returns all documents shared with the current user")
  @ApiResponse(responseCode = "200", description = "List of shared documents")
  public ResponseEntity<List<DocumentShareResponse>> getSharedWithMe(
      @Parameter(description = "User ID", hidden = true) @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId) {
    List<DocumentShareResponse> response = documentShareService.getActiveSharesWithUser(userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/shared-with/{userId}")
  @Operation(summary = "Get shares with user", description = "Returns all documents shared with a specific user")
  @ApiResponse(responseCode = "200", description = "List of shares")
  public ResponseEntity<List<DocumentShareResponse>> getSharesWithUser(
      @Parameter(description = "User ID to check") @PathVariable String userId) {
    List<DocumentShareResponse> response = documentShareService.getSharesWithUser(userId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/shares/cleanup")
  @Operation(summary = "Cleanup expired shares", description = "Removes all expired document shares")
  @ApiResponse(responseCode = "200", description = "Number of cleaned up shares")
  public ResponseEntity<Integer> cleanupExpiredShares() {
    int cleaned = documentShareService.cleanupExpiredShares();
    return ResponseEntity.ok(cleaned);
  }
}
