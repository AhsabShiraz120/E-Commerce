package com.bookworm;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Full happy-path integration test against H2 in PostgreSQL mode.
 * Register → login → add cart line → checkout → pay → verify order state.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthFlowTest {

    @LocalServerPort
    int port;

    @Autowired
    org.springframework.core.env.Environment env;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @Test
    void register_login_addToCart_checkout_pay_flow() {
        String email = "alice+" + System.nanoTime() + "@example.com";
        String password = "Str0ng@Pass";

        // 1. Register
        String accessToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "%s",
                          "firstName": "Alice",
                          "lastName": "Wonder"
                        }
                        """.formatted(email, password))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .body("accessToken", not(emptyString()))
                .body("user.email", equalToIgnoringCase(email))
                .extract().path("accessToken");

        // 2. Add an address
        Integer addressId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body("""
                        {"line1":"12 MG Road","city":"Bengaluru","state":"KA","pin":"560001"}
                        """)
                .when()
                .post("/me/addresses")
                .then()
                .statusCode(201)
                .body("isDefault", equalTo(true))
                .extract().path("id");

        // 3. Add a book to cart (seeded id=1 by V3)
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body("""
                        {"bookId": 1, "quantity": 1}
                        """)
                .when()
                .post("/cart/items")
                .then()
                .statusCode(200)
                .body("items.size()", equalTo(1));

        // 4. Checkout
        Integer orderId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body("""
                        {"addressId": %d, "useGiftPoints": 0}
                        """.formatted(addressId))
                .when()
                .post("/orders/checkout")
                .then()
                .statusCode(201)
                .body("status", equalTo("PENDING"))
                .extract().path("id");

        // 5. Pay — non-0000 card succeeds
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body("""
                        {"method":"CREDIT","cardNumber":"4111111111111234",
                         "cardholderName":"A","expiry":"12/28","cvv":"123"}
                        """)
                .when()
                .post("/orders/" + orderId + "/pay")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));

        // 6. Verify order is now PAID
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("status", equalTo("PAID"));
    }

    @Test
    void payment_declined_when_card_ends_in_0000() {
        String email = "bob+" + System.nanoTime() + "@example.com";
        String token = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"Str0ng@Pass","firstName":"Bob","lastName":"Q"}
                        """.formatted(email))
                .when()
                .post("/auth/register")
                .then().statusCode(201).extract().path("accessToken");

        int addressId = given()
                .contentType(ContentType.JSON).header("Authorization", "Bearer " + token)
                .body("""
                        {"line1":"1 Elm","city":"Bengaluru","state":"KA","pin":"560001"}
                        """)
                .when().post("/me/addresses")
                .then().statusCode(201).extract().path("id");

        given()
                .contentType(ContentType.JSON).header("Authorization", "Bearer " + token)
                .body("""
                        {"bookId":1,"quantity":1}
                        """)
                .when().post("/cart/items").then().statusCode(200);

        int orderId = given()
                .contentType(ContentType.JSON).header("Authorization", "Bearer " + token)
                .body("""
                        {"addressId":%d,"useGiftPoints":0}
                        """.formatted(addressId))
                .when().post("/orders/checkout")
                .then().statusCode(201).extract().path("id");

        given()
                .contentType(ContentType.JSON).header("Authorization", "Bearer " + token)
                .body("""
                        {"method":"CREDIT","cardNumber":"4111111111110000",
                         "cardholderName":"B","expiry":"12/28","cvv":"123"}
                        """)
                .when().post("/orders/" + orderId + "/pay")
                .then()
                .statusCode(402)
                .body("code", equalTo("payment_declined"));

        // Order must still be PENDING so the user can retry
        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/orders/" + orderId)
                .then().statusCode(200).body("status", equalTo("PENDING"));
    }
}
