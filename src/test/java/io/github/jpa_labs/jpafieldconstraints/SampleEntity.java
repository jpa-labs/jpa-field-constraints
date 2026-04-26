package io.github.jpa_labs.jpafieldconstraints;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  private String status;

  private String role;

  private Boolean enabled;

  private Integer level;

  private UUID tenantId;

  @Enumerated(EnumType.STRING)
  private Access access;

  SampleEntity() {}

  SampleEntity(UUID id, String code, String secondaryCode) {
    this(id, code, secondaryCode, null, null, null, null, null, null);
  }

  SampleEntity(UUID id, String code, String secondaryCode, String status, String role) {
    this(id, code, secondaryCode, status, role, null, null, null, null);
  }

  SampleEntity(
      UUID id,
      String code,
      String secondaryCode,
      String status,
      String role,
      Boolean enabled,
      Integer level,
      UUID tenantId,
      Access access) {
    this.id = id;
    this.code = code;
    this.secondaryCode = secondaryCode;
    this.status = status;
    this.role = role;
    this.enabled = enabled;
    this.level = level;
    this.tenantId = tenantId;
    this.access = access;
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

  public String getStatus() {
    return status;
  }

  public String getRole() {
    return role;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public Integer getLevel() {
    return level;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public Access getAccess() {
    return access;
  }

  enum Access {
    ADMIN,
    USER
  }
}
