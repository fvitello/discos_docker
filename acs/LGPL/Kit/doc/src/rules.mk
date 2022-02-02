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
# This Makefile follows ACS Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#------------------------------------------------------------------------

#
# C programs (public and local)
# -----------------------------
EXECUTABLES:=docDoManPages docA2MIF docExDb docExPc \
             docDoDbTree docSplitIntoSingleComments docSetCommentNumber

#
# Man Pages extractor 
docDoManPages_OBJECTS:=docDoManPages
docDoManPages_EXTENSION:=c

#
# Filter to convert ASCII into MIF
docA2MIF_OBJECTS:=docA2MIF
docA2MIF_EXTENSION:=c

#
# Filter to extract db info
docExDb_OBJECTS:=docExDb
docExDb_EXTENSION:=c

#
# Filter to extract pseudo code
docExPc_OBJECTS:=docExPc
docExPc_EXTENSION:=c

#
# Display (DB) directory structure
docDoDbTree_OBJECTS:=docDoDbTree
docDoDbTree_EXTENSION:=c

#
# doc review procedure tools
docSplitIntoSingleComments_OBJECTS:=docSplitIntoSingleComments
docSplitIntoSingleComments_EXTENSION:=c

docSetCommentNumber_OBJECTS:=docSetCommentNumber
docSetCommentNumber_EXTENSION:=c

# Scripts (public and local)
# ----------------------------
SCRIPTS:=docFMupdate docDos2Unix docExDbDir docDoDbInfo \
         docArchive doc docSelectDocument docSelectIssue \
         docSelectPreparation docSelectOption docMakeReport \
         docMergeComments docMoveOldVersions docCopyMif\
         docImported4to5.ex docImporting4to5.ex docModManBuild\
	 docDeroff docPrepareComments

#
# TCL scripts (public and local)
# ------------------------------
TCL_SCRIPTS:=docPcode docManExtract docUnix2Dos
TCL_SCRIPTS_L:=

#
# Gianluca's format Pseudo code extractor
docPcode_OBJECTS:=docPcode
docPcode_TCLSH:=tcl -f

docManExtract_OBJECTS:=docManExtract
docManExtract_TCLSH:=seqSh

docUnix2Dos_OBJECTS:=docUnix2Dos
docUnix2Dos_TCLSH:=seqSh



#
#
# man pages to be done
# --------------------
MANSECTIONS:=7
MAN7:=docDoManPages.c docA2MIF.c docFMupdate docExDb.c docExDbDir \
      docExPc.c docDoDbTree.c docDoDbInfo docUnix2Dos.tcl docDos2Unix \
      doc docArchive docMergeComments docSetCommentNumber.c \
      docMoveOldVersions docCopyMif docManExtract docModManBuild docDeroff
