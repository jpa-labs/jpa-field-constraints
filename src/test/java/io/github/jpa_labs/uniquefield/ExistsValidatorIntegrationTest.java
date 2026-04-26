package io.github.jpa_labs.uniquefield;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(
    properties = {
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never",
    })
class ExistsValidatorIntegrationTest {

  @Autowired private Validator validator;

  @Autowired private SampleEntityRepository repository;

  @Nested
  class FieldLevel {

    @Test
    void passesWhenValueExists() {
      repository.save(new SampleEntity(null, "known-code", null));
      Set<ConstraintViolation<FieldLevelDto>> violations =
          validator.validate(new FieldLevelDto("known-code"));
      assertThat(violations).isEmpty();
    }

    @Test
    void rejectsWhenValueDoesNotExist() {
      repository.save(new SampleEntity(null, "known-code", null));
      Set<ConstraintViolation<FieldLevelDto>> violations =
          validator.validate(new FieldLevelDto("missing-code"));
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("code");
    }
  }

  @Nested
  class TypeLevel {

    @Test
    void validatesNestedDtoFieldPath() {
      repository.save(new SampleEntity(null, "nested", null));
      Set<ConstraintViolation<TypeLevelNestedDto>> violations =
          validator.validate(new TypeLevelNestedDto(new InnerCode("nested")));
      assertThat(violations).isEmpty();
    }

    @Test
    void rejectsMissingNestedValue() {
      Set<ConstraintViolation<TypeLevelNestedDto>> violations =
          validator.validate(new TypeLevelNestedDto(new InnerCode("absent")));
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("inner.code");
    }
  }

  static class FieldLevelDto {

    @Exists(entity = SampleEntity.class, column = "code")
    private final String code;

    FieldLevelDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  @Exists(entity = SampleEntity.class, column = "code", dtoField = "inner.code")
  static class TypeLevelNestedDto {
    private final InnerCode inner;

    TypeLevelNestedDto(InnerCode inner) {
      this.inner = inner;
    }

    public InnerCode getInner() {
      return inner;
    }
  }

  static class InnerCode {
    private final String code;

    InnerCode(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

}
