package tn.cyberious.compta.document.service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.document.dto.DocumentVersionResponse;
import tn.cyberious.compta.document.dto.DocumentVersionUploadRequest;
import tn.cyberious.compta.document.generated.tables.pojos.DocumentVersions;
import tn.cyberious.compta.document.generated.tables.pojos.Documents;
import tn.cyberious.compta.document.repository.DocumentRepository;
import tn.cyberious.compta.document.repository.DocumentVersionRepository;
import tn.cyberious.compta.document.service.StorageService.UploadResult;
import tn.cyberious.compta.exception.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentVersionService {

  private final DocumentVersionRepository documentVersionRepository;
  private final DocumentRepository documentRepository;
  private final StorageService storageService;

  private static final Duration PRESIGNED_URL_DURATION = Duration.ofHours(1);

  @Transactional
  public DocumentVersionResponse uploadNewVersion(
      Long documentId,
      MultipartFile file,
      DocumentVersionUploadRequest request,
      String uploadedBy) {
    log.info("Uploading new version for document {} by {}", documentId, uploadedBy);

    Documents document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

    Integer nextVersion = documentVersionRepository.getNextVersionNumber(documentId);

    DocumentVersions currentVersionRecord = new DocumentVersions();
    currentVersionRecord.setDocumentId(documentId);
    currentVersionRecord.setVersionNumber(document.getVersion());
    currentVersionRecord.setFileName(document.getFileName());
    currentVersionRecord.setFilePath(document.getFilePath());
    currentVersionRecord.setFileSize(document.getFileSize());
    currentVersionRecord.setUploadedBy(document.getUploadedBy());
    currentVersionRecord.setChecksum(document.getChecksum());
    currentVersionRecord.setChangeDescription("Version before update to v" + nextVersion);

    if (nextVersion == 1) {
      documentVersionRepository.insert(currentVersionRecord);
      nextVersion = 2;
    }

    UploadResult uploadResult = storageService.uploadFile(file);

    DocumentVersions newVersion = new DocumentVersions();
    newVersion.setDocumentId(documentId);
    newVersion.setVersionNumber(nextVersion);
    newVersion.setFileName(file.getOriginalFilename());
    newVersion.setFilePath(uploadResult.filePath());
    newVersion.setFileSize(uploadResult.fileSize());
    newVersion.setUploadedBy(uploadedBy);
    newVersion.setChangeDescription(request.getChangeDescription());
    newVersion.setChecksum(uploadResult.checksum());

    DocumentVersions savedVersion = documentVersionRepository.insert(newVersion);

    documentRepository.updateFile(
        documentId,
        file.getOriginalFilename(),
        uploadResult.filePath(),
        uploadResult.fileSize(),
        file.getContentType(),
        uploadResult.checksum(),
        nextVersion);

    return toResponse(savedVersion);
  }

  @Transactional(readOnly = true)
  public List<DocumentVersionResponse> getVersions(Long documentId) {
    log.debug("Getting versions for document: {}", documentId);

    if (!documentRepository.exists(documentId)) {
      throw new ResourceNotFoundException("Document not found: " + documentId);
    }

    return documentVersionRepository.findByDocumentId(documentId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public DocumentVersionResponse getVersion(Long documentId, Integer versionNumber) {
    log.debug("Getting version {} for document {}", versionNumber, documentId);

    if (!documentRepository.exists(documentId)) {
      throw new ResourceNotFoundException("Document not found: " + documentId);
    }

    DocumentVersions version =
        documentVersionRepository
            .findByDocumentIdAndVersion(documentId, versionNumber)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Version " + versionNumber + " not found for document " + documentId));

    return toResponse(version);
  }

  public byte[] downloadVersion(Long documentId, Integer versionNumber) {
    log.info("Downloading version {} for document {}", versionNumber, documentId);

    Documents document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

    String filePath;
    if (document.getVersion().equals(versionNumber)) {
      filePath = document.getFilePath();
    } else {
      DocumentVersions version =
          documentVersionRepository
              .findByDocumentIdAndVersion(documentId, versionNumber)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Version " + versionNumber + " not found for document " + documentId));
      filePath = version.getFilePath();
    }

    try {
      return storageService.downloadFile(filePath).readAllBytes();
    } catch (Exception e) {
      log.error("Failed to download version", e);
      throw new RuntimeException("Failed to download version: " + e.getMessage(), e);
    }
  }

  public String getVersionDownloadUrl(Long documentId, Integer versionNumber) {
    log.debug("Getting download URL for version {} of document {}", versionNumber, documentId);

    Documents document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

    String filePath;
    if (document.getVersion().equals(versionNumber)) {
      filePath = document.getFilePath();
    } else {
      DocumentVersions version =
          documentVersionRepository
              .findByDocumentIdAndVersion(documentId, versionNumber)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Version " + versionNumber + " not found for document " + documentId));
      filePath = version.getFilePath();
    }

    return storageService.generatePresignedUrl(filePath, PRESIGNED_URL_DURATION);
  }

  private DocumentVersionResponse toResponse(DocumentVersions version) {
    DocumentVersionResponse.DocumentVersionResponseBuilder builder =
        DocumentVersionResponse.builder()
            .id(version.getId())
            .documentId(version.getDocumentId())
            .versionNumber(version.getVersionNumber())
            .fileName(version.getFileName())
            .fileSize(version.getFileSize())
            .uploadedBy(version.getUploadedBy())
            .changeDescription(version.getChangeDescription())
            .checksum(version.getChecksum())
            .createdAt(version.getCreatedAt());

    try {
      builder.downloadUrl(
          storageService.generatePresignedUrl(version.getFilePath(), PRESIGNED_URL_DURATION));
    } catch (Exception e) {
      log.warn(
          "Failed to generate presigned URL for version {}: {}", version.getId(), e.getMessage());
    }

    return builder.build();
  }
}
