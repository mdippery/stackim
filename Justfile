set dotenv-load := true

docker_image := 'stack.im'
docker_opts  := '--rm -e DATABASE_URL -p 8080:8080'

docker_test_image := docker_image + '-test'
docker_test_opts  := '--rm -v $(pwd):/build'

# List all available tasks
[private]
default:
  @just -l

# Run the web application
run: build
  docker run {{docker_opts}} {{docker_image}}

# Open a shell into the application's Docker container
sh: build-test
  docker run {{docker_test_opts}} -it --entrypoint=/bin/sh {{docker_test_image}}

# Run the test suite
test: build-test
  docker run {{docker_test_opts}} {{docker_test_image}}

# Open up a Clojure REPL
repl: build-test
  docker run {{docker_test_opts}} -it {{docker_test_image}} repl

# Check Clojure code for formatting errors
lint:
  docker run {{docker_test_opts}} {{docker_test_image}} cljfmt check

# Format Clojure code
fmt: build-test
  docker run {{docker_test_opts}} {{docker_test_image}} cljfmt fix

# Run the web application and supporting infrastructure
run-compose:
  docker compose up --build

# Connect to the local Postgres database
psql:
  psql "postgres://stackim:stackim@localhost:${POSTGRES_PORT:-5432}/stackim"

# Build the web application's Docker image
build:
  docker build -t {{docker_image}} .

# Build the web application's test environment
build-test:
  docker build -f docker/test/Dockerfile -t {{docker_image}}-test .
