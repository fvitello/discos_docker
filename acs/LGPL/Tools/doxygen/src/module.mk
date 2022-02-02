PLATFORM := $(shell uname)
DOX_VER = 1.7.0
GRAPH_VER = 2.26.0
MODULES=graphviz-$(GRAPH_VER) doxygen-$(DOX_VER)
DOX_BINS:=doxygen doxytag
DOX_MANS:=man1/doxygen.1 man1/doxytag.1
GRAPH_BINS:=acyclic bcomps ccomps circo diffimg dijkstra dot dot2gxl dotty fdp gc gml2gv gv2gxl gvcolor gvgen gvpack gvpr gxl2dot gxl2gv lneato mm2gv neato nop osage prune sccmap sfdp tred twopi unflatten
GRAPH_LIBS:=graphviz/ libcdt.a libcdt.la libcgraph.a libcgraph.la libgraph.a libgraph.la libgvc.a libgvc.la libgvpr.a libgvpr.la libpathplan.a libpathplan.la libxdot.a libxdot.la
GRAPH_MISC:=share/graphviz/
GRAPH_MAN1:=$(addprefix man1/,acyclic.1 ccomps.1 dijkstra.1 dotty.1 gc.1 gv2gxl.1 gvgen.1 gvpr.1 lneato.1 neato.1 osage.1 sccmap.1 smyrna.1 twopi.1 bcomps.1 circo.1 dot.1 fdp.1 gml2gv.1 gvcolor.1 gvpack.1 gxl2gv.1 mm2gv.1 nop.1 prune.1 sfdp.1 tred.1 unflatten.1)
GRAPH_MAN3:=$(addprefix man3/,cdt.3 cgraph.3 gdtclft.3tcl graph.3 gvc.3 pathplan.3 tcldot.3tcl tkspline.3tk xdot.3)
GRAPH_MAN7:=$(addprefix man7/,graphviz.7)
GRAPH_MANS:=$(GRAPH_MAN1) $(GRAPH_MAN3) $(GRAPH_MAN7)

$(MODRULE)all: $(MODPATH) $(MODDEP) $(addprefix $(MODPATH)/bin/,$(GRAPH_BINS)) $(addprefix $(MODPATH)/lib/,$(GRAPH_LIBS)) $(addprefix $(MODPATH)/man/,$(GRAPH_MANS)) $(addprefix $(MODPATH)/bin/,$(DOX_BINS)) $(addprefix $(MODPATH)/man/,$(DOX_MANS))
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/bin/,$(DOX_BINS))),rm $(wildcard $(addprefix $(MODPATH)/bin/,$(DOX_BINS))),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/man/,$(DOX_MANS))),rm $(wildcard $(addprefix $(MODPATH)/man/,$(DOX_MANS))),)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . removing the doxygen/graphviz directories and log file . . ."
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/src/,doxygen-$(DOX_VER) graphviz-$(GRAPH_VER))),rm -r $(wildcard $(addprefix $(MODPATH)/src/,doxygen-$(DOX_VER) graphviz-$(GRAPH_VER))),)
	$(AT)$(if $(wildcard $(MODPATH)/src/config_all.log),rm $(MODPATH)/src/config_all.log,)
	$(AT)echo " . . . $@ done"

$(addprefix $(MODPATH)/bin/,$(GRAPH_BINS)) $(addprefix $(MODPATH)/lib/,$(GRAPH_LIBS)) $(addprefix $(MODPATH)/man/,$(GRAPH_MANS)): $(MODRULE)graph_compile

.INTERMEDIATE: $(MODRULE)graph_compile
$(MODRULE)graph_compile: $(MODPATH)/src/graphviz-$(GRAPH_VER)/Makefile | $(MODPATH)/man/man1 $(MODPATH)/man/man3 $(MODPATH)/man/man7
	$(AT)export CFLAGS="$(CFLAGS) -fPIC"; export CXXFLAGS="$(CXXFLAGS) -fPIC"; $(MAKE) $(MAKE_PARS) -C $(MODPATH)/src/graphviz-$(GRAPH_VER) all
	$(AT)$(MAKE) -C $(MODPATH)/src/graphviz-$(GRAPH_VER) install
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/share/man/,$(GRAPH_MAN1))),cp $(wildcard $(addprefix $(MODPATH)/share/man/,$(GRAPH_MAN1))) $(MODPATH)/man/man1/,)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/share/man/,$(GRAPH_MAN3))),cp $(wildcard $(addprefix $(MODPATH)/share/man/,$(GRAPH_MAN3))) $(MODPATH)/man/man3/,)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/share/man/,$(GRAPH_MAN7))),cp $(wildcard $(addprefix $(MODPATH)/share/man/,$(GRAPH_MAN7))) $(MODPATH)/man/man7/,)

