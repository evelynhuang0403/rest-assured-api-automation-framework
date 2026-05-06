package com.restassured.api.tests;

import com.restassured.api.models.request.cart.AddCartRequest;
import com.restassured.api.models.request.cart.CartProductRequest;
import com.restassured.api.models.request.cart.PatchCartRequest;
import com.restassured.api.models.request.cart.UpdateCartRequest;
import com.restassured.api.models.response.cart.Cart;
import com.restassured.api.models.response.cart.CartList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.restassured.api.clients.CartClient.addCart;
import static com.restassured.api.clients.CartClient.deleteCart;
import static com.restassured.api.clients.CartClient.getCart;
import static com.restassured.api.clients.CartClient.getCartsByUser;
import static com.restassured.api.clients.CartClient.patchCart;
import static com.restassured.api.clients.CartClient.updateCart;
import static com.restassured.api.constants.SchemaPaths.CART_SCHEMA;
import static com.restassured.api.constants.SchemaPaths.ERROR_SCHEMA;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertCartBelongsToUser;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertCartHasId;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertCartHasOnlyProductIds;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertCartHasProductLineCount;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertCartIncludesProductIds;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertCartTotalQuantityIs;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertEveryCartBelongsToUser;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertProductLineQuantityIs;
import static com.restassured.api.tests.assertions.cart.CartAssertions.assertValidCartSummary;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class CartTest extends BaseApiTest {

    private static final int KNOWN_CART_ID = 1;
    private static final int USER_WITH_CARTS_ID = 33;

    @Test
    @DisplayName("GET /carts/{id} returns a well-formed cart")
    void existingCartIsWellFormed() {
        Cart cart = getCart(KNOWN_CART_ID)
                .then()
                    .statusCode(200)
                    .body(matchesJsonSchemaInClasspath(CART_SCHEMA))
                .extract()
                    .as(Cart.class);

        assertCartHasId(cart, KNOWN_CART_ID);
        assertValidCartSummary(cart);
    }

    @ParameterizedTest(name = "GET /carts/{0} returns 404 for invalid cart id")
    @ValueSource(ints = {0, 999999, 1000000})
    void invalidCartIdReturns404(int invalidId) {
        getCart(invalidId)
                .then()
                    .statusCode(404)
                    .body(matchesJsonSchemaInClasspath(ERROR_SCHEMA))
                    .body("message", equalTo("Cart with id '" + invalidId + "' not found"));
    }

    @Test
    @DisplayName("GET /carts/user/{userId} returns only that user's carts")
    void cartsByUserContainsOnlyThatUsersCarts() {
        CartList cartList = getCartsByUser(USER_WITH_CARTS_ID)
                .then()
                    .statusCode(200)
                .extract()
                    .as(CartList.class);

        assertEveryCartBelongsToUser(cartList, USER_WITH_CARTS_ID);
    }

    @Test
    @DisplayName("POST /carts/add creates a cart with the requested products")
    void addCartCreatesCartWithRequestedProducts() {
        int userId = 1;
        AddCartRequest request = new AddCartRequest(userId, List.of(
                new CartProductRequest(144, 4),
                new CartProductRequest(98, 1)
        ));

        Cart created = addCart(request)
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
    @DisplayName("PUT /carts/{id} with merge=true adds new products to the existing cart")
    void putCartWithMergeTrueAddsProductsToExistingCart() {
        Cart existing = getCart(KNOWN_CART_ID).then().statusCode(200).extract().as(Cart.class);
        int newProductId = 1;
        UpdateCartRequest request = new UpdateCartRequest(true, List.of(
                new CartProductRequest(newProductId, 2)
        ));

        Cart updated = updateCart(KNOWN_CART_ID, request)
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
    @DisplayName("PATCH /carts/{id} replaces the cart's product list")
    void patchCartReplacesProductList() {
        int replacementProductId = 1;
        PatchCartRequest request = new PatchCartRequest(List.of(
                new CartProductRequest(replacementProductId, 5)
        ));

        Cart patched = patchCart(KNOWN_CART_ID, request)
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
    @DisplayName("DELETE /carts/{id} marks the cart as deleted")
    void deleteCartMarksCartAsDeleted() {
        deleteCart(KNOWN_CART_ID)
                .then()
                    .statusCode(200)
                    .body("id", equalTo(KNOWN_CART_ID))
                    .body("isDeleted", is(true));
    }

}
