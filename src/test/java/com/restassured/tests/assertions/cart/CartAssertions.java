package com.restassured.tests.assertions.cart;

import com.restassured.models.response.cart.Cart;
import com.restassured.models.response.cart.CartList;
import com.restassured.models.response.cart.CartProduct;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public final class CartAssertions {
    private CartAssertions() {
    }

    /** Validates common cart invariants returned by successful cart endpoints. */
    public static void assertValidCartSummary(Cart cart) {
        assertThat(cart.getUserId(), greaterThan(0));
        assertThat(cart.getProducts(), is(not(empty())));
        assertThat(cart.getTotal(), greaterThan(0F));
        assertThat(cart.getTotalProducts(), equalTo(cart.getProducts().size()));
        assertThat(cart.getTotalQuantity(), equalTo(totalQuantity(cart)));
        assertThat(cart.getTotal(), equalTo(totalPrice(cart)));
    }

    //region Cart Identity and Ownership Assertions
    /** Verifies that the cart has the expected cart id. */
    public static void assertCartHasId(Cart cart, int expectedCartId) {
        assertThat(cart.getId(), equalTo(expectedCartId));
    }

    /** Verifies that the cart belongs to the expected user id. */
    public static void assertCartBelongsToUser(Cart cart, int expectedUserId) {
        assertThat(cart.getUserId(), equalTo(expectedUserId));
    }

    /** Verifies that every cart in the list belongs to the expected user id. */
    public static void assertEveryCartBelongsToUser(CartList cartList, int expectedUserId) {
        assertThat(cartList.getCarts(), is(not(empty())));
        assertThat(userIds(cartList), everyItem(equalTo(expectedUserId)));
    }
    //endregion

    //region Cart Product List Expectations Assertions
    /** Verifies the number of product lines in the cart. */
    public static void assertCartHasProductLineCount(Cart cart, int expectedLineCount) {
        assertThat(cart.getProducts(), hasSize(expectedLineCount));
        assertThat(cart.getTotalProducts(), equalTo(expectedLineCount));
    }

    /** Verifies that the cart includes the expected product ids. */
    public static void assertCartIncludesProductIds(Cart cart, Integer... expectedProductIds) {
        assertThat(productIds(cart), hasItems(expectedProductIds));
    }

    /** Verifies that the cart contains exactly the expected product ids. */
    public static void assertCartHasOnlyProductIds(Cart cart, Integer... expectedProductIds) {
        assertThat(productIds(cart), containsInAnyOrder(expectedProductIds));
    }
    //endregion

    //region Quantity Assertions
    /** Verifies the cart's total quantity value. */
    public static void assertCartTotalQuantityIs(Cart cart, int expectedTotalQuantity) {
        assertThat(cart.getTotalQuantity(), equalTo(expectedTotalQuantity));
    }

    /** Verifies the quantity for one product line in the cart. */
    public static void assertProductLineQuantityIs(Cart cart, int productId, int expectedQuantity) {
        assertThat(quantityForProduct(cart, productId), equalTo(expectedQuantity));
    }
    //endregion

    //region Helper methods
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
    //endregion
}
