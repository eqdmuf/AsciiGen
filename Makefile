SRC=src
BUILD=build
JC=javac
JFLAGS=-d $(BUILD) -cp $(BUILD)
JARFILE=Ascii.jar
JARFLAGS=cfe
MANIFEST=manifest.txt

CLASSES=Ascii.class main.class main$$ImgPreview.class

default: jar

MAIN: Ascii.class main.class

%.class: $(SRC)/%.java
	${JC} ${JFLAGS} $<
jar: MAIN
	cd $(BUILD);\
	jar $(JARFLAGS) $(JARFILE) main *.class;\
	cd -;\
	mv $(BUILD)/$(JARFILE) .
	
clean: 
	rm -rf $(BUILD)/*.class $(JARFILE)

