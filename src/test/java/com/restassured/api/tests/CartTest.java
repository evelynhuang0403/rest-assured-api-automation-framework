package com.restassured.api.tests;

import com.restassured.api.models.request.cart.AddCartRequest;
import com.restassured.api.models.request.cart.CartProductRequest;
import com.restassured.api.models.response.cart.Cart;
import com.restassured.api.models.response.cart.CartList;
import com.restassured.api.models.response.cart.CartProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.restassured.api.clients.CartClient.addCart;
import static com.restassured.api.clients.CartClient.getCart;
import static com.restassured.api.clients.CartClient.getCartsByUser;
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
        assertThat(created.getTotalProducts(), equalTo(2));
        //assert total price is sum of total price of each product
        assertThat(created.getTotal(), equalTo(totalPrice(created)));
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

    // endregion


}

