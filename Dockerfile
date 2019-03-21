FROM alpine:3.9

RUN apk add --update bash curl openjdk8 nss

WORKDIR /usr/local/bin
RUN curl -O https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
RUN chmod a+x lein
RUN lein

COPY . /app
WORKDIR /app
RUN lein uberjar

EXPOSE 5000

CMD ["java", "-jar", "target/uberjar/stackim-standalone.jar"]
