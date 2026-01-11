package tn.cyberious.compta.document.repository;

import static tn.cyberious.compta.document.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.document.generated.tables.pojos.Documents;
import tn.cyberious.compta.document.generated.tables.records.DocumentsRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentRepository {

  private final DSLContext dsl;

  public Documents insert(Documents document) {
    log.debug("Inserting document: {}", document.getTitle());

    DocumentsRecord record =
        dsl.insertInto(DOCUMENTS)
            .set(DOCUMENTS.TITLE, document.getTitle())
            .set(DOCUMENTS.DESCRIPTION, document.getDescription())
            .set(DOCUMENTS.FILE_NAME, document.getFileName())
            .set(DOCUMENTS.FILE_PATH, document.getFilePath())
            .set(DOCUMENTS.FILE_SIZE, document.getFileSize())
            .set(DOCUMENTS.MIME_TYPE, document.getMimeType())
            .set(DOCUMENTS.CATEGORY_ID, document.getCategoryId())
            .set(DOCUMENTS.UPLOADED_BY, document.getUploadedBy())
            .set(
                DOCUMENTS.IS_PUBLIC,
                document.getIsPublic() != null ? document.getIsPublic() : false)
            .set(DOCUMENTS.VERSION, 1)
            .set(DOCUMENTS.CHECKSUM, document.getChecksum())
            .set(DOCUMENTS.CREATED_AT, LocalDateTime.now())
            .set(DOCUMENTS.UPDATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(Documents.class) : null;
  }

  public Documents update(Documents document) {
    log.debug("Updating document: {}", document.getId());

    DocumentsRecord record =
        dsl.update(DOCUMENTS)
            .set(DOCUMENTS.TITLE, document.getTitle())
            .set(DOCUMENTS.DESCRIPTION, document.getDescription())
            .set(DOCUMENTS.CATEGORY_ID, document.getCategoryId())
            .set(DOCUMENTS.IS_PUBLIC, document.getIsPublic())
            .set(DOCUMENTS.UPDATED_AT, LocalDateTime.now())
            .where(DOCUMENTS.ID.eq(document.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Documents.class) : null;
  }

  public Documents updateFile(
      Long id,
      String fileName,
      String filePath,
      Long fileSize,
      String mimeType,
      String checksum,
      Integer newVersion) {
    log.debug("Updating document file: {}", id);

    DocumentsRecord record =
        dsl.update(DOCUMENTS)
            .set(DOCUMENTS.FILE_NAME, fileName)
            .set(DOCUMENTS.FILE_PATH, filePath)
            .set(DOCUMENTS.FILE_SIZE, fileSize)
            .set(DOCUMENTS.MIME_TYPE, mimeType)
            .set(DOCUMENTS.CHECKSUM, checksum)
            .set(DOCUMENTS.VERSION, newVersion)
            .set(DOCUMENTS.UPDATED_AT, LocalDateTime.now())
            .where(DOCUMENTS.ID.eq(id))
            .returning()
            .fetchOne();

    return record != null ? record.into(Documents.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting document: {}", id);
    int deleted = dsl.deleteFrom(DOCUMENTS).where(DOCUMENTS.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Documents> findById(Long id) {
    log.debug("Finding document by id: {}", id);
    return dsl.selectFrom(DOCUMENTS)
        .where(DOCUMENTS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Documents.class));
  }

  public List<Documents> findAll() {
    log.debug("Finding all documents");
    return dsl.selectFrom(DOCUMENTS)
        .orderBy(DOCUMENTS.CREATED_AT.desc())
        .fetch()
        .into(Documents.class);
  }

  public List<Documents> findByUploadedBy(String uploadedBy) {
    log.debug("Finding documents by uploadedBy: {}", uploadedBy);
    return dsl.selectFrom(DOCUMENTS)
        .where(DOCUMENTS.UPLOADED_BY.eq(uploadedBy))
        .orderBy(DOCUMENTS.CREATED_AT.desc())
        .fetch()
        .into(Documents.class);
  }

  public List<Documents> findByCategoryId(Long categoryId) {
    log.debug("Finding documents by category: {}", categoryId);
    return dsl.selectFrom(DOCUMENTS)
        .where(DOCUMENTS.CATEGORY_ID.eq(categoryId))
        .orderBy(DOCUMENTS.CREATED_AT.desc())
        .fetch()
        .into(Documents.class);
  }

  public List<Documents> findPublic() {
    log.debug("Finding public documents");
    return dsl.selectFrom(DOCUMENTS)
        .where(DOCUMENTS.IS_PUBLIC.eq(true))
        .orderBy(DOCUMENTS.CREATED_AT.desc())
        .fetch()
        .into(Documents.class);
  }

  public List<Documents> search(String query, Long categoryId, String uploadedBy) {
    log.debug(
        "Searching documents: query={}, categoryId={}, uploadedBy={}",
        query,
        categoryId,
        uploadedBy);

    Condition condition = DSL.trueCondition();

    if (query != null && !query.isBlank()) {
      String searchPattern = "%" + query.toLowerCase() + "%";
      condition =
          condition.and(
              DOCUMENTS
                  .TITLE
                  .lower()
                  .like(searchPattern)
                  .or(DOCUMENTS.DESCRIPTION.lower().like(searchPattern))
                  .or(DOCUMENTS.FILE_NAME.lower().like(searchPattern)));
    }

    if (categoryId != null) {
      condition = condition.and(DOCUMENTS.CATEGORY_ID.eq(categoryId));
    }

    if (uploadedBy != null && !uploadedBy.isBlank()) {
      condition = condition.and(DOCUMENTS.UPLOADED_BY.eq(uploadedBy));
    }

    return dsl.selectFrom(DOCUMENTS)
        .where(condition)
        .orderBy(DOCUMENTS.CREATED_AT.desc())
        .fetch()
        .into(Documents.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if document exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(DOCUMENTS).where(DOCUMENTS.ID.eq(id)));
  }

  public long count() {
    return dsl.fetchCount(DOCUMENTS);
  }

  public long countByUploadedBy(String uploadedBy) {
    return dsl.fetchCount(dsl.selectFrom(DOCUMENTS).where(DOCUMENTS.UPLOADED_BY.eq(uploadedBy)));
  }
}
