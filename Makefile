IMAGE_NAME     = mipadi/stackim
IMAGE_VERSION ?= $(shell head -n 1 project.clj | cut -d ' ' -f 3 | cut -d - -f 1 | tr -d '"')

build:
	docker build -t ${IMAGE_NAME} .

tag: build
	docker tag ${IMAGE_NAME} ${IMAGE_NAME}:${IMAGE_VERSION}
	@docker image ls

clean:
	yes | docker container prune
	yes | docker image prune
