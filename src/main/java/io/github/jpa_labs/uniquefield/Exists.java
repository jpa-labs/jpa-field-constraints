package io.github.jpa_labs.uniquefield;

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

  String message() default "{io.github.jpa_labs.uniquefield.Exists.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /** JPA entity class to query. */
  Class<?> entity();

  /**
   * Name of the entity attribute (JavaBean property), e.g. {@code "id"} or {@code
   * "basicInfo.fanNumber"} for nested paths.
   */
  String column();

  /**
   * When non-blank, {@link ExistsValidator} treats the validated object as the root DTO and reads
   * the value to check from this property path (e.g. {@code "id"} or {@code "inner.id"}). Must be
   * blank when the annotation is placed on a field, method, or parameter.
   */
  String dtoField() default "";

  /**
   * When true, null and blank strings are considered valid (no DB check). When false, null fails
   * existence unless you also add {@code @NotNull}.
   */
  boolean ignoreNullOrEmpty() default true;

  /**
   * For string values only: compare using {@link String#equalsIgnoreCase(String)} in the database
   * query (implemented as lower() = lower() for portability).
   */
  boolean ignoreCase() default false;

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
    Exists[] value();
  }
}
