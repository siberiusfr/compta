package tn.cyberious.compta.document.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.document.dto.DocumentShareRequest;
import tn.cyberious.compta.document.dto.DocumentShareResponse;
import tn.cyberious.compta.document.generated.tables.pojos.DocumentShares;
import tn.cyberious.compta.document.generated.tables.pojos.Documents;
import tn.cyberious.compta.document.repository.DocumentRepository;
import tn.cyberious.compta.document.repository.DocumentShareRepository;
import tn.cyberious.compta.exception.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentShareService {

  private final DocumentShareRepository documentShareRepository;
  private final DocumentRepository documentRepository;

  @Transactional
  public DocumentShareResponse share(Long documentId, DocumentShareRequest request, String createdBy) {
    log.info("Sharing document {} with {} by {}", documentId, request.getSharedWith(), createdBy);

    Documents document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

    if (document.getUploadedBy().equals(request.getSharedWith())) {
      throw new IllegalArgumentException("Cannot share document with its owner");
    }

    if (documentShareRepository.findByDocumentIdAndSharedWith(documentId, request.getSharedWith()).isPresent()) {
      throw new IllegalArgumentException("Document is already shared with this user");
    }

    DocumentShares share = new DocumentShares();
    share.setDocumentId(documentId);
    share.setSharedWith(request.getSharedWith());
    share.setPermission(request.getPermission() != null ? request.getPermission() : "READ");
    share.setExpiresAt(request.getExpiresAt());
    share.setCreatedBy(createdBy);

    DocumentShares saved = documentShareRepository.insert(share);
    return toResponse(saved, document.getTitle());
  }

  @Transactional
  public DocumentShareResponse update(Long shareId, DocumentShareRequest request) {
    log.info("Updating share: {}", shareId);

    DocumentShares existing =
        documentShareRepository
            .findById(shareId)
            .orElseThrow(() -> new ResourceNotFoundException("Share not found: " + shareId));

    existing.setPermission(request.getPermission());
    existing.setExpiresAt(request.getExpiresAt());

    DocumentShares updated = documentShareRepository.update(existing);

    String documentTitle =
        documentRepository.findById(updated.getDocumentId()).map(Documents::getTitle).orElse(null);

    return toResponse(updated, documentTitle);
  }

  @Transactional
  public void revoke(Long shareId) {
    log.info("Revoking share: {}", shareId);

    if (!documentShareRepository.findById(shareId).isPresent()) {
      throw new ResourceNotFoundException("Share not found: " + shareId);
    }

    documentShareRepository.delete(shareId);
  }

  @Transactional(readOnly = true)
  public List<DocumentShareResponse> getSharesByDocument(Long documentId) {
    log.debug("Getting shares for document: {}", documentId);

    Documents document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

    return documentShareRepository.findByDocumentId(documentId).stream()
        .map(s -> toResponse(s, document.getTitle()))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DocumentShareResponse> getSharesWithUser(String userId) {
    log.debug("Getting shares with user: {}", userId);

    return documentShareRepository.findBySharedWith(userId).stream()
        .map(
            s -> {
              String title = documentRepository.findById(s.getDocumentId()).map(Documents::getTitle).orElse(null);
              return toResponse(s, title);
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DocumentShareResponse> getActiveSharesWithUser(String userId) {
    log.debug("Getting active shares with user: {}", userId);

    return documentShareRepository.findActiveBySharedWith(userId).stream()
        .map(
            s -> {
              String title = documentRepository.findById(s.getDocumentId()).map(Documents::getTitle).orElse(null);
              return toResponse(s, title);
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public boolean hasAccess(Long documentId, String userId, String requiredPermission) {
    return documentShareRepository.hasAccess(documentId, userId, requiredPermission);
  }

  @Transactional
  public int cleanupExpiredShares() {
    log.info("Cleaning up expired shares");
    return documentShareRepository.deleteExpired();
  }

  private DocumentShareResponse toResponse(DocumentShares share, String documentTitle) {
    boolean isActive =
        share.getExpiresAt() == null || share.getExpiresAt().isAfter(LocalDateTime.now());

    return DocumentShareResponse.builder()
        .id(share.getId())
        .documentId(share.getDocumentId())
        .documentTitle(documentTitle)
        .sharedWith(share.getSharedWith())
        .permission(share.getPermission())
        .expiresAt(share.getExpiresAt())
        .isActive(isActive)
        .createdBy(share.getCreatedBy())
        .createdAt(share.getCreatedAt())
        .build();
  }
}
