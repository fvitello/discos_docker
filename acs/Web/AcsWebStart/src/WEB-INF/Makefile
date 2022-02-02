ifndef MAKE_VERBOSE
   a = @
endif

# msc (apr2006): must compile for java 1.4 since we use an old tomcat
JAVAC_OPTS=-target 1.4 -source 1.4 -classpath $(CLASSPATH):./servlet.jar:.

ReqParamsFilter.class: ReqParamsFilter.java
	@ echo Compiling...
	$(a) javac ReqParamsFilter.java $(JAVAC_OPTS)

StringConstantsFilter.class: StringConstantsFilter.java
	@ echo Compiling...
	$(a) javac StringConstantsFilter.java $(JAVAC_OPTS)

GetFileServlet.class: GetFileServlet.java
	@ echo Compiling...
	$(a) javac GetFileServlet.java $(JAVAC_OPTS)

ResponseWrapper.class: ResponseWrapper.java
	@ echo Compiling...
	$(a) javac ResponseWrapper.java $(JAVAC_OPTS)

LogHelper.class: LogHelper.java
	@ echo Compiling...
	$(a) javac LogHelper.java $(JAVAC_OPTS)

all: GetFileServlet.class ReqParamsFilter.class StringConstantsFilter.class ResponseWrapper.class LogHelper.class

install: 
	@ mkdir -p $(WEBDIR)/WEB-INF/classes; mkdir -p $(WEBDIR)/WEB-INF/lib
	cp -u StringConstants.properties $(WEBDIR)
	cp -u web.xml $(WEBDIR)/WEB-INF
	cp -u *.class $(WEBDIR)/WEB-INF/classes
	@ # msc (apr2006): use JDK JnlpDownloadServlet
	cp -u $(JAVA_HOME)/sample/jnlp/servlet/*.jar $(WEBDIR)/WEB-INF/lib
clean:
	@ rm -f *.class *~
