package tn.cyberious.compta.document.repository;

import static tn.cyberious.compta.document.generated.Tables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.document.generated.tables.pojos.Tags;
import tn.cyberious.compta.document.generated.tables.records.TagsRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TagRepository {

  private final DSLContext dsl;

  public Tags insert(Tags tag) {
    log.debug("Inserting tag: {}", tag.getName());

    TagsRecord record =
        dsl.insertInto(TAGS)
            .set(TAGS.NAME, tag.getName())
            .set(TAGS.CREATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(Tags.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting tag: {}", id);
    int deleted = dsl.deleteFrom(TAGS).where(TAGS.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Tags> findById(Long id) {
    log.debug("Finding tag by id: {}", id);
    return dsl.selectFrom(TAGS)
        .where(TAGS.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Tags.class));
  }

  public Optional<Tags> findByName(String name) {
    log.debug("Finding tag by name: {}", name);
    return dsl.selectFrom(TAGS)
        .where(TAGS.NAME.eq(name))
        .fetchOptional()
        .map(record -> record.into(Tags.class));
  }

  public Tags findOrCreate(String name) {
    log.debug("Finding or creating tag: {}", name);
    return findByName(name)
        .orElseGet(
            () -> {
              Tags newTag = new Tags();
              newTag.setName(name);
              return insert(newTag);
            });
  }

  public List<Tags> findAll() {
    log.debug("Finding all tags");
    return dsl.selectFrom(TAGS).orderBy(TAGS.NAME).fetch().into(Tags.class);
  }

  public List<Tags> searchByName(String query) {
    log.debug("Searching tags by name: {}", query);
    return dsl.selectFrom(TAGS)
        .where(TAGS.NAME.lower().like("%" + query.toLowerCase() + "%"))
        .orderBy(TAGS.NAME)
        .fetch()
        .into(Tags.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if tag exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(TAGS).where(TAGS.ID.eq(id)));
  }

  public boolean existsByName(String name) {
    return dsl.fetchExists(dsl.selectFrom(TAGS).where(TAGS.NAME.eq(name)));
  }
}
