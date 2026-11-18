# ── Stage 1: dependency cache ────────────────────────────────────────────────
FROM eclipse-temurin:11-jdk-jammy AS deps
WORKDIR /app
COPY pom.xml .
RUN apt-get update && apt-get install -y maven --no-install-recommends \
    && rm -rf /var/lib/apt/lists/* \
    && mvn dependency:go-offline -q

# ── Stage 2: test runner ─────────────────────────────────────────────────────
FROM eclipse-temurin:11-jdk-jammy AS runner

# Install Maven
RUN apt-get update && apt-get install -y maven --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

# Install Google Chrome (stable)
RUN apt-get update && apt-get install -y wget gnupg2 apt-transport-https --no-install-recommends \
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub \
       | gpg --dearmor -o /usr/share/keyrings/google-chrome.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] \
       http://dl.google.com/linux/chrome/deb/ stable main" \
       > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update && apt-get install -y google-chrome-stable --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

# Install Firefox ESR
RUN apt-get update && apt-get install -y firefox-esr --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy cached Maven dependencies from stage 1
COPY --from=deps /root/.m2 /root/.m2

# Copy project sources
COPY pom.xml .
COPY src/ src/

# Create allure-history directory (populated at runtime via volume mount)
RUN mkdir -p allure-history

# Tests must run headless inside the container.
# --no-sandbox and --disable-dev-shm-usage are added automatically by
# DriverManager when headless=true (see config.properties or -Dheadless=true).
ENV headless=true

CMD ["mvn", "clean", "test", \
     "-Dheadless=true", \
     "-Dallure.results.directory=target/allure-results"]
