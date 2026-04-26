package io.github.jpa_labs.jpafieldconstraints.apt;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

class UniqueConstraintAnnotationProcessorTest {

  @Test
  void failsWhenEntityIsNotJpaEntity() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    class Pojo {}
                    @UniqueField(entity=Pojo.class, column="code", dtoField="code")
                    public class ProcessorGenBad {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("@jakarta.persistence.Entity");
  }

  @Test
  void failsWhenUniqueFieldsRuleHasBlankDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenBad2",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @UniqueFields({ @UniqueField(entity=SampleEntity.class, column="code", dtoField="") })
                    public class ProcessorGenBad2 {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must not be blank");
  }

  @Test
  void passesForValidFieldLevelAnnotation() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenGood",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenGood {
                      @UniqueField(entity=SampleEntity.class, column="code")
                      private String code;
                    }
                    """));
    assertThat(compilation).succeeded();
  }

  @Test
  void failsWhenExistsHasDtoFieldOnField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenExistsBad {
                      @Exists(entity=SampleEntity.class, column="code", dtoField="code")
                      private String code;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @Exists");
  }

  @Test
  void failsWhenAllExistsHasDtoFieldOnField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    import java.util.List;
                    public class ProcessorGenAllExistsBad {
                      @AllExists(entity=SampleEntity.class, column="code", dtoField="codes")
                      private List<String> codes;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @AllExists");
  }

  @Test
  void passesForValidTypeLevelExistsAndAllExistsAnnotations() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenGoodExistsAllExists",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenGoodExistsAllExists {
                      @Exists(entity=SampleEntity.class, column="code", dtoField="code")
                      static class ExistsTypeLevelDto {
                        String code;
                      }

                      @AllExists(entity=SampleEntity.class, column="code", dtoField="codes")
                      static class AllExistsTypeLevelDto {
                        java.util.List<String> codes;
                      }
                    }
                    """));
    assertThat(compilation).succeeded();
  }

  @Test
  void failsWhenTypeLevelExistsMissingDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsTypeMissingDtoField",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @Exists(entity=SampleEntity.class, column="code")
                    public class ProcessorGenExistsTypeMissingDtoField {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField is required when @Exists is placed on a type");
  }

  @Test
  void failsWhenTypeLevelAllExistsMissingDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsTypeMissingDtoField",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @AllExists(entity=SampleEntity.class, column="code")
                    public class ProcessorGenAllExistsTypeMissingDtoField {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField is required when @AllExists is placed on a type");
  }

  @Test
  void failsWhenTypeLevelUniqueFieldMissingDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldTypeMissingDtoField",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @UniqueField(entity=SampleEntity.class, column="code")
                    public class ProcessorGenUniqueFieldTypeMissingDtoField {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField is required when @UniqueField is placed on a type");
  }

  @Test
  void failsWhenUniqueFieldListContainsInvalidRule() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldListBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenUniqueFieldListBad {
                      @UniqueField.List({
                        @UniqueField(entity=SampleEntity.class, column="code", dtoField="code")
                      })
                      private String code;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @UniqueField");
  }

  @Test
  void failsWhenExistsListContainsInvalidRule() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsListBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenExistsListBad {
                      @Exists.List({
                        @Exists(entity=SampleEntity.class, column="code", dtoField="code")
                      })
                      private String code;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @Exists");
  }

  @Test
  void failsWhenAllExistsListContainsInvalidRule() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsListBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    import java.util.List;
                    public class ProcessorGenAllExistsListBad {
                      @AllExists.List({
                        @AllExists(entity=SampleEntity.class, column="code", dtoField="codes")
                      })
                      private List<String> codes;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @AllExists");
  }

  @Test
  void passesForValidRepeatableListAnnotations() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenValidLists",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    import java.util.List;
                    public class ProcessorGenValidLists {
                      @UniqueField.List({
                        @UniqueField(entity=SampleEntity.class, column="code")
                      })
                      private String code;

                      @Exists.List({
                        @Exists(entity=SampleEntity.class, column="code")
                      })
                      private String existsCode;

                      @AllExists.List({
                        @AllExists(entity=SampleEntity.class, column="code")
                      })
                      private List<String> codes;
                    }
                    """));
    assertThat(compilation).succeeded();
  }

  @Test
  void failsWhenExistsEntityIsNotJpaEntity() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsNonEntity",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    class Pojo {}
                    public class ProcessorGenExistsNonEntity {
                      @Exists(entity=Pojo.class, column="code")
                      private String code;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("@jakarta.persistence.Entity");
  }

  @Test
  void failsWhenAllExistsEntityIsNotJpaEntity() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsNonEntity",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    import java.util.List;
                    class Pojo {}
                    public class ProcessorGenAllExistsNonEntity {
                      @AllExists(entity=Pojo.class, column="code")
                      private List<String> codes;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("@jakarta.persistence.Entity");
  }

  @Test
  void failsWhenUniqueFieldsHasInvalidColumnShape() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldsBadColumn",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @UniqueFields({
                      @UniqueField(entity=SampleEntity.class, column="bad-path", dtoField="code")
                    })
                    public class ProcessorGenUniqueFieldsBadColumn {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Invalid");
  }

  @Test
  void failsWhenUniqueFieldsHasUnsafeExcludePath() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldsBadExclude",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @UniqueFields({
                      @UniqueField(
                        entity=SampleEntity.class,
                        column="code",
                        dtoField="code",
                        excludeIdDtoField="holder.class.name"
                      )
                    })
                    public class ProcessorGenUniqueFieldsBadExclude {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Unsafe");
  }

  @Test
  void failsWhenUniqueFieldsHasInvalidEntityIdProperty() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldsBadEntityId",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @UniqueFields({
                      @UniqueField(
                        entity=SampleEntity.class,
                        column="code",
                        dtoField="code",
                        entityIdProperty="id.leaf"
                      )
                    })
                    public class ProcessorGenUniqueFieldsBadEntityId {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("single property name");
  }

  @Test
  void failsWhenAnnotationAppliedToAnnotationTypeForExists() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsUnsupportedPlacement",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @Exists(entity=SampleEntity.class, column="code", dtoField="code")
                    public @interface ProcessorGenExistsUnsupportedPlacement {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("@Exists is not supported");
  }

  @Test
  void failsWhenAnnotationAppliedToAnnotationTypeForAllExists() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsUnsupportedPlacement",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @AllExists(entity=SampleEntity.class, column="code", dtoField="codes")
                    public @interface ProcessorGenAllExistsUnsupportedPlacement {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("@AllExists is not supported");
  }

  @Test
  void failsWhenAnnotationAppliedToAnnotationTypeForUniqueField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldUnsupportedPlacement",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @UniqueField(entity=SampleEntity.class, column="code", dtoField="code")
                    public @interface ProcessorGenUniqueFieldUnsupportedPlacement {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("@UniqueField is not supported");
  }

  @Test
  void failsWhenExistsHasInvalidColumnShape() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsBadColumn",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenExistsBadColumn {
                      @Exists(entity=SampleEntity.class, column="bad-path")
                      private String code;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Invalid column");
  }

  @Test
  void failsWhenAllExistsHasInvalidColumnShape() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsBadColumn",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    import java.util.List;
                    public class ProcessorGenAllExistsBadColumn {
                      @AllExists(entity=SampleEntity.class, column="bad-path")
                      private List<String> codes;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Invalid column");
  }

  @Test
  void passesWhenEntityAnnotationInheritedFromSuperclass() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenSuperclassEntity",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    import jakarta.persistence.Entity;
                    @Entity
                    class BaseEntity {}
                    class DerivedEntity extends BaseEntity {}
                    public class ProcessorGenSuperclassEntity {
                      @Exists(entity=DerivedEntity.class, column="id")
                      private String id;
                    }
                    """));
    assertThat(compilation).succeeded();
  }

  @Test
  void failsWhenEntityAttributeIsPrimitiveClassLiteral() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenPrimitiveEntityType",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenPrimitiveEntityType {
                      @Exists(entity=int.class, column="id")
                      private String id;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("entity must be a class or interface type");
  }

  @Test
  void failsWhenUniqueFieldOnMethodHasDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldMethodBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenUniqueFieldMethodBad {
                      @UniqueField(entity=SampleEntity.class, column="code", dtoField="code")
                      public String code() { return ""; }
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @UniqueField");
  }

  @Test
  void failsWhenExistsOnParameterHasDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsParamBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenExistsParamBad {
                      public void run(@Exists(entity=SampleEntity.class, column="code", dtoField="code") String code) {}
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @Exists");
  }

  @Test
  void failsWhenAllExistsOnRecordComponentHasDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsRecordBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    import java.util.List;
                    public record ProcessorGenAllExistsRecordBad(
                      @AllExists(entity=SampleEntity.class, column="code", dtoField="codes") List<String> codes
                    ) {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @AllExists");
  }

  @Test
  void failsWhenExistsOnInterfaceTypeMissingDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsInterfaceMissingDto",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @Exists(entity=SampleEntity.class, column="code")
                    public interface ProcessorGenExistsInterfaceMissingDto {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField is required when @Exists is placed on a type");
  }

  @Test
  void failsWhenAllExistsOnEnumTypeMissingDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsEnumMissingDto",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @AllExists(entity=SampleEntity.class, column="code")
                    public enum ProcessorGenAllExistsEnumMissingDto { A }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField is required when @AllExists is placed on a type");
  }

  @Test
  void passesWhenEntityAnnotationInheritedFromInterface() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenInterfaceEntity",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    import jakarta.persistence.Entity;
                    @Entity
                    interface MarkerEntity {}
                    class ImplEntity implements MarkerEntity {}
                    public class ProcessorGenInterfaceEntity {
                      @Exists(entity=ImplEntity.class, column="id")
                      private String id;
                    }
                    """));
    assertThat(compilation).succeeded();
  }

  @Test
  void failsWhenUniqueFieldOnMethodHasExcludeIdWithoutDtoField() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldMethodExcludeBad",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    public class ProcessorGenUniqueFieldMethodExcludeBad {
                      @UniqueField(entity=SampleEntity.class, column="code", excludeIdDtoField="id")
                      public String code() { return ""; }
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation)
        .hadErrorContaining("excludeIdDtoField must be blank when dtoField is blank");
  }

  @Test
  void failsWhenUniqueFieldTypeHasUnsafeExcludePath() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldTypeUnsafeExclude",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @UniqueField(
                      entity=SampleEntity.class,
                      column="code",
                      dtoField="code",
                      excludeIdDtoField="holder.class.name"
                    )
                    public class ProcessorGenUniqueFieldTypeUnsafeExclude {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Unsafe");
  }

  @Test
  void failsWhenUniqueFieldTypeHasInvalidEntityIdProperty() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenUniqueFieldTypeBadEntityId",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @UniqueField(
                      entity=SampleEntity.class,
                      column="code",
                      dtoField="code",
                      entityIdProperty="id.value"
                    )
                    public class ProcessorGenUniqueFieldTypeBadEntityId {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("single property name");
  }

  @Test
  void failsWhenExistsTypeHasUnsafeDtoPath() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenExistsTypeUnsafeDto",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @Exists(entity=SampleEntity.class, column="code", dtoField="holder.class.name")
                    public class ProcessorGenExistsTypeUnsafeDto {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Unsafe");
  }

  @Test
  void failsWhenAllExistsTypeHasUnsafeDtoPath() {
    Compilation compilation =
        javac()
            .withClasspathFrom(UniqueConstraintAnnotationProcessorTest.class.getClassLoader())
            .withProcessors(new UniqueConstraintAnnotationProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "io.github.jpa_labs.jpafieldconstraints.ProcessorGenAllExistsTypeUnsafeDto",
                    """
                    package io.github.jpa_labs.jpafieldconstraints;
                    @AllExists(entity=SampleEntity.class, column="code", dtoField="holder.class.name")
                    public class ProcessorGenAllExistsTypeUnsafeDto {}
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("Unsafe");
  }
}
