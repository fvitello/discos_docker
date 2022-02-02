ifndef MAKE_VERBOSE
   a = @
endif


orbproperties.jar: orb.properties

all: orbproperties.jar
	jar cf orbproperties.jar orb.properties

install:
	cp -u *.jar $(TMPLIB)

clean:
	@ rm -f orbproperties.jar *.class *~
