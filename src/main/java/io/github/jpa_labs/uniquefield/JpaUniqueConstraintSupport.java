package io.github.jpa_labs.uniquefield;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class JpaUniqueConstraintSupport {

  private JpaUniqueConstraintSupport() {}

  static void validateAttributePath(EntityManager entityManager, Class<?> entityClass, String attributePath) {
    EntityType<?> rootEntity = entityManager.getMetamodel().entity(entityClass);
    ManagedType<?> current = rootEntity;
    String[] segments = attributePath.split("\\.");
    for (int i = 0; i < segments.length; i++) {
      String segment = segments[i];
      Attribute<?, ?> attr = current.getAttribute(segment);
      boolean last = i == segments.length - 1;
      if (last) {
        return;
      }
      switch (attr.getPersistentAttributeType()) {
        case MANY_TO_ONE, ONE_TO_ONE -> {
          @SuppressWarnings("unchecked")
          Class<?> target = (Class<?>) attr.getJavaType();
          current = entityManager.getMetamodel().entity(target);
        }
        case EMBEDDED -> {
          @SuppressWarnings("unchecked")
          Class<?> embeddableClass = (Class<?>) attr.getJavaType();
          EmbeddableType<?> embeddable =
              entityManager.getMetamodel().embeddable(embeddableClass);
          current = embeddable;
        }
        default ->
            throw new IllegalArgumentException(
                "Path segment '"
                    + segment
                    + "' in '"
                    + attributePath
                    + "' must be an association or embeddable when not the final segment");
      }
    }
  }

  /**
   * @param excludeEntityId when non-null, rows whose {@code entityIdProperty} equals this id are
   *     ignored (update / same-row semantics)
   */
  static long countRowsEqual(
      EntityManager entityManager,
      Class<?> entityClass,
      String attributePath,
      Object value,
      boolean ignoreCase,
      Object excludeEntityId,
      String entityIdProperty) {
    validateAttributePath(entityManager, entityClass, attributePath);
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    @SuppressWarnings("unchecked")
    Root<Object> root = (Root<Object>) cq.from(entityClass);
    Path<?> path = resolvePath(root, attributePath);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(buildEqualsPredicate(cb, path, value, ignoreCase));
    if (excludeEntityId != null) {
      Path<?> idPath = root.get(entityIdProperty);
      predicates.add(cb.notEqual(idPath, excludeEntityId));
    }
    cq.select(cb.count(root)).where(predicates.toArray(Predicate[]::new));
    return entityManager.createQuery(cq).getSingleResult();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static Predicate buildEqualsPredicate(
      CriteriaBuilder cb, Path<?> path, Object value, boolean ignoreCase) {
    if (ignoreCase && value instanceof String s) {
      Expression<String> pathExpr = (Expression<String>) path;
      Expression<String> lowerPath = cb.lower(pathExpr);
      Expression<String> lowerValue = cb.literal(s.toLowerCase(Locale.ROOT));
      return cb.equal(lowerPath, lowerValue);
    }
    return cb.equal(path, value);
  }

  private static Path<?> resolvePath(Root<?> root, String path) {
    String[] segments = path.split("\\.");
    Path<?> current = root;
    for (String segment : segments) {
      current = current.get(segment);
    }
    return current;
  }

  static boolean isEmptyValue(Object value, boolean ignoreNullOrEmpty) {
    if (!ignoreNullOrEmpty) {
      return false;
    }
    if (value == null) {
      return true;
    }
    if (value instanceof String s) {
      return s.isBlank();
    }
    return false;
  }
}
