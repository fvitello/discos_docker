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
from os import chdir, mkdir, path, listdir,rename,system, environ, remove, path
from JavadocBuilder import AcsJavadocBuilder
from subprocess import call
from ACSErrorChecker import *
from shutil import copy, copytree
from re import match

class ApiDocsGenerator(object):
    '''
    The class to generate the API docs i.e. 
       - doxygen for IDL and C++
       - javadoc for java 
       - xsddoc for XML schemas
       - pydoc for python
       - ACSErrorChecker for error definitions
       
    Documenatation is actually generated into the temporary folder where
    ACS has been checked out? <temp>/ACS/LGPL/man
    
    When the generation completed, the HTMLs must be moved into the web folder
    '''
    
    
    def __init__(self, webFolder,acsRoot, acsTag):
        '''
        Constructor
        
        @param webFolder: the web folder
        @param acsRoot: ACSROOT environment variable
        @param acsTag: the tag to build API docs
        '''
        assert webFolder is not None
        self.webFolder=webFolder
        self.acsRoot=acsRoot
        self.acsTag=acsTag

    def genDoxygenDocs(self):
        '''
        Generate the online documentation with doxygen and javadoc
        by invoking the makefile of  acsBUILD
        
        @return False in case of error, True otherwise
        '''
        self.webFolder.chdirTemp()
        chdir("ACS/LGPL")
        # iterate over configuration files and languages
        doxygenConfigs= [ 
                        ("acsBUILD/config/Doxyfile.idl","IDL"),
                        ("acsBUILD/config/Doxyfile.cpp","Cpp")]
        
        # Call doxygen for all the languages
        errorsDetected=True
        for language in doxygenConfigs:
            outFile=self.webFolder.getTempFolder()+"/doxygen"+language[1]+".out"
            with open(outFile, 'w') as f:
                print "Generating ",language[1],"documentation with doxygen; output in",outFile
                cmd=[]
                cmd.append("doxygen")
                cmd.append(language[0])
                ret=call(cmd,stdout=f,stderr=f)
                if ret!=0:
                    print "!!!!! Error calling doxygen for IDL, check output file for details:",outFile
                    errorsDetected=False
        
        return errorsDetected
    
    def genIndex(self,title):
        '''
        Generate index.html for the HTML files present in the folder
        
        @param title The tile to show on the index
        '''
        #list of all the HTML in the main directory
        fileList = listdir(".")
        #list of main packages and scripts only
        goodList = []
        
        #populate the good list with main packages and scripts only!
        for file in fileList:
            if file.count('.')==1:
                goodList.append(file)
        
        #organize it
        goodList.sort()
        goodList.reverse()
        listLength = len(goodList)
        elementNum = listLength/4 + 1
        
        with open("index.html","w") as outF:
            header='''<!doctype html PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
                <html><head><title>ALMA Common Software</title>
                </head><body bgcolor="#f0f0f8">

                <table width="100%" cellspacing=0 cellpadding=2 border=0 summary="heading">
                <tr bgcolor="#7799ee">
                <td valign=bottom>&nbsp;<br>
                <font color="#ffffff" face="helvetica, arial">&nbsp;<br><big><big><strong>'''
            header=header+title+'''</strong></big></big></font></td>
                <td align=right valign=bottom><font color="#ffffff" face="helvetica, arial">&nbsp;</font></td></tr></table>

                <p><p>
                <table width="100%" cellspacing=0 cellpadding=2 border=0 summary="section">
                <tr bgcolor="#ee77aa">
                <td colspan=3 valign=bottom>&nbsp;<br>
                <font color="#ffffff" face="helvetica, arial"><big><strong>Packages, Modules, and Scripts</strong></big></font></td></tr>
    
                <tr><td bgcolor="#ee77aa"><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</tt></td><td>&nbsp;</td>
                <td width="100%"><table width="100%" summary="list"><tr>'''
            outF.write(header)
            
            #there will be four seperate columns
            for i in range(0, 4):
                outF.write('  <td width="25%" valign=top>')
            
                #each column is limited to elementNum elements
                for j in range(0, elementNum):
                    if len(goodList)==0:
                        break
            
                    curFile = goodList.pop()
            
                    outF.write('    <a href="'+curFile+'">'+curFile.split(".")[0]+'</a><br>')
            
                outF.write('  </td>')
            
            
            outF.write('</tr></table></td></tr></table> <p></body></html>')
    
    def genJavadocs(self):
        '''
        Generate javadocs
        @param outF: The file to save the output generated by javadoc 
        @return False in case of error, True otherwise
        '''
        dstFolder = self.webFolder.getAPIsFolder()+path.sep+'javadoc'
        if not path.exists(dstFolder):
            mkdir(dstFolder)
        
        # Sources are in the temporary folder    
        srcFolder = self.webFolder.getTempFolder()
        
        outFile=self.webFolder.getTempFolder()+"/javadoc.out"
        ret = True
        with open(outFile, 'w') as f:
            print "Generating java documentation with javadoc; output in ",outFile
            javadocsBuilder = AcsJavadocBuilder(srcFolder,dstFolder,False,f)
            ret=javadocsBuilder.buildJavadocs()
            if ret!=0:
                print "!!!!! Error calling javadoc, check output file for details:",outFile
                print "javadocs can be valid in spite of errors that could also come from comments in java sources!"
                ret = False
        return ret
        
    
    def genPyDocs(self):
        '''
        Generate python documentation
        
        @return False in case of error, True otherwise
        '''
        outFile=self.webFolder.getTempFolder()+"/pydoc.out"
        self.webFolder.chdirTemp()
        chdir("ACS/LGPL")
        if not path.exists("man"):
            mkdir("man")
        chdir("man")
        if not path.exists("py"):
            mkdir("py")
        chdir("py")
        # Run pydoc.
        # pydoc generates a set of HTML files but no index so we have to...
        with open(outFile, 'w') as f:
            print "Generating python documentation with pydoc; output in",outFile
            cmd=[]
            cmd.append("pydoc")
            cmd.append("-w")
            cmd.append(self.acsRoot+"/lib/python/site-packages/")
            ret=call(cmd,stdout=f,stderr=f)
        
        if ret!=0:
            print "!!!!! Error calling pydoc, check output file for details:",outFile
            
        # Generate the HTML with the list of files generated by pydoc
        print "Generating index.html for pydoc html files"
        self.genIndex("ACS "+self.acsTag+" python documentation")
        return ret!=0
    
    def genErrorsDocumentation(self):
        '''
        Generate the ACS errors definition documentation
        
        @return True in case of error; False otherwise
        '''
        self.webFolder.chdirTemp()
        dirToScan=self.webFolder.getTempFolder()+"/ACS/"
        
        # The error generator wants a configuration file
        xmlConfig = '<?xml version="1.0" encoding="UTF-8"?>\n'
        xmlConfig = xmlConfig+'<ErrorChecker>\n'
        xmlConfig = xmlConfig+'\t<ReportDir path="."/>\n'
        xmlConfig = xmlConfig+'\t<SearchDir DirName="idl"/>\n'
        xmlConfig = xmlConfig+'\t<ExcludeDir DirName="test"/>\n'
        xmlConfig = xmlConfig+'\t<ExampleAndTestRange Min="900000" Max="909999" />\n'
        xmlConfig = xmlConfig+'\t<SubSystem Name="ACS" ReportFormat="html" Min="0" Max="9999" BaseDir="../../../../ACS/"/>\n'
        xmlConfig = xmlConfig+'\t</ErrorChecker>\n'
        #The name of the configuration file 
        configFileName = path.abspath("./ErrorChecker.xml")
        # Write the config file
        outF = file(configFileName,"w+")
        outF.write(xmlConfig)
        outF.flush()
        outF.close()
        
        print "Generating errors documentation with config file",configFileName
        
        chdir("ACS/LGPL")
        if not path.exists("man"):
            mkdir("man")
        chdir("man")
        if not path.exists("errors"):
            mkdir("errors")
        chdir("errors")
        
        ret=ErrorChecker.checkWithConfigFile(configFileName)
        rename("ACS.html","index.html")
        
        return ret!=0
    
    def convertManPagesToHTML(self):
        '''
        Convert man pages in html with groff
        
        @return True in case of error; False otherwise
        '''
        print "Generating documentation for man pages"
        self.webFolder.chdirTemp()
        chdir("ACS/LGPL/man/man3")
        filesToConvert=listdir(".")
        ret=False
        for file in filesToConvert:
            if file.endswith(".3"):
                outFile=file[:-2]+".html"
            else:
                continue
            if system("groff -mandoc -Thtml <"+file+" >"+outFile)!=0:
                ret=True
        
        # Remove the man pages as we do not want them in the web server
        for file in filesToConvert:
            remove(file)
            
        # Generate the index
        self.genIndex("ACS "+self.acsTag+" man pages")
                
    
    def genSchemasDoc(self):
        '''
        Generate the documentation for schema files
        
        @return True in case of error; False otherwise
        '''
        # The build of the documentation is done in a temporary folder
        self.webFolder.chdirTemp()
        if not path.exists("schemas"):
            mkdir("schemas")
            
        # Build the directory structure for the documentation
        # The documentation generated by xssdoc will go in this folder
        
        self.webFolder.chdirTemp()
        chdir("ACS/LGPL")
        if not path.exists("man"):
            mkdir("man")
        chdir("man")
        if not path.exists("schemas"):
            mkdir("schemas")
        xssdDocDestination=self.webFolder.getTempFolder()+'/ACS/LGPL/man/schemas'
        
        # Copy schemas into schemas folder
        # Copy xsd and dtd from $ACSROOT/config/CDB/schemas into schemas
        self.webFolder.chdirTemp()
        dir=self.acsRoot+"/config/CDB/schemas/"
        filesToCopy=listdir(dir)
        for file in filesToCopy:
            temp=file.upper()
            if temp.endswith(".XSD") or  temp.endswith(".DTD"):
                copy(dir+file,"schemas")
        # Copy xsd and dtd from $ACSROOT/idl into schemas       
        dir=self.acsRoot+"/idl/"
        filesToCopy=listdir(dir)
        for file in filesToCopy:
            temp=file.upper()
            if temp.endswith(".XSD"):
                copy(dir+file,"schemas")
        
        # Copy the config file for ant
        # We have to set the output folder so we copy the file
        # by reading th eold one (that works with ACS build i.e. Makefile)
        # and replace the output tag witho our preference
        self.webFolder.chdirTemp()
        with open("ACS/LGPL/acsBUILD/config/xsddocSchemaAntBuild.xml", "r") as configFile:
            lines=configFile.readlines()
        with open("schemas/build.xml", 'w') as f:
            for line in lines:
                if match('\s*out\s*=\s*\".*\"\s*',line) is not None:
                    f.write('out="'+xssdDocDestination+'"\n')
                elif match('\s*title\s*=\s*\".*\"\s*',line) is not None:
                    f.write('title="ACS '+self.acsTag+' schemas"\n')
                else:
                    f.write(line)
        
        # Move to the temporary folder for building the docs
        self.webFolder.chdirTemp()
        chdir("schemas")
        outFile=self.webFolder.getTempFolder()+"/xsddoc.out"
        print "Generating XSD documentation with xsddoc; output in",outFile
        originalClassPath=environ['CLASSPATH']
        classPath=self.acsRoot+"/lib/xsddoc.jar:"+self.acsRoot+"/lib/xalan.jar:"+self.acsRoot+"/lib/xalan_serializer.jar:"+originalClassPath
        cmd="export CLASSPATH="+classPath+";" "ant 2>&1 >"+outFile
        return system(cmd)!=0
    
    def copyDocsIntoWeb(self):
        '''
        Copy the generated HTMLs into the web folder for APIs
        '''
        print "Copying files into the folder for APIs"
        self.webFolder.chdirTemp()
        chdir("ACS/LGPL/man")
        filesToCopy = listdir(".")
        destFolder=self.webFolder.getAPIsFolder()
        
        for file in filesToCopy:
            if path.isdir(file):
                copytree(file,destFolder+"/"+file)
            else:
                print "\t",file,"skipped: is not a folder."
    
    def generateAllDocs(self):
        '''
        Generate all the API docs by delegating to:
           - genDoxygenDocs():
           - genPyDocs():
           - genErrorsDocumentation():
           - genSchemasDoc():
        
        @return 0 in case of success otherwise the number of errors
        '''
        print "Generating API docs for",self.acsTag
        numOfErrs=0
        if not self.genDoxygenDocs():
            print "\nErrors detected generating API documentation with doxygen\n"
            numOfErrs=numOfErrs+1
        if not self.genJavadocs():
            print "\nErrors detected generating API documentation with javadoc\n"
            numOfErrs=numOfErrs+1
        if self.genPyDocs():
            print "\nErrors detected generating API documentation with pydoc\n" 
            numOfErrs=numOfErrs+1
        if self.genErrorsDocumentation():
            print "\nErrors detected generating ACS errors documentation\n"
            numOfErrs=numOfErrs+1
        if self.genSchemasDoc():
            print "\nErrors detected generating schemas documentation\n"
            numOfErrs=numOfErrs+1
        if self.convertManPagesToHTML():
            print "\nErrors converting man pages into html\n"
            numOfErrs=numOfErrs+1
            
        self.copyDocsIntoWeb()
        self.genAPIindex()
        
        return numOfErrs
    
    def genAPIindex(self):
        '''
        Generate the index for the API from the template file 
        WebDocsGenerator.Data.APIindex.template.
        
        The file generated by this call is APIs/index.html
        and contains the link to all the index.html of the generated documentation.
        '''
        print "Generating index.html from the template in",self.webFolder.getAPIsFolder()
        this_dir, this_filename = path.split(__file__)
        DATA_PATH = path.join(this_dir, "Data", "APIindex.template")
        with open(DATA_PATH) as f:
            content = f.readlines()
            
        acsName=self.acsTag.replace("-ACS-B","")
        acsName=acsName.replace("-",".")
        
        with open(self.webFolder.getAPIsFolder()+"/index.html", 'w') as f:
            for line in content:
                if "%ACS_TAG%" in line:
                    line=line.replace("%ACS_TAG%",acsName)
                f.write(line)
        
#
# ___oOo___
