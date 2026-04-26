# jpa-field-constraints

Jakarta Bean Validation constraints for **JPA-backed field checks** on DTOs: `@UniqueField`, `@UniqueFields`, `@Exists`, and `@AllExists` for Spring Boot 3 with Hibernate / JPA.

- **Java 17**, Spring Boot 3.x (BOM aligned in this repo to 3.5.x).
- **Runtime**: validators are Spring beans (EntityManager is injected); auto-configuration registers them.
- **Optional APT**: add the same coordinates as `annotationProcessor` so invalid annotation config fails at **compile time** (not only when constraints initialize at runtime).

Maven coordinates (artifact id `jpa-field-constraints`, Java packages `io.github.jpa_labs.jpafieldconstraints.*`):

| Source | `groupId` | `artifactId` | Version |
|--------|-----------|--------------|---------|
| [JitPack](https://jitpack.io/#jpa-labs/jpa-field-constraints) | `com.github.jpa-labs` | `jpa-field-constraints` | Git tag or commit hash |
| GitHub Packages | `com.github.jpa-labs` | `jpa-field-constraints` | Release tag (see workflow) |

## Gradle

**JitPack** — add the repository and dependency:

```gradle
repositories {
    mavenCentral()
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.jpa-labs:jpa-field-constraints:TAG_OR_COMMIT'
    annotationProcessor 'com.github.jpa-labs:jpa-field-constraints:TAG_OR_COMMIT' // optional, recommended
}
```

**GitHub Packages** — use the registry URL and credentials (PAT with `read:packages`). Example `gradle.properties`:

```properties
gpr.user=GITHUB_USERNAME
gpr.key=GITHUB_TOKEN_OR_PAT
```

Repository URL: `https://maven.pkg.github.com/jpa-labs/jpa-field-constraints` (see `build.gradle` `publishing`).

Publishing from CI is handled by `.github/workflows/gradle-publish.yml` (on GitHub Release; `VERSION` is taken from the release tag).

## Maven

JitPack:

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

Add the same artifact as `annotationProcessor` scope if you want compile-time checks.

## Usage

Ensure **`spring-boot-starter-data-jpa`** (or equivalent) and validation are on the classpath. Auto-configuration imports the validators.

**Single property** — annotate the field (or method / parameter); `entity` must be a JPA `@Entity` class; `column` is the **entity JavaBean property path** (e.g. nested `basicInfo.fanNumber`):

```java
public class RegisterResidentRequest {
  @UniqueField(entity = Resident.class, column = "urid")
  private String urid;
}
```

**Type-level / multiple paths** — use `@UniqueFields` on the class; each nested `@UniqueField` must set a non-blank `dtoField()`:

```java
@UniqueFields({
  @UniqueField(entity = Item.class, column = "code", dtoField = "code"),
  @UniqueField(entity = Item.class, column = "sku", dtoField = "sku")
})
public class ItemRequest { ... }
```

**Updates (exclude current row)** — set `excludeIdDtoField` to a DTO path for the entity id and optionally `entityIdProperty` (default `id`). This only changes the uniqueness query; **authorization** for that id remains your responsibility (see Javadoc on `excludeIdDtoField`).

Other useful attributes: `ignoreNullOrEmpty`, `ignoreCase`, repeatable `@UniqueField` via `@UniqueField.List`.

**Existence check** - use `@Exists` when a DTO value must already be present in an entity attribute:

```java
public class AssignRequest {
  @Exists(entity = Resident.class, column = "urid")
  private String residentUrid;
}
```

`@Exists` supports field/method/parameter placement and type-level placement via `dtoField`, similar to `@UniqueField`.

**Collection existence check** - use `@AllExists` when all incoming IDs/codes must already exist:

```java
public class AssignRolesRequest {
  @AllExists(entity = Role.class, column = "id")
  private List<UUID> roleIds;
}
```

`@AllExists` supports field/method/parameter placement (on iterable values) and type-level placement via `dtoField`.

## Build & test

```bash
./gradlew build
```

## CI

- `.github/workflows/ci.yml` — build on push / pull request to `main` or `master`.
- `.github/workflows/gradle-publish.yml` — publish to GitHub Packages on release (and manual dispatch).

[JitPack](https://jitpack.io/#jpa-labs/jpa-field-constraints) builds use `jitpack.yml` (OpenJDK 17); version for Gradle is supplied by JitPack via the `VERSION` environment variable when applicable.

## License

Apache License 2.0 (see POM in `build.gradle`).
