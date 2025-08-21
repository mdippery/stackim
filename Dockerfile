#############################################################################
# BUILDER

FROM alpine:3.22 AS builder

RUN apk update && apk add leiningen

COPY . /build
WORKDIR /build
RUN lein uberjar


#############################################################################
# RUNNER

FROM alpine:3.22 AS runner

RUN apk update && apk add openjdk21-jre-headless

WORKDIR /app
COPY --from=builder /build/target/uberjar/stackim-standalone.jar .

ENV PORT=8080 \
    CANONICAL_HOST=localhost \
    DATABASE_URL="postgres://stackim:stackim@localhost/stackim" \
    JAVA_OPTS='-XX:UseSVE=0'
ENV CANONICAL_PORT=${PORT}

EXPOSE ${PORT}

ENTRYPOINT ["java"]
CMD ["-jar", "stackim-standalone.jar"]
