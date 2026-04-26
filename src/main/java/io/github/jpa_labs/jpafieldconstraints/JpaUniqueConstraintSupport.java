package io.github.jpa_labs.jpafieldconstraints;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

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
          Class<?> target = attr.getJavaType();
          current = entityManager.getMetamodel().entity(target);
        }
        case EMBEDDED -> {
          Class<?> embeddableClass = attr.getJavaType();
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
    return countRowsEqual(
        entityManager,
        entityClass,
        attributePath,
        value,
        ignoreCase,
        new EqualityQueryOptions(excludeEntityId, entityIdProperty, List.of()));
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
      EqualityQueryOptions options) {
    validateAttributePath(entityManager, entityClass, attributePath);
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    @SuppressWarnings("unchecked")
    Root<Object> root = (Root<Object>) cq.from(entityClass);
    Path<?> path = resolvePath(root, attributePath);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(buildEqualsPredicate(cb, path, value, ignoreCase));
    List<StaticEqualsFilter> additionalFilters = options.additionalFilters();
    if (additionalFilters != null && !additionalFilters.isEmpty()) {
      for (StaticEqualsFilter filter : additionalFilters) {
        validateAttributePath(entityManager, entityClass, filter.attributePath());
        Path<?> filterPath = resolvePath(root, filter.attributePath());
        Object typedLiteral = coerceLiteral(filter.value(), filterPath.getJavaType(), filter.attributePath());
        predicates.add(buildEqualsPredicate(cb, filterPath, typedLiteral, filter.ignoreCase()));
      }
    }
    if (options.excludeEntityId() != null) {
      Path<?> idPath = root.get(options.entityIdProperty());
      predicates.add(cb.notEqual(idPath, options.excludeEntityId()));
    }
    cq.select(cb.count(root)).where(predicates.toArray(Predicate[]::new));
    return entityManager.createQuery(cq).getSingleResult();
  }

  static long countRowsIn(
      EntityManager entityManager,
      Class<?> entityClass,
      String attributePath,
      Set<Object> values,
      boolean ignoreCase) {
    validateAttributePath(entityManager, entityClass, attributePath);
    if (values == null || values.isEmpty()) {
      return 0;
    }
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    @SuppressWarnings("unchecked")
    Root<Object> root = (Root<Object>) cq.from(entityClass);
    Path<?> path = resolvePath(root, attributePath);
    Predicate predicate = buildInPredicate(cb, path, values, ignoreCase);
    Expression<?> countExpr =
        ignoreCase && path.getJavaType() == String.class ? cb.lower(path.as(String.class)) : path;
    cq.select(cb.countDistinct(countExpr)).where(predicate);
    return entityManager.createQuery(cq).getSingleResult();
  }

  @SuppressWarnings("unchecked")
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

  private static Predicate buildInPredicate(
      CriteriaBuilder cb, Path<?> path, Set<Object> values, boolean ignoreCase) {
    if (ignoreCase && path.getJavaType() == String.class) {
      Set<String> normalized = new LinkedHashSet<>();
      for (Object value : values) {
        if (!(value instanceof String s)) {
          return cb.disjunction();
        }
        normalized.add(s.toLowerCase(Locale.ROOT));
      }
      Expression<String> loweredPath = cb.lower(path.as(String.class));
      return loweredPath.in(normalized);
    }
    return path.in(values);
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

  /**
   * Coerces {@link Exists.Where#value()} into the target JPA attribute type.
   *
   * <p>Supported target types are:
   *
   * <ul>
   *   <li>{@link String}
   *   <li>{@link Boolean}/{@code boolean}
   *   <li>numeric primitives and wrappers: {@code byte}/{@link Byte}, {@code short}/{@link Short},
   *       {@code int}/{@link Integer}, {@code long}/{@link Long}, {@code float}/{@link Float},
   *       {@code double}/{@link Double}
   *   <li>{@link Character}/{@code char}
   *   <li>{@link UUID}
   *   <li>enums (via {@link #parseEnumLiteral(Class, String)})
   * </ul>
   *
   * <p>Date/time types ({@code LocalDate}, {@code LocalDateTime}, {@code Instant}) are currently
   * unsupported. {@code BigInteger}/{@code BigDecimal} are also not yet supported.
   *
   * <p>This method throws {@link IllegalArgumentException} when a target type is unsupported.
   * Extend this method to add support for additional literal coercions.
   */
  private static Object coerceLiteral(String literal, Class<?> targetType, String attributePath) {
    if (targetType == String.class) {
      return literal;
    }
    if (targetType == Boolean.class || targetType == boolean.class) {
      return Boolean.parseBoolean(literal);
    }
    if (targetType == Byte.class || targetType == byte.class) {
      return Byte.parseByte(literal);
    }
    if (targetType == Character.class || targetType == char.class) {
      if (literal.length() != 1) {
        throw new IllegalArgumentException(
            "where.value must be a single character for attribute '" + attributePath + "'");
      }
      return literal.charAt(0);
    }
    if (targetType == Integer.class || targetType == int.class) {
      return Integer.parseInt(literal);
    }
    if (targetType == Long.class || targetType == long.class) {
      return Long.parseLong(literal);
    }
    if (targetType == Short.class || targetType == short.class) {
      return Short.parseShort(literal);
    }
    if (targetType == Double.class || targetType == double.class) {
      return Double.parseDouble(literal);
    }
    if (targetType == Float.class || targetType == float.class) {
      return Float.parseFloat(literal);
    }
    if (targetType == UUID.class) {
      return UUID.fromString(literal);
    }
    if (Enum.class.isAssignableFrom(targetType)) {
      return parseEnumLiteral(targetType, literal);
    }
    throw new IllegalArgumentException(
        "Unsupported where.value type '" + targetType.getName() + "' for attribute '" + attributePath + "'");
  }

  @SuppressWarnings({"unchecked"})
  private static <E extends Enum<E>> E parseEnumLiteral(Class<?> enumType, String literal) {
    return Enum.valueOf((Class<E>) enumType.asSubclass(Enum.class), literal);
  }

  record EqualityQueryOptions(
      Object excludeEntityId, String entityIdProperty, List<StaticEqualsFilter> additionalFilters) {}

  record StaticEqualsFilter(String attributePath, String value, boolean ignoreCase) {}
}
