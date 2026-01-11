package tn.cyberious.compta.document.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static tn.cyberious.compta.document.generated.Tables.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.cyberious.compta.document.generated.tables.pojos.Categories;
import tn.cyberious.compta.document.generated.tables.records.CategoriesRecord;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryRepository {

  private final DSLContext dsl;

  public Categories insert(Categories category) {
    log.debug("Inserting category: {}", category.getName());

    CategoriesRecord record =
        dsl.insertInto(CATEGORIES)
            .set(CATEGORIES.NAME, category.getName())
            .set(CATEGORIES.DESCRIPTION, category.getDescription())
            .set(CATEGORIES.PARENT_CATEGORY_ID, category.getParentCategoryId())
            .set(CATEGORIES.CREATED_AT, LocalDateTime.now())
            .set(CATEGORIES.UPDATED_AT, LocalDateTime.now())
            .returning()
            .fetchOne();

    return record != null ? record.into(Categories.class) : null;
  }

  public Categories update(Categories category) {
    log.debug("Updating category: {}", category.getId());

    CategoriesRecord record =
        dsl.update(CATEGORIES)
            .set(CATEGORIES.NAME, category.getName())
            .set(CATEGORIES.DESCRIPTION, category.getDescription())
            .set(CATEGORIES.PARENT_CATEGORY_ID, category.getParentCategoryId())
            .set(CATEGORIES.UPDATED_AT, LocalDateTime.now())
            .where(CATEGORIES.ID.eq(category.getId()))
            .returning()
            .fetchOne();

    return record != null ? record.into(Categories.class) : null;
  }

  public boolean delete(Long id) {
    log.debug("Deleting category: {}", id);
    int deleted = dsl.deleteFrom(CATEGORIES).where(CATEGORIES.ID.eq(id)).execute();
    return deleted > 0;
  }

  public Optional<Categories> findById(Long id) {
    log.debug("Finding category by id: {}", id);
    return dsl.selectFrom(CATEGORIES)
        .where(CATEGORIES.ID.eq(id))
        .fetchOptional()
        .map(record -> record.into(Categories.class));
  }

  public Optional<Categories> findByName(String name) {
    log.debug("Finding category by name: {}", name);
    return dsl.selectFrom(CATEGORIES)
        .where(CATEGORIES.NAME.eq(name))
        .fetchOptional()
        .map(record -> record.into(Categories.class));
  }

  public List<Categories> findAll() {
    log.debug("Finding all categories");
    return dsl.selectFrom(CATEGORIES).fetch().into(Categories.class);
  }

  public List<Categories> findByParentId(Long parentId) {
    log.debug("Finding categories by parent id: {}", parentId);
    if (parentId == null) {
      return dsl.selectFrom(CATEGORIES)
          .where(CATEGORIES.PARENT_CATEGORY_ID.isNull())
          .fetch()
          .into(Categories.class);
    }
    return dsl.selectFrom(CATEGORIES)
        .where(CATEGORIES.PARENT_CATEGORY_ID.eq(parentId))
        .fetch()
        .into(Categories.class);
  }

  public boolean exists(Long id) {
    log.debug("Checking if category exists: {}", id);
    return dsl.fetchExists(dsl.selectFrom(CATEGORIES).where(CATEGORIES.ID.eq(id)));
  }

  public boolean existsByName(String name) {
    return dsl.fetchExists(dsl.selectFrom(CATEGORIES).where(CATEGORIES.NAME.eq(name)));
  }
}
