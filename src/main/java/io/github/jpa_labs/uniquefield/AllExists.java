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
 * Asserts that every value in the annotated iterable exists on {@link #entity()} for {@link
 * #column()}.
 *
 * <p>Field, method, or parameter: leave {@link #dtoField()} blank and annotate an iterable value.
 * For type-level validation, set {@link #dtoField()} to a DTO property path that resolves to an
 * iterable.
 */
@Documented
@Constraint(validatedBy = AllExistsValidator.class)
@Target({
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
  ElementType.TYPE,
  ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllExists.List.class)
public @interface AllExists {

  String message() default "{io.github.jpa_labs.uniquefield.AllExists.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /** JPA entity class to query. */
  Class<?> entity();

  /** Name of the entity attribute (JavaBean property). */
  String column();

  /**
   * DTO property path used in type-level mode; must be blank for field/method/parameter placement.
   */
  String dtoField() default "";

  /**
   * When true, null/empty iterables and null/blank string elements are ignored. When false,
   * null/blank elements fail validation.
   */
  boolean ignoreNullOrEmpty() default true;

  /** For string values only, compare case-insensitively. */
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
    AllExists[] value();
  }
}
