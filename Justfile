set dotenv-load := true

docker_image := 'stack.im'
docker_opts  := '--rm -e DATABASE_URL -p 8080:8080'

run: build
  docker run {{docker_opts}} {{docker_image}}

sh: build
  docker run {{docker_opts}} -it --entrypoint=/bin/sh {{docker_image}}

run-compose:
  docker compose up

build:
  docker build -t {{docker_image}} .
