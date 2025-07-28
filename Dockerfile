#############################################################################
# INSTALLER

FROM alpine:3.22 AS builder

RUN apk update && apk add leiningen

COPY . /build
WORKDIR /build
RUN lein uberjar

ENTRYPOINT ["/bin/bash"]


#############################################################################
# RUNNER

FROM alpine:3.22 AS runner

RUN apk update && apk add java-jdk

WORKDIR /app
COPY --from=builder /build/target/uberjar/stackim-standalone.jar .

ENV PORT=8080 \
    DATABASE_URL="postgres://stackim:stackim@localhost/stackim"

ENTRYPOINT ["java"]
CMD ["-XX:UseSVE=0", "-jar", "stackim-standalone.jar"]
