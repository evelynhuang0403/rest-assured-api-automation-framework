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
        assertThat("Cart user id should be positive", cart.getUserId(), greaterThan(0));
        assertThat("Cart should contain at least one product line", cart.getProducts(), is(not(empty())));
        assertThat("Cart total should be greater than zero", cart.getTotal(), greaterThan(0F));
        assertThat("Cart totalProducts should match product line count",
                cart.getTotalProducts(),
                equalTo(cart.getProducts().size()));
        assertThat("Cart totalQuantity should equal the sum of product line quantities",
                cart.getTotalQuantity(),
                equalTo(totalQuantity(cart)));
        assertThat("Cart total should equal the sum of product line totals", cart.getTotal(), equalTo(totalPrice(cart)));
    }

    //region Cart Identity and Ownership Assertions
    /** Verifies that the cart has the expected cart id. */
    public static void assertCartHasId(Cart cart, int expectedCartId) {
        assertThat("Cart id should match the requested id", cart.getId(), equalTo(expectedCartId));
    }

    /** Verifies that the cart belongs to the expected user id. */
    public static void assertCartBelongsToUser(Cart cart, int expectedUserId) {
        assertThat("Cart user id should match the expected owner", cart.getUserId(), equalTo(expectedUserId));
    }

    /** Verifies that every cart in the list belongs to the expected user id. */
    public static void assertEveryCartBelongsToUser(CartList cartList, int expectedUserId) {
        assertThat("User cart response should include at least one cart", cartList.getCarts(), is(not(empty())));
        assertThat("Every cart should belong to user id " + expectedUserId,
                userIds(cartList),
                everyItem(equalTo(expectedUserId)));
    }
    //endregion

    //region Cart Product List Expectations Assertions
    /** Verifies the number of product lines in the cart. */
    public static void assertCartHasProductLineCount(Cart cart, int expectedLineCount) {
        assertThat("Cart product line count should match expectation", cart.getProducts(), hasSize(expectedLineCount));
        assertThat("Cart totalProducts should match expected line count",
                cart.getTotalProducts(),
                equalTo(expectedLineCount));
    }

    /** Verifies that the cart includes the expected product ids. */
    public static void assertCartIncludesProductIds(Cart cart, Integer... expectedProductIds) {
        assertThat("Cart should include expected product ids", productIds(cart), hasItems(expectedProductIds));
    }

    /** Verifies that the cart contains exactly the expected product ids. */
    public static void assertCartHasOnlyProductIds(Cart cart, Integer... expectedProductIds) {
        assertThat("Cart should contain exactly the expected product ids",
                productIds(cart),
                containsInAnyOrder(expectedProductIds));
    }
    //endregion

    //region Quantity Assertions
    /** Verifies the cart's total quantity value. */
    public static void assertCartTotalQuantityIs(Cart cart, int expectedTotalQuantity) {
        assertThat("Cart total quantity should match expected quantity",
                cart.getTotalQuantity(),
                equalTo(expectedTotalQuantity));
    }

    /** Verifies the quantity for one product line in the cart. */
    public static void assertProductLineQuantityIs(Cart cart, int productId, int expectedQuantity) {
        assertThat("Cart product id %d should have expected quantity".formatted(productId),
                quantityForProduct(cart, productId),
                equalTo(expectedQuantity));
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
