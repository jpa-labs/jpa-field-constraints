package io.github.jpa_labs.jpafieldconstraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

class ValidatorNullBranchUnitTest {

  @Test
  void uniqueFieldTypeLevelReturnsTrueForNullRootDto() {
    UniqueField annotation = UniqueFieldTypeLevelDto.class.getAnnotation(UniqueField.class);
    UniqueFieldValidator validator = new UniqueFieldValidator();
    validator.initialize(annotation);

    boolean valid = validator.isValid(null, mock(ConstraintValidatorContext.class));

    assertThat(valid).isTrue();
  }

  @Test
  void existsTypeLevelReturnsTrueForNullRootDto() {
    Exists annotation = ExistsTypeLevelDto.class.getAnnotation(Exists.class);
    ExistsValidator validator = new ExistsValidator();
    validator.initialize(annotation);

    boolean valid = validator.isValid(null, mock(ConstraintValidatorContext.class));

    assertThat(valid).isTrue();
  }

  @Test
  void allExistsTypeLevelReturnsTrueForNullRootDto() {
    AllExists annotation = AllExistsTypeLevelDto.class.getAnnotation(AllExists.class);
    AllExistsValidator validator = new AllExistsValidator();
    validator.initialize(annotation);

    boolean valid = validator.isValid(null, mock(ConstraintValidatorContext.class));

    assertThat(valid).isTrue();
  }

  @Test
  void uniqueFieldsReturnsTrueForNullRootDto() {
    UniqueFields annotation = UniqueFieldsTypeLevelDto.class.getAnnotation(UniqueFields.class);
    UniqueFieldsValidator validator = new UniqueFieldsValidator();
    validator.initialize(annotation);

    boolean valid = validator.isValid(null, mock(ConstraintValidatorContext.class));

    assertThat(valid).isTrue();
  }

  @UniqueField(entity = SampleEntity.class, column = "code", dtoField = "code")
  static class UniqueFieldTypeLevelDto {}

  @Exists(entity = SampleEntity.class, column = "code", dtoField = "code")
  static class ExistsTypeLevelDto {}

  @AllExists(entity = SampleEntity.class, column = "code", dtoField = "codes")
  static class AllExistsTypeLevelDto {}

  @UniqueFields({
    @UniqueField(entity = SampleEntity.class, column = "code", dtoField = "code")
  })
  static class UniqueFieldsTypeLevelDto {}
}
