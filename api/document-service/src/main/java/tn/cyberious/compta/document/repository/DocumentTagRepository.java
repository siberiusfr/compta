package tn.cyberious.compta.document.repository;

import static tn.cyberious.compta.document.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.document.generated.tables.pojos.Tags;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentTagRepository {

  private final DSLContext dsl;

  public void addTagToDocument(Long documentId, Long tagId) {
    log.debug("Adding tag {} to document {}", tagId, documentId);

    dsl.insertInto(DOCUMENT_TAGS)
        .set(DOCUMENT_TAGS.DOCUMENT_ID, documentId)
        .set(DOCUMENT_TAGS.TAG_ID, tagId)
        .set(DOCUMENT_TAGS.CREATED_AT, LocalDateTime.now())
        .onDuplicateKeyIgnore()
        .execute();
  }

  public void removeTagFromDocument(Long documentId, Long tagId) {
    log.debug("Removing tag {} from document {}", tagId, documentId);

    dsl.deleteFrom(DOCUMENT_TAGS)
        .where(DOCUMENT_TAGS.DOCUMENT_ID.eq(documentId).and(DOCUMENT_TAGS.TAG_ID.eq(tagId)))
        .execute();
  }

  public void removeAllTagsFromDocument(Long documentId) {
    log.debug("Removing all tags from document {}", documentId);

    dsl.deleteFrom(DOCUMENT_TAGS).where(DOCUMENT_TAGS.DOCUMENT_ID.eq(documentId)).execute();
  }

  public List<Tags> findTagsByDocumentId(Long documentId) {
    log.debug("Finding tags for document {}", documentId);

    return dsl.select(TAGS.fields())
        .from(TAGS)
        .join(DOCUMENT_TAGS)
        .on(DOCUMENT_TAGS.TAG_ID.eq(TAGS.ID))
        .where(DOCUMENT_TAGS.DOCUMENT_ID.eq(documentId))
        .orderBy(TAGS.NAME)
        .fetch()
        .into(Tags.class);
  }

  public List<Long> findDocumentIdsByTagId(Long tagId) {
    log.debug("Finding document IDs for tag {}", tagId);

    return dsl.select(DOCUMENT_TAGS.DOCUMENT_ID)
        .from(DOCUMENT_TAGS)
        .where(DOCUMENT_TAGS.TAG_ID.eq(tagId))
        .fetch(DOCUMENT_TAGS.DOCUMENT_ID);
  }

  public List<Long> findDocumentIdsByTagName(String tagName) {
    log.debug("Finding document IDs for tag name {}", tagName);

    return dsl.select(DOCUMENT_TAGS.DOCUMENT_ID)
        .from(DOCUMENT_TAGS)
        .join(TAGS)
        .on(TAGS.ID.eq(DOCUMENT_TAGS.TAG_ID))
        .where(TAGS.NAME.eq(tagName))
        .fetch(DOCUMENT_TAGS.DOCUMENT_ID);
  }

  public boolean hasTag(Long documentId, Long tagId) {
    return dsl.fetchExists(
        dsl.selectFrom(DOCUMENT_TAGS)
            .where(DOCUMENT_TAGS.DOCUMENT_ID.eq(documentId).and(DOCUMENT_TAGS.TAG_ID.eq(tagId))));
  }
}
