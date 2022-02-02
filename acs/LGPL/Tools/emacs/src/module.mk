OPTIMIZE = On
USER_CFLAGS = -O -s -DETAGS
YACC   = yacc
YFLAGS = -v
LEX    = lex
LDLIBS = -ll

etags_OBJECTS   = etags parser
etags_LIBS      = l
etags_CFLAGS   = -O -s -DETAGS
parser_CFLAGS  = -O -s -DETAGS
lexer_CFLAGS   = -O -s -DETAGS

W3=w3-4.0pre.47

JDE=jde-2.1.5
EMACS_JDE = \
		beanshell \
		imenu \
		speedbar \
		jde-compile \
		jde-db \
		jde-gen \
		jde-make \
		jde-run \
		jde-wiz \
		jde \
		rpm \
		sb-gud \
		sb-info \
		sb-w3

EMACS_JDE_CMP =

DOXYMACS = doxymacs-1.3.0/lisp
EMACS_DOXYMACS =   doxymacs
EMACS_DOXYMACS_CMP = xml-parse

EMACS_LISP_CMP = \
             hscroll \
             c++-browse\
             vlt-cmm \
             comment \
             vlt-menu \
             vlt-saveconf \
             vlt-tags \
             hilit-change-log   \
             hilit-tcl	\
             add-log \
	     browse-url \
	     html-helper-mode \
	     tempo \
             occam-mode \
             toolset-compile \
             python-mode \
             tempo \
	     $(EMACS_JDE_CMP) \
	     $(EMACS_DOXYMACS_CMP)

EMACS_LISP = $(EMACS_JDE) $(EMACS_DOXYMACS)

$(MODRULE)all: $(MODPATH) $(MODDEP) parser.c do_links do_all do_w3 do_emacs
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	- $(AT) if [ ! -d $(LIB)/emacs ]     ; then mkdir $(LIB)/emacs     ; fi
	- $(AT) if [ ! -d $(LIB)/emacs/lisp ]; then mkdir $(LIB)/emacs/lisp; fi
	- $(AT) if [ ! -d $(LIB)/emacs/Info ]; then mkdir $(LIB)/emacs/Info; fi
	- $(AT) cp ../bin/*.elc ../bin/*.el $(LIB)/emacs/lisp; chmod u+w $(LIB)/emacs/lisp/*
	- $(AT) cp ../info/[a-z]* $(LIB)/emacs/Info; chmod u+w $(LIB)/emacs/Info/*
	- $(AT) if [ ! -d $(PRJTOP)/System ]; then mkdir $(PRJTOP)/System; fi
	- $(AT) echo "\n\n"
	- $(AT) echo "Delete $(LIB)/emacs/Info if you do not need it!"
	- $(AT) echo "Delete $(PRJTOP)/src/emacs if you do not need it!"
	- $(AT) echo "\nTODO:"
	- $(AT) echo "Add two links in order to use the new emacs setup:"
	- $(AT) echo "ln -s $(PRJTOP)/System/Emacs ~/.emacs"
	- $(AT) echo "ln -s $(PRJTOP)/System/Emacs.local ~/.emacs.local"
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP) clean_links
	$(RM) ../*~ *~ \#* *.aux *.cp *.dvi *.fn *.ky *.log *.pg *.toc *.tp *.vr *.o TAGS
	$(RM) parser.c lexer.c y.output
	$(RM) ../bin/*.el ../bin/*.elc *.elc
	$(RM) ../object/parser.o ../object/etags.o
	$(AT) if [ -f $(W3)/Makefile ] ; then cd $(W3); make distclean; fi
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"

../bin/%.elc : %.el
	- @echo "== emacs compile : $(<F)"
	$(AT) emacs -batch -l xml-parse.el -f batch-byte-compile  $(<F); mv $(*F).elc ../bin

../bin/%.el : %.el
	- @echo "== emacs lisp : $(<F)"
	$(AT) cp $(*F).el ../bin

do_emacs: $(foreach name, $(EMACS_LISP_CMP), ../bin/$(name).elc) \
           $(foreach name, $(EMACS_LISP), ../bin/$(name).el)

do_w3:
	(cd $(W3); chmod u+x ./configure; ./configure --with-emacs --prefix=$(LIB)/emacs; make $(MAKE_PARS)); cp $(W3)/lisp/*.elc $(W3)/lisp/*.el ../bin

COMMON_SOURCES = \
	$(foreach exe, $(EMACS_JDE_CMP) $(EMACS_JDE), $(exe).el)
COMMON_SOURCES_ORIG = \
	$(foreach exe, $(EMACS_JDE_CMP) $(EMACS_JDE), $(JDE)/$(exe).el)

$(COMMON_SOURCES) : $(COMMON_SOURCES_ORIG)
	- @echo "== Linking source: $@"
	- $(AT) $(RM) $@ ; ln -s $(JDE)/$@ $@

COMMON_SOURCES2 = \
	$(foreach exe, $(EMACS_DOXYMACS_CMP) $(EMACS_DOXYMACS), $(exe).el)
COMMON_SOURCES_ORIG2 = \
	$(foreach exe, $(EMACS_DOXYMACS_CMP) $(EMACS_DOXYMACS), $(DOXYMACS)/$(exe).el)

$(COMMON_SOURCES2) : $(COMMON_SOURCES_ORIG2)
	- @echo "== Linking source: $@"
	- $(AT) $(RM) $@ ; ln -s $(DOXYMACS)/$@ $@

do_links: $(COMMON_SOURCES) $(COMMON_SOURCES2)

clean_links:
	$(AT) $(RM) $(COMMON_SOURCES) $(COMMON_SOURCES2)

# Targets to build etags tool
parser.c : parser.y lexer.c
	$(YACC) $(YFLAGS) parser.y
	mv -f y.tab.c $@

lexer.c : lexer.l
	$(LEX) $(LFLAGS) -t lexer.l >$@
