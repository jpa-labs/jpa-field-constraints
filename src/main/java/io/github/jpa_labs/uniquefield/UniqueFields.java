package io.github.jpa_labs.uniquefield;

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

  String message() default "{io.github.jpa_labs.uniquefield.UniqueFields.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  UniqueField[] value();
}
