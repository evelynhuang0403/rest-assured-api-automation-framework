package com.restassured.api.tests;

import com.restassured.api.models.request.cart.AddCartRequest;
import com.restassured.api.models.request.cart.CartProductRequest;
import com.restassured.api.models.request.cart.PatchCartRequest;
import com.restassured.api.models.request.cart.UpdateCartRequest;
import com.restassured.api.models.response.cart.Cart;
import com.restassured.api.models.response.cart.CartList;
import com.restassured.api.models.response.cart.CartProduct;
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
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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

        assertThat(cart.getId(), equalTo(KNOWN_CART_ID));
        assertThat(cart.getUserId(), greaterThan(0));
        assertThat(cart.getProducts(), is(not(empty())));
        assertThat(cart.getTotal(), greaterThan(0F));
        assertThat(cart.getTotalProducts(), equalTo(cart.getProducts().size()));
    }

    @ParameterizedTest(name = "GET /carts/{0} returns 404 for invalid cart id")
    @ValueSource(ints = {0, 999999, 1000000})
    void invalidCartIdReturns404(int invalidId) {
        getCart(invalidId)
                .then()
                    .statusCode(404)
                    .body(matchesJsonSchemaInClasspath(ERROR_SCHEMA))
                    .body("message", containsString(String.valueOf(invalidId)));
    }

    @Test
    @DisplayName("GET /carts/user/{userId} returns only that user's carts")
    void cartsByUserContainsOnlyThatUsersCarts() {
        CartList cartList = getCartsByUser(USER_WITH_CARTS_ID)
                .then()
                    .statusCode(200)
                .extract()
                    .as(CartList.class);

        assertThat(cartList.getCarts(), is(not(empty())));
        assertThat(userIds(cartList), everyItem(equalTo(USER_WITH_CARTS_ID)));
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

        assertThat(created.getUserId(), equalTo(userId));
        assertThat(created.getProducts(), hasSize(2));
        assertThat(productIds(created), containsInAnyOrder(144, 98));
        assertThat(created.getTotalQuantity(), equalTo(5));
        assertThat(created.getTotalProducts(), equalTo(2));
        assertThat(created.getTotal(), equalTo(totalPrice(created)));
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

        assertThat(updated.getId(), equalTo(KNOWN_CART_ID));
        assertThat(productIds(updated), hasItem(newProductId));
        assertThat(updated.getTotalProducts(), equalTo(existing.getTotalProducts() + 1));
        assertThat(quantityForProduct(updated, newProductId), equalTo(2));
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

        assertThat(patched.getId(), equalTo(KNOWN_CART_ID));
        assertThat(patched.getProducts(), hasSize(1));
        assertThat(productIds(patched), containsInAnyOrder(replacementProductId));
        assertThat(patched.getTotalProducts(), equalTo(1));
        assertThat(quantityForProduct(patched, replacementProductId), equalTo(5));
        assertThat(patched.getTotalQuantity(), equalTo(5));
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

    // region Helper methods
    private static List<Integer> productIds(Cart cart) {
        return cart.getProducts().stream().map(CartProduct::getId).toList();
    }

    private static List<Integer> userIds(CartList cartList) {
        return cartList.getCarts().stream().map(Cart::getUserId).toList();
    }

    private static float totalPrice(Cart cart) {
        return cart.getProducts().stream()
                .map(CartProduct::getTotal)
                .reduce(0F, Float::sum);
    }

    private static int totalQuantity(Cart cart) {
        return cart.getProducts().stream()
                .mapToInt(CartProduct::getQuantity)
                .sum();
    }

    private static int quantityForProduct(Cart cart, int productId) {
        return cart.getProducts().stream()
                .filter(product -> product.getId() == productId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Product not found: " + productId))
                .getQuantity();
    }
    // endregion
}
