package tn.cyberious.compta.document.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.document.dto.CategoryRequest;
import tn.cyberious.compta.document.dto.CategoryResponse;
import tn.cyberious.compta.document.generated.tables.pojos.Categories;
import tn.cyberious.compta.document.repository.CategoryRepository;
import tn.cyberious.compta.exception.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    log.info("Creating category: {}", request.getName());

    if (categoryRepository.existsByName(request.getName())) {
      throw new IllegalArgumentException(
          "Category with name '" + request.getName() + "' already exists");
    }

    if (request.getParentCategoryId() != null
        && !categoryRepository.exists(request.getParentCategoryId())) {
      throw new ResourceNotFoundException(
          "Parent category not found: " + request.getParentCategoryId());
    }

    Categories category = new Categories();
    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setParentCategoryId(request.getParentCategoryId());

    Categories saved = categoryRepository.insert(category);
    return toResponse(saved, null);
  }

  @Transactional
  public CategoryResponse update(Long id, CategoryRequest request) {
    log.info("Updating category: {}", id);

    Categories existing =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

    if (!existing.getName().equals(request.getName())
        && categoryRepository.existsByName(request.getName())) {
      throw new IllegalArgumentException(
          "Category with name '" + request.getName() + "' already exists");
    }

    if (request.getParentCategoryId() != null) {
      if (request.getParentCategoryId().equals(id)) {
        throw new IllegalArgumentException("Category cannot be its own parent");
      }
      if (!categoryRepository.exists(request.getParentCategoryId())) {
        throw new ResourceNotFoundException(
            "Parent category not found: " + request.getParentCategoryId());
      }
    }

    existing.setName(request.getName());
    existing.setDescription(request.getDescription());
    existing.setParentCategoryId(request.getParentCategoryId());

    Categories updated = categoryRepository.update(existing);
    return toResponse(updated, getParentName(updated.getParentCategoryId()));
  }

  @Transactional
  public void delete(Long id) {
    log.info("Deleting category: {}", id);

    if (!categoryRepository.exists(id)) {
      throw new ResourceNotFoundException("Category not found: " + id);
    }

    List<Categories> children = categoryRepository.findByParentId(id);
    if (!children.isEmpty()) {
      throw new IllegalArgumentException(
          "Cannot delete category with sub-categories. Delete sub-categories first.");
    }

    categoryRepository.delete(id);
  }

  @Transactional(readOnly = true)
  public CategoryResponse getById(Long id) {
    log.debug("Getting category: {}", id);

    Categories category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

    List<Categories> children = categoryRepository.findByParentId(id);
    CategoryResponse response = toResponse(category, getParentName(category.getParentCategoryId()));
    response.setChildren(
        children.stream().map(c -> toResponse(c, null)).collect(Collectors.toList()));
    return response;
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> getAll() {
    log.debug("Getting all categories");
    List<Categories> all = categoryRepository.findAll();
    Map<Long, String> nameMap =
        all.stream().collect(Collectors.toMap(Categories::getId, Categories::getName));

    return all.stream()
        .map(
            c -> {
              String parentName =
                  c.getParentCategoryId() != null ? nameMap.get(c.getParentCategoryId()) : null;
              return toResponse(c, parentName);
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> getRootCategories() {
    log.debug("Getting root categories");
    return categoryRepository.findByParentId(null).stream()
        .map(c -> toResponse(c, null))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> getTree() {
    log.debug("Getting category tree");
    List<Categories> all = categoryRepository.findAll();

    Map<Long, List<Categories>> childrenMap =
        all.stream()
            .filter(c -> c.getParentCategoryId() != null)
            .collect(Collectors.groupingBy(Categories::getParentCategoryId));

    return all.stream()
        .filter(c -> c.getParentCategoryId() == null)
        .map(c -> buildTree(c, childrenMap))
        .collect(Collectors.toList());
  }

  private CategoryResponse buildTree(Categories category, Map<Long, List<Categories>> childrenMap) {
    CategoryResponse response = toResponse(category, null);
    List<Categories> children = childrenMap.getOrDefault(category.getId(), new ArrayList<>());
    response.setChildren(
        children.stream().map(c -> buildTree(c, childrenMap)).collect(Collectors.toList()));
    return response;
  }

  private String getParentName(Long parentId) {
    if (parentId == null) return null;
    return categoryRepository.findById(parentId).map(Categories::getName).orElse(null);
  }

  private CategoryResponse toResponse(Categories category, String parentName) {
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .description(category.getDescription())
        .parentCategoryId(category.getParentCategoryId())
        .parentCategoryName(parentName)
        .createdAt(category.getCreatedAt())
        .updatedAt(category.getUpdatedAt())
        .build();
  }
}
