package io.github.jpa_labs.jpafieldconstraints;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "unique_field_sample")
class SampleEntity {

  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

  private String code;

  private String secondaryCode;

  SampleEntity() {}

  SampleEntity(UUID id, String code, String secondaryCode) {
    this.id = id;
    this.code = code;
    this.secondaryCode = secondaryCode;
  }

  public UUID getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getSecondaryCode() {
    return secondaryCode;
  }
}
