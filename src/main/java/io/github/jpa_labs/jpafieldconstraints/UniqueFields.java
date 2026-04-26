package io.github.jpa_labs.jpafieldconstraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container for several {@link UniqueField} rules on a single DTO type. Each nested rule must
 * declare a non-blank {@link UniqueField#dtoField()}.
 *
 * <p>Paths and entity types are validated when constraints initialize: only JPA {@code @Entity}
 * classes, bounded property path depth and length, and a small blocklist of unsafe DTO path
 * segments for {@code BeanWrapper} access.
 */
@Documented
@Constraint(validatedBy = UniqueFieldsValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueFields {

  /**
   * Message template used when any nested uniqueness rule fails.
   *
   * @return Bean Validation message template
   */
  String message() default "{io.github.jpa_labs.jpafieldconstraints.UniqueFields.message}";

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

  /** @return The set of nested uniqueness rules to apply. */
  UniqueField[] value();
}
