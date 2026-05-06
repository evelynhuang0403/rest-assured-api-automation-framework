package com.restassured.tests.assertions.product;

import com.restassured.models.response.product.Product;
import com.restassured.models.response.product.ProductList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public final class ProductAssertions {
    private ProductAssertions() {
    }

    /** Validates common product invariants returned by successful product endpoints. */
    public static void assertValidProductSummary(Product product) {
        assertThat(product.getId(), greaterThan(0));
        assertThat(product.getTitle(), is(not(emptyOrNullString())));
        assertThat(product.getDescription(), is(not(emptyOrNullString())));
        assertThat(product.getCategory(), is(not(emptyOrNullString())));
        assertThat(product.getPrice(), greaterThan(0F));
    }

    //region Product Identity and Field Assertions
    /** Verifies that the product has the expected id. */
    public static void assertProductHasId(Product product, int expectedId) {
        assertThat(product.getId(), equalTo(expectedId));
    }

    /** Verifies that the product has the expected title. */
    public static void assertProductTitleIs(Product product, String expectedTitle) {
        assertThat(product.getTitle(), equalTo(expectedTitle));
    }

    /** Verifies that the product has the expected description. */
    public static void assertProductDescriptionIs(Product product, String expectedDescription) {
        assertThat(product.getDescription(), equalTo(expectedDescription));
    }

    /** Verifies that the product has the expected price. */
    public static void assertProductPriceIs(Product product, float expectedPrice) {
        assertThat((double) product.getPrice(), closeTo(expectedPrice, 0.01));
    }

    /** Verifies that the product has the expected stock value. */
    public static void assertProductStockIs(Product product, int expectedStock) {
        assertThat(product.getStock(), equalTo(expectedStock));
    }

    /** Verifies that the product has the expected category. */
    public static void assertProductCategoryIs(Product product, String expectedCategory) {
        assertThat(product.getCategory(), equalTo(expectedCategory));
    }

    /** Verifies that the product has the expected brand. */
    public static void assertProductBrandIs(Product product, String expectedBrand) {
        assertThat(product.getBrand(), equalTo(expectedBrand));
    }

    /** Verifies the common editable product fields against expected values. */
    public static void assertProductFieldsMatch(
            Product product,
            String expectedTitle,
            String expectedDescription,
            String expectedCategory,
            float expectedPrice,
            int expectedStock,
            String expectedBrand
    ) {
        assertProductTitleIs(product, expectedTitle);
        assertProductDescriptionIs(product, expectedDescription);
        assertProductCategoryIs(product, expectedCategory);
        assertProductPriceIs(product, expectedPrice);
        assertProductStockIs(product, expectedStock);
        assertProductBrandIs(product, expectedBrand);
    }
    //endregion

    //region Product List Assertions
    /** Verifies pagination metadata for a product list response. */
    public static void assertProductListPagination(ProductList productList, int expectedLimit, int expectedSkip) {
        assertThat(productList.getLimit(), equalTo(expectedLimit));
        assertThat(productList.getSkip(), equalTo(expectedSkip));
    }

    /** Verifies that a product list response contains at least one product. */
    public static void assertProductListHasProducts(ProductList productList) {
        assertThat(productList.getProducts(), is(not(empty())));
        assertThat(productList.getTotal(), greaterThan(0));
    }

    /** Verifies that every returned product matches the search term. */
    public static void assertEveryProductMatchesSearchTerm(ProductList productList, String searchTerm) {
        assertThat(productList.getProducts().stream()
                .allMatch(product -> isProductMatchingSearchTerm(product, searchTerm)), is(true));
    }
    //endregion

    //region Helper methods
    /** Helper method to determine if a product matches the search term in title, description, or category. */
    private static boolean isProductMatchingSearchTerm(Product product, String searchTerm) {
        String normalizedSearchTerm = searchTerm.toLowerCase();

        return product.getTitle().toLowerCase().contains(normalizedSearchTerm)
                || product.getDescription().toLowerCase().contains(normalizedSearchTerm)
                || product.getCategory().toLowerCase().contains(normalizedSearchTerm);
    }
    //endregion
}
