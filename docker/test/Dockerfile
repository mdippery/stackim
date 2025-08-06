FROM alpine:3.22

RUN apk update && apk add leiningen

COPY . /build
WORKDIR /build

ENTRYPOINT ["lein", "test"]