$(addprefix $(MODPATH)/bin/,$(DOX_BINS)) $(addprefix $(MODPATH)/man/,$(DOX_MANS)): $(MODRULE)dox_compile

.INTERMEDIATE: $(MODRULE)dox_compile
$(MODRULE)dox_compile: $(MODPATH)/src/doxygen-$(DOX_VER)/Makefile
	$(AT)CFLAGS="$(CFLAGS) -fPIC" CXXFLAGS="$(CXXFLAGS) -fPIC" $(MAKE) $(MAKE_PARS) -C $(MODPATH)/src/doxygen-$(DOX_VER) all
	$(AT)$(MAKE) -C $(MODPATH)/src/doxygen-$(DOX_VER) install

$(MODPATH)/src/graphviz-$(GRAPH_VER)/Makefile: $(MODPATH)/src/graphviz-$(GRAPH_VER)/configure
	$(AT)cd $(MODPATH)/src/graphviz-$(GRAPH_VER); ./configure --prefix=$(MODPATH) --disable-swig --build=x86_64
	$(AT)touch $@
$(MODPATH)/src/doxygen-$(DOX_VER)/Makefile: $(MODPATH)/src/doxygen-$(DOX_VER)/qtools/qvaluestack.h $(MODPATH)/src/doxygen-$(DOX_VER)/src/util.cpp
	$(AT)cd $(MODPATH)/src/doxygen-$(DOX_VER)/; ./configure --prefix $(MODPATH) --platform linux-64
	$(AT)touch $@

$(MODRULE)db:
	@echo " . . . ../DB done"

# This target prepares the patch file
# after new patches have been applied/coded.
# It assumes that the new/patched files are in
# in doxygen-$(DOX_VER)
# and unpacks the unpatched code to make the diff
# tmp_unpack/doxygen-$(DOX_VER).orig
#
# Does not use doxygen as directory name but adds .orig
# to make clearer reading the patch file.
# Before preparing the patch also cleans up the code with the patches
# Makes a copy of the previous patch file for comparison
# and deleted the unpatched code afterwards.
# 
# I had to put a 'true' because patch returns -1. No idea why.
#
$(MODRULE)preparePatch:
	mv doxygen.patch doxygen.patch.old
	rm -rf tmp_unpack; mkdir -p tmp_unpack
	cd tmp_unpack; gtar -xzf ../doxygen-$(DOX_VER).src.tar.gz; mv doxygen-$(DOX_VER) doxygen-$(DOX_VER).orig
	cd doxygen-$(DOX_VER);
	LC_ALL=C TZ=UTC0 diff -Naur tmp_unpack/doxygen-$(DOX_VER).orig doxygen-$(DOX_VER) > doxygen.patch; true
	rm -rf tmp_unpack
	@echo " . . . patch file prepared"

#Unpack the tar files with the original distribution
$(MODPATH)/src/doxygen-$(DOX_VER): $(MODPATH)/src/doxygen-$(DOX_VER).src.tar.gz
	$(AT)gtar -C $(MODPATH)/src -xzf $<
	$(AT)chmod -fR +x $(MODPATH)/src/doxygen-$(DOX_VER)/addon/doxywizard
	$(AT)touch $@

$(MODPATH)/src/graphviz-$(GRAPH_VER): $(MODPATH)/src/graphviz-$(GRAPH_VER).tar.gz
	$(AT)gtar -C $(MODPATH)/src -xzf $<
	$(AT)touch $@

#Apply the patches
$(MODPATH)/src/graphviz-$(GRAPH_VER)/configure: $(MODPATH)/src/graphviz.patch | $(MODPATH)/src/graphviz-$(GRAPH_VER)
	$(AT)patch -d $| -p1 < $<

$(MODPATH)/src/doxygen-$(DOX_VER)/qtools/qvaluestack.h $(MODPATH)/src/doxygen-$(DOX_VER)/src/util.cpp: $(MODRULE)doxygen_patch

.INTERMEDIATE: $(MODRULE)doxygen_patch
$(MODRULE)doxygen_patch: $(MODPATH)/src/doxygen.patch | $(MODPATH)/src/doxygen-$(DOX_VER)
	$(AT)patch -d $| -p1 < $<
