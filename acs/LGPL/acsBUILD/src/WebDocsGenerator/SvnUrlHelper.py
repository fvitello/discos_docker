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
from subprocess import call
from os import chdir
from os.path import exists, isdir

class SvnUrlHelper(object):
    '''SvnUrlHelper is a helper to manipulate SVN URL.'''
    
    def __init__(self,url):
        '''Initialize the object with a passed SVN URL in the form 
          * https://alma-svn.hq.eso.org/p2/branches/branches-B
          * https://alma-svn.hq.eso.org/p2/tags/branches-TEMP
          * https://alma-svn.hq.eso.org/p2/trunk
        
        A subsystem, like /ACS, can be specified in the SVN URL.
        
        @param url: The SVN URL
        '''
        if not self.checkUrl(url):
            raise Exception("Invalid SVN URL") 
        
        self.url=url
        
    def checkUrl(self,url):
        '''
        Check if the passed URL is a valid SVN URL
        
        @return: True if the URL is valid, False otherwise
        '''
        if url is None or len(url)==0:
            return False
        
        if not url.startswith("http"):
            return False
        
        self.isBranchUrl = "/branches" in url
        self.isTagUrl    = "/tags" in url
        self.isTrunk     = "/trunk" in url
        
        if not self.isBranchUrl and not self.isTagUrl and not self.isTrunk:
            return False
        
        types=0
        if self.isBranchUrl:
           types=types+1 
        if self.isTagUrl:
           types=types+1
        if self.isTrunk:
           types=types+1
        if types!=1:
            return False
        
        return True
    
    def getSvnRepository(self):
        '''
        @return:  the repository of the URL like https://alma-svn.hq.eso.org/p2
        '''
        if self.isBranchUrl:
           return self.url.split("/branches")[0]
        elif self.isTagUrl:
           return self.url.split("/tags")[0]
        elif self.isTrunk:
           return self.url.split("/trunk")[0]
        else:
          raise Exception("Malformed SVN URL") 
    
    def isBranch(self):
        '''
        @return True if this usr represents a branch
        '''
        return self.isBranchUrl
    
    def isTag(self):
        '''
        @return True if this usr represents a tag
        '''
        return self.isTagUrl
    
    def isTrunk(self):
        '''
        @return True if this url represents the trunk
        '''
        return self.isTrunk
    
    def getTag(self):
        '''
        @return: the name of the branch or tag represented by this URL
                 If the URL is a trunk this method return TRUNK
        '''
        if self.isTrunk:
            return "TRUNK"
        
        temp=None
        if self.isBranchUrl:
            temp = self.url.split("/branches/")
        if self.isTagUrl:
            temp = self.url.split("/tags/")
            
        if temp is None or len(temp)==1:
            raise Exception("Malformed SVN URL")
        
        return temp[1]
    
    def checkOutACSFolder(self):
        '''
        Checkout ACS folder from SVN.
        ACS is checked out with --depth files so it is empty
        
        @return: False in case of errors, True otherwise
        '''
        # Add /ACS to the URL if not already present
        if not self.url.endswith("/ACS"):
            url=self.url+"/ACS"
        else:
            url=self.url
        print "Checking out from SVN:",url
        # Checkout ACS folder with the Makefile
        cmd=[]
        cmd.append("svn")
        cmd.append("co")
        cmd.append(url)
        cmd.append("--depth")
        cmd.append("files")
        ret=call(cmd)
        if ret!=0:
            print "Error checking out",url
            return False
        return True
        
    def checkoutACS(self):
        '''
        Checkout ACS NO-LGPL from SVN in the current folder.
        
        To checkout ACS, it invokes SVN and then the ACS Makefile
        
        @return: False in case of errors, True otherwise
        '''
        if not exists("ACS"):
            print "ACS not found: checking out from SVN"
            ret=self.checkOutACSFolder()
            if not ret:
                return False
        elif not isdir("ACS"):
            print "ACS is not a folder!"
            return False
            
        chdir("ACS")
        print "Checking out the ACS sources"
        cmd=[]
        cmd.append("make")
        cmd.append("svn-get-no-lgpl")
        ret=call(cmd)
        if ret!=0:
            print "Error running make to checkout ACS sources"
            return False
        
        return True
    
    def checkoutAcsDocuments(self):
        '''
        Checkout ACS/Documents in the current folder
        
        @return: False in case of errors, True otherwise
        '''
        if not exists("ACS"):
            print "ACS not found: checking out from SVN"
            ret=self.checkOutACSFolder()
            if not ret:
                return False
        elif not isdir("ACS"):
            print "ACS is not a folder!"
            return False
       
        chdir("ACS")
        
        outFile="./svnCheckOutDocs.out"
        print "Checking out the ACS/Documents - output in ",outFile
        with open(outFile, 'w') as f:
            cmd=[]
            cmd.append("svn")
            cmd.append("up")
            cmd.append("Documents")
            ret=call(cmd,stdout=f,stderr=f)
            if ret!=0:
                print "Error running checking out ACS/Documents. See output for details",outFile
                return False
        
        return True
