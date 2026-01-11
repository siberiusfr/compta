package tn.cyberious.compta.document.repository;

import static tn.cyberious.compta.document.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.document.generated.tables.pojos.DocumentMetadata;
import tn.cyberious.compta.document.generated.tables.records.DocumentMetadataRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentMetadataRepository {

  private final DSLContext dsl;

  public DocumentMetadata insert(DocumentMetadata metadata) {
    log.debug(
        "Inserting metadata for document {}: key={}", metadata.getDocumentId(), metadata.getKey());

    DocumentMetadataRecord record =
        dsl.insertInto(DOCUMENT_METADATA)
            .set(DOCUMENT_METADATA.DOCUMENT_ID, metadata.getDocumentId())
            .set(DOCUMENT_METADATA.KEY, metadata.getKey())
            .set(DOCUMENT_METADATA.VALUE, metadata.getValue())
            .set(DOCUMENT_METADATA.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(DocumentMetadata.class) : null;
  }

  public DocumentMetadata upsert(Long documentId, String key, String value) {
    log.debug("Upserting metadata for document {}: key={}", documentId, key);

    DocumentMetadataRecord record =
        dsl.insertInto(DOCUMENT_METADATA)
            .set(DOCUMENT_METADATA.DOCUMENT_ID, documentId)
            .set(DOCUMENT_METADATA.KEY, key)
            .set(DOCUMENT_METADATA.VALUE, value)
            .set(DOCUMENT_METADATA.CREATED_AT, LocalDateTime.now())
            .onConflict(DOCUMENT_METADATA.DOCUMENT_ID, DOCUMENT_METADATA.KEY)
            .doUpdate()
            .set(DOCUMENT_METADATA.VALUE, value)
            .returning()
            .fetchOne();

    return record != null ? record.into(DocumentMetadata.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting metadata: {}", id);
    int deleted = dsl.deleteFrom(DOCUMENT_METADATA).where(DOCUMENT_METADATA.ID.eq(id)).execute();
    return deleted > 0;
  }

  public boolean deleteByDocumentIdAndKey(Long documentId, String key) {
    log.debug("Deleting metadata for document {} key {}", documentId, key);
    int deleted =
        dsl.deleteFrom(DOCUMENT_METADATA)
            .where(DOCUMENT_METADATA.DOCUMENT_ID.eq(documentId).and(DOCUMENT_METADATA.KEY.eq(key)))
            .execute();
    return deleted > 0;
  }

  public Optional<DocumentMetadata> findById(Long id) {
    log.debug("Finding metadata by id: {}", id);
    return dsl.selectFrom(DOCUMENT_METADATA)
        .where(DOCUMENT_METADATA.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(DocumentMetadata.class));
  }

  public List<DocumentMetadata> findByDocumentId(Long documentId) {
    log.debug("Finding metadata for document: {}", documentId);
    return dsl.selectFrom(DOCUMENT_METADATA)
        .where(DOCUMENT_METADATA.DOCUMENT_ID.eq(documentId))
        .orderBy(DOCUMENT_METADATA.KEY)
        .fetch()
        .into(DocumentMetadata.class);
  }

  public Map<String, String> findAsMapByDocumentId(Long documentId) {
    log.debug("Finding metadata map for document: {}", documentId);
    return dsl
        .selectFrom(DOCUMENT_METADATA)
        .where(DOCUMENT_METADATA.DOCUMENT_ID.eq(documentId))
        .fetch()
        .stream()
        .collect(
            Collectors.toMap(
                r -> r.get(DOCUMENT_METADATA.KEY),
                r -> r.get(DOCUMENT_METADATA.VALUE) != null ? r.get(DOCUMENT_METADATA.VALUE) : ""));
  }

  public Optional<DocumentMetadata> findByDocumentIdAndKey(Long documentId, String key) {
    log.debug("Finding metadata for document {} key {}", documentId, key);
    return dsl.selectFrom(DOCUMENT_METADATA)
        .where(DOCUMENT_METADATA.DOCUMENT_ID.eq(documentId).and(DOCUMENT_METADATA.KEY.eq(key)))
        .fetchOptional()
        .map(record -> record.into(DocumentMetadata.class));
  }

  public boolean deleteByDocumentId(Long documentId) {
    log.debug("Deleting all metadata for document: {}", documentId);
    int deleted =
        dsl.deleteFrom(DOCUMENT_METADATA)
            .where(DOCUMENT_METADATA.DOCUMENT_ID.eq(documentId))
            .execute();
    return deleted > 0;
  }

  public void setMetadata(Long documentId, Map<String, String> metadata) {
    log.debug("Setting metadata for document {}: {} entries", documentId, metadata.size());

    metadata.forEach((key, value) -> upsert(documentId, key, value));
  }
}
