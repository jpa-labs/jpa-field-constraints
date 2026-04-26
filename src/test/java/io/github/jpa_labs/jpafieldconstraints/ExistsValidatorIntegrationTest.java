package io.github.jpa_labs.jpafieldconstraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDate;
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
    void passesWhenWhereClauseCoercesBooleanLiteral() {
      repository.save(
          new SampleEntity(null, "emp-boolean", null, null, null, true, null, null, null));
      Set<ConstraintViolation<EnabledEmployeeDto>> violations =
          validator.validate(new EnabledEmployeeDto("emp-boolean"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesIntegerLiteral() {
      repository.save(
          new SampleEntity(null, "emp-level", null, null, null, null, 3, null, null));
      Set<ConstraintViolation<LevelEmployeeDto>> violations =
          validator.validate(new LevelEmployeeDto("emp-level"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesUuidLiteral() {
      UUID tenant = UUID.fromString("57d2f094-c4fc-4ca5-94cd-3a84f909de4a");
      repository.save(
          new SampleEntity(null, "emp-tenant", null, null, null, null, null, tenant, null));
      Set<ConstraintViolation<TenantEmployeeDto>> violations =
          validator.validate(new TenantEmployeeDto("emp-tenant"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesEnumLiteral() {
      repository.save(
          new SampleEntity(
              null, "emp-access", null, null, null, null, null, null, SampleEntity.Access.ADMIN));
      Set<ConstraintViolation<AdminAccessEmployeeDto>> violations =
          validator.validate(new AdminAccessEmployeeDto("emp-access"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesByteLiteral() {
      repository.save(new SampleEntity(null, "emp-byte", null).setScoreByte((byte) 7));
      Set<ConstraintViolation<ByteScoreEmployeeDto>> violations =
          validator.validate(new ByteScoreEmployeeDto("emp-byte"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesShortLiteral() {
      repository.save(new SampleEntity(null, "emp-short", null).setScoreShort((short) 9));
      Set<ConstraintViolation<ShortScoreEmployeeDto>> violations =
          validator.validate(new ShortScoreEmployeeDto("emp-short"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesLongLiteral() {
      repository.save(new SampleEntity(null, "emp-long", null).setScoreLong(42L));
      Set<ConstraintViolation<LongScoreEmployeeDto>> violations =
          validator.validate(new LongScoreEmployeeDto("emp-long"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesFloatLiteral() {
      repository.save(new SampleEntity(null, "emp-float", null).setScoreFloat(3.5f));
      Set<ConstraintViolation<FloatScoreEmployeeDto>> violations =
          validator.validate(new FloatScoreEmployeeDto("emp-float"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesDoubleLiteral() {
      repository.save(new SampleEntity(null, "emp-double", null).setScoreDouble(4.25d));
      Set<ConstraintViolation<DoubleScoreEmployeeDto>> violations =
          validator.validate(new DoubleScoreEmployeeDto("emp-double"));
      assertThat(violations).isEmpty();
    }

    @Test
    void passesWhenWhereClauseCoercesCharacterLiteral() {
      repository.save(new SampleEntity(null, "emp-grade", null).setGrade('A'));
      Set<ConstraintViolation<GradeEmployeeDto>> violations =
          validator.validate(new GradeEmployeeDto("emp-grade"));
      assertThat(violations).isEmpty();
    }

    @Test
    void failsWhenWhereClauseCharacterLiteralHasMultipleCharacters() {
      InvalidGradeEmployeeDto dto = new InvalidGradeEmployeeDto("emp-grade");
      assertThatThrownBy(() -> validator.validate(dto))
          .rootCause()
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("single character");
    }

    @Test
    void failsWhenWhereClauseTypeIsUnsupported() {
      repository.save(new SampleEntity(null, "emp-date", null).setStartedOn(LocalDate.now()));
      UnsupportedTypeEmployeeDto dto = new UnsupportedTypeEmployeeDto("emp-date");
      assertThatThrownBy(() -> validator.validate(dto))
          .rootCause()
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unsupported where.value type");
    }

    @Test
    void failsWhenWhereClauseValueIsBlankAtRuntimeInitialization() {
      BlankWhereValueEmployeeDto dto = new BlankWhereValueEmployeeDto("emp-blank");
      assertThatThrownBy(() -> validator.validate(dto))
          .rootCause()
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("where.value must not be blank");
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

    @Test
    void allowsBlankNestedValueWhenIgnoreNullOrEmptyEnabled() {
      Set<ConstraintViolation<TypeLevelNestedDto>> violations =
          validator.validate(new TypeLevelNestedDto(new InnerCode("   ")));
      assertThat(violations).isEmpty();
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

  static class EnabledEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "enabled", value = "true")})
    private final String code;

    EnabledEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class LevelEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "level", value = "3")})
    private final String code;

    LevelEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class TenantEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "tenantId", value = "57d2f094-c4fc-4ca5-94cd-3a84f909de4a")})
    private final String code;

    TenantEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class AdminAccessEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "access", value = "ADMIN")})
    private final String code;

    AdminAccessEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class ByteScoreEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "scoreByte", value = "7")})
    private final String code;

    ByteScoreEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class ShortScoreEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "scoreShort", value = "9")})
    private final String code;

    ShortScoreEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class LongScoreEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "scoreLong", value = "42")})
    private final String code;

    LongScoreEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class FloatScoreEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "scoreFloat", value = "3.5")})
    private final String code;

    FloatScoreEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class DoubleScoreEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "scoreDouble", value = "4.25")})
    private final String code;

    DoubleScoreEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class GradeEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "grade", value = "A")})
    private final String code;

    GradeEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class InvalidGradeEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "grade", value = "AB")})
    private final String code;

    InvalidGradeEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class UnsupportedTypeEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "startedOn", value = "2026-01-01")})
    private final String code;

    UnsupportedTypeEmployeeDto(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  static class BlankWhereValueEmployeeDto {

    @Exists(
        entity = SampleEntity.class,
        column = "code",
        where = {@Exists.Where(column = "status", value = "   ")})
    private final String code;

    BlankWhereValueEmployeeDto(String code) {
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
