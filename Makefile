DATABASE_URL  ?= postgres://localhost:5432/
IMAGE_NAME     = mipadi/stackim
IMAGE_VERSION ?= $(shell head -n 1 project.clj | cut -d ' ' -f 3 | cut -d - -f 1 | tr -d '"')

build:
	docker build -t ${IMAGE_NAME} .

tag:
	docker build -t ${IMAGE_NAME} -t ${IMAGE_NAME}:${IMAGE_VERSION} .
	@docker image ls

run:
	docker run -d --name stackim -p 5000:5000 --env DATABASE_URL=${DATABASE_URL} ${IMAGE_NAME}

clean:
	yes | docker container prune
	yes | docker image prune
	@docker image ls
