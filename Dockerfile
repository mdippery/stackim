#############################################################################
# BUILDER

FROM alpine:3.22 AS builder

RUN apk add leiningen

COPY . /build
WORKDIR /build
RUN lein uberjar


#############################################################################
# RUNNER

FROM alpine:3.22 AS runner

RUN apk add openjdk21-jre-headless

WORKDIR /app
COPY --from=builder /build/target/uberjar/stackim-standalone.jar .

ENV PORT=8080 \
    DATABASE_URL="postgres://stackim:stackim@localhost/stackim"

EXPOSE ${PORT}

ENTRYPOINT ["java"]
CMD ["-jar", "stackim-standalone.jar"]
