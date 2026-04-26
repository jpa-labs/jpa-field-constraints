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
class UniqueFieldValidatorIntegrationTest {

  @Autowired private Validator validator;

  @Autowired private SampleEntityRepository repository;

  @Autowired private CustomPkEntityRepository customPkRepository;

  @Nested
  class FieldLevelUnchanged {

    @Test
    void allowsUniqueValue() {
      repository.save(new SampleEntity(null, "existing", null));
      var dto = new SampleDto("other");
      Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);
      assertThat(violations).isEmpty();
    }

    @Test
    void rejectsDuplicateValue() {
      repository.save(new SampleEntity(null, "taken", null));
      var dto = new SampleDto("taken");
      Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("code");
    }
  }

  @Nested
  class TypeLevelWithExclude {

    @Test
    void allowsSameRowWhenIdExcluded() {
      SampleEntity saved = repository.save(new SampleEntity(null, "alpha", null));
      var dto = new TypeLevelUpdateDto(saved.getId(), "alpha");
      assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void rejectsDuplicateFromAnotherRow() {
      repository.save(new SampleEntity(null, "shared", null));
      var dto = new TypeLevelUpdateDto(UUID.randomUUID(), "shared");
      Set<ConstraintViolation<TypeLevelUpdateDto>> violations = validator.validate(dto);
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("code");
    }
  }

  @Nested
  class UniqueFieldsContainer {

    @Test
    void multipleRulesBothViolated() {
      repository.save(new SampleEntity(null, "c1", "s1"));
      var dto = new MultiUniqueFieldsDto("c1", "s1");
      Set<ConstraintViolation<MultiUniqueFieldsDto>> violations = validator.validate(dto);
      assertThat(violations).hasSize(2);
      assertThat(violations)
          .extracting(v -> v.getPropertyPath().toString())
          .containsExactlyInAnyOrder("code", "secondaryCode");
    }

    @Test
    void passesWhenBothUnique() {
      repository.save(new SampleEntity(null, "c1", "s1"));
      var dto = new MultiUniqueFieldsDto("c2", "s2");
      assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void oneRuleViolatesAndOnePasses() {
      repository.save(new SampleEntity(null, "c1", "s1"));
      var dto = new MultiUniqueFieldsDto("c1", "s2");
      Set<ConstraintViolation<MultiUniqueFieldsDto>> violations = validator.validate(dto);
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("code");
    }
  }

  @Nested
  class TypeLevelFlags {

    @Test
    void ignoreNullOrEmptySkipsDbCheck() {
      repository.save(new SampleEntity(null, "only", null));
      var dto = new TypeLevelNullableDto(null);
      assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void ignoreCaseDetectsDuplicate() {
      repository.save(new SampleEntity(null, "MixedCase", null));
      var dto = new TypeLevelIgnoreCaseDto("mixedcase");
      Set<ConstraintViolation<TypeLevelIgnoreCaseDto>> violations = validator.validate(dto);
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("code");
    }

    @Test
    void fieldLevelIgnoreCaseDetectsDuplicate() {
      repository.save(new SampleEntity(null, "FieldMixed", null));
      Set<ConstraintViolation<FieldLevelIgnoreCaseDto>> violations =
          validator.validate(new FieldLevelIgnoreCaseDto("fieldmixed"));
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("code");
    }
  }

  @Nested
  class InitializeValidation {

    @Test
    void throwsWhenExcludeIdDtoFieldSetWithoutDtoFieldOnField() {
      var dto = new BadFieldExcludeDto("x");
      assertThatThrownBy(() -> validator.validate(dto))
          .rootCause()
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("excludeIdDtoField must be blank when dtoField is blank");
    }
  }

  @Nested
  class NestedDtoFieldPath {

    @Test
    void typeLevelReadsNestedProperty() {
      repository.save(new SampleEntity(null, "nested-val", null));
      var dto = new NestedHolderDto(new NestedCode("nested-val"));
      Set<ConstraintViolation<NestedHolderDto>> violations = validator.validate(dto);
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("inner.code");
    }
  }

  @Nested
  class CustomEntityIdProperty {

    @Test
    void excludeUsesNonDefaultIdProperty() {
      CustomPkEntity saved = customPkRepository.save(new CustomPkEntity(null, "tok"));
      var dto = new CustomPkUpdateDto(saved.getEntityPk(), "tok");
      assertThat(validator.validate(dto)).isEmpty();
    }
  }

  static class SampleDto {

    @UniqueField(entity = SampleEntity.class, column = "code")
    private final String code;

    SampleDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  @UniqueField(
      entity = SampleEntity.class,
      column = "code",
      dtoField = "code",
      excludeIdDtoField = "entityId")
  static class TypeLevelUpdateDto {
    private final UUID entityId;
    private final String code;

    TypeLevelUpdateDto(UUID entityId, String code) {
      this.entityId = entityId;
      this.code = code;
    }

    public UUID getEntityId() {
      return entityId;
    }

    public String getCode() {
      return code;
    }
  }

  @UniqueFields(
      value = {
        @UniqueField(entity = SampleEntity.class, column = "code", dtoField = "code"),
        @UniqueField(
            entity = SampleEntity.class,
            column = "secondaryCode",
            dtoField = "secondaryCode"),
      })
  static class MultiUniqueFieldsDto {
    private final String code;
    private final String secondaryCode;

    MultiUniqueFieldsDto(String code, String secondaryCode) {
      this.code = code;
      this.secondaryCode = secondaryCode;
    }

    public String getCode() {
      return code;
    }

    public String getSecondaryCode() {
      return secondaryCode;
    }
  }

  @UniqueField(
      entity = SampleEntity.class,
      column = "code",
      dtoField = "code",
      ignoreNullOrEmpty = true)
  static class TypeLevelNullableDto {
    private final String code;

    TypeLevelNullableDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  @UniqueField(
      entity = SampleEntity.class,
      column = "code",
      dtoField = "code",
      ignoreCase = true)
  static class TypeLevelIgnoreCaseDto {
    private final String code;

    TypeLevelIgnoreCaseDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class FieldLevelIgnoreCaseDto {

    @UniqueField(entity = SampleEntity.class, column = "code", ignoreCase = true)
    private final String code;

    FieldLevelIgnoreCaseDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class BadFieldExcludeDto {

    @UniqueField(
        entity = SampleEntity.class,
        column = "code",
        excludeIdDtoField = "entityId")
    private final String code;

    BadFieldExcludeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    @SuppressWarnings("unused")
    public UUID getEntityId() {
      return UUID.randomUUID();
    }
  }

  static class NestedCode {
    private final String code;

    NestedCode(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  @UniqueField(entity = SampleEntity.class, column = "code", dtoField = "inner.code")
  static class NestedHolderDto {
    private final NestedCode inner;

    NestedHolderDto(NestedCode inner) {
      this.inner = inner;
    }

    public NestedCode getInner() {
      return inner;
    }
  }

  @UniqueField(
      entity = CustomPkEntity.class,
      column = "token",
      dtoField = "token",
      excludeIdDtoField = "pk",
      entityIdProperty = "entityPk")
  static class CustomPkUpdateDto {
    private final UUID pk;
    private final String token;

    CustomPkUpdateDto(UUID pk, String token) {
      this.pk = pk;
      this.token = token;
    }

    public UUID getPk() {
      return pk;
    }

    public String getToken() {
      return token;
    }
  }
}
