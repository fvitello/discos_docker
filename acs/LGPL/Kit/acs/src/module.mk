#*******************************************************************************
# ALMA - Atacama Large Millimeter Array
# Copyright (c) Associated Universities Inc., 2020
# (in the framework of the ALMA collaboration).
# All rights reserved.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#*******************************************************************************

#*******************************************************************************
# This Makefile follows ACS Standards (see Makefile manpage documentation).
#
# REMARK: this should be the first module to be installed.
#         For such a reason, in few cases, it needs to work from /src.
#*******************************************************************************

-include $(MODPATH)/src/rules.mk

$(MODRULE)all: $(MODDEP) do_acsPort
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP) $(PRJTOP)/include/vltPort.h
	$(AT)echo " . . . $@ done"

$(PRJTOP)/include/vltPort.h: $(PRJTOP)/include/acsPort.h
	$(AT)rm -f $(PRJTOP)/include/vltPort.h
	$(AT)ln -s $(PRJTOP)/include/acsPort.h $(PRJTOP)/include/vltPort.h

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	-$(AT) cp $</include/acsPort.h.DUMMY $</include/acsPort.h
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)rm -f $(PRJTOP)/include/vltPort.h
	$(AT)echo " . . . $@ done"

man   : do_man
	@echo " . . . man page(s) done"

do_acsPort: $(MODPATH)
	-$(AT) chmod 666   $</include/acsPort.h; cp $</include/acsPort.h.UNSUPPORTED $</include/acsPort.h; chmod 666   $</include/acsPort.h
	-$(AT) if [ "`uname`" = "Linux" ]; then cp $</include/acsPort.h.Linux $</include/acsPort.h; chmod 666 $</include/acsPort.h; fi
	-$(AT) if [ "`uname`" = "$(CYGWIN_VER)" ]; then cp $</include/acsPort.h.Cygwin $</include/acsPort.h; chmod 666 $</include/acsPort.h; fi
