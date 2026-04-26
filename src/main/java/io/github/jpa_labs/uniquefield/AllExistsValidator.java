package io.github.jpa_labs.uniquefield;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

@Component
class AllExistsValidator implements ConstraintValidator<AllExists, Object> {

  @PersistenceContext private EntityManager entityManager;

  private Class<?> entityClass;
  private String attributePath;
  private boolean ignoreNullOrEmpty;
  private boolean ignoreCase;
  private String dtoField;
  private boolean typeLevel;

  @Override
  public void initialize(AllExists constraintAnnotation) {
    this.entityClass = constraintAnnotation.entity();
    UniqueConstraintPathSecurity.assertJpaEntityClass(this.entityClass);
    this.attributePath = constraintAnnotation.column();
    this.ignoreNullOrEmpty = constraintAnnotation.ignoreNullOrEmpty();
    this.ignoreCase = constraintAnnotation.ignoreCase();
    this.dtoField = nullToEmpty(constraintAnnotation.dtoField());
    UniqueConstraintPathSecurity.assertJpaAttributePath(this.attributePath, "column");
    this.typeLevel = !this.dtoField.isBlank();
    if (this.typeLevel) {
      UniqueConstraintPathSecurity.assertDtoPropertyPath(this.dtoField, "dtoField");
    }
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    Object source = value;
    if (typeLevel) {
      if (value == null) {
        return true;
      }
      source = new BeanWrapperImpl(value).getPropertyValue(dtoField);
    }
    if (source == null) {
      return ignoreNullOrEmpty;
    }
    if (!(source instanceof Iterable<?> iterable)) {
      return false;
    }
    Set<Object> normalized = normalizeValues(iterable);
    if (normalized.isEmpty()) {
      return true;
    }
    long matched =
        JpaUniqueConstraintSupport.countRowsIn(
            entityManager, entityClass, attributePath, normalized, ignoreCase);
    if (matched == normalized.size()) {
      return true;
    }
    if (typeLevel) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode(dtoField)
          .addConstraintViolation();
    }
    return false;
  }

  private Set<Object> normalizeValues(Iterable<?> iterable) {
    Set<Object> values = new LinkedHashSet<>();
    for (Object item : iterable) {
      if (item == null || (item instanceof String s && s.isBlank())) {
        if (!ignoreNullOrEmpty) {
          values.add(item);
        }
      } else {
        values.add(item);
      }
    }
    return values;
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }
}
