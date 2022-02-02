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
# acaproni  2014-06-04  created
#
import os.path
from subprocess import call

class DocumentExporter(object):
    '''
    Objects of this class export a Microsoft Word document in pdf
    
    In the web page, each Word document is exported in PDF and doc format. This class produce the url
    to be published in the web too.
    
    The conversion is done with libreoffice that must be already installed.
    In bash you would write the following command line:
        libreoffice --headless --nologo --convert-to pdf --outdir ~/Desktop/ "ACS Use for CTA.docx"
    '''
    
    def __init__(self, docName, outFolder, title):
        '''
        The constructor
        
        @param doc: the name of the document to convert
        @param outFolder: the folder to write the pdf into
        @param title: the title of the document to be written in the URL
        '''
        assert docName is not None
        self.docName=docName
        if self.docName.count(".")!=1:
            raise Exception("Wrong file name to convert "+self.docName+": it should contain one and only one '.'")
        assert outFolder is not None
        self.outFolder=outFolder
        assert title is not None
        self.title=title
        
        if os.path.exists(self.docName) and os.path.isfile(self.docName):
            self.cmd=self.buildCommandToConvert(self.docName,self.outFolder)
            self.convert(self.cmd)
        else:
            raise Exception(self.docName+" can't be read!")
        
    def buildCommandToConvert(self, docName,outFolder):
        '''
        Build the bash command to convert the document
        
        @param doc: the name of the document to convert
        @param outFolder: the folder to write the pdf into
        '''
        cmd=[]
        cmd.append("libreoffice4.2")
        cmd.append("--headless")
        cmd.append("--nologo")
        cmd.append("--convert-to")
        cmd.append("pdf")
        cmd.append("--outdir")
        cmd.append(outFolder)
        cmd.append(docName)
        return cmd
    
    def convert(self,cmd):
        '''
        Convert the word document into pdf by running the passed cmd
        
        @param cmd: The command to convert the document
        @return False in case of error, True otherwise
        '''
        ret=call(cmd)
        return ret!=0
    
    def buildHTML(self,folder="./"):
        '''
        Build the string and the links to be included in the HTML.
        The passed folder is the folder that must be included in the HTML and
        can or cannot be different from folder where the doc has been exported
        
        @param folder: the folder to link the pdf and doc in the HTML string
        @return the HTML string in the form
                 title (link to doc) (link to pdf)
        '''
        if not folder.endswith("/"):
            folder=folder+"/"
        #Generate the url for the doc
        docUrl=folder+self.docName
        # Generate the URL for the PDF
        temp=self.docName.split(".")
        pdfUrl=folder+temp[0]+".pdf"
        
        ret=self.title
        ret = ret + ' (<A href="'+docUrl+'">doc</A>)'
        ret = ret + ' (<A href="'+pdfUrl+'">pdf</A>)'
        return ret
#
# ___oOo___
