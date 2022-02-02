images:=$(wildcard centos*)
build: $(images)
.PHONY: $(images)
all: $(images)
$(images):
	@IMAGE_OS=$@ CONTAINER_DIR=$@ docker-compose build $@
$(addprefix up-,$(images)):
	@IMAGE_OS=$(subst up-,,$@) CONTAINER_DIR=$(subst up-,,$@) docker-compose up $(subst up-,,$@)

$(addprefix down-,$(images)):
	@IMAGE_OS=$(subst down-,,$@) CONTAINER_DIR=$(subst down-,,$@) docker-compose down $(subst down-,,$@)
