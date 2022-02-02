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
# acaproni  2014-05-27  created
#
from os import path, mkdir, chdir, getcwd
import os
from shutil import rmtree

class WebFolder(object):
    '''
    WebFolder manipulates the folder where the web content for this release
    will be created.
    
    The folder has the following structure
    
    WebContent
       Docs
       APIs
       temp
       
    Were:
      - WbeContent is the name passed by the user in the constructor 
        (generally the name of the branch)
      - APIs is the folder for the API documentation like that created by doxygen
      - temp is a folder to store temporary files 
    '''
    
    def __init__(self,webFolderName,erasePrevious=False):
        '''
        Constructor
        
        @param webFolderName: The name of the web folder
        @param erasePrevious: if True erase a folder with this same name if it already exists
        '''
        if webFolderName is None or len(webFolderName)==0:
            raise Exception("Invalid Name for WEB folder")
        
        if webFolderName.endswith(os.sep):
            self.RootFolderName=webFolderName[:-1]
        else:
            self.RootFolderName=webFolderName
            
        self.initFolder(self.RootFolderName,erasePrevious)
        
        print "Folder structure created:"
        print "\t",self.RootFolderName,"(Main folder;",self.absRootFolderName,")"
        print "\t\t",self.tempFolder,"(temporary files;",self.absTempFolder,")"
        print "\t\t",self.docsFolder,"(documents;",self.absDocsFolder,")"
        print "\t\t",self.APIsFolder,"(APIs;",self.absAPIsFolder,")"
        
    def initFolder(self,outFolder,erasePrevious):
        '''
        Prepare the folder to checkout ACS and build the documentation
        
        @param outFolder: The name of the root folder
        @param erasePrevious: If True erase the old folder if it already exists
        '''
        if outFolder=='.':
            # Nothing to do for the current folder
            return
        
        if path.exists(outFolder) and erasePrevious:
            print outFolder,"already exist: removing!"
            rmtree(outFolder)
        
        # creates the main WEB root
        if not path.exists(outFolder):
            mkdir(outFolder)
            
        # Creates the temporary folder
        self.tempFolder=outFolder+os.sep+"temp"
        if not path.exists(self.tempFolder):
            mkdir(self.tempFolder)
            
        # Creates the Docs folder
        self.docsFolder=outFolder+os.sep+"Docs"
        if not path.exists(self.docsFolder):
            mkdir(self.docsFolder)
            
        # Creates the APIs folder
        self.APIsFolder=outFolder+os.sep+"APIs"
        if not path.exists(self.APIsFolder):
            mkdir(self.APIsFolder)
            
        self.absRootFolderName=path.abspath(self.RootFolderName)
        self.absTempFolder=path.abspath(self.tempFolder)
        self.absDocsFolder=path.abspath(self.docsFolder)
        self.absAPIsFolder=path.abspath(self.APIsFolder)

    def getTempFolder(self):
        ''' 
        @return: The temporary folder name
        '''
        return self.absTempFolder
    
    def getAPIsFolder(self):
        ''' 
        @return: The APIs folder name
        '''
        return self.absAPIsFolder
    
    def getDocsFolder(self):
        ''' 
        @return: The Docs folder name
        '''
        return self.absDocsFolder
    
    def getWebFolder(self):
        ''' 
        @return: The root WEB folder name
        '''
        return self.absRootFolderName
    
    def chdirTemp(self):
        '''
        Change the current dir to the temporary folder
        '''
        chdir(self.absTempFolder)
        
    def chdirAPIs(self):
        '''
        Change the current dir to the APIs folder
        '''
        chdir(self.absAPIsFolder)
        
    def chdirDocs(self):
        '''
        Change the current dir to the Docs folder
        '''
        chdir(self.absDocsFolder)
        
    def chdirWeb(self):
        '''
        Change the current dir to root root WEB folder
        '''
        chdir(self.absRootFolderName)