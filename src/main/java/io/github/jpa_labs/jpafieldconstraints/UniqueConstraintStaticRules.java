package io.github.jpa_labs.jpafieldconstraints;

import jakarta.persistence.Entity;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared validation for {@link UniqueField} / {@link UniqueFields} configuration, used at runtime
 * and by the bundled {@code UniqueConstraintAnnotationProcessor} at compile time so
 * misconfiguration can fail the build when this library is on the {@code annotationProcessor}
 * classpath.
 */
public final class UniqueConstraintStaticRules {

  private UniqueConstraintStaticRules() {}

  private static final Pattern PROPERTY_PATH =
      Pattern.compile("^[a-zA-Z_]\\w*(?:\\.[a-zA-Z_]\\w*)*$");

  private static final Pattern ENTITY_ID_PROPERTY = Pattern.compile("^[a-zA-Z_]\\w*$");

  private static final int MAX_PROPERTY_PATH_LENGTH = 384;

  private static final int MAX_PATH_SEGMENTS = 32;

  private static final Set<String> BLOCKED_DTO_PATH_SEGMENTS =
      Set.of(
          "class",
          "classloader",
          "protectiondomain",
          "module",
          "package",
          "declaredfields",
          "declaredmethods",
          "declaredconstructors",
          "signers",
          "stacktrace",
          "annotatedsuperclass",
          "enclosingclass",
          "enclosingmethod");

  /**
   * Validates that a configured entity class is usable for JPA-backed constraint checks.
   *
   * @param entityClass candidate entity class
   * @throws IllegalArgumentException when class is null, primitive/array, or not annotated
   *     {@link Entity}
   */
  public static void validateEntityClass(Class<?> entityClass) {
    if (entityClass == null) {
      throw new IllegalArgumentException("entity class must not be null");
    }
    if (entityClass.isPrimitive() || entityClass.isArray()) {
      throw new IllegalArgumentException("entity must be a non-array reference type");
    }
    if (!entityClass.isAnnotationPresent(Entity.class)) {
      throw new IllegalArgumentException(
          "entity must be annotated with @jakarta.persistence.Entity: " + entityClass.getName());
    }
  }

  /**
   * Validates an entity id property name used for update exclusion checks.
   *
   * @param name configured id property name
   * @param label logical field name used in exception messages
   * @throws IllegalArgumentException when the value is blank, nested, malformed, or blocked
   */
  public static void validateEntityIdPropertyName(String name, String label) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    if (name.contains(".")) {
      throw new IllegalArgumentException(label + " must be a single property name, got: " + name);
    }
    if (!ENTITY_ID_PROPERTY.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid " + label + ": " + name);
    }
    assertNoBlockedSegment(name, label, false);
  }

  /**
   * Validates a JPA attribute path used in constraints (e.g. {@code "email"} or
   * {@code "profile.login"}).
   *
   * @param path configured entity attribute path
   * @param label logical field name used in exception messages
   * @throws IllegalArgumentException when the path is blank, malformed, or too deep/long
   */
  public static void validateJpaAttributePath(String path, String label) {
    assertPropertyPathShape(path, label, false);
  }

  /**
   * Validates a DTO property path resolved through {@code BeanWrapper}.
   *
   * @param path configured DTO path (e.g. {@code "request.id"})
   * @param label logical field name used in exception messages
   * @throws IllegalArgumentException when the path is blank, malformed, too deep/long, or includes
   *     blocked segments
   */
  public static void validateDtoPropertyPath(String path, String label) {
    assertPropertyPathShape(path, label, true);
  }

  private static void assertPropertyPathShape(String path, String label, boolean dtoPath) {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    if (path.length() > MAX_PROPERTY_PATH_LENGTH) {
      throw new IllegalArgumentException(
          label + " exceeds maximum length (" + MAX_PROPERTY_PATH_LENGTH + " characters)");
    }
    if (!PROPERTY_PATH.matcher(path).matches()) {
      throw new IllegalArgumentException("Invalid " + label + ": " + path);
    }
    String[] segments = path.split("\\.", -1);
    if (segments.length > MAX_PATH_SEGMENTS) {
      throw new IllegalArgumentException(
          label + " exceeds maximum depth (" + MAX_PATH_SEGMENTS + " segments)");
    }
    for (String segment : segments) {
      if (dtoPath) {
        assertNoBlockedSegment(segment, label, true);
      }
    }
  }

  private static void assertNoBlockedSegment(String segment, String label, boolean nestedInPath) {
    String key = segment.toLowerCase(Locale.ROOT);
    if (BLOCKED_DTO_PATH_SEGMENTS.contains(key)) {
      throw new IllegalArgumentException(
          "Unsafe "
              + label
              + " segment '"
              + segment
              + "' is not allowed"
              + (nestedInPath ? "" : " for this property"));
    }
  }
}
