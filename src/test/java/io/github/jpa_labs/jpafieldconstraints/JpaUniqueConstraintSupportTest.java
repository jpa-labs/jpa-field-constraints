package io.github.jpa_labs.jpafieldconstraints;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.Set;
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
class JpaUniqueConstraintSupportTest {

  @Autowired private EntityManager entityManager;

  @Test
  void countRowsInReturnsZeroForEmptyInputSet() {
    long count =
        JpaUniqueConstraintSupport.countRowsIn(
            entityManager, SampleEntity.class, "code", Set.of(), false);
    assertThat(count).isZero();
  }
}
