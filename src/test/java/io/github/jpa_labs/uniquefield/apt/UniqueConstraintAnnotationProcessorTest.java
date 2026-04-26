package io.github.jpa_labs.uniquefield.apt;

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
                    "io.github.jpa_labs.uniquefield.ProcessorGenBad",
                    """
                    package io.github.jpa_labs.uniquefield;
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
                    "io.github.jpa_labs.uniquefield.ProcessorGenBad2",
                    """
                    package io.github.jpa_labs.uniquefield;
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
                    "io.github.jpa_labs.uniquefield.ProcessorGenGood",
                    """
                    package io.github.jpa_labs.uniquefield;
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
                    "io.github.jpa_labs.uniquefield.ProcessorGenExistsBad",
                    """
                    package io.github.jpa_labs.uniquefield;
                    public class ProcessorGenExistsBad {
                      @Exists(entity=SampleEntity.class, column="code", dtoField="code")
                      private String code;
                    }
                    """));
    assertThat(compilation).failed();
    assertThat(compilation).hadErrorContaining("dtoField must be blank when @Exists");
  }
}
