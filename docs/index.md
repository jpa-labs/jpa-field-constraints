# jpa-field-constraints

Jakarta Bean Validation constraints for JPA-backed field checks in Spring Boot.

## What this library provides

- `@UniqueField` and `@UniqueFields` for uniqueness validation.
- `@Exists` and `@AllExists` for existence validation.
- Runtime validators with optional annotation processing for compile-time checks.

## Quick links

- [Repository](https://github.com/jpa-labs/jpa-field-constraints)
- [JitPack Package](https://jitpack.io/#jpa-labs/jpa-field-constraints)

## Requirements

- Java 17+
- Spring Boot 3.5.x or 4.0.x
- Jakarta Validation and JPA in your application
- A JPA provider (Hibernate is common)

## Installation

### Gradle (JitPack)

Add JitPack repository:

```gradle
repositories {
    mavenCentral()
    maven { url = 'https://jitpack.io' }
}
```

Add dependency (and optional annotation processor):

```gradle
dependencies {
    implementation 'com.github.jpa-labs:jpa-field-constraints:TAG_OR_COMMIT'
    annotationProcessor 'com.github.jpa-labs:jpa-field-constraints:TAG_OR_COMMIT' // optional, recommended
}
```

### Gradle (GitHub Packages)

Set credentials in `gradle.properties`:

```properties
gpr.user=GITHUB_USERNAME
gpr.key=GITHUB_TOKEN_OR_PAT
```

Use the package repository:

```gradle
repositories {
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/jpa-labs/jpa-field-constraints')
        credentials {
            username = findProperty('gpr.user') as String
            password = findProperty('gpr.key') as String
        }
    }
}
```

### Maven (JitPack)

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.jpa-labs</groupId>
  <artifactId>jpa-field-constraints</artifactId>
  <version>TAG_OR_COMMIT</version>
</dependency>
```

## Spring Boot setup

Ensure your application includes:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'com.github.jpa-labs:jpa-field-constraints:TAG_OR_COMMIT'
}
```

Validators are registered through auto-configuration; no manual bean setup is required.

## Usage examples

### 1) Unique single field with `@UniqueField`

```java
import io.github.jpa_labs.jpafieldconstraints.UniqueField;

public class RegisterResidentRequest {
    @UniqueField(entity = Resident.class, column = "urid")
    private String urid;
}
```

### 2) Multiple unique fields with `@UniqueFields`

```java
import io.github.jpa_labs.jpafieldconstraints.UniqueField;
import io.github.jpa_labs.jpafieldconstraints.UniqueFields;

@UniqueFields({
    @UniqueField(entity = Item.class, column = "code", dtoField = "code"),
    @UniqueField(entity = Item.class, column = "sku", dtoField = "sku")
})
public class ItemRequest {
    private String code;
    private String sku;
}
```

### 3) Update scenario (exclude current entity ID)

```java
import io.github.jpa_labs.jpafieldconstraints.UniqueField;

public class UpdateResidentRequest {
    private Long id;

    @UniqueField(
        entity = Resident.class,
        column = "urid",
        excludeIdDtoField = "id",
        entityIdProperty = "id"
    )
    private String urid;
}
```

### 4) Value must exist with `@Exists`

```java
import io.github.jpa_labs.jpafieldconstraints.Exists;

public class AssignRequest {
    @Exists(entity = Resident.class, column = "urid")
    private String residentUrid;
}
```

With additional filters:

```java
public class ApproveEmployeeRequest {
    @Exists(
        entity = Employee.class,
        column = "employeeNumber",
        where = {@Exists.Where(column = "status", value = "ACTIVE")}
    )
    private String employeeNumber;
}
```

Multiple filters are joined with AND:

```java
public class AssignAdminRequest {
    @Exists(
        entity = User.class,
        column = "username",
        where = {
            @Exists.Where(column = "status", value = "ACTIVE"),
            @Exists.Where(column = "role", value = "ADMIN")
        }
    )
    private String username;
}
```

### 5) All values must exist with `@AllExists`

```java
import io.github.jpa_labs.jpafieldconstraints.AllExists;
import java.util.List;
import java.util.UUID;

public class AssignRolesRequest {
    @AllExists(entity = Role.class, column = "id")
    private List<UUID> roleIds;
}
```

## More usage patterns

### 6) Type-level `@UniqueField` with `dtoField`

```java
import io.github.jpa_labs.jpafieldconstraints.UniqueField;

@UniqueField(entity = Product.class, column = "sku", dtoField = "sku")
public class CreateProductRequest {
    private String sku;
}
```

### 7) Nested entity property path

```java
import io.github.jpa_labs.jpafieldconstraints.Exists;

public class RegisterHouseholdRequest {
    @Exists(entity = Resident.class, column = "basicInfo.fanNumber")
    private String fanNumber;
}
```

### 8) Case-insensitive uniqueness

```java
import io.github.jpa_labs.jpafieldconstraints.UniqueField;

public class CreateUserRequest {
    @UniqueField(entity = User.class, column = "email", ignoreCase = true)
    private String email;
}
```

### 9) Ignore null/blank values

```java
import io.github.jpa_labs.jpafieldconstraints.UniqueField;

public class PatchResidentRequest {
    @UniqueField(
        entity = Resident.class,
        column = "phoneNumber",
        ignoreNullOrEmpty = true
    )
    private String phoneNumber;
}
```

### 10) Method parameter validation

```java
import io.github.jpa_labs.jpafieldconstraints.Exists;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
class AssignmentController {

    @PostMapping("/assign")
    public void assignResident(
        @RequestParam
        @Exists(entity = Resident.class, column = "urid") String residentUrid,
        @Valid AssignRequest request
    ) {
        // business logic
    }
}
```

### 11) Type-level `@Exists` with `dtoField`

```java
import io.github.jpa_labs.jpafieldconstraints.Exists;

@Exists(entity = Department.class, column = "code", dtoField = "departmentCode")
public class TransferRequest {
    private String departmentCode;
}
```

### 12) Type-level `@AllExists` with `dtoField`

```java
import io.github.jpa_labs.jpafieldconstraints.AllExists;
import java.util.List;
import java.util.UUID;

@AllExists(entity = Permission.class, column = "id", dtoField = "permissionIds")
public class UpdateRolePermissionsRequest {
    private List<UUID> permissionIds;
}
```

## End-to-end request validation flow

Use `@Valid` in controllers so Bean Validation runs automatically:

```java
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ResidentController {

    @PostMapping("/residents")
    ResponseEntity<Void> create(@Valid @RequestBody RegisterResidentRequest request) {
        // If constraints fail, Spring returns 400 with validation details.
        return ResponseEntity.ok().build();
    }
}
```

## Important notes

- `column` uses the entity JavaBean property path (for example: `basicInfo.fanNumber`).
- For type-level constraints, provide `dtoField` to point to the DTO property.
- `excludeIdDtoField` only changes uniqueness query behavior; authorization remains in your service layer.
- Useful options include `ignoreNullOrEmpty` and `ignoreCase`.

## Build and test

```bash
./gradlew build
```

Test against specific Spring Boot versions:

```bash
./gradlew clean test -PbootVersion=3.5.4
./gradlew clean test -PbootVersion=4.0.0
```

## License

Apache License 2.0.
