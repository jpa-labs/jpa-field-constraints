package io.github.jpa_labs.jpafieldconstraints;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration that registers all JPA-backed Bean Validation constraint validators.
 */
@AutoConfiguration
@Import({
  UniqueFieldValidator.class,
  UniqueFieldsValidator.class,
  ExistsValidator.class,
  AllExistsValidator.class
})
public class UniqueFieldValidationAutoConfiguration {}
