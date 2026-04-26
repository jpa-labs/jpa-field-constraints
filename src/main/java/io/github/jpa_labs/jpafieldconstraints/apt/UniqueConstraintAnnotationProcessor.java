package io.github.jpa_labs.jpafieldconstraints.apt;

import io.github.jpa_labs.jpafieldconstraints.UniqueConstraintStaticRules;
import io.github.jpa_labs.jpafieldconstraints.AllExists;
import io.github.jpa_labs.jpafieldconstraints.Exists;
import io.github.jpa_labs.jpafieldconstraints.UniqueField;
import io.github.jpa_labs.jpafieldconstraints.UniqueFields;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Validates {@link UniqueField} and {@link UniqueFields} at compile time. Enable by adding this
 * artifact as an {@code annotationProcessor} dependency (same coordinates as {@code
 * implementation}).
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class UniqueConstraintAnnotationProcessor extends AbstractProcessor {

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(
        AllExists.class.getCanonicalName(),
        AllExists.List.class.getCanonicalName(),
        Exists.class.getCanonicalName(),
        Exists.List.class.getCanonicalName(),
        UniqueField.class.getCanonicalName(),
        UniqueFields.class.getCanonicalName(),
        UniqueField.List.class.getCanonicalName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(AllExists.class)) {
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        if (isAllExistsMirror(am)) {
          validateAllExistsMirror(am, element);
        }
      }
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(AllExists.List.class)) {
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        if (isAllExistsListMirror(am)) {
          validateAllExistsListMirror(am, element);
        }
      }
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(Exists.class)) {
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        if (isExistsMirror(am)) {
          validateExistsMirror(am, element);
        }
      }
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(Exists.List.class)) {
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        if (isExistsListMirror(am)) {
          validateExistsListMirror(am, element);
        }
      }
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(UniqueField.class)) {
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        if (isUniqueFieldMirror(am)) {
          validateUniqueFieldMirror(am, element);
        }
      }
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(UniqueField.List.class)) {
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        if (isUniqueFieldListMirror(am)) {
          validateUniqueFieldListMirror(am, element);
        }
      }
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(UniqueFields.class)) {
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        if (isUniqueFieldsMirror(am)) {
          validateUniqueFieldsMirror(am, element);
        }
      }
    }
    return false;
  }

  private boolean isUniqueFieldMirror(AnnotationMirror am) {
    return UniqueField.class
        .getCanonicalName()
        .contentEquals(
            ((TypeElement) processingEnv.getTypeUtils().asElement(am.getAnnotationType()))
                .getQualifiedName()
                .toString());
  }

  private boolean isExistsMirror(AnnotationMirror am) {
    return Exists.class
        .getCanonicalName()
        .contentEquals(
            ((TypeElement) processingEnv.getTypeUtils().asElement(am.getAnnotationType()))
                .getQualifiedName()
                .toString());
  }

  private boolean isAllExistsMirror(AnnotationMirror am) {
    return AllExists.class
        .getCanonicalName()
        .contentEquals(
            ((TypeElement) processingEnv.getTypeUtils().asElement(am.getAnnotationType()))
                .getQualifiedName()
                .toString());
  }

  private boolean isAllExistsListMirror(AnnotationMirror am) {
    return AllExists.List.class
        .getCanonicalName()
        .contentEquals(
            ((TypeElement) processingEnv.getTypeUtils().asElement(am.getAnnotationType()))
                .getQualifiedName()
                .toString());
  }

  private boolean isExistsListMirror(AnnotationMirror am) {
    return Exists.List.class
        .getCanonicalName()
        .contentEquals(
            ((TypeElement) processingEnv.getTypeUtils().asElement(am.getAnnotationType()))
                .getQualifiedName()
                .toString());
  }

  private boolean isUniqueFieldListMirror(AnnotationMirror am) {
    return UniqueField.List.class
        .getCanonicalName()
        .contentEquals(
            ((TypeElement) processingEnv.getTypeUtils().asElement(am.getAnnotationType()))
                .getQualifiedName()
                .toString());
  }

  private boolean isUniqueFieldsMirror(AnnotationMirror am) {
    return UniqueFields.class
        .getCanonicalName()
        .contentEquals(
            ((TypeElement) processingEnv.getTypeUtils().asElement(am.getAnnotationType()))
                .getQualifiedName()
                .toString());
  }

  private void validateUniqueFieldListMirror(AnnotationMirror listMirror, Element element) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> values =
        processingEnv.getElementUtils().getElementValuesWithDefaults(listMirror);
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
      if (!e.getKey().getSimpleName().contentEquals("value")) {
        continue;
      }
      Object raw = e.getValue().getValue();
      if (!(raw instanceof List<?> list)) {
        continue;
      }
      for (Object o : list) {
        if (o instanceof AnnotationMirror nested) {
          validateUniqueFieldMirror(nested, element);
        }
      }
    }
  }

  private void validateExistsListMirror(AnnotationMirror listMirror, Element element) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> values =
        processingEnv.getElementUtils().getElementValuesWithDefaults(listMirror);
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
      if (!e.getKey().getSimpleName().contentEquals("value")) {
        continue;
      }
      Object raw = e.getValue().getValue();
      if (!(raw instanceof List<?> list)) {
        continue;
      }
      for (Object o : list) {
        if (o instanceof AnnotationMirror nested) {
          validateExistsMirror(nested, element);
        }
      }
    }
  }

  private void validateAllExistsListMirror(AnnotationMirror listMirror, Element element) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> values =
        processingEnv.getElementUtils().getElementValuesWithDefaults(listMirror);
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
      if (!e.getKey().getSimpleName().contentEquals("value")) {
        continue;
      }
      Object raw = e.getValue().getValue();
      if (!(raw instanceof List<?> list)) {
        continue;
      }
      for (Object o : list) {
        if (o instanceof AnnotationMirror nested) {
          validateAllExistsMirror(nested, element);
        }
      }
    }
  }

  private void validateUniqueFieldsMirror(AnnotationMirror containerMirror, Element element) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> values =
        processingEnv.getElementUtils().getElementValuesWithDefaults(containerMirror);
    AnnotationValue valueAttr = null;
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
      if (e.getKey().getSimpleName().contentEquals("value")) {
        valueAttr = e.getValue();
        break;
      }
    }
    if (valueAttr == null) {
      printError(element, "UniqueFields.value() missing");
      return;
    }
    Object raw = valueAttr.getValue();
    if (!(raw instanceof List<?> list) || list.isEmpty()) {
      printError(element, "UniqueFields.value() must not be empty");
      return;
    }
    int i = 0;
    for (Object o : list) {
      if (!(o instanceof AnnotationMirror nested)) {
        continue;
      }
      validateCompositeUniqueFieldRule(nested, element, i);
      i++;
    }
  }

  private void validateCompositeUniqueFieldRule(
      AnnotationMirror ruleMirror, Element element, int index) {
    ParsedUniqueField p = parseUniqueFieldMirror(ruleMirror);
    String prefix = "UniqueFields[" + index + "]: ";
    if (!assertJpaEntity(p.entityType, element, prefix + "entity ")) {
      return;
    }
    String dtoField = p.dtoField == null ? "" : p.dtoField.trim();
    if (dtoField.isBlank()) {
      printError(element, prefix + "dtoField must not be blank");
      return;
    }
    try {
      UniqueConstraintStaticRules.validateDtoPropertyPath(dtoField, prefix + "dtoField");
      UniqueConstraintStaticRules.validateJpaAttributePath(p.column, prefix + "column");
      String exclude = p.excludeIdDtoField == null ? "" : p.excludeIdDtoField.trim();
      if (!exclude.isBlank()) {
        UniqueConstraintStaticRules.validateDtoPropertyPath(exclude, prefix + "excludeIdDtoField");
      }
      UniqueConstraintStaticRules.validateEntityIdPropertyName(
          p.entityIdProperty == null ? "" : p.entityIdProperty, prefix + "entityIdProperty");
    } catch (IllegalArgumentException ex) {
      printError(element, prefix + ex.getMessage());
    }
  }

  private void validateUniqueFieldMirror(AnnotationMirror am, Element element) {
    ParsedUniqueField p = parseUniqueFieldMirror(am);
    if (!assertJpaEntity(p.entityType, element, "")) {
      return;
    }
    try {
      UniqueConstraintStaticRules.validateEntityIdPropertyName(
          p.entityIdProperty == null ? "" : p.entityIdProperty, "entityIdProperty");
      UniqueConstraintStaticRules.validateJpaAttributePath(p.column, "column");
    } catch (IllegalArgumentException ex) {
      printError(element, ex.getMessage());
      return;
    }

    String dtoField = p.dtoField == null ? "" : p.dtoField.trim();
    String exclude = p.excludeIdDtoField == null ? "" : p.excludeIdDtoField.trim();

    boolean fieldLike =
        element.getKind() == ElementKind.FIELD
            || element.getKind() == ElementKind.METHOD
            || element.getKind() == ElementKind.PARAMETER
            || element.getKind() == ElementKind.RECORD_COMPONENT;

    boolean typePlacement =
        element.getKind() == ElementKind.CLASS
            || element.getKind() == ElementKind.INTERFACE
            || element.getKind() == ElementKind.RECORD
            || element.getKind() == ElementKind.ENUM;

    if (fieldLike) {
      if (!dtoField.isBlank()) {
        printError(
            element, "dtoField must be blank when @UniqueField is on a field, method, or parameter");
      }
      if (!exclude.isBlank()) {
        printError(
            element,
            "excludeIdDtoField must be blank when dtoField is blank (field/method/parameter mode)");
      }
      return;
    }

    if (typePlacement) {
      if (dtoField.isBlank()) {
        printError(element, "dtoField is required when @UniqueField is placed on a type");
        return;
      }
      try {
        UniqueConstraintStaticRules.validateDtoPropertyPath(dtoField, "dtoField");
        if (!exclude.isBlank()) {
          UniqueConstraintStaticRules.validateDtoPropertyPath(exclude, "excludeIdDtoField");
        }
      } catch (IllegalArgumentException ex) {
        printError(element, ex.getMessage());
      }
      return;
    }

    printError(
        element,
        "@UniqueField is not supported on " + element.getKind() + " (unsupported placement)");
  }

  private void validateExistsMirror(AnnotationMirror am, Element element) {
    ParsedExists p = parseExistsMirror(am);
    if (!assertJpaEntity(p.entityType, element, "")) {
      return;
    }
    try {
      UniqueConstraintStaticRules.validateJpaAttributePath(p.column, "column");
      validateExistsWhereClauses(p.whereClauses);
    } catch (IllegalArgumentException ex) {
      printError(element, ex.getMessage());
      return;
    }

    String dtoField = p.dtoField == null ? "" : p.dtoField.trim();

    boolean fieldLike =
        element.getKind() == ElementKind.FIELD
            || element.getKind() == ElementKind.METHOD
            || element.getKind() == ElementKind.PARAMETER
            || element.getKind() == ElementKind.RECORD_COMPONENT;

    boolean typePlacement =
        element.getKind() == ElementKind.CLASS
            || element.getKind() == ElementKind.INTERFACE
            || element.getKind() == ElementKind.RECORD
            || element.getKind() == ElementKind.ENUM;

    if (fieldLike) {
      if (!dtoField.isBlank()) {
        printError(element, "dtoField must be blank when @Exists is on a field, method, or parameter");
      }
      return;
    }

    if (typePlacement) {
      if (dtoField.isBlank()) {
        printError(element, "dtoField is required when @Exists is placed on a type");
        return;
      }
      try {
        UniqueConstraintStaticRules.validateDtoPropertyPath(dtoField, "dtoField");
      } catch (IllegalArgumentException ex) {
        printError(element, ex.getMessage());
      }
      return;
    }

    printError(
        element, "@Exists is not supported on " + element.getKind() + " (unsupported placement)");
  }

  private void validateAllExistsMirror(AnnotationMirror am, Element element) {
    ParsedAllExists p = parseAllExistsMirror(am);
    if (!assertJpaEntity(p.entityType, element, "")) {
      return;
    }
    try {
      UniqueConstraintStaticRules.validateJpaAttributePath(p.column, "column");
    } catch (IllegalArgumentException ex) {
      printError(element, ex.getMessage());
      return;
    }

    String dtoField = p.dtoField == null ? "" : p.dtoField.trim();

    boolean fieldLike =
        element.getKind() == ElementKind.FIELD
            || element.getKind() == ElementKind.METHOD
            || element.getKind() == ElementKind.PARAMETER
            || element.getKind() == ElementKind.RECORD_COMPONENT;

    boolean typePlacement =
        element.getKind() == ElementKind.CLASS
            || element.getKind() == ElementKind.INTERFACE
            || element.getKind() == ElementKind.RECORD
            || element.getKind() == ElementKind.ENUM;

    if (fieldLike) {
      if (!dtoField.isBlank()) {
        printError(
            element, "dtoField must be blank when @AllExists is on a field, method, or parameter");
      }
      return;
    }

    if (typePlacement) {
      if (dtoField.isBlank()) {
        printError(element, "dtoField is required when @AllExists is placed on a type");
        return;
      }
      try {
        UniqueConstraintStaticRules.validateDtoPropertyPath(dtoField, "dtoField");
      } catch (IllegalArgumentException ex) {
        printError(element, ex.getMessage());
      }
      return;
    }

    printError(
        element, "@AllExists is not supported on " + element.getKind() + " (unsupported placement)");
  }

  private boolean assertJpaEntity(TypeMirror entityType, Element reportOn, String prefix) {
    if (entityType == null || entityType.getKind() == TypeKind.ERROR) {
      printError(reportOn, prefix + "entity type is invalid");
      return false;
    }
    if (!(entityType instanceof DeclaredType declared)) {
      printError(reportOn, prefix + "entity must be a class or interface type");
      return false;
    }
    Element typeEl = processingEnv.getTypeUtils().asElement(declared);
    if (!(typeEl instanceof TypeElement typeElement)) {
      printError(reportOn, prefix + "entity must be a declared type");
      return false;
    }
    if (!hasAnnotation(typeElement, "jakarta.persistence.Entity")) {
      printError(
          reportOn,
          prefix + "entity must be annotated with @jakarta.persistence.Entity: "
              + typeElement.getQualifiedName());
      return false;
    }
    return true;
  }

  private boolean hasAnnotation(TypeElement typeElement, String annotationFqn) {
    for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
      TypeElement ann = (TypeElement) processingEnv.getTypeUtils().asElement(m.getAnnotationType());
      if (annotationFqn.contentEquals(ann.getQualifiedName().toString())) {
        return true;
      }
    }
    for (TypeMirror iface : typeElement.getInterfaces()) {
      Element ifaceEl = processingEnv.getTypeUtils().asElement(iface);
      if (ifaceEl instanceof TypeElement te && hasAnnotation(te, annotationFqn)) {
        return true;
      }
    }
    TypeMirror sup = typeElement.getSuperclass();
    if (sup instanceof DeclaredType supDecl) {
      Element supEl = processingEnv.getTypeUtils().asElement(supDecl);
      if (supEl instanceof TypeElement te && hasAnnotation(te, annotationFqn)) {
        return true;
      }
    }
    return false;
  }

  private ParsedUniqueField parseUniqueFieldMirror(AnnotationMirror am) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> map =
        processingEnv.getElementUtils().getElementValuesWithDefaults(am);
    ParsedUniqueField p = new ParsedUniqueField();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : map.entrySet()) {
      String key = e.getKey().getSimpleName().toString();
      Object v = e.getValue().getValue();
      switch (key) {
        case "entity" -> {
          if (v instanceof TypeMirror tm) {
            p.entityType = tm;
          }
        }
        case "column" -> p.column = v instanceof String s ? s : null;
        case "dtoField" -> p.dtoField = v instanceof String s ? s : null;
        case "excludeIdDtoField" -> p.excludeIdDtoField = v instanceof String s ? s : null;
        case "entityIdProperty" -> p.entityIdProperty = v instanceof String s ? s : null;
        default -> {}
      }
    }
    return p;
  }

  private ParsedExists parseExistsMirror(AnnotationMirror am) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> map =
        processingEnv.getElementUtils().getElementValuesWithDefaults(am);
    ParsedExists p = new ParsedExists();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : map.entrySet()) {
      String key = e.getKey().getSimpleName().toString();
      Object v = e.getValue().getValue();
      switch (key) {
        case "entity" -> {
          if (v instanceof TypeMirror tm) {
            p.entityType = tm;
          }
        }
        case "column" -> p.column = v instanceof String s ? s : null;
        case "dtoField" -> p.dtoField = v instanceof String s ? s : null;
        case "where" -> p.whereClauses = parseExistsWhereClauses(v);
        default -> {}
      }
    }
    return p;
  }

  private List<ParsedExistsWhere> parseExistsWhereClauses(Object value) {
    if (!(value instanceof List<?> list) || list.isEmpty()) {
      return List.of();
    }
    List<ParsedExistsWhere> clauses = new java.util.ArrayList<>();
    for (Object item : list) {
      if (!(item instanceof AnnotationValue annotationValue)) {
        continue;
      }
      Object raw = annotationValue.getValue();
      if (!(raw instanceof AnnotationMirror clauseMirror)) {
        continue;
      }
      clauses.add(parseExistsWhereClause(clauseMirror));
    }
    return List.copyOf(clauses);
  }

  private ParsedExistsWhere parseExistsWhereClause(AnnotationMirror clauseMirror) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> map =
        processingEnv.getElementUtils().getElementValuesWithDefaults(clauseMirror);
    ParsedExistsWhere clause = new ParsedExistsWhere();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : map.entrySet()) {
      String key = e.getKey().getSimpleName().toString();
      Object v = e.getValue().getValue();
      switch (key) {
        case "column" -> clause.column = v instanceof String s ? s : null;
        case "value" -> clause.value = v instanceof String s ? s : null;
        default -> {}
      }
    }
    return clause;
  }

  private void validateExistsWhereClauses(List<ParsedExistsWhere> clauses) {
    if (clauses == null || clauses.isEmpty()) {
      return;
    }
    for (ParsedExistsWhere clause : clauses) {
      UniqueConstraintStaticRules.validateJpaAttributePath(clause.column, "where.column");
      String value = clause.value == null ? "" : clause.value.trim();
      if (value.isBlank()) {
        throw new IllegalArgumentException("where.value must not be blank");
      }
    }
  }

  private ParsedAllExists parseAllExistsMirror(AnnotationMirror am) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> map =
        processingEnv.getElementUtils().getElementValuesWithDefaults(am);
    ParsedAllExists p = new ParsedAllExists();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : map.entrySet()) {
      String key = e.getKey().getSimpleName().toString();
      Object v = e.getValue().getValue();
      switch (key) {
        case "entity" -> {
          if (v instanceof TypeMirror tm) {
            p.entityType = tm;
          }
        }
        case "column" -> p.column = v instanceof String s ? s : null;
        case "dtoField" -> p.dtoField = v instanceof String s ? s : null;
        default -> {}
      }
    }
    return p;
  }

  private void printError(Element element, String msg) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
  }

  private static final class ParsedUniqueField {
    TypeMirror entityType;
    String column;
    String dtoField;
    String excludeIdDtoField;
    String entityIdProperty;
  }

  private static final class ParsedExists {
    TypeMirror entityType;
    String column;
    String dtoField;
    List<ParsedExistsWhere> whereClauses = List.of();
  }

  private static final class ParsedExistsWhere {
    String column;
    String value;
  }

  private static final class ParsedAllExists {
    TypeMirror entityType;
    String column;
    String dtoField;
  }
}
