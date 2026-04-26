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
 * Asserts that the annotated element's value is not already present on {@link #entity()} for the
 * given JPA attribute {@link #column()} (entity property name, not database column name).
 *
 * <p>Requires Spring Boot with JPA and this library's auto-configuration (validator must be a
 * Spring bean so {@link jakarta.persistence.EntityManager} is injected).
 *
 * <p><b>Compile-time validation:</b> add this artifact as an {@code annotationProcessor} dependency
 * (same Maven coordinates as {@code implementation}) so invalid annotation usage fails compilation,
 * not only at runtime when the constraint initializes.
 *
 * <p>Field, method, or parameter: leave {@link #dtoField()} blank; the validated value is the
 * property value. For type-level validation, set {@link #dtoField()} to a bean property path on
 * the validated type (annotation target must be {@link ElementType#TYPE}).
 *
 * <p>Example:
 *
 * <pre>{@code
 * public class RegisterResidentRequest {
 *   @UniqueField(entity = Resident.class, column = "urid")
 *   private String urid;
 * }
 * }</pre>
 */
@Documented
@Constraint(validatedBy = UniqueFieldValidator.class)
@Target({
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
  ElementType.TYPE,
  ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UniqueField.List.class)
public @interface UniqueField {

  String message() default "{io.github.jpa_labs.jpafieldconstraints.UniqueField.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /** JPA entity class to query. */
  Class<?> entity();

  /**
   * Name of the entity attribute (JavaBean property) that must be unique, e.g. {@code "urid"} or
   * {@code "basicInfo.fanNumber"} for nested paths.
   */
  String column();

  /**
   * When non-blank, {@link UniqueFieldValidator} treats the validated object as the root DTO and
   * reads the value to check from this property path (e.g. {@code "code"} or {@code "inner.code"}
   * ). Must be blank when the annotation is placed on a field, method, or parameter.
   */
  String dtoField() default "";

  /**
   * When {@link #dtoField()} is non-blank, optional property path on the root DTO for the entity
   * id to exclude from the uniqueness check (same-row / update semantics). Must be blank when
   * {@link #dtoField()} is blank.
   *
   * <p><b>Security:</b> this only affects uniqueness checking. Callers must still authorize that
   * the principal may act on the row identified by this id (otherwise a client could attempt
   * id-guessing together with duplicate values; real protection belongs in your service layer).
   */
  String excludeIdDtoField() default "";

  /**
   * Entity id property name used with {@link #excludeIdDtoField()} (ignored when no exclude id is
   * provided).
   */
  String entityIdProperty() default "id";

  /**
   * When true, null and blank strings are considered valid (no DB check). When false, null fails
   * uniqueness only if you also add {@code @NotNull}.
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
    UniqueField[] value();
  }
}
