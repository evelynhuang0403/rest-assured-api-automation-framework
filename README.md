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
- Automatic AI-assisted failure triage with Allure attachments and CI artifacts

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
src/test/java
├── com/ai/               AI-assisted failure triage support
│   ├── client/           OpenAI Responses API client wrapper
│   ├── config/           AI triage configuration reader
│   ├── context/          Per-test evidence capture context
│   ├── model/            Triage evidence and result records
│   └── service/          Triage orchestration and markdown rendering
└── com/restassured
    ├── api/
    │   ├── clients/      Reusable API clients for Auth, Products, and Carts
    │   └── specs/        REST Assured request and response specifications
    ├── constants/        Endpoint, schema, and test data path constants
    ├── models/           Request, response, and test data POJOs
    ├── tests/            JUnit 5 API test classes
    └── utils/            Config, JSON data reading, properties, and logging

src/test/resources
├── allure/               Allure environment and category metadata templates
├── schemas/              JSON schema contracts
├── testdata/             External JSON test data
├── ai-triage.properties.template
│                         Safe AI triage defaults and local API key placeholder
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

## AI-Assisted Failure Triage

The framework automatically generates failure triage when a test fails. It collects redacted REST Assured request/response evidence, JUnit failure metadata, and a stack trace excerpt, then attaches an `AI Failure Triage` markdown summary to the failed test in Allure.

Live AI triage uses the OpenAI Responses API when `ai.triage.apiKey` is configured in the ignored local properties file. The model returns structured JSON, Java validates it into an `AiTriageSummary`, and the framework renders the final markdown itself for stable report formatting. If the key is missing, the model call fails, or the model returns invalid output, the framework attaches an `AI Failure Triage Unavailable` note with the reason instead of generating a rule-based substitute. AI triage failures never change the Maven test result; pass/fail status remains controlled by the API tests.

Optional configuration:

Use `src/test/resources/ai-triage.properties.template` as a copy-only template. The runtime config file is `src/test/resources/ai-triage.properties`, which is ignored by Git for local overrides:

```properties
ai.triage.apiKey=
ai.triage.model=gpt-5-mini
ai.triage.timeout.seconds=45
ai.triage.maxRetries=1
ai.triage.maxOutputTokens=3000
ai.triage.reportDirectory=target/ai-triage
```

For local live AI triage, put the OpenAI key in the ignored `ai.triage.apiKey` value. The Java framework reads this properties file directly and does not require an environment variable. For CI, store the key in GitHub Secrets as `OPENAI_API_KEY`; the workflow creates the ignored properties file during the run.
If the local properties file is absent, the framework uses the same built-in defaults and marks AI triage unavailable when no API key is available.

Run the suite and generate the report:

```bash
mvn clean test
mvn allure:report
open reports/allure-report/index.html
```

When a test fails, review these Allure attachments:

- `Failure Summary`
- `HTTP Request`
- `HTTP Response`
- `AI Failure Triage`

AI triage output locations:

- Per-failure markdown files: `target/ai-triage`
- Aggregated markdown report: `target/ai-triage/failure-triage-report.md`
- CI artifact: `ai-triage-reports`

The report includes a defect title, category, expected result, actual result, suspected root cause, reproduction steps, recommended next debugging action, and confidence score.

## CI/CD Reporting

GitHub Actions runs the API suite on pushes to `main`, pull requests to `main`, and manual workflow dispatches.

The CI workflow:

- runs `mvn -B clean test`
- generates the Allure HTML report even when tests fail
- uploads raw Allure results, Surefire reports, AI triage reports, and the HTML report as workflow artifacts
- publishes the latest Allure report to the `gh-pages` branch after successful `main` branch workflow execution

Pull request runs upload report artifacts only. They do not publish to GitHub Pages.

Allure Trend is usually empty in local reports because `mvn clean` removes previous `target` history. In CI, the workflow restores the previous report's `history/` directory from the `gh-pages` branch before generating the next report, so trend charts become useful after at least two `main` branch runs.

Generated report output remains ignored by Git:

- `target/allure-results`
- `reports/allure-report`
- `.allure`
