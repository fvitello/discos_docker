ifndef MAKE_VERBOSE
   a = @
endif


ObjectExplorerConfig.jar: DefaultAbean.data

all: ObjectExplorerConfig.jar
	jar cf ObjectExplorerConfig.jar DefaultAbean.data

install:
	cp -u *.jar $(TMPLIB)
	
clean:
	@ rm -f *.jar *.class *~
