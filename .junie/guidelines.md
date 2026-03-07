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
  package ch.denic0la;

  import io.quarkus.test.junit.QuarkusTest;
  import org.junit.jupiter.api.Test;
  import static io.restassured.RestAssured.given;
  import static org.hamcrest.CoreMatchers.is;

  @QuarkusTest
  public class MyResourceTest {
      @Test
      public void testEndpoint() {
          given()
            .when().get("/hello")
            .then()
               .statusCode(200)
               .body(is("Hello from Quarkus REST"));
      }
  }
  ```

#### 3. Additional Development Information

- **Code Style**: Follow standard Java coding conventions. The project uses Jakarta EE annotations (e.g., `@ApplicationScoped`, `@Inject`, `@Path`).
- **Database Access**: Hibernate ORM with Panache is used for database operations. See `MyEntity.java` for an example of a Panache entity.
- **Migrations**: Database migrations are managed by Flyway. Place new migration scripts in `src/main/resources/db/migration`.
- **Docker**: Multiple Dockerfiles are available in `src/main/docker/` for different deployment targets (JVM, Native, etc.).
