package com.restassured.tests;

import com.restassured.models.request.cart.AddCartRequest;
import com.restassured.models.request.cart.CartProductRequest;
import com.restassured.models.request.cart.PatchCartRequest;
import com.restassured.models.request.cart.UpdateCartRequest;
import com.restassured.models.response.cart.Cart;
import com.restassured.models.response.cart.CartList;
import com.restassured.api.clients.CartClient;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.restassured.constants.SchemaPaths.CART_SCHEMA;
import static com.restassured.constants.SchemaPaths.ERROR_SCHEMA;
import static com.restassured.tests.assertions.cart.CartAssertions.assertCartBelongsToUser;
import static com.restassured.tests.assertions.cart.CartAssertions.assertCartHasId;
import static com.restassured.tests.assertions.cart.CartAssertions.assertCartHasOnlyProductIds;
import static com.restassured.tests.assertions.cart.CartAssertions.assertCartHasProductLineCount;
import static com.restassured.tests.assertions.cart.CartAssertions.assertCartIncludesProductIds;
import static com.restassured.tests.assertions.cart.CartAssertions.assertCartTotalQuantityIs;
import static com.restassured.tests.assertions.cart.CartAssertions.assertEveryCartBelongsToUser;
import static com.restassured.tests.assertions.cart.CartAssertions.assertProductLineQuantityIs;
import static com.restassured.tests.assertions.cart.CartAssertions.assertValidCartSummary;
import static io.qameta.allure.SeverityLevel.MINOR;
import static io.qameta.allure.SeverityLevel.NORMAL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Epic("REST Assured API Automation Framework")
@Feature("Carts")
@Owner("Evelyn Wong")
@Tag("api")
@Tag("carts")
class CartTest extends BaseApiTest {

    private static final int KNOWN_CART_ID = 1;
    private static final int USER_WITH_CARTS_ID = 33;

    @Test
    @Story("Cart schema validation")
    @Severity(NORMAL)
    @DisplayName("GET /carts/{id} returns a well-formed cart")
    void existingCartIsWellFormed() {
        Cart cart = CartClient.getCart(KNOWN_CART_ID)
                .then()
                    .statusCode(200)
                    .body(matchesJsonSchemaInClasspath(CART_SCHEMA))
                .extract()
                    .as(Cart.class);

        assertCartHasId(cart, KNOWN_CART_ID);
        assertValidCartSummary(cart);
    }

    @ParameterizedTest(name = "GET /carts/{0} returns 404 for invalid cart id")
    @Story("Invalid cart id validation")
    @Severity(MINOR)
    @ValueSource(ints = {0, 999999, 1000000})
    void invalidCartIdReturns404(int invalidId) {
        CartClient.getCart(invalidId)
                .then()
                    .statusCode(404)
                    .body(matchesJsonSchemaInClasspath(ERROR_SCHEMA))
                    .body("message", equalTo("Cart with id '" + invalidId + "' not found"));
    }

    @Test
    @Story("User cart lookup")
    @Severity(NORMAL)
    @DisplayName("GET /carts/user/{userId} returns only that user's carts")
    void cartsByUserContainsOnlyThatUsersCarts() {
        CartList cartList = CartClient.getCartsByUser(USER_WITH_CARTS_ID)
                .then()
                    .statusCode(200)
                .extract()
                    .as(CartList.class);

        assertEveryCartBelongsToUser(cartList, USER_WITH_CARTS_ID);
    }

    @Test
    @Story("Cart creation")
    @Severity(NORMAL)
    @DisplayName("POST /carts/add creates a cart with the requested products")
    void addCartCreatesCartWithRequestedProducts() {
        int userId = 1;
        AddCartRequest request = new AddCartRequest(userId, List.of(
                new CartProductRequest(144, 4),
                new CartProductRequest(98, 1)
        ));

        Cart created = CartClient.addCart(request)
                .then()
                    .statusCode(201)
                    .body(matchesJsonSchemaInClasspath(CART_SCHEMA))
                .extract()
                    .as(Cart.class);

        assertValidCartSummary(created);
        assertCartBelongsToUser(created, userId);
        assertCartHasProductLineCount(created, 2);
        assertCartHasOnlyProductIds(created, 144, 98);
        assertCartTotalQuantityIs(created, 5);
    }

    @Test
    @Story("Cart full update")
    @Severity(NORMAL)
    @DisplayName("PUT /carts/{id} with merge=true adds new products to the existing cart")
    void putCartWithMergeTrueAddsProductsToExistingCart() {
        Cart existing = CartClient.getCart(KNOWN_CART_ID).then().statusCode(200).extract().as(Cart.class);
        int newProductId = 1;
        UpdateCartRequest request = new UpdateCartRequest(true, List.of(
                new CartProductRequest(newProductId, 2)
        ));

        Cart updated = CartClient.updateCart(KNOWN_CART_ID, request)
                .then()
                    .statusCode(200)
                    .body(matchesJsonSchemaInClasspath(CART_SCHEMA))
                .extract()
                    .as(Cart.class);

        assertValidCartSummary(updated);
        assertCartHasId(updated, KNOWN_CART_ID);
        assertCartIncludesProductIds(updated, newProductId);
        assertThat(updated.getTotalProducts(), equalTo(existing.getTotalProducts() + 1));
        assertProductLineQuantityIs(updated, newProductId, 2);
    }

    @Test
    @Story("Cart partial update")
    @Severity(NORMAL)
    @DisplayName("PATCH /carts/{id} replaces the cart's product list")
    void patchCartReplacesProductList() {
        int replacementProductId = 1;
        PatchCartRequest request = new PatchCartRequest(List.of(
                new CartProductRequest(replacementProductId, 5)
        ));

        Cart patched = CartClient.patchCart(KNOWN_CART_ID, request)
                .then()
                    .statusCode(200)
                    .body(matchesJsonSchemaInClasspath(CART_SCHEMA))
                .extract()
                    .as(Cart.class);

        assertValidCartSummary(patched);
        assertCartHasId(patched, KNOWN_CART_ID);
        assertCartHasProductLineCount(patched, 1);
        assertCartHasOnlyProductIds(patched, replacementProductId);
        assertCartTotalQuantityIs(patched, 5);
        assertProductLineQuantityIs(patched, replacementProductId, 5);
    }

    @Test
    @Story("Cart deletion")
    @Severity(NORMAL)
    @DisplayName("DELETE /carts/{id} marks the cart as deleted")
    void deleteCartMarksCartAsDeleted() {
        CartClient.deleteCart(KNOWN_CART_ID)
                .then()
                    .statusCode(200)
                    .body("id", equalTo(KNOWN_CART_ID))
                    .body("isDeleted", is(true));
    }

}
