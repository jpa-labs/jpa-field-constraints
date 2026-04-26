package io.github.jpa_labs.jpafieldconstraints;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "unique_field_custom_pk")
class CustomPkEntity {

  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID entityPk;

  private String token;

  CustomPkEntity() {}

  CustomPkEntity(UUID entityPk, String token) {
    this.entityPk = entityPk;
    this.token = token;
  }

  public UUID getEntityPk() {
    return entityPk;
  }

  public String getToken() {
    return token;
  }
}
