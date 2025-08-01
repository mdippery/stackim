set dotenv-load := true

run:
  docker compose up

build:
  docker build -t stack.im .
