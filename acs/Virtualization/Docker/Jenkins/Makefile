define distro
$1: acs-$1 alma-$1
clean_$1: clean_acs_$1 clean_alma_$1
clean_dist_$1: clean_dist_acs_$1 clean_dist_alma_$1
acs-$1: .acs-$1
.acs-$1: acs-$1/Dockerfile
	docker build -t acs/$1 acs-$1
	touch $$@
clean_acs_$1:
	$(if $(wildcard .acs-$1),rm .acs-$1,)
clean_dist_acs_$1: clean_acs_$1 clean_dist_alma_$1
	docker image rm acs/$1
alma-$1: acs-$1 .alma-$1
.alma-$1: .acs-$1 alma-$1/Dockerfile
	docker build -t alma/$1 alma-$1
	touch $$@
clean_alma_$1:
	$(if $(wildcard .alma-$1),rm .alma-$1,)
clean_dist_alma_$1: clean_alma_$1
	docker image rm alma/$1
endef

images:=$(wildcard acs-*)
distros:=$(subst acs-,,$(images))
$(foreach d,$(distros),$(eval $(call distro,$d)))

all: $(distros)
clean: $(addprefix clean_,$(distros))
clean_dist: $(addprefix clean_dist_,$(distros))
