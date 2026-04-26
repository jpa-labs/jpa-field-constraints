package io.github.jpa_labs.jpafieldconstraints;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
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
class JpaUniqueConstraintSupportIntegrationTest {

  @Autowired private EntityManager entityManager;

  @Autowired private SampleEntityRepository repository;

  @Test
  void countRowsEqualMatchesExistingValue() {
    String code = "eq-" + UUID.randomUUID();
    repository.save(new SampleEntity(null, code, null));

    long count =
        JpaUniqueConstraintSupport.countRowsEqual(
            entityManager, SampleEntity.class, "code", code, false, null, "id");

    assertThat(count).isEqualTo(1);
  }

  @Test
  void countRowsEqualHonorsExcludeEntityId() {
    String code = "ex-" + UUID.randomUUID();
    SampleEntity first = repository.save(new SampleEntity(null, code, null));
    repository.save(new SampleEntity(null, code, null));

    long count =
        JpaUniqueConstraintSupport.countRowsEqual(
            entityManager, SampleEntity.class, "code", code, false, first.getId(), "id");

    assertThat(count).isEqualTo(1);
  }

  @Test
  void countRowsInMatchesDistinctCaseInsensitiveValues() {
    String a = "in-" + UUID.randomUUID();
    String b = "in-" + UUID.randomUUID();
    repository.save(new SampleEntity(null, a, null));
    repository.save(new SampleEntity(null, b, null));

    Set<Object> values = new LinkedHashSet<>();
    values.add(a.toLowerCase());
    values.add(a.toUpperCase());
    values.add(b.toLowerCase());

    long count =
        JpaUniqueConstraintSupport.countRowsIn(
            entityManager, SampleEntity.class, "code", values, true);

    assertThat(count).isEqualTo(2);
  }

  @Test
  void countRowsInReturnsZeroForMixedTypesWhenIgnoreCaseEnabled() {
    String code = "mix-" + UUID.randomUUID();
    repository.save(new SampleEntity(null, code, null));

    Set<Object> values = new LinkedHashSet<>();
    values.add(code.toLowerCase());
    values.add(42);

    long count =
        JpaUniqueConstraintSupport.countRowsIn(
            entityManager, SampleEntity.class, "code", values, true);

    assertThat(count).isZero();
  }

  @Test
  void isEmptyValueCoversFalseAndNonStringBranches() {
    assertThat(JpaUniqueConstraintSupport.isEmptyValue(null, false)).isFalse();
    assertThat(JpaUniqueConstraintSupport.isEmptyValue(123, true)).isFalse();
  }
}
