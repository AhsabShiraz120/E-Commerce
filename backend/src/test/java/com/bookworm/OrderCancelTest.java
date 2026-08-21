package com.bookworm;

import com.bookworm.order.entity.OrderEntity;
import com.bookworm.order.repo.OrderRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Verifies the 48-hour cancel window.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderCancelTest {

    @LocalServerPort int port;

    @Autowired OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @Test
    void cancel_within_window_succeeds() {
        Prep p = registerAndPlaceOrder();

        given()
                .header("Authorization", "Bearer " + p.token)
                .when().post("/orders/" + p.orderId + "/cancel")
                .then().statusCode(200).body("status", equalTo("CANCELLED"));
    }

    @Test
    void cancel_after_window_returns_409() {
        Prep p = registerAndPlaceOrder();

        // Age the order so the cancel window is closed.
        // Must commit before the HTTP call — no @Transactional on the test method.
        OrderEntity o = orderRepository.findById((long) p.orderId).orElseThrow();
        o.setCancellableUntil(OffsetDateTime.now().minusHours(1));
        orderRepository.saveAndFlush(o);

        given()
                .header("Authorization", "Bearer " + p.token)
                .when().post("/orders/" + p.orderId + "/cancel")
                .then().statusCode(409).body("code", equalTo("cancel_window_closed"));
    }

    // ---------------------------------------------------------------------------

    private record Prep(String token, int orderId) {}

    private Prep registerAndPlaceOrder() {
        String email = "user+" + System.nanoTime() + "@example.com";
        String token = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"Str0ng@Pass","firstName":"U","lastName":"T"}
                        """.formatted(email))
                .when().post("/auth/register").then().statusCode(201).extract().path("accessToken");

        int addressId = given()
                .contentType(ContentType.JSON).header("Authorization", "Bearer " + token)
                .body("""
                        {"line1":"1 Test","city":"Bengaluru","state":"KA","pin":"560001"}
                        """)
                .when().post("/me/addresses").then().statusCode(201).extract().path("id");

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
                .when().post("/orders/checkout").then().statusCode(201).extract().path("id");

        return new Prep(token, orderId);
    }
}
