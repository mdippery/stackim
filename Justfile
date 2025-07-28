run: build
  docker run --rm -p 8080:8080 stack.im

build:
  docker build -t stack.im .
