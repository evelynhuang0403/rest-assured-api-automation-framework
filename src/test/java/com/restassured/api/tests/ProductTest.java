package com.restassured.api.tests;

import com.restassured.api.models.request.product.ProductRequest;
import com.restassured.api.models.response.product.Product;
import com.restassured.api.models.response.product.ProductList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.restassured.api.clients.ProductClient.addProduct;
import static com.restassured.api.clients.ProductClient.deleteProduct;
import static com.restassured.api.clients.ProductClient.getProductById;
import static com.restassured.api.clients.ProductClient.getProducts;
import static com.restassured.api.clients.ProductClient.patchProduct;
import static com.restassured.api.clients.ProductClient.searchProducts;
import static com.restassured.api.clients.ProductClient.updateProduct;
import static com.restassured.api.constants.SchemaPaths.ERROR_SCHEMA;
import static com.restassured.api.constants.SchemaPaths.PRODUCT_SCHEMA;
import static com.restassured.api.tests.assertions.product.ProductAssertions.assertEveryProductMatchesSearchTerm;
import static com.restassured.api.tests.assertions.product.ProductAssertions.assertProductFieldsMatch;
import static com.restassured.api.tests.assertions.product.ProductAssertions.assertProductHasId;
import static com.restassured.api.tests.assertions.product.ProductAssertions.assertProductListHasProducts;
import static com.restassured.api.tests.assertions.product.ProductAssertions.assertProductListPagination;
import static com.restassured.api.tests.assertions.product.ProductAssertions.assertValidProductSummary;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

class ProductTest extends BaseApiTest {

    private static final int PAGE_LIMIT = 10;
    private static final int PAGE_SKIP = 0;
    private static final int KNOWN_PRODUCT_ID = 1;
    private static final int PRODUCT_TO_UPDATE_ID = 1;
    private static final int PRODUCT_TO_PATCH_ID = 1;
    private static final int PRODUCT_TO_DELETE_ID = 1;

    @Test
    @DisplayName("GET /products returns a paginated, well-formed list")
    void getProductsReturnsPaginatedResponse() {
        ProductList productList = getProducts(PAGE_LIMIT, PAGE_SKIP)
                .then()
                    .statusCode(200)
                .extract()
                    .as(ProductList.class);

        assertProductListPagination(productList, PAGE_LIMIT, PAGE_SKIP);
        assertProductListHasProducts(productList);
    }

    @Test
    @DisplayName("GET /products/{id} returns a product conforming to the product schema")
    void getProductByIdReturnsProductMatchingSchema() {
        Product product = getProductById(KNOWN_PRODUCT_ID)
                .then()
                    .statusCode(200)
                    .body(matchesJsonSchemaInClasspath(PRODUCT_SCHEMA))
                .extract()
                    .as(Product.class);

        assertProductHasId(product, KNOWN_PRODUCT_ID);
        assertValidProductSummary(product);
    }

    @ParameterizedTest(name = "GET /products/{0} returns 404 for invalid product id")
    @ValueSource(ints = {0, -1, 999999})
    void getProductByIdReturnsNotFoundForInvalidIds(int productId) {
        getProductById(productId)
                .then()
                    .statusCode(404)
                    .body(matchesJsonSchemaInClasspath(ERROR_SCHEMA))
                    .body("message", equalTo("Product with id '" + productId + "' not found"));
    }

    @ParameterizedTest(name = "GET /products/search?q={0} returns products matching the term")
    @ValueSource(strings = {"phone", "watch", "shirt"})
    void searchProductsReturnsMatchingResults(String searchTerm) {
        ProductList productList = searchProducts(searchTerm)
                .then()
                    .statusCode(200)
                .extract()
                    .as(ProductList.class);

        assertProductListHasProducts(productList);
        assertEveryProductMatchesSearchTerm(productList, searchTerm);
    }

    @Test
    @DisplayName("POST /products/add creates a product with the requested fields")
    void addProductCreatesProductWithRequestedFields() {
        String title = "Portfolio API Testing Backpack";
        String description = "A durable backpack used to demonstrate product creation assertions.";
        String category = "accessories";
        float price = 79.99F;
        int stock = 25;
        String brand = "QA Gear";

        ProductRequest request = new ProductRequest(title, description, category, price, stock, brand);

        Product created = addProduct(request)
                .then()
                    .statusCode(201)
                .extract()
                    .as(Product.class);

        assertProductHasId(created, 195);
        //verify that all fields in the created product match the request values
        assertProductFieldsMatch(created, title, description, category, price, stock, brand);
    }

    @Test
    @DisplayName("PUT /products/{id} updates a product with a complete request body")
    void putProductUpdatesProductWithCompleteRequestBody() {
        String updatedTitle = "Updated API Testing Backpack";
        String updatedDescription = "Updated backpack used to demonstrate complete product update assertions.";
        String updatedCategory = "travel";
        float updatedPrice = 89.99F;
        int updatedStock = 30;
        String updatedBrand = "Updated QA Gear";

        ProductRequest request = new ProductRequest(updatedTitle, updatedDescription, updatedCategory, updatedPrice, updatedStock, updatedBrand);

        Product updated = updateProduct(PRODUCT_TO_UPDATE_ID, request)
                .then()
                    .statusCode(200)
                .extract()
                    .as(Product.class);

        assertProductHasId(updated, PRODUCT_TO_UPDATE_ID);
        //verify that all fields in the updated product match the request values
        assertProductFieldsMatch(updated, updatedTitle, updatedDescription, updatedCategory, updatedPrice, updatedStock, updatedBrand);
    }

    @Test
    @DisplayName("PATCH /products/{id} partially updates a product and preserves omitted fields")
    void patchProductPartiallyUpdatesProductAndPreservesOmittedFields() {
        Product original = getProductById(PRODUCT_TO_PATCH_ID)
                .then()
                    .statusCode(200)
                    .extract()
                    .as(Product.class);

        String patchedTitle = "Patched API Testing Backpack";

        //Send a PATCH request with only the title field to update
        ProductRequest request = new ProductRequest(patchedTitle, null, null, null, null, null);

        Product patched = patchProduct(PRODUCT_TO_PATCH_ID, request)
                .then()
                    .statusCode(200)
                .extract()
                    .as(Product.class);

        assertProductHasId(patched, PRODUCT_TO_PATCH_ID);
        //verify that only the title was updated and all other fields remain unchanged
        assertProductFieldsMatch(
                patched,
                patchedTitle,
                original.getDescription(),
                original.getCategory(),
                original.getPrice(),
                original.getStock(),
                original.getBrand()
        );
    }

    @Test
    @DisplayName("DELETE /products/{id} marks the product as deleted")
    void deleteProductMarksProductAsDeleted() {
        deleteProduct(PRODUCT_TO_DELETE_ID)
                .then()
                    .statusCode(200)
                    .body("id", equalTo(PRODUCT_TO_DELETE_ID))
                    .body("isDeleted", equalTo(true));
    }
}
