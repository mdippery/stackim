set dotenv-load := true

docker_image := 'stack.im'
docker_opts  := '--rm -e DATABASE_URL -p 8080:8080'

# Run the web application
run: build
  docker run {{docker_opts}} {{docker_image}}

# Open a shell into the application's Docker container
sh: build
  docker run {{docker_opts}} -it --entrypoint=/bin/sh {{docker_image}}

# Run the test suite
test: build-test
  docker run {{docker_image}}-test

# Run the web application and supporting infrastructure
run-compose:
  docker compose up

# Build the web application's Docker image
build:
  docker build -t {{docker_image}} .

# Build the web application's test environment
build-test:
  docker build -f docker/test/Dockerfile -t {{docker_image}}-test .
