# ── Stage 1: dependency cache ────────────────────────────────────────────────
# focal = Ubuntu 20.04: Firefox is a real apt binary here, not a snap wrapper.
# jammy (22.04) moved Firefox to snap which cannot run inside Docker containers.
FROM eclipse-temurin:11-jdk-focal AS deps
WORKDIR /app
COPY pom.xml .
RUN apt-get update && apt-get install -y maven --no-install-recommends \
    && rm -rf /var/lib/apt/lists/* \
    && mvn dependency:go-offline -q

# ── Stage 2: test runner ─────────────────────────────────────────────────────
FROM eclipse-temurin:11-jdk-focal AS runner

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

# Install Firefox (native apt binary on Ubuntu 20.04)
RUN apt-get update && apt-get install -y firefox --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2
COPY pom.xml .
COPY src/ src/

RUN mkdir -p allure-history

ENV headless=true

CMD ["mvn", "clean", "test", \
     "-Dheadless=true", \
     "-Dallure.results.directory=target/allure-results"]
