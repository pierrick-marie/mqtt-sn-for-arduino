BIN= ./bin
LIB= ./lib/jssc-2.8.0.jar
SRC= ./src/main/java
PKG= $(SRC)/gateway

JC= javac
FLAGS = -g -Xlint:unchecked

J= java

RM= rm -rf
MKDIR= mkdir -p

CLASSES= $(PKG)/Main.java 

.SUFFIXES: .java .class
.java.class:
	$(JC) -d $(BIN) -sourcepath $(SRC) -cp $(LIB) $(FLAGS) $*.java

default: ${BIN} classes

${BIN}:
	${MKDIR} ${BIN}

classes: $(CLASSES:.java=.class)

clean:
	$(RM) $(BIN)/*