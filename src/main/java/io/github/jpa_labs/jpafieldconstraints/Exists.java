package io.github.jpa_labs.jpafieldconstraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Asserts that the annotated element's value already exists on {@link #entity()} for the given JPA
 * attribute {@link #column()} (entity property name, not database column name).
 *
 * <p>Field, method, or parameter: leave {@link #dtoField()} blank; the validated value is the
 * property value. For type-level validation, set {@link #dtoField()} to a bean property path on
 * the validated type (annotation target must be {@link ElementType#TYPE}).
 */
@Documented
@Constraint(validatedBy = ExistsValidator.class)
@Target({
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
  ElementType.TYPE,
  ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Exists.List.class)
public @interface Exists {

  /**
   * Message template used when validation fails.
   *
   * @return Bean Validation message template
   */
  String message() default "{io.github.jpa_labs.jpafieldconstraints.Exists.message}";

  /**
   * Bean Validation groups this constraint belongs to.
   *
   * @return validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload for clients of the Bean Validation API.
   *
   * @return payload types
   */
  Class<? extends Payload>[] payload() default {};

  /** @return JPA entity class to query. */
  Class<?> entity();

  /**
   * Name of the entity attribute (JavaBean property), e.g. {@code "id"} or {@code
   * "basicInfo.fanNumber"} for nested paths.
   *
   * @return entity attribute path used for the lookup
   */
  String column();

  /**
   * When non-blank, {@link ExistsValidator} treats the validated object as the root DTO and reads
   * the value to check from this property path (e.g. {@code "id"} or {@code "inner.id"}). Must be
   * blank when the annotation is placed on a field, method, or parameter.
   *
   * @return DTO property path for type-level validation, or blank for direct-value mode
   */
  String dtoField() default "";

  /**
   * When true, null and blank strings are considered valid (no DB check). When false, null fails
   * existence unless you also add {@code @NotNull}.
   *
   * @return whether null/blank values should be ignored
   */
  boolean ignoreNullOrEmpty() default true;

  /**
   * For string values only: compare using {@link String#equalsIgnoreCase(String)} in the database
   * query (implemented as lower() = lower() for portability).
   *
   * @return whether string comparison should be case-insensitive
   */
  boolean ignoreCase() default false;

  /**
   * Additional static equality filters joined with AND (for example status=ACTIVE).
   *
   * <p>Useful for scoped existence checks like "employee exists and is active".
   *
   * @return static filters applied to the existence query
   */
  Where[] where() default {};

  /** Static filter specification used by {@link #where()}. */
  @Documented
  @Target({})
  @Retention(RetentionPolicy.RUNTIME)
  @interface Where {
    /** @return Entity attribute path to filter on. */
    String column();

    /** @return Literal value compared with equality. */
    String value();

    /** @return whether filter comparison should ignore case for string attributes. */
    boolean ignoreCase() default false;
  }

  /** Container annotation that enables repeating {@link Exists}. */
  @Documented
  @Target({
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PARAMETER,
    ElementType.TYPE,
    ElementType.ANNOTATION_TYPE
  })
  @Retention(RetentionPolicy.RUNTIME)
  @interface List {
    /** @return repeated {@link Exists} declarations. */
    Exists[] value();
  }
}
