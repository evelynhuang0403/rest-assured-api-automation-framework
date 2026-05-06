# REST Assured API Automation Framework

Portfolio-ready Java API automation framework built to demonstrate practical QA Automation and SDET skills with REST Assured, JUnit 5, Maven, reusable clients, schema validation, and data-driven testing.

The framework targets [DummyJSON](https://dummyjson.com/docs), a public API with realistic product, cart, user, and authentication workflows.

## What This Demonstrates

- Java 17 API test automation with Maven
- REST Assured request and response validation
- JUnit 5 test organization and parameterized tests
- Reusable API client layer for Products, Auth, and Carts
- Request and response POJOs for non-trivial payloads
- JSON-driven negative login scenarios
- Positive and negative API validation
- Authentication token flow through `/auth/login` and `/auth/me`
- End-to-end API flow from login to authenticated user cart lookup
- JSON schema validation for product, cart, auth, and error contracts
- Logback-backed request, response, and test execution logging

## Test Coverage

### Authentication

- Valid login response validation
- Invalid login scenarios from external JSON test data
- Authenticated profile lookup with bearer token
- Login and profile schema validation

### Products

- Paginated product listing
- Product lookup by ID
- Product search relevance validation
- Invalid product ID handling
- Product create, update, patch, and delete flows
- Product schema validation

### Carts

- Cart lookup by ID
- Invalid cart ID handling
- Carts by user validation
- Cart create, update, patch, and delete flows
- Cart schema validation

### End-to-End Flow

- Login as a configured user
- Extract access token
- Fetch authenticated profile through `/auth/me`
- Use the authenticated user ID to validate the user's carts

## Project Structure

```text
src/test/java/com/restassured
├── api/
│   ├── clients/          Reusable API clients for Auth, Products, and Carts
│   └── specs/            REST Assured request and response specifications
├── constants/            Endpoint, schema, and test data path constants
├── models/
│   ├── request/          Request POJOs for non-trivial payloads
│   ├── response/         Response POJOs used by assertions and flows
│   └── testdata/         POJOs for external JSON test data
├── tests/                JUnit 5 API and end-to-end test classes
│   └── assertions/       Domain-specific assertion helpers
└── utils/                Config, JSON data reading, properties, and logging

src/test/resources
├── schemas/              JSON schema contracts
├── testdata/             External JSON test data
├── logback-test.xml      Test logging configuration
└── test-config.properties
```

## Tech Stack

- Java 17
- Maven
- JUnit 5
- REST Assured
- JSON Schema Validator
- Jackson
- Logback

## Run Tests

```bash
mvn test
```

The test suite runs against the live DummyJSON API, so an internet connection is required.

## Current Status

The current MVP includes Products, Auth, Carts, and an end-to-end authenticated flow. The next planned portfolio enhancements are Allure reporting, GitHub Actions CI, and AI-assisted failure triage.
