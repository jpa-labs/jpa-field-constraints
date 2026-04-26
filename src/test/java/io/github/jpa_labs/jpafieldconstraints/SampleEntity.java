package io.github.jpa_labs.jpafieldconstraints;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
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

  private Byte scoreByte;

  private Short scoreShort;

  private Long scoreLong;

  private Float scoreFloat;

  private Double scoreDouble;

  private Character grade;

  private LocalDate startedOn;

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

  public Byte getScoreByte() {
    return scoreByte;
  }

  public Short getScoreShort() {
    return scoreShort;
  }

  public Long getScoreLong() {
    return scoreLong;
  }

  public Float getScoreFloat() {
    return scoreFloat;
  }

  public Double getScoreDouble() {
    return scoreDouble;
  }

  public Character getGrade() {
    return grade;
  }

  public LocalDate getStartedOn() {
    return startedOn;
  }

  public SampleEntity setScoreByte(Byte scoreByte) {
    this.scoreByte = scoreByte;
    return this;
  }

  public SampleEntity setScoreShort(Short scoreShort) {
    this.scoreShort = scoreShort;
    return this;
  }

  public SampleEntity setScoreLong(Long scoreLong) {
    this.scoreLong = scoreLong;
    return this;
  }

  public SampleEntity setScoreFloat(Float scoreFloat) {
    this.scoreFloat = scoreFloat;
    return this;
  }

  public SampleEntity setScoreDouble(Double scoreDouble) {
    this.scoreDouble = scoreDouble;
    return this;
  }

  public SampleEntity setGrade(Character grade) {
    this.grade = grade;
    return this;
  }

  public SampleEntity setStartedOn(LocalDate startedOn) {
    this.startedOn = startedOn;
    return this;
  }

  enum Access {
    ADMIN,
    USER
  }
}
