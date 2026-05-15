# syntax=docker/dockerfile:1.7
#
# Reusable Dockerfile for 4 of the 5 Ktor microservices: ms-discord, ms-email,
# ms-cloudinary, ms-telegram. (ms-playwright uses Dockerfile.playwright because
# it needs the Playwright browser binaries baked in.)
#
# Jenkins runs `./gradlew installDist` for the whole monorepo BEFORE this build,
# so the build context already contains a runnable distribution at
# <service>/build/install/<service>/{bin,lib}/. This Dockerfile just packages it.
#
# Build with:
#   docker build --build-arg SERVICE=ms-discord -t localhost:5000/parallax-ms-discord:latest .

FROM eclipse-temurin:21-jre-alpine

ARG SERVICE
ENV SERVICE_NAME=${SERVICE}

WORKDIR /app

# Copy the pre-built install distribution.
COPY ${SERVICE}/build/install/${SERVICE} /app

# Stable launcher path regardless of service name.
RUN ln -s /app/bin/${SERVICE} /app/bin/run

# The actual port is whatever application.conf declares (8082..8087).
# We don't EXPOSE it here because it differs per service; compose handles publishing.

ENTRYPOINT ["/app/bin/run"]
