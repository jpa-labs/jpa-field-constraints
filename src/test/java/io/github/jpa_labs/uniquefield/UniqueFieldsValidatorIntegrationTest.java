package io.github.jpa_labs.uniquefield;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.Validator;
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
class UniqueFieldsValidatorIntegrationTest {

  @Autowired private Validator validator;

  @Test
  void initializeThrowsWhenRuleHasBlankDtoField() {
    assertThatThrownBy(() -> validator.validate(new BlankDtoFieldRulesDto()))
        .rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("non-blank dtoField");
  }

  @Test
  void initializeThrowsWhenValueArrayEmpty() {
    assertThatThrownBy(() -> validator.validate(new EmptyUniqueFieldsDto()))
        .rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UniqueFields.value() must not be empty");
  }

  @Test
  void initializeRejectsNonJpaEntityClass() {
    assertThatThrownBy(() -> validator.validate(new NonEntityRuleDto()))
        .rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("@jakarta.persistence.Entity");
  }

  @Test
  void initializeRejectsUnsafeDtoPathSegment() {
    assertThatThrownBy(() -> validator.validate(new UnsafeDtoPathDto()))
        .rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsafe");
  }

  @Test
  void initializeRejectsCompositeEntityIdProperty() {
    assertThatThrownBy(() -> validator.validate(new CompositeEntityIdPropertyDto()))
        .rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("single property name");
  }

  @Test
  void initializeRejectsEntityIdPropertyNamedClass() {
    assertThatThrownBy(() -> validator.validate(new EntityIdNamedClassDto()))
        .rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsafe");
  }

  @UniqueFields(
      value = {
        @UniqueField(entity = SampleEntity.class, column = "code", dtoField = ""),
      })
  static class BlankDtoFieldRulesDto {}

  @UniqueFields(value = {})
  static class EmptyUniqueFieldsDto {}

  static final class NotJpaEntity {}

  @UniqueFields(
      value = {
        @UniqueField(entity = NotJpaEntity.class, column = "code", dtoField = "code"),
      })
  static class NonEntityRuleDto {}

  @UniqueFields(
      value = {
        @UniqueField(
            entity = SampleEntity.class,
            column = "code",
            dtoField = "holder.class.name",
            excludeIdDtoField = "id"),
      })
  static class UnsafeDtoPathDto {
    @SuppressWarnings("unused")
    private final Object holder = new Object();

    @SuppressWarnings("unused")
    private final java.util.UUID id = java.util.UUID.randomUUID();
  }

  @UniqueFields(
      value = {
        @UniqueField(
            entity = SampleEntity.class,
            column = "code",
            dtoField = "code",
            entityIdProperty = "id.leaf"),
      })
  static class CompositeEntityIdPropertyDto {}

  @UniqueFields(
      value = {
        @UniqueField(
            entity = SampleEntity.class,
            column = "code",
            dtoField = "code",
            entityIdProperty = "class"),
      })
  static class EntityIdNamedClassDto {}
}
