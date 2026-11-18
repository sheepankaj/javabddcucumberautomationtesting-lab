# CLAUDE.md — Java BDD Cucumber Automation Framework

This file gives Claude Code context about this project so it can make informed suggestions without re-reading everything from scratch.

## Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Build | Maven 3.9 |
| BDD | Cucumber 7.14 + JUnit 4 |
| UI automation | Selenium 4.15 (Selenium Manager for driver management) |
| API testing | REST Assured 5.3 |
| Reporting | Allure 2.24 + Cucumber native HTML |
| CI | GitHub Actions |
| Containers | Docker + Docker Compose |

## Key architectural decisions

### Driver management — Selenium Manager, not WebDriverManager
We removed WebDriverManager because v5.6.3 is incompatible with Chrome 115+ (released after WDM's last update). Selenium 4.15 ships with Selenium Manager which auto-downloads the matching driver binary at runtime. No `.setup()` calls are needed — `new ChromeDriver()` is sufficient.

### Parallel cross-browser — JVM forks, not threads
`ChromeTests`, `FirefoxTests`, `APITests` each set their browser via a `static {}` block. Maven Surefire `forkCount=3 reuseForks=false` launches them in 3 separate JVM processes simultaneously. The browser system property is JVM-local, so there is zero cross-contamination. ThreadLocal<WebDriver> in `DriverManager` handles thread safety within each fork.

### Allure TREND — file-based history outside `target/`
`allure-history/` at the project root stores Allure's `history/` files between runs. `Hooks.restoreAllureHistory()` copies them into `target/allure-results/history/` before the first scenario, so Allure picks them up when generating the report. `generate-report.bat` saves the new history back after the report is built. This survives `mvn clean`.

### Allure historyId per browser
Without a custom `historyId`, `AllureCucumber7Jvm` computes the same hash for Chrome and Firefox runs of the same scenario (feature + name, no browser in formula). Allure then collapses them as "retries". `Hooks.initBrowser()` sets `Allure.label("historyId", hash(browser + ":" + scenario.getId()))` to make each browser+scenario a distinct test entry.

### allure-maven 2.12.0 is incompatible with Java 17+
`mvn allure:serve` / `mvn allure:report` crash with `ExceptionInInitializerError` on Java 17+. The report IS generated successfully before the crash, but build fails so the antrun history-save step is skipped. **Workaround:** use `.\generate-report.bat` (Windows) which calls `.allure/allure-2.24.0/bin/allure.bat generate` directly — the downloaded Allure 2.24.0 CLI has no Java 17+ issue.

## Project structure

```
src/
  main/java/com/automation/
    config/ConfigReader.java      — loads config.properties; system props override
    pages/BasePage.java           — WebDriver/Wait base; POM helper methods
    pages/LoginPage.java          — SauceDemo login POM
    utils/DriverManager.java      — ThreadLocal<WebDriver> + browser version capture
  test/java/com/automation/
    hooks/Hooks.java              — Allure setup, browser init/quit, screenshot on fail
    runners/ChromeTests.java      — Cucumber JUnit4 runner, sets browser=chrome
    runners/FirefoxTests.java     — Cucumber JUnit4 runner, sets browser=firefox
    runners/APITests.java         — Cucumber JUnit4 runner, no browser
    stepdefinitions/
      LoginSteps.java             — SauceDemo login step implementations
      UserAPISteps.java           — JSONPlaceholder API step implementations
  test/resources/
    features/ui/login.feature     — 6 login scenarios (@ui @smoke @login-*)
    features/api/users.feature    — GET /users/2 field validation (@api)
    config.properties             — base.url, api.base.url, browser, headless, timeouts
    allure.properties             — allure.results.directory
    cucumber.properties           — cucumber.glue (for IDE step resolution)
allure-history/                   — persistent Allure TREND history (commit this)
generate-report.bat               — generate report + save history + open (Windows)
Dockerfile                        — headless test runner image (Chrome + Firefox)
docker-compose.yml                — Docker Compose for local container runs
.github/workflows/ci.yml          — GitHub Actions: Chrome + Firefox parallel jobs
```

## Common commands

```bash
# Run all tests (Chrome + Firefox + API in parallel)
mvn clean test

# Run with headless browsers (CI / Docker)
mvn clean test -Dheadless=true

# Run specific browser only
mvn clean test -Dtest=ChromeTests,APITests -Dheadless=true -Dbrowser=chrome
mvn clean test -Dtest=FirefoxTests -Dheadless=true -Dbrowser=firefox

# Generate Allure report + save history (Windows)
.\generate-report.bat

# Docker
docker compose up --build

# CI
# Push to main or develop → GitHub Actions triggers automatically
```

## Config overrides (all via -D system property)

| Property | Default | Description |
|---|---|---|
| `browser` | `chrome` | chrome / firefox / edge |
| `headless` | `false` | true for CI/Docker |
| `base.url` | `https://www.saucedemo.com` | UI base URL |
| `api.base.url` | `https://jsonplaceholder.typicode.com` | API base URL |
| `implicit.wait` | `10` | seconds |
| `explicit.wait` | `15` | seconds |
| `allure.results.directory` | `target/allure-results` | Allure output path |

## Test targets

- **UI:** https://www.saucedemo.com — login scenarios only (success + invalid credentials + locked-out)
- **API:** https://jsonplaceholder.typicode.com/users/2 — validates `name`, `username`, `email` for ID=2
