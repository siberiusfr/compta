package tn.cyberious.compta.document.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static tn.cyberious.compta.document.generated.Tables.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.document.generated.tables.pojos.DocumentShares;
import tn.cyberious.compta.document.generated.tables.records.DocumentSharesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentShareRepository {

  private final DSLContext dsl;

  public DocumentShares insert(DocumentShares share) {
    log.debug(
        "Creating share for document {} with user {}",
        share.getDocumentId(),
        share.getSharedWith());

    DocumentSharesRecord record =
        dsl.insertInto(DOCUMENT_SHARES)
            .set(DOCUMENT_SHARES.DOCUMENT_ID, share.getDocumentId())
            .set(DOCUMENT_SHARES.SHARED_WITH, share.getSharedWith())
            .set(
                DOCUMENT_SHARES.PERMISSION,
                share.getPermission() != null ? share.getPermission() : "READ")
            .set(DOCUMENT_SHARES.EXPIRES_AT, share.getExpiresAt())
            .set(DOCUMENT_SHARES.CREATED_BY, share.getCreatedBy())
            .set(DOCUMENT_SHARES.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(DocumentShares.class) : null;
  }

  public DocumentShares update(DocumentShares share) {
    log.debug("Updating share: {}", share.getId());

    DocumentSharesRecord record =
        dsl.update(DOCUMENT_SHARES)
            .set(DOCUMENT_SHARES.PERMISSION, share.getPermission())
            .set(DOCUMENT_SHARES.EXPIRES_AT, share.getExpiresAt())
            .where(DOCUMENT_SHARES.ID.eq(share.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(DocumentShares.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting share: {}", id);
    int deleted = dsl.deleteFrom(DOCUMENT_SHARES).where(DOCUMENT_SHARES.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<DocumentShares> findById(Long id) {
    log.debug("Finding share by id: {}", id);
    return dsl.selectFrom(DOCUMENT_SHARES)
        .where(DOCUMENT_SHARES.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(DocumentShares.class));
  }

  public List<DocumentShares> findByDocumentId(Long documentId) {
    log.debug("Finding shares for document: {}", documentId);
    return dsl.selectFrom(DOCUMENT_SHARES)
        .where(DOCUMENT_SHARES.DOCUMENT_ID.eq(documentId))
        .orderBy(DOCUMENT_SHARES.CREATED_AT.desc())
        .fetch()
        .into(DocumentShares.class);
  }

  public List<DocumentShares> findBySharedWith(String sharedWith) {
    log.debug("Finding shares for user: {}", sharedWith);
    return dsl.selectFrom(DOCUMENT_SHARES)
        .where(DOCUMENT_SHARES.SHARED_WITH.eq(sharedWith))
        .orderBy(DOCUMENT_SHARES.CREATED_AT.desc())
        .fetch()
        .into(DocumentShares.class);
  }

  public List<DocumentShares> findActiveBySharedWith(String sharedWith) {
    log.debug("Finding active shares for user: {}", sharedWith);
    return dsl.selectFrom(DOCUMENT_SHARES)
        .where(
            DOCUMENT_SHARES
                .SHARED_WITH
                .eq(sharedWith)
                .and(
                    DOCUMENT_SHARES
                        .EXPIRES_AT
                        .isNull()
                        .or(DOCUMENT_SHARES.EXPIRES_AT.gt(LocalDateTime.now()))))
        .orderBy(DOCUMENT_SHARES.CREATED_AT.desc())
        .fetch()
        .into(DocumentShares.class);
  }

  public Optional<DocumentShares> findByDocumentIdAndSharedWith(
      Long documentId, String sharedWith) {
    log.debug("Finding share for document {} and user {}", documentId, sharedWith);
    return dsl.selectFrom(DOCUMENT_SHARES)
        .where(
            DOCUMENT_SHARES
                .DOCUMENT_ID
                .eq(documentId)
                .and(DOCUMENT_SHARES.SHARED_WITH.eq(sharedWith)))
        .fetchOptional()
        .map(record -> record.into(DocumentShares.class));
  }

  public boolean hasAccess(Long documentId, String userId, String requiredPermission) {
    log.debug(
        "Checking access for user {} on document {} with permission {}",
        userId,
        documentId,
        requiredPermission);

    List<String> allowedPermissions =
        "WRITE".equals(requiredPermission) ? List.of("WRITE") : List.of("READ", "WRITE");

    return dsl.fetchExists(
        dsl.selectFrom(DOCUMENT_SHARES)
            .where(
                DOCUMENT_SHARES
                    .DOCUMENT_ID
                    .eq(documentId)
                    .and(DOCUMENT_SHARES.SHARED_WITH.eq(userId))
                    .and(DOCUMENT_SHARES.PERMISSION.in(allowedPermissions))
                    .and(
                        DOCUMENT_SHARES
                            .EXPIRES_AT
                            .isNull()
                            .or(DOCUMENT_SHARES.EXPIRES_AT.gt(LocalDateTime.now())))));
  }

  public boolean deleteByDocumentId(Long documentId) {
    log.debug("Deleting all shares for document: {}", documentId);
    int deleted =
        dsl.deleteFrom(DOCUMENT_SHARES).where(DOCUMENT_SHARES.DOCUMENT_ID.eq(documentId)).execute();
    return deleted > 0;
  }

  public int deleteExpired() {
    log.debug("Deleting expired shares");
    return dsl.deleteFrom(DOCUMENT_SHARES)
        .where(
            DOCUMENT_SHARES
                .EXPIRES_AT
                .isNotNull()
                .and(DOCUMENT_SHARES.EXPIRES_AT.lt(LocalDateTime.now())))
        .execute();
  }
}
