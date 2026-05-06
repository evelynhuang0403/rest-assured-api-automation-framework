package com.restassured.api.tests.assertions.product;

import java.util.Map;

public final class ProductSearchAssertions {
    private ProductSearchAssertions() {
    }

    /** Checks if a product matches the search term against title, description, or category (case-insensitive). */
    public static boolean isProductMatchingSearchTerm(Map<String, Object> product, String searchTerm) {
        String normalizedSearchTerm = searchTerm.toLowerCase();

        String title = String.valueOf(product.get("title")).toLowerCase();
        String description = String.valueOf(product.get("description")).toLowerCase();
        String category = String.valueOf(product.get("category")).toLowerCase();

        return title.contains(normalizedSearchTerm)
                || description.contains(normalizedSearchTerm)
                || category.contains(normalizedSearchTerm);
    }
}
