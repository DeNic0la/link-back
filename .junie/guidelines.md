### Project Guidelines

#### 1. Build and Configuration

This project is a Quarkus application using Java 21 and Gradle.

- **Prerequisites**: Java 21+ and Docker (for local database).
- **Development Database**: A local PostgreSQL database is required. A `dev-db-compose.yaml` file is provided to start it:
  ```powershell
  docker compose -f dev-db-compose.yaml up -d
  ```
- **Configuration**: Main configuration is in `src/main/resources/application.yml`.
  - The application is configured to use PostgreSQL.
  - Flyway migrations are enabled and run on startup (`quarkus.flyway.migrate-at-start: true`).
- **Running in Dev Mode**:
  ```powershell
  ./gradlew quarkusDev
  ```

#### 2. Testing

Quarkus testing is used with JUnit 5 and RestAssured.

- **Running Tests**:
  ```powershell
  ./gradlew test
  ```
- **Adding New Tests**:
  - Use the `@QuarkusTest` annotation on your test classes.
  - Use `io.restassured.RestAssured` for testing REST endpoints.
- **Example Test**:
  ```java
  package ch.denic0la.controller;

  import io.quarkus.test.junit.QuarkusTest;
  import org.junit.jupiter.api.Test;
  import static io.restassured.RestAssured.given;
  import static org.hamcrest.CoreMatchers.notNullValue;

  @QuarkusTest
  public class SecuredLinkControllerTest {
      @Test
      public void testEndpoint() {
          given()
            .contentType("application/json")
            .body("{\"targetLink\": \"https://google.com\"}")
            .when().post("/secured-links")
            .then()
               .statusCode(201)
               .body("accessKey", notNullValue());
      }
  }
  ```

#### 3. Additional Development Information

- **Code Style**: Follow standard Java coding conventions. The project uses Jakarta EE annotations (e.g., `@ApplicationScoped`, `@Inject`, `@Path`).
- **Database Access**: Hibernate ORM with Panache is used for database operations. See `SecuredLink.java` for an example of a Panache entity.
- **Migrations**: Database migrations are managed by Flyway. Place new migration scripts in `src/main/resources/db/migration`.
- **Docker**: Multiple Dockerfiles are available in `src/main/docker/` for different deployment targets (JVM, Native, etc.).

#### 4. Secured Link Logic

The application provides a REST controller, `SecuredLinkController`, to manage secured links.

##### Entity: `SecuredLink`

- `accessKey`: (String, Unique) A unique identifier for the link.
- `secondFactorKey`: (String, Hashed) A 6-digit PIN, stored as a BCrypt hash.
- `targetLink`: (String) The destination URL.
- `hasBeenAccessed`: (Boolean) Tracks if the link has been used (defaults to `false`).

##### Controller: `SecuredLinkController`

- `POST /secured-links`: Creates a new secured link.
    - `accessKey`: Optional. If provided, it must be alphanumeric (spaces are replaced with `-`). If not provided, a random 8-character alphanumeric string is generated.
    - `secondFactorKey`: Optional. Must be exactly 6 digits. If not provided, a random 6-digit PIN is generated.
    - `targetLink`: Mandatory.
    - **Response**: Includes the `accessKey`, `secondFactorKeyRaw` (the unhashed PIN), `secondFactorKeyHashed`, `targetLink`, and `accessLink`.
- `GET /secured-links/{accessKey}`: Retrieves a `SecuredLink` by its access key.

##### Configuration

- `app.base-url`: Required. Used to construct the `accessLink` in the response. It is validated at startup to ensure it's not blank.
