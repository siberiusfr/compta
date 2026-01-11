package tn.cyberious.compta.document.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.document.dto.TagRequest;
import tn.cyberious.compta.document.dto.TagResponse;
import tn.cyberious.compta.document.service.TagService;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Document tag management endpoints")
public class TagController {

  private final TagService tagService;

  @PostMapping
  @Operation(summary = "Create a new tag", description = "Creates a new document tag")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Tag created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "409", description = "Tag with this name already exists")
  })
  public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
    TagResponse response = tagService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a tag", description = "Deletes a tag")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Tag not found")
  })
  public ResponseEntity<Void> delete(@Parameter(description = "Tag ID") @PathVariable Long id) {
    tagService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a tag by ID", description = "Returns a tag by its ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tag found"),
    @ApiResponse(responseCode = "404", description = "Tag not found")
  })
  public ResponseEntity<TagResponse> getById(
      @Parameter(description = "Tag ID") @PathVariable Long id) {
    TagResponse response = tagService.getById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Get all tags",
      description = "Returns all tags, optionally filtered by search query")
  @ApiResponse(responseCode = "200", description = "List of tags")
  public ResponseEntity<List<TagResponse>> getAll(
      @Parameter(description = "Search query") @RequestParam(required = false) String search) {
    List<TagResponse> response;
    if (search != null && !search.isBlank()) {
      response = tagService.search(search);
    } else {
      response = tagService.getAll();
    }
    return ResponseEntity.ok(response);
  }
}
