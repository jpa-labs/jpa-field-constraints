package io.github.jpa_labs.uniquefield;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.StringJoiner;
import org.junit.jupiter.api.Test;

class UniqueConstraintPathSecurityTest {

  @Test
  void rejectsDtoPathExceedingMaxDepth() {
    StringJoiner joiner = new StringJoiner(".");
    for (int i = 0; i < 33; i++) {
      joiner.add("s" + i);
    }
    String path = joiner.toString();
    assertThatThrownBy(() -> UniqueConstraintPathSecurity.assertDtoPropertyPath(path, "dtoField"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("maximum depth");
  }

  @Test
  void rejectsDtoPathExceedingMaxLength() {
    String longSegment = "a".repeat(400);
    assertThatThrownBy(
            () -> UniqueConstraintPathSecurity.assertDtoPropertyPath(longSegment, "dtoField"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("maximum length");
  }

  @Test
  void allowsReasonableDtoPath() {
    assertThatCode(() -> UniqueConstraintPathSecurity.assertDtoPropertyPath("inner.code", "dtoField"))
        .doesNotThrowAnyException();
  }

  @Test
  void rejectsNonEntityType() {
    assertThatThrownBy(() -> UniqueConstraintPathSecurity.assertJpaEntityClass(Object.class))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("@jakarta.persistence.Entity");

    assertThatCode(() -> UniqueConstraintPathSecurity.assertJpaEntityClass(SampleEntity.class))
        .doesNotThrowAnyException();
  }

  @Test
  void rejectsUnsafeDtoPathSegment() {
    assertThatThrownBy(
            () -> UniqueConstraintPathSecurity.assertDtoPropertyPath("holder.class.name", "dtoField"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsafe");
  }

  @Test
  void rejectsCompositeEntityIdPropertyName() {
    assertThatThrownBy(
            () -> UniqueConstraintPathSecurity.assertEntityIdPropertyName("id.value", "entityIdProperty"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("single property name");
  }

  @Test
  void rejectsUnsafeEntityIdPropertyName() {
    assertThatThrownBy(
            () -> UniqueConstraintPathSecurity.assertEntityIdPropertyName("class", "entityIdProperty"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsafe");
  }

  @Test
  void rejectsNullAndArrayEntityClass() {
    assertThatThrownBy(() -> UniqueConstraintPathSecurity.assertJpaEntityClass(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null");

    assertThatThrownBy(() -> UniqueConstraintPathSecurity.assertJpaEntityClass(SampleEntity[].class))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("non-array reference type");
  }

  @Test
  void rejectsInvalidJpaAttributePathShape() {
    assertThatThrownBy(
            () -> UniqueConstraintPathSecurity.assertJpaAttributePath("bad-path", "column"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid");
  }

  @Test
  void rejectsBlankJpaAttributePath() {
    assertThatThrownBy(() -> UniqueConstraintPathSecurity.assertJpaAttributePath(" ", "column"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be blank");
  }

  @Test
  void rejectsInvalidEntityIdPropertyShape() {
    assertThatThrownBy(
            () -> UniqueConstraintPathSecurity.assertEntityIdPropertyName("id-value", "entityIdProperty"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid entityIdProperty");
  }
}
