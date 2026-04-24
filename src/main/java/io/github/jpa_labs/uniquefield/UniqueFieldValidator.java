package io.github.jpa_labs.uniquefield;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

@Component
class UniqueFieldValidator implements ConstraintValidator<UniqueField, Object> {

  @PersistenceContext private EntityManager entityManager;

  private Class<?> entityClass;
  private String attributePath;
  private boolean ignoreNullOrEmpty;
  private boolean ignoreCase;
  private String dtoField;
  private String excludeIdDtoField;
  private String entityIdProperty;
  private boolean typeLevel;

  @Override
  public void initialize(UniqueField constraintAnnotation) {
    this.entityClass = constraintAnnotation.entity();
    UniqueConstraintPathSecurity.assertJpaEntityClass(this.entityClass);
    this.attributePath = constraintAnnotation.column();
    this.ignoreNullOrEmpty = constraintAnnotation.ignoreNullOrEmpty();
    this.ignoreCase = constraintAnnotation.ignoreCase();
    this.dtoField = nullToEmpty(constraintAnnotation.dtoField());
    this.excludeIdDtoField = nullToEmpty(constraintAnnotation.excludeIdDtoField());
    this.entityIdProperty = nullToEmpty(constraintAnnotation.entityIdProperty());
    UniqueConstraintPathSecurity.assertEntityIdPropertyName(this.entityIdProperty, "entityIdProperty");
    UniqueConstraintPathSecurity.assertJpaAttributePath(this.attributePath, "column");
    this.typeLevel = !this.dtoField.isBlank();
    if (this.typeLevel) {
      if (!this.excludeIdDtoField.isBlank()) {
        UniqueConstraintPathSecurity.assertDtoPropertyPath(this.excludeIdDtoField, "excludeIdDtoField");
      }
      UniqueConstraintPathSecurity.assertDtoPropertyPath(this.dtoField, "dtoField");
    } else {
      if (!this.excludeIdDtoField.isBlank()) {
        throw new IllegalArgumentException(
            "excludeIdDtoField must be blank when dtoField is blank (field/method/parameter mode)");
      }
    }
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (typeLevel) {
      return validateTypeLevel(value, context);
    }
    if (JpaUniqueConstraintSupport.isEmptyValue(value, ignoreNullOrEmpty)) {
      return true;
    }
    long count =
        JpaUniqueConstraintSupport.countRowsEqual(
            entityManager,
            entityClass,
            attributePath,
            value,
            ignoreCase,
            null,
            entityIdProperty);
    return count == 0;
  }

  private boolean validateTypeLevel(Object rootDto, ConstraintValidatorContext context) {
    if (rootDto == null) {
      return true;
    }
    BeanWrapperImpl wrapper = new BeanWrapperImpl(rootDto);
    Object fieldValue = wrapper.getPropertyValue(dtoField);
    if (JpaUniqueConstraintSupport.isEmptyValue(fieldValue, ignoreNullOrEmpty)) {
      return true;
    }
    Object excludeEntityId = null;
    if (!excludeIdDtoField.isBlank()) {
      excludeEntityId = wrapper.getPropertyValue(excludeIdDtoField);
    }
    long count =
        JpaUniqueConstraintSupport.countRowsEqual(
            entityManager,
            entityClass,
            attributePath,
            fieldValue,
            ignoreCase,
            excludeEntityId,
            entityIdProperty);
    if (count > 0) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode(dtoField)
          .addConstraintViolation();
      return false;
    }
    return true;
  }
}
