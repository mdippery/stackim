DB_PASSWORD   ?= password
DB_PORT       ?= 5423
IMAGE_NAME     = mipadi/stackim
IMAGE_VERSION ?= $(shell head -n 1 project.clj | cut -d ' ' -f 3 | cut -d - -f 1 | tr -d '"')
NETWORK_NAME  ?= stackim

build:
	docker build -t ${IMAGE_NAME} .

tag: build
	docker tag ${IMAGE_NAME} ${IMAGE_NAME}:${IMAGE_VERSION}
	@docker image ls

db:
	docker run -d --name postgres --net ${NETWORK_NAME} -p ${DB_PORT}:${DB_PORT} -e POSTGRES_PASSWORD=${DB_PASSWORD} postgres

network:
	docker network create ${NETWORK_NAME}

run:
	docker run -d --name stackim --net ${NETWORK_NAME} -p 5000:5000 ${IMAGE_NAME}

clean:
	yes | docker container prune
	yes | docker image prune
