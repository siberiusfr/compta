package tn.cyberious.compta.document.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.document.dto.DocumentResponse;
import tn.cyberious.compta.document.dto.DocumentSearchRequest;
import tn.cyberious.compta.document.dto.DocumentUpdateRequest;
import tn.cyberious.compta.document.dto.DocumentUploadRequest;
import tn.cyberious.compta.document.dto.TagResponse;
import tn.cyberious.compta.document.generated.tables.pojos.Documents;
import tn.cyberious.compta.document.generated.tables.pojos.Tags;
import tn.cyberious.compta.document.repository.CategoryRepository;
import tn.cyberious.compta.document.repository.DocumentMetadataRepository;
import tn.cyberious.compta.document.repository.DocumentRepository;
import tn.cyberious.compta.document.repository.DocumentTagRepository;
import tn.cyberious.compta.document.service.StorageService.UploadResult;
import tn.cyberious.compta.exception.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final CategoryRepository categoryRepository;
  private final DocumentTagRepository documentTagRepository;
  private final DocumentMetadataRepository documentMetadataRepository;
  private final StorageService storageService;
  private final TagService tagService;

  private static final Duration PRESIGNED_URL_DURATION = Duration.ofHours(1);

  @Transactional
  public DocumentResponse upload(
      MultipartFile file, DocumentUploadRequest request, String uploadedBy) {
    log.info("Uploading document: {} by {}", request.getTitle(), uploadedBy);

    if (request.getCategoryId() != null && !categoryRepository.exists(request.getCategoryId())) {
      throw new ResourceNotFoundException("Category not found: " + request.getCategoryId());
    }

    UploadResult uploadResult = storageService.uploadFile(file);

    Documents document = new Documents();
    document.setTitle(request.getTitle());
    document.setDescription(request.getDescription());
    document.setFileName(file.getOriginalFilename());
    document.setFilePath(uploadResult.filePath());
    document.setFileSize(uploadResult.fileSize());
    document.setMimeType(file.getContentType());
    document.setCategoryId(request.getCategoryId());
    document.setUploadedBy(uploadedBy);
    document.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
    document.setChecksum(uploadResult.checksum());

    Documents saved = documentRepository.insert(document);

    if (request.getTags() != null && !request.getTags().isEmpty()) {
      for (String tagName : request.getTags()) {
        Tags tag = tagService.findOrCreate(tagName);
        documentTagRepository.addTagToDocument(saved.getId(), tag.getId());
      }
    }

    return toResponse(saved, true);
  }

  @Transactional
  public DocumentResponse update(Long id, DocumentUpdateRequest request) {
    log.info("Updating document: {}", id);

    Documents existing =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

    if (request.getCategoryId() != null && !categoryRepository.exists(request.getCategoryId())) {
      throw new ResourceNotFoundException("Category not found: " + request.getCategoryId());
    }

    if (request.getTitle() != null) {
      existing.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      existing.setDescription(request.getDescription());
    }
    if (request.getCategoryId() != null) {
      existing.setCategoryId(request.getCategoryId());
    }
    if (request.getIsPublic() != null) {
      existing.setIsPublic(request.getIsPublic());
    }

    Documents updated = documentRepository.update(existing);

    if (request.getTags() != null) {
      documentTagRepository.removeAllTagsFromDocument(id);
      for (String tagName : request.getTags()) {
        Tags tag = tagService.findOrCreate(tagName);
        documentTagRepository.addTagToDocument(id, tag.getId());
      }
    }

    return toResponse(updated, true);
  }

  @Transactional
  public void delete(Long id) {
    log.info("Deleting document: {}", id);

    Documents document =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

    try {
      storageService.deleteFile(document.getFilePath());
    } catch (Exception e) {
      log.warn("Failed to delete file from storage: {}", e.getMessage());
    }

    documentTagRepository.removeAllTagsFromDocument(id);
    documentMetadataRepository.deleteByDocumentId(id);
    documentRepository.delete(id);
  }

  @Transactional(readOnly = true)
  public DocumentResponse getById(Long id) {
    log.debug("Getting document: {}", id);

    Documents document =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

    return toResponse(document, true);
  }

  @Transactional(readOnly = true)
  public List<DocumentResponse> getAll() {
    log.debug("Getting all documents");
    return documentRepository.findAll().stream()
        .map(d -> toResponse(d, false))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DocumentResponse> getByUploadedBy(String uploadedBy) {
    log.debug("Getting documents by uploader: {}", uploadedBy);
    return documentRepository.findByUploadedBy(uploadedBy).stream()
        .map(d -> toResponse(d, false))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DocumentResponse> getByCategoryId(Long categoryId) {
    log.debug("Getting documents by category: {}", categoryId);
    return documentRepository.findByCategoryId(categoryId).stream()
        .map(d -> toResponse(d, false))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DocumentResponse> getPublic() {
    log.debug("Getting public documents");
    return documentRepository.findPublic().stream()
        .map(d -> toResponse(d, false))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DocumentResponse> search(DocumentSearchRequest request) {
    log.debug("Searching documents: {}", request);

    List<Documents> documents =
        documentRepository.search(
            request.getQuery(), request.getCategoryId(), request.getUploadedBy());

    if (request.getTag() != null && !request.getTag().isBlank()) {
      List<Long> taggedDocIds = documentTagRepository.findDocumentIdsByTagName(request.getTag());
      documents =
          documents.stream()
              .filter(d -> taggedDocIds.contains(d.getId()))
              .collect(Collectors.toList());
    }

    return documents.stream().map(d -> toResponse(d, false)).collect(Collectors.toList());
  }

  @Transactional
  public void setMetadata(Long documentId, Map<String, String> metadata) {
    log.info("Setting metadata for document {}: {} entries", documentId, metadata.size());

    if (!documentRepository.exists(documentId)) {
      throw new ResourceNotFoundException("Document not found: " + documentId);
    }

    documentMetadataRepository.setMetadata(documentId, metadata);
  }

  @Transactional
  public void deleteMetadataKey(Long documentId, String key) {
    log.info("Deleting metadata key {} from document {}", key, documentId);

    if (!documentRepository.exists(documentId)) {
      throw new ResourceNotFoundException("Document not found: " + documentId);
    }

    documentMetadataRepository.deleteByDocumentIdAndKey(documentId, key);
  }

  @Transactional(readOnly = true)
  public Map<String, String> getMetadata(Long documentId) {
    log.debug("Getting metadata for document: {}", documentId);

    if (!documentRepository.exists(documentId)) {
      throw new ResourceNotFoundException("Document not found: " + documentId);
    }

    return documentMetadataRepository.findAsMapByDocumentId(documentId);
  }

  public byte[] download(Long id) {
    log.info("Downloading document: {}", id);

    Documents document =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

    try {
      return storageService.downloadFile(document.getFilePath()).readAllBytes();
    } catch (Exception e) {
      log.error("Failed to download file", e);
      throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
    }
  }

  public String getDownloadUrl(Long id) {
    log.debug("Getting download URL for document: {}", id);

    Documents document =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

    return storageService.generatePresignedUrl(document.getFilePath(), PRESIGNED_URL_DURATION);
  }

  private DocumentResponse toResponse(Documents document, boolean includeDetails) {
    DocumentResponse.DocumentResponseBuilder builder =
        DocumentResponse.builder()
            .id(document.getId())
            .title(document.getTitle())
            .description(document.getDescription())
            .fileName(document.getFileName())
            .fileSize(document.getFileSize())
            .mimeType(document.getMimeType())
            .categoryId(document.getCategoryId())
            .uploadedBy(document.getUploadedBy())
            .isPublic(document.getIsPublic())
            .version(document.getVersion())
            .checksum(document.getChecksum())
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdatedAt());

    if (document.getCategoryId() != null) {
      categoryRepository
          .findById(document.getCategoryId())
          .ifPresent(c -> builder.categoryName(c.getName()));
    }

    if (includeDetails) {
      List<Tags> tags = documentTagRepository.findTagsByDocumentId(document.getId());
      builder.tags(
          tags.stream()
              .map(
                  t ->
                      TagResponse.builder()
                          .id(t.getId())
                          .name(t.getName())
                          .createdAt(t.getCreatedAt())
                          .build())
              .collect(Collectors.toList()));

      builder.metadata(documentMetadataRepository.findAsMapByDocumentId(document.getId()));

      try {
        builder.downloadUrl(
            storageService.generatePresignedUrl(document.getFilePath(), PRESIGNED_URL_DURATION));
      } catch (Exception e) {
        log.warn(
            "Failed to generate presigned URL for document {}: {}",
            document.getId(),
            e.getMessage());
      }
    }

    return builder.build();
  }
}
