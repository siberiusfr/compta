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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.cyberious.compta.document.dto.CategoryRequest;
import tn.cyberious.compta.document.dto.CategoryResponse;
import tn.cyberious.compta.document.service.CategoryService;

@RestController
@RequestMapping("/api/documents/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Document category management endpoints")
public class CategoryController {

  private final CategoryService categoryService;

  @PostMapping
  @Operation(summary = "Create a new category", description = "Creates a new document category")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Category created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "409", description = "Category with this name already exists")
  })
  public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
    CategoryResponse response = categoryService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a category", description = "Updates an existing category")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<CategoryResponse> update(
      @Parameter(description = "Category ID") @PathVariable Long id,
      @Valid @RequestBody CategoryRequest request) {
    CategoryResponse response = categoryService.update(id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a category", description = "Deletes a category if it has no sub-categories")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Category has sub-categories"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<Void> delete(@Parameter(description = "Category ID") @PathVariable Long id) {
    categoryService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a category by ID", description = "Returns a category with its sub-categories")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category found"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<CategoryResponse> getById(
      @Parameter(description = "Category ID") @PathVariable Long id) {
    CategoryResponse response = categoryService.getById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "Get all categories", description = "Returns a flat list of all categories")
  @ApiResponse(responseCode = "200", description = "List of categories")
  public ResponseEntity<List<CategoryResponse>> getAll() {
    List<CategoryResponse> response = categoryService.getAll();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/root")
  @Operation(summary = "Get root categories", description = "Returns only top-level categories without parents")
  @ApiResponse(responseCode = "200", description = "List of root categories")
  public ResponseEntity<List<CategoryResponse>> getRootCategories() {
    List<CategoryResponse> response = categoryService.getRootCategories();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/tree")
  @Operation(summary = "Get category tree", description = "Returns the complete category hierarchy as a tree")
  @ApiResponse(responseCode = "200", description = "Category tree")
  public ResponseEntity<List<CategoryResponse>> getTree() {
    List<CategoryResponse> response = categoryService.getTree();
    return ResponseEntity.ok(response);
  }
}
