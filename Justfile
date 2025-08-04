set dotenv-load := true

docker_image := 'stack.im'

run: build
  docker run --rm -e DATABASE_URL -p 8080:8080 {{docker_image}}

run-compose:
  docker compose up

build:
  docker build -t {{docker_image}} .
