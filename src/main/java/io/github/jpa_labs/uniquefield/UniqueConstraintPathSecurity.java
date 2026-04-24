package io.github.jpa_labs.uniquefield;

/**
 * Runtime entry points for uniqueness constraint path hardening; delegates to {@link
 * UniqueConstraintStaticRules} so the same rules apply at compile time via the annotation
 * processor.
 */
final class UniqueConstraintPathSecurity {

  private UniqueConstraintPathSecurity() {}

  static void assertJpaEntityClass(Class<?> entityClass) {
    UniqueConstraintStaticRules.validateEntityClass(entityClass);
  }

  static void assertEntityIdPropertyName(String name, String label) {
    UniqueConstraintStaticRules.validateEntityIdPropertyName(name, label);
  }

  static void assertJpaAttributePath(String path, String label) {
    UniqueConstraintStaticRules.validateJpaAttributePath(path, label);
  }

  static void assertDtoPropertyPath(String path, String label) {
    UniqueConstraintStaticRules.validateDtoPropertyPath(path, label);
  }
}
