package io.github.jpa_labs.uniquefield;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
  UniqueFieldValidator.class,
  UniqueFieldsValidator.class,
  ExistsValidator.class,
  AllExistsValidator.class
})
public class UniqueFieldValidationAutoConfiguration {}
