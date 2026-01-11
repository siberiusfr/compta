package tn.cyberious.compta.document.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static tn.cyberious.compta.document.generated.Tables.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.document.generated.tables.pojos.DocumentVersions;
import tn.cyberious.compta.document.generated.tables.records.DocumentVersionsRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentVersionRepository {

  private final DSLContext dsl;

  public DocumentVersions insert(DocumentVersions version) {
    log.debug(
        "Inserting document version: document={}, version={}",
        version.getDocumentId(),
        version.getVersionNumber());

    DocumentVersionsRecord record =
        dsl.insertInto(DOCUMENT_VERSIONS)
            .set(DOCUMENT_VERSIONS.DOCUMENT_ID, version.getDocumentId())
            .set(DOCUMENT_VERSIONS.VERSION_NUMBER, version.getVersionNumber())
            .set(DOCUMENT_VERSIONS.FILE_NAME, version.getFileName())
            .set(DOCUMENT_VERSIONS.FILE_PATH, version.getFilePath())
            .set(DOCUMENT_VERSIONS.FILE_SIZE, version.getFileSize())
            .set(DOCUMENT_VERSIONS.UPLOADED_BY, version.getUploadedBy())
            .set(DOCUMENT_VERSIONS.CHANGE_DESCRIPTION, version.getChangeDescription())
            .set(DOCUMENT_VERSIONS.CHECKSUM, version.getChecksum())
            .set(DOCUMENT_VERSIONS.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(DocumentVersions.class) : null;
  }

  public Optional<DocumentVersions> findById(Long id) {
    log.debug("Finding document version by id: {}", id);
    return dsl.selectFrom(DOCUMENT_VERSIONS)
        .where(DOCUMENT_VERSIONS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(DocumentVersions.class));
  }

  public List<DocumentVersions> findByDocumentId(Long documentId) {
    log.debug("Finding versions for document: {}", documentId);
    return dsl.selectFrom(DOCUMENT_VERSIONS)
        .where(DOCUMENT_VERSIONS.DOCUMENT_ID.eq(documentId))
        .orderBy(DOCUMENT_VERSIONS.VERSION_NUMBER.desc())
        .fetch()
        .into(DocumentVersions.class);
  }

  public Optional<DocumentVersions> findByDocumentIdAndVersion(
      Long documentId, Integer versionNumber) {
    log.debug("Finding document {} version {}", documentId, versionNumber);
    return dsl.selectFrom(DOCUMENT_VERSIONS)
        .where(
            DOCUMENT_VERSIONS
                .DOCUMENT_ID
                .eq(documentId)
                .and(DOCUMENT_VERSIONS.VERSION_NUMBER.eq(versionNumber)))
        .fetchOptional()
        .map(record -> record.into(DocumentVersions.class));
  }

  public Optional<DocumentVersions> findLatestByDocumentId(Long documentId) {
    log.debug("Finding latest version for document: {}", documentId);
    return dsl.selectFrom(DOCUMENT_VERSIONS)
        .where(DOCUMENT_VERSIONS.DOCUMENT_ID.eq(documentId))
        .orderBy(DOCUMENT_VERSIONS.VERSION_NUMBER.desc())
        .limit(1)
        .fetchOptional()
        .map(record -> record.into(DocumentVersions.class));
  }

  public Integer getNextVersionNumber(Long documentId) {
    Integer maxVersion =
        dsl.select(org.jooq.impl.DSL.max(DOCUMENT_VERSIONS.VERSION_NUMBER))
            .from(DOCUMENT_VERSIONS)
            .where(DOCUMENT_VERSIONS.DOCUMENT_ID.eq(documentId))
            .fetchOne(0, Integer.class);
    return maxVersion == null ? 1 : maxVersion + 1;
  }

  public boolean deleteByDocumentId(Long documentId) {
    log.debug("Deleting all versions for document: {}", documentId);
    int deleted =
        dsl.deleteFrom(DOCUMENT_VERSIONS)
            .where(DOCUMENT_VERSIONS.DOCUMENT_ID.eq(documentId))
            .execute();
    return deleted > 0;
  }

  public long countByDocumentId(Long documentId) {
    return dsl.fetchCount(
        dsl.selectFrom(DOCUMENT_VERSIONS).where(DOCUMENT_VERSIONS.DOCUMENT_ID.eq(documentId)));
  }
}
