package io.github.jpa_labs.uniquefield;

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

  public static void validateJpaAttributePath(String path, String label) {
    assertPropertyPathShape(path, label, false);
  }

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
      if (segment.isEmpty()) {
        throw new IllegalArgumentException("Invalid " + label + " (empty segment): " + path);
      }
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
