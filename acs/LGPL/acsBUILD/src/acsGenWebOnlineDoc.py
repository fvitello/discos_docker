#! /usr/bin/env python
#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# Copyright (c) European Southern Observatory, 2014 
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#
#
# who       when      what
# --------  --------  ----------------------------------------------
# acaproni  2014-05-26  created
#

# Generate the WEB online documentation for a given tag/branch or the trunk

'''
This script generates the ACS documentation for a given release.
Several parameters/switches can or must be passed through the command line: see the help (-h)!

The web documentation for a release is generated in a folder named from the passed SVN URL
(i.e. the name of the banch/tag or trunk); however, it is possible to redirect the output 
a user defined folder. 
The structure of the folder is described in WebFolder: 
   - all temporary files go into a temp folder that can be removed when the generation terminates.
   - Web content for documents and APIs goes into specilised folders

This is a summary of the steps done by this script:
   1 prepare the output folder and its structure
   2 checkout ACS
   3 invoke doxygen for IDL, java and C++
'''


from optparse import OptionParser
import sys, os
from WebDocsGenerator.SvnUrlHelper import SvnUrlHelper
from WebDocsGenerator.WebFolder import WebFolder    
from WebDocsGenerator.APIDocsGenerator import ApiDocsGenerator
from WebDocsGenerator.DocsGenerator import DocsGenerator
    
if __name__=="__main__":
    # Parse the command line
    parser = OptionParser()
    parser.add_option("-u", "--url", dest="svnUrl",help="SVN URL to checkout ACS and build the documentation", default=None)
    parser.add_option("-o", "--outDir", dest="outFolder", help="Folder to write the documentation into (if not set uses the name of the branch from the SVN URL)", default=None)
    parser.add_option("--eraseOldFolder", dest="eraseOutFolder", help="Erase the output folder if it already exists and is not '.'", action="store_true", default=False)
    parser.add_option("-a", "--apiOnly", dest="apiOnly", help="If set generate only the documentation for the APIs", action="store_true", default=False)
    parser.add_option("-d", "--docsOnly", dest="docsOnly", help="If set generate only the documents (pdfs, words,...)", action="store_true", default=False)
    
    (options, args) = parser.parse_args()
    
    if  options.svnUrl is None:
        print "No SVN URL in command line: expected a URL like https://alma-svn.hq.eso.org/p2/branches/2014-02-ACS-B!"
        parser.print_help()
        sys.exit(1)
    svnUrlHelper=SvnUrlHelper(options.svnUrl)
    
    acsROOT=os.environ["ACSROOT"]
    if acsROOT is None or len(acsROOT)==0:
        print "Error: ACSROOT not defined... bailing out!"
    
        
    if options.outFolder is None:
        outFolder=svnUrlHelper.getTag()
    else:
        outFolder=options.outFolder
    print "\n\n\nGeneration of ACS web documentation started"
    print "Building ACS WEB documentation from",options.svnUrl
    print "Output folder is",outFolder
    print "ACSROOT",acsROOT
    print "ACS TAG",svnUrlHelper.getTag()
    print 
    
    if options.apiOnly and options.docsOnly:
        print "You can select only one between -a and -d!"
        parser.print_help()
        sys.exit(1)
    
    # Prepare the folder for the output
    print "Preparing the folders"
    webFolder=WebFolder(outFolder,options.eraseOutFolder)
    
    # Checkout ACS in the passed output folder under
    webFolder.chdirTemp()
    try:
        ret=svnUrlHelper.checkoutACS()
    except Exception as e:
        print e
        exc_type, exc_obj, exc_tb = sys.exc_info()
        fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
        print(exc_type, fname, exc_tb.tb_lineno)
        ret=False
    if not ret:
        print "Error checking out ACS: bailing out"
        sys.exit(-1)
        
    # Generate the API documentation
    #
    # The documentation is generated into the temporary folder
    # and moved into the APIs folde for pubblication 
    if not options.docsOnly:
        apiDocsGenerator=ApiDocsGenerator(webFolder,acsROOT,svnUrlHelper.getTag())
        ret=apiDocsGenerator.generateAllDocs()
        print
        print "Online (API) documentation generated",
        if ret>0:
            print "("+str(ret)+" errors detected)"
        else:
            print
        print
    
    #
    # Prepare the other documents
    #
    
    # Checkout ACS/Documents in the passed output folder under
    if not options.apiOnly:
        webFolder.chdirTemp()
        try:
            ret=svnUrlHelper.checkoutAcsDocuments()
        except Exception as e:
            print e
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print(exc_type, fname, exc_tb.tb_lineno)
            ret=False
        if not ret:
            print "Error checking out ACS: bailing out"
            sys.exit(-1)
            
        dc=DocsGenerator(webFolder, svnUrlHelper)
        dc.buildDocs()
#
# ___oOo___
