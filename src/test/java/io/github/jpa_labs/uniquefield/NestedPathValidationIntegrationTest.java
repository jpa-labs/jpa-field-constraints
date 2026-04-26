package io.github.jpa_labs.uniquefield;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
class NestedPathValidationIntegrationTest {

  @Autowired private Validator validator;

  @Autowired private NestedPathHolderEntityRepository repository;

  @Test
  void existsSupportsAssociationPath() {
    repository.save(
        new NestedPathHolderEntity(new NestedPathRelatedEntity("rel-" + UUID.randomUUID()), null));
    String existingCode = "rel-" + UUID.randomUUID();
    repository.save(new NestedPathHolderEntity(new NestedPathRelatedEntity(existingCode), null));

    Set<ConstraintViolation<ExistsAssociationDto>> violations =
        validator.validate(new ExistsAssociationDto(existingCode));

    assertThat(violations).isEmpty();
  }

  @Test
  void allExistsSupportsEmbeddablePath() {
    String token1 = "tok-" + UUID.randomUUID();
    String token2 = "tok-" + UUID.randomUUID();
    repository.save(new NestedPathHolderEntity(null, new NestedPathDetails(token1)));
    repository.save(new NestedPathHolderEntity(null, new NestedPathDetails(token2)));

    Set<ConstraintViolation<AllExistsEmbeddableDto>> violations =
        validator.validate(new AllExistsEmbeddableDto(List.of(token1, token2)));

    assertThat(violations).isEmpty();
  }

  @Test
  void uniqueFieldSupportsAssociationPath() {
    String existingCode = "uniq-" + UUID.randomUUID();
    repository.save(new NestedPathHolderEntity(new NestedPathRelatedEntity(existingCode), null));

    Set<ConstraintViolation<UniqueAssociationDto>> violations =
        validator.validate(new UniqueAssociationDto(existingCode));

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getPropertyPath()).hasToString("relatedCode");
  }

  static class ExistsAssociationDto {
    @Exists(entity = NestedPathHolderEntity.class, column = "related.code")
    private final String relatedCode;

    ExistsAssociationDto(String relatedCode) {
      this.relatedCode = relatedCode;
    }

    public String getRelatedCode() {
      return relatedCode;
    }
  }

  static class AllExistsEmbeddableDto {
    @AllExists(entity = NestedPathHolderEntity.class, column = "details.token")
    private final List<String> tokens;

    AllExistsEmbeddableDto(List<String> tokens) {
      this.tokens = tokens;
    }

    public List<String> getTokens() {
      return tokens;
    }
  }

  static class UniqueAssociationDto {
    @UniqueField(entity = NestedPathHolderEntity.class, column = "related.code")
    private final String relatedCode;

    UniqueAssociationDto(String relatedCode) {
      this.relatedCode = relatedCode;
    }

    public String getRelatedCode() {
      return relatedCode;
    }
  }
}
