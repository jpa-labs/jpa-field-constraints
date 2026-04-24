package io.github.jpa_labs.uniquefield;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

@Component
class UniqueFieldsValidator implements ConstraintValidator<UniqueFields, Object> {

  @PersistenceContext private EntityManager entityManager;

  private UniqueField[] rules;

  @Override
  public void initialize(UniqueFields constraintAnnotation) {
    this.rules = constraintAnnotation.value();
    if (rules == null || rules.length == 0) {
      throw new IllegalArgumentException("UniqueFields.value() must not be empty");
    }
    for (int i = 0; i < rules.length; i++) {
      UniqueField rule = rules[i];
      UniqueConstraintPathSecurity.assertJpaEntityClass(rule.entity());
      String dtoField = rule.dtoField() == null ? "" : rule.dtoField();
      if (dtoField.isBlank()) {
        throw new IllegalArgumentException(
            "UniqueFields: rule at index " + i + " must declare a non-blank dtoField");
      }
      UniqueConstraintPathSecurity.assertDtoPropertyPath(dtoField, "UniqueFields.dtoField[" + i + "]");
      String column = rule.column();
      if (column == null || column.isBlank()) {
        throw new IllegalArgumentException("UniqueFields: column must not be blank at index " + i);
      }
      UniqueConstraintPathSecurity.assertJpaAttributePath(column, "UniqueFields.column[" + i + "]");
      String excludePath = rule.excludeIdDtoField() == null ? "" : rule.excludeIdDtoField();
      if (!excludePath.isBlank()) {
        UniqueConstraintPathSecurity.assertDtoPropertyPath(
            excludePath, "UniqueFields.excludeIdDtoField[" + i + "]");
      }
      String entityIdProperty = rule.entityIdProperty();
      UniqueConstraintPathSecurity.assertEntityIdPropertyName(
          entityIdProperty == null ? "" : entityIdProperty,
          "UniqueFields.entityIdProperty[" + i + "]");
    }
  }

  @Override
  public boolean isValid(Object rootDto, ConstraintValidatorContext context) {
    if (rootDto == null) {
      return true;
    }
    boolean allValid = true;
    BeanWrapperImpl wrapper = new BeanWrapperImpl(rootDto);
    for (UniqueField rule : rules) {
      if (!validateOneRule(wrapper, rule, context)) {
        allValid = false;
      }
    }
    return allValid;
  }

  private boolean validateOneRule(
      BeanWrapperImpl wrapper, UniqueField rule, ConstraintValidatorContext context) {
    String dtoField = rule.dtoField();
    Object fieldValue = wrapper.getPropertyValue(dtoField);
    if (JpaUniqueConstraintSupport.isEmptyValue(fieldValue, rule.ignoreNullOrEmpty())) {
      return true;
    }
    String excludePath = rule.excludeIdDtoField() == null ? "" : rule.excludeIdDtoField();
    Object excludeEntityId = excludePath.isBlank() ? null : wrapper.getPropertyValue(excludePath);
    String entityIdProperty = rule.entityIdProperty();
    if (entityIdProperty == null || entityIdProperty.isBlank()) {
      throw new IllegalStateException("entityIdProperty must not be blank");
    }
    long count =
        JpaUniqueConstraintSupport.countRowsEqual(
            entityManager,
            rule.entity(),
            rule.column(),
            fieldValue,
            rule.ignoreCase(),
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
