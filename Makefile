all : docs layout.jar

docs : manual/manual.pdf out/demo.pdf MarkdownTutorial.pdf

manual/manual.pdf: manual/manual.tex
	cd manual; lualatex --interaction=nonstopmode manual.tex; cd ..

out/demo.pdf: demo/settings.properties layout.jar demo/000title.tex demo/010lipsum.tex
	rm -f out/*; java -jar layout.jar demo/

MarkdownTutorial.pdf : MarkdownTutorial.md
	lualatex --interaction=nonstopmode --jobname=MarkdownTutorial \
	\\input\\documentclass\{article\}\\usepackage\[\
	definitionLists,html,fencedCode,mark\]\{markdown\}\
	"\\usepackage{csquotes}\\MakeOuterQuote{\"}\\EnableQuotes"\
	\\begin\{document\}\
	\\markdownInput\{MarkdownTutorial.md\}\
	\\end\{document\}

SRC_DIR := java/layout/src/

OUT_DIR := java/layout/out/production/

JAVA_SRCS := $(shell find java/layout/src -name '*.java')

#CLS := $(JAVA_SRCS:$(SRC_DIR)/%.java=$(OUT_DIR)/%.class)

layout.jar : classes
	jar cef org.homelinux.rjlee.news.Layout layout.jar \
	-C java/layout/resources/ META-INF/MANIFEST.MF \
	-C ${OUT_DIR}/layout .

# Targets that do not produce output files
.PHONY: classes clean

classes : $(JAVA_SRCS)
	mkdir -p $(OUT_DIR)/layout; \
	javac -g:none -nowarn -d $(OUT_DIR)/layout $(JAVA_SRCS)

clean:
	rm -rf $(OUT_DIR)/* layout.jar \
	manual/manual.pdf out/demo.pdf MarkdownTutorial.pdf \
	*.log *.aux *.markdown.lua *~
