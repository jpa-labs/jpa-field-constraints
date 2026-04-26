package io.github.jpa_labs.uniquefield;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
class AllExistsValidatorIntegrationTest {

  @Autowired private Validator validator;

  @Autowired private SampleEntityRepository repository;

  @Nested
  class FieldLevel {

    @Test
    void passesWhenAllValuesExist() {
      String v1 = "ax-" + UUID.randomUUID();
      String v2 = "ax-" + UUID.randomUUID();
      repository.save(new SampleEntity(null, v1, null));
      repository.save(new SampleEntity(null, v2, null));
      Set<ConstraintViolation<FieldListDto>> violations =
          validator.validate(new FieldListDto(List.of(v1, v2)));
      assertThat(violations).isEmpty();
    }

    @Test
    void rejectsWhenAnyValueMissing() {
      String v1 = "ax-" + UUID.randomUUID();
      repository.save(new SampleEntity(null, v1, null));
      Set<ConstraintViolation<FieldListDto>> violations =
          validator.validate(new FieldListDto(List.of(v1, "ax-missing-" + UUID.randomUUID())));
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("codes");
    }
  }

  @Nested
  class TypeLevel {

    @Test
    void validatesNestedIterablePath() {
      String v1 = "ax-" + UUID.randomUUID();
      String v2 = "ax-" + UUID.randomUUID();
      repository.save(new SampleEntity(null, v1, null));
      repository.save(new SampleEntity(null, v2, null));
      Set<ConstraintViolation<TypeLevelDto>> violations =
          validator.validate(new TypeLevelDto(new InnerCodes(List.of(v1, v2))));
      assertThat(violations).isEmpty();
    }

    @Test
    void rejectsMissingNestedItem() {
      String v1 = "ax-" + UUID.randomUUID();
      repository.save(new SampleEntity(null, v1, null));
      Set<ConstraintViolation<TypeLevelDto>> violations =
          validator.validate(new TypeLevelDto(new InnerCodes(List.of(v1, "ax-nope-" + UUID.randomUUID()))));
      assertThat(violations).hasSize(1);
      assertThat(violations.iterator().next().getPropertyPath()).hasToString("inner.codes");
    }
  }

  static class FieldListDto {

    @AllExists(entity = SampleEntity.class, column = "code")
    private final List<String> codes;

    FieldListDto(List<String> codes) {
      this.codes = codes;
    }

    public List<String> getCodes() {
      return codes;
    }
  }

  @AllExists(entity = SampleEntity.class, column = "code", dtoField = "inner.codes")
  static class TypeLevelDto {
    private final InnerCodes inner;

    TypeLevelDto(InnerCodes inner) {
      this.inner = inner;
    }

    public InnerCodes getInner() {
      return inner;
    }
  }

  static class InnerCodes {
    private final List<String> codes;

    InnerCodes(List<String> codes) {
      this.codes = codes;
    }

    public List<String> getCodes() {
      return codes;
    }
  }
}
