package ch.denic0la.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusTest
public class SecuredLinkControllerTest {

    @Test
    public void testCreateSecuredLink_Complete() {
        String key = "key-" + System.currentTimeMillis();
        String json = """
                {
                    "accessKey": "%s",
                    "secondFactorKey": "123456",
                    "targetLink": "https://google.com"
                }
                """.formatted(key);

        String expectedAccessKey = key.replace(" ", "-");
        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/secured-links")
                .then()
                .statusCode(201)
                .body("accessKey", is(expectedAccessKey))
                .body("secondFactorKeyRaw", is("123456"))
                .body("secondFactorKeyHashed", notNullValue())
                .body("targetLink", is("https://google.com"))
                .body("accessLink", containsString("http://localhost:8080/secured/" + expectedAccessKey));
    }

    @Test
    public void testCreateSecuredLink_Generated() {
        String json = """
                {
                    "targetLink": "https://google.com"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/secured-links")
                .then()
                .statusCode(201)
                .body("accessKey", notNullValue())
                .body("secondFactorKeyRaw", matchesPattern("^\\d{6}$"))
                .body("secondFactorKeyHashed", notNullValue())
                .body("accessLink", notNullValue());
    }

    @Test
    public void testCreateSecuredLink_InvalidAccessKey() {
        String json = """
                {
                    "accessKey": "key@123",
                    "targetLink": "https://google.com"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/secured-links")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateSecuredLink_InvalidSecondFactor() {
        String json = """
                {
                    "secondFactorKey": "12345",
                    "targetLink": "https://google.com"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/secured-links")
                .then()
                .statusCode(400);
    }

    @Test
    public void testGetSecuredLink() {
        String key = "get-key-" + System.currentTimeMillis();
        String json = """
                {
                    "accessKey": "%s",
                    "secondFactorKey": "111222",
                    "targetLink": "https://example.com"
                }
                """.formatted(key);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/secured-links")
                .then()
                .statusCode(201);

        given()
                .when().get("/secured-links/" + key)
                .then()
                .statusCode(200)
                .body("accessKey", is(key))
                .body("targetLink", is("https://example.com"))
                .body("secondFactorKey", notNullValue()); // Hashed in entity
    }

    @Test
    public void testCreateSecuredLink_DuplicateAccessKey() {
        String key = "duplicate-key-" + System.currentTimeMillis();
        String json = """
                {
                    "accessKey": "%s",
                    "targetLink": "https://google.com"
                }
                """.formatted(key);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/secured-links")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/secured-links")
                .then()
                .statusCode(409)
                .body(is("accessKey already exists"));
    }

    @Test
    public void testGetSecuredLink_NotFound() {
        given()
                .when().get("/secured-links/non-existent")
                .then()
                .statusCode(404);
    }
}
