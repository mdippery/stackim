FROM alpine:3.22

RUN apk add leiningen

COPY . /build
WORKDIR /build

ENV LEIN_JVM_OPTS='-XX:UseSVE=0'
ENV JAVA_OPTS=$LEIN_JVM_OPTS

CMD ["test"]
ENTRYPOINT ["lein"]
