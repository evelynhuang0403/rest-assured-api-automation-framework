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
- JSON schema validation for product, cart, auth, and error contracts
- Logback-backed request, response, and test execution logging
- Allure reporting with domain labels, severity, execution metadata, and redacted HTTP evidence

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
├── tests/                JUnit 5 API test classes
│   └── assertions/       Domain-specific assertion helpers
└── utils/                Config, JSON data reading, properties, and logging

src/test/resources
├── allure/               Allure environment and category metadata templates
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
mvn clean test
```

The test suite runs against the live DummyJSON API, so an internet connection is required.

## Allure Reporting

Run the full suite and create raw Allure result files:

```bash
mvn clean test
```

Generate the static HTML report:

```bash
mvn allure:report
```

Open the generated report:

```bash
open reports/allure-report/index.html
```

You can also start a temporary local report server:

```bash
mvn allure:serve
```

Allure output locations:

- Raw result files: `target/allure-results`
- Static HTML report: `reports/allure-report`

The Allure report groups tests by API domain, story, severity, and tags. It also includes local execution metadata and redacted HTTP request/response evidence so failures can be investigated without exposing credentials, bearer tokens, or cookies.

## Current Status

The current MVP includes Products, Auth, Carts, and enterprise-style Allure reporting. The next planned portfolio enhancements are GitHub Actions CI and AI-assisted failure triage.
