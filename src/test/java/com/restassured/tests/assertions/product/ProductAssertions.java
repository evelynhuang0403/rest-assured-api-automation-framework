package com.restassured.tests.assertions.product;

import com.restassured.models.response.product.Product;
import com.restassured.models.response.product.ProductList;

import java.util.List;

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
        assertThat("Product id should be positive", product.getId(), greaterThan(0));
        assertThat("Product title should be present", product.getTitle(), is(not(emptyOrNullString())));
        assertThat("Product description should be present", product.getDescription(), is(not(emptyOrNullString())));
        assertThat("Product category should be present", product.getCategory(), is(not(emptyOrNullString())));
        assertThat("Product price should be greater than zero", product.getPrice(), greaterThan(0F));
    }

    //region Product Identity and Field Assertions
    /** Verifies that the product has the expected id. */
    public static void assertProductHasId(Product product, int expectedId) {
        assertThat("Product id should match the requested id", product.getId(), equalTo(expectedId));
    }

    /** Verifies that the product has the expected title. */
    public static void assertProductTitleIs(Product product, String expectedTitle) {
        assertThat("Product title should match the request payload", product.getTitle(), equalTo(expectedTitle));
    }

    /** Verifies that the product has the expected description. */
    public static void assertProductDescriptionIs(Product product, String expectedDescription) {
        assertThat("Product description should match the request payload", product.getDescription(), equalTo(expectedDescription));
    }

    /** Verifies that the product has the expected price. */
    public static void assertProductPriceIs(Product product, float expectedPrice) {
        assertThat("Product price should match the request payload within rounding tolerance",
                (double) product.getPrice(),
                closeTo(expectedPrice, 0.01));
    }

    /** Verifies that the product has the expected stock value. */
    public static void assertProductStockIs(Product product, int expectedStock) {
        assertThat("Product stock should match the request payload", product.getStock(), equalTo(expectedStock));
    }

    /** Verifies that the product has the expected category. */
    public static void assertProductCategoryIs(Product product, String expectedCategory) {
        assertThat("Product category should match the request payload", product.getCategory(), equalTo(expectedCategory));
    }

    /** Verifies that the product has the expected brand. */
    public static void assertProductBrandIs(Product product, String expectedBrand) {
        assertThat("Product brand should match the request payload", product.getBrand(), equalTo(expectedBrand));
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
        assertThat("Product page limit should match requested limit", productList.getLimit(), equalTo(expectedLimit));
        assertThat("Product page skip should match requested skip", productList.getSkip(), equalTo(expectedSkip));
    }

    /** Verifies that a product list response contains at least one product. */
    public static void assertProductListHasProducts(ProductList productList) {
        assertThat("Product list should include at least one product", productList.getProducts(), is(not(empty())));
        assertThat("Product list total should be greater than zero", productList.getTotal(), greaterThan(0));
    }

    /** Verifies that every returned product matches the search term. */
    public static void assertEveryProductMatchesSearchTerm(ProductList productList, String searchTerm) {
        List<Integer> unmatchedProductIds = productList.getProducts().stream()
                .filter(product -> !isProductMatchingSearchTerm(product, searchTerm))
                .map(Product::getId)
                .toList();

        assertThat(
                "Every returned product should match search term '%s'; unmatched product ids: %s"
                        .formatted(searchTerm, unmatchedProductIds),
                unmatchedProductIds,
                is(empty())
        );
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
