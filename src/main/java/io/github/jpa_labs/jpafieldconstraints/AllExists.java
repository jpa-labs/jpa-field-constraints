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

  /**
   * Message template used when validation fails.
   *
   * @return Bean Validation message template
   */
  String message() default "{io.github.jpa_labs.jpafieldconstraints.AllExists.message}";

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

  /** @return Name of the entity attribute (JavaBean property). */
  String column();

  /**
   * DTO property path used in type-level mode; must be blank for field/method/parameter placement.
   *
   * @return DTO property path for type-level validation, or blank for direct-value mode
   */
  String dtoField() default "";

  /**
   * When true, null/empty iterables and null/blank string elements are ignored. When false,
   * null/blank elements fail validation.
   *
   * @return whether null/empty inputs should be ignored
   */
  boolean ignoreNullOrEmpty() default true;

  /** @return whether string comparison should be case-insensitive. */
  boolean ignoreCase() default false;

  /** Container annotation that enables repeating {@link AllExists}. */
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
    /** @return repeated {@link AllExists} declarations. */
    AllExists[] value();
  }
}
