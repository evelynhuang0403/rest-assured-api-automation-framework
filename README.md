# REST Assured Automation Framework

Java API automation framework for practicing enterprise-style REST API testing with REST Assured and JUnit 5.

## Current Scope

- REST Assured request setup
- JUnit 5 test execution
- Reusable request specification
- First smoke test against DummyJSON

## Tech Stack

- Java 21
- Maven
- JUnit 5
- REST Assured
- JSON Schema Validator

## Run Tests

```bash
mvn test
```

## Learning Goal

This project will grow into an AI-assisted API automation framework with reusable request specs, authentication handling, schema validation, negative testing, and failure-triage support.

## API Under Test

The framework currently targets [DummyJSON](https://dummyjson.com/docs), which provides realistic practice domains such as products, carts, users, auth, posts, comments, and todos.
