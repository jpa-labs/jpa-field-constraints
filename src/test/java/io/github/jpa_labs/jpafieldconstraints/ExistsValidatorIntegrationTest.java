package io.github.jpa_labs.jpafieldconstraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestApplication.class)
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

    @Test
    void passesWhenIgnoreCaseEnabled() {
      String stored = "known-" + UUID.randomUUID();
      repository.save(new SampleEntity(null, stored, null));
      Set<ConstraintViolation<IgnoreCaseFieldLevelDto>> violations =
          validator.validate(new IgnoreCaseFieldLevelDto(stored.toLowerCase()));
      assertThat(violations).isEmpty();
    }

    @Test
    void rejectsNullWhenIgnoreNullOrEmptyDisabled() {
      Set<ConstraintViolation<StrictFieldLevelDto>> violations =
          validator.validate(new StrictFieldLevelDto(null));
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("code");
    }

    @Test
    void allowsBlankWhenIgnoreNullOrEmptyEnabled() {
      Set<ConstraintViolation<FieldLevelDto>> violations = validator.validate(new FieldLevelDto("   "));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClausesMatchSameRow() {
      repository.save(new SampleEntity(null, "emp-001", null, "ACTIVE", "STAFF"));
      repository.save(new SampleEntity(null, "emp-001", null, "INACTIVE", "STAFF"));
      Set<ConstraintViolation<ActiveEmployeeDto>> violations =
          validator.validate(new ActiveEmployeeDto("emp-001"));
      assertThat(violations).isEmpty();
    }

    @Test
    void rejectsWhenWhereClausesDoNotMatch() {
      repository.save(new SampleEntity(null, "emp-001", null, "INACTIVE", "STAFF"));
      repository.save(new SampleEntity(null, "emp-001", null, "ACTIVE", "USER"));
      Set<ConstraintViolation<ActiveAdminDto>> violations =
          validator.validate(new ActiveAdminDto("emp-001"));
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("code");
    }

    @Test
    void failsWhenEntityColumnPathHasInvalidMidSegmentType() {
      InvalidEntityPathDto dto = new InvalidEntityPathDto("v");
      assertThatThrownBy(() -> validator.validate(dto))
          .rootCause()
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must be an association or embeddable");
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

    @Test
    void rejectsNullNestedValueWhenIgnoreNullOrEmptyDisabled() {
      Set<ConstraintViolation<StrictTypeLevelNestedDto>> violations =
          validator.validate(new StrictTypeLevelNestedDto(new InnerCode(null)));
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

  static class IgnoreCaseFieldLevelDto {

    @Exists(entity = SampleEntity.class, column = "code", ignoreCase = true)
    private final String code;

    IgnoreCaseFieldLevelDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class StrictFieldLevelDto {

    @Exists(entity = SampleEntity.class, column = "code", ignoreNullOrEmpty = false)
    private final String code;

    StrictFieldLevelDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class ActiveEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "status", value = "ACTIVE")})
    private final String code;

    ActiveEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class ActiveAdminDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {
          @Exists.Where(column = "status", value = "ACTIVE"),
          @Exists.Where(column = "role", value = "ADMIN")
        })
    private final String code;

    ActiveAdminDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class InvalidEntityPathDto {

    @Exists(entity = SampleEntity.class, column = "code.inner")
    private final String code;

    InvalidEntityPathDto(String code) {
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

  @Exists(
      entity = SampleEntity.class,
      column = "code",
      dtoField = "inner.code",
      ignoreNullOrEmpty = false)
  static class StrictTypeLevelNestedDto {
    private final InnerCode inner;

    StrictTypeLevelNestedDto(InnerCode inner) {
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
