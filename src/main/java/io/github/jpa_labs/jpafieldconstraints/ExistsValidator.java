package io.github.jpa_labs.jpafieldconstraints;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

@Component
class ExistsValidator implements ConstraintValidator<Exists, Object> {

  @PersistenceContext private EntityManager entityManager;

  private Class<?> entityClass;
  private String attributePath;
  private boolean ignoreNullOrEmpty;
  private boolean ignoreCase;
  private String dtoField;
  private boolean typeLevel;
  private List<JpaUniqueConstraintSupport.StaticEqualsFilter> whereFilters;

  @Override
  public void initialize(Exists constraintAnnotation) {
    this.entityClass = constraintAnnotation.entity();
    UniqueConstraintPathSecurity.assertJpaEntityClass(this.entityClass);
    this.attributePath = constraintAnnotation.column();
    this.ignoreNullOrEmpty = constraintAnnotation.ignoreNullOrEmpty();
    this.ignoreCase = constraintAnnotation.ignoreCase();
    this.dtoField = nullToEmpty(constraintAnnotation.dtoField());
    UniqueConstraintPathSecurity.assertJpaAttributePath(this.attributePath, "column");
    this.whereFilters = buildWhereFilters(constraintAnnotation.where());
    this.typeLevel = !this.dtoField.isBlank();
    if (this.typeLevel) {
      UniqueConstraintPathSecurity.assertDtoPropertyPath(this.dtoField, "dtoField");
    }
  }

  private static String nullToEmpty(String s) {
    return java.util.Objects.requireNonNullElse(s, "");
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
            new JpaUniqueConstraintSupport.EqualityQueryOptions(null, "id", whereFilters));
    return count > 0;
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
    long count =
        JpaUniqueConstraintSupport.countRowsEqual(
            entityManager,
            entityClass,
            attributePath,
            fieldValue,
            ignoreCase,
            new JpaUniqueConstraintSupport.EqualityQueryOptions(null, "id", whereFilters));
    if (count == 0) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode(dtoField)
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  private List<JpaUniqueConstraintSupport.StaticEqualsFilter> buildWhereFilters(Exists.Where[] where) {
    if (where == null || where.length == 0) {
      return List.of();
    }
    List<JpaUniqueConstraintSupport.StaticEqualsFilter> filters = new ArrayList<>();
    for (Exists.Where clause : where) {
      String whereColumn = nullToEmpty(clause.column());
      String whereValue = nullToEmpty(clause.value());
      UniqueConstraintPathSecurity.assertJpaAttributePath(whereColumn, "where.column");
      if (whereValue.isBlank()) {
        throw new IllegalArgumentException("where.value must not be blank");
      }
      filters.add(
          new JpaUniqueConstraintSupport.StaticEqualsFilter(
              whereColumn, whereValue, clause.ignoreCase()));
    }
    return List.copyOf(filters);
  }
}
