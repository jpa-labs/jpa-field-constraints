package io.github.jpa_labs.uniquefield;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "nested_path_related")
class NestedPathRelatedEntity {

  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

  private String code;

  NestedPathRelatedEntity() {}

  NestedPathRelatedEntity(String code) {
    this.code = code;
  }
}

@Embeddable
class NestedPathDetails {
  private String token;

  NestedPathDetails() {}

  NestedPathDetails(String token) {
    this.token = token;
  }
}

@Entity
@Table(name = "nested_path_holder")
class NestedPathHolderEntity {

  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

  @ManyToOne(cascade = CascadeType.ALL)
  private NestedPathRelatedEntity related;

  @Embedded private NestedPathDetails details;

  NestedPathHolderEntity() {}

  NestedPathHolderEntity(NestedPathRelatedEntity related, NestedPathDetails details) {
    this.related = related;
    this.details = details;
  }
}
