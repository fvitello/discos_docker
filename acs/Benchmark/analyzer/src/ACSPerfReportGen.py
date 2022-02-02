#!/usr/bin/env python
#------------------------------------------------------------------------------
# @(#) $Id: ACSPerfReportGen.py,v 1.3 2004/10/15 22:16:35 dfugate Exp $
#
#    ALMA - Atacama Large Millimiter Array
#    (c) Associated Universities, Inc. Washington DC, USA, 2001
#    (c) European Southern Observatory, 2002
#    Copyright by ESO (in the framework of the ALMA collaboration)
#    and Cosylab 2002, All rights reserved
#
#    This library is free software; you can redistribute it and/or
#    modify it under the terms of the GNU Lesser General Public
#    License as published by the Free Software Foundation; either
#    version 2.1 of the License, or (at your option) any later version.
#
#    This library is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#    Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with this library; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#------------------------------------------------------------------------------
'''

TODO:
- all
'''
#------------------------------------------------------------------------------
from __future__ import print_function
__version__ = "$Id: ACSPerfReportGen.py,v 1.3 2004/10/15 22:16:35 dfugate Exp $"
#------------------------------------------------------------------------------
from sys  import argv, stdout
from copy import deepcopy
import    dbm
import    time

from AcsutilPy.FindFile import findFile
#------------------------------------------------------------------------------
htmlBegin='''
<html>
<head>
  <meta content="text/html; charset=ISO-8859-1" http-equiv="content-type">
  <title>ACS Performance Analysis Report</title>
  <meta content="ACS Performance Analysis Report" name="description">
</head>
<body>
<img alt="ALMA Logo" src="http://www.eso.org/projects/alma/develop/software/alma-it/images/almapic.jpg" style="width: 152px; height: 105px;">
<big><big><big>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ACS Performance Analysis Report<br></big></big></big>
<hr style="width: 100%; height: 2px;"><br><br>
'''

msgBegin='''
<div style="text-align: center;"><big><big style="font-weight: bold;"> %s </big></big><br></div>
<table style="text-align: left;" border="1" cellpadding="2"
cellspacing="2">
<tbody>
'''

msgEnd='''
</tbody>
</table>
<br>
<!--
<br>
<big style="font-weight: bold;"><big>Detailed Analysis<br>
<br>
</big></big>Some sort of detailed analysis will eventually go in here!<br>
&nbsp;<br>
-->
<hr style="width: 100%; height: 2px;">
'''

htmlEnd='''
<br>
<br>
<big>This report was generated on: %s </big>
</body>
</html>
'''

testMachineInfo = {}

specialKeys = ['cpu', 'mem' ]
#------------------------------------------------------------------------------
def getDBasDict(dbName):
    '''
    Helper function returns an ACS performance database as a Python dictionary.
    In this dict, the keys are the test type and the values are lists of tests.
    '''
    if findFile(dbName)[0]!="":
        dbName=findFile(dbName)[0]

    DB = dbm.open(DBNAME, 'r')

    retVal = {}

    #get each and every individual test from the database
    for key in DB.keys():
        #turn the value back into a Python dictionary
        tDict = eval(DB[key])

        #now figure out what type of performance test we're dealing with...
        msg = tDict['msg']

        #add this type of performance test to the return dictionary if it has
        #not been encountered previously
        if msg not in retVal:
            retVal[msg]=[]

        #add the dictionary describing the test to the return value
        retVal[msg].append(tDict)

    DB.close()
    return retVal
#------------------------------------------------------------------------------
def sortDict(inDict):
    '''
    Sorts a dictionary (of the same type returned by the getDBasDict function).
    '''
    #sort each value (e.g., list)
    for key in inDict.keys():
        inDict[key].sort(key=lambda c: (c['ip'],c['date']))
#------------------------------------------------------------------------------
def generateHTML(inDict, outFile=stdout):
    '''
    Helper function generates an HTML report based on a dictionary returned by
    getDBasDict function.
    '''

    #make a deepcopy of this so we can do with it as we please!
    inDict = deepcopy(inDict)

    #sort it
    sortDict(inDict)

    #print out the HTML tags all reports will begin with
    print(htmlBegin, file=outFile)

    for msg in inDict.keys():
        print(msgBegin % (msg), file=outFile)
        genHTMLForMsg(inDict[msg], outFile)

    #print out remaining HTML
    printMachineInfo(outFile)
    print(htmlEnd % (time.asctime()), file=outFile)
#------------------------------------------------------------------------------
def genHTMLForMsg(inList, outFile=stdout):
    '''
    '''
    tList = []

    for tDict in inList:
        #remove this key
        del tDict['msg']

    #get a list of all remaining keys
    realKeys = list(inList[0].keys())

    #rearrange the list of keys
    keys = ['ip', 'lang', 'date', 'runs', 'avg', 'mindur', 'maxdur', 'cpu', 'mem', 'units']

    #strip out duplicated keys
    for key in keys:
        try:
            realKeys.remove(key)
        except:
            pass
    #merge the two lists
    keys = keys + realKeys

    #remove cpu and mem
    keys.remove('cpu')
    keys.remove('mem')


    #print out an initial row full of column descriptions
    print('<tr>', file=outFile)
    for key in keys:
        print('<td style="background-color: rgb(51, 102, 255); vertical-align: top;"><br>%s</td>' % (getNiceColHeader(key)), file=outFile)
    print('</tr>', file=outFile)

    #print out the rest of the rows
    for tDict in inList:
        print('<tr>', file=outFile)
        for key in keys:
            print('<td style="vertical-align: top;"><br>%s</td>' % (tDict[key]), file=outFile)

        if tDict['ip'] not in testMachineInfo:
            testMachineInfo[tDict['ip']] = { 'cpu':tDict['cpu'], 'mem':tDict['mem'] }

        if testMachineInfo[tDict['ip']]['cpu'] == "Unknown":
            testMachineInfo[tDict['ip']]['cpu'] = tDict['cpu']
        if testMachineInfo[tDict['ip']]['mem'] == "Unknown":
            testMachineInfo[tDict['ip']]['mem'] = tDict['mem']

        print('</tr>', file=outFile)


    #print out remaining HTML
    print(msgEnd, file=outFile)

#------------------------------------------------------------------------------
def getNiceColHeader(origHeader):
    '''
    Helper function returns a more descriptive column header or just the original
    header if no alternative is known.
    '''
    tDict = { 'msg' : "<center>Description</center>",
              'avg' : "<center>Average<br>Time to<br>Complete</center>",
              'runs' : "<center>Number<br>of<br>Runs</center>",
              'mindur' : "<center>Min.<br>Run<br>Time</center>",
              'maxdur' : "<center>Max.<br>Run<br>Time</center>",
              'cpu' : "<center>PC<br>Speed</center>",
              'mem' : "<center>PC<br>Memory</center>",
              'date' : "<center>Date<br>of<br>Run</center>",
              'ip' : "<center>PC<br>Name</center>",
              'lang' : "<center>Prog.<br>Lang</center>",
              'units' : "<center>Time<br>Units</center>"
             }

    if origHeader in tDict:
        return tDict[origHeader]
    else:
        return origHeader
#------------------------------------------------------------------------------
def printMachineInfo(outFile):
    '''
    '''
    print(msgBegin % ("General Info on Test Machines"), file=outFile)

    #print out an initial row full of column descriptions
    print('<tr><td style="background-color: rgb(51, 102, 255); vertical-align: top;"><br>%s</td>' % ("PC Name"), file=outFile)
    for key in specialKeys:
        print('<td style="background-color: rgb(51, 102, 255); vertical-align: top;"><br>%s</td>' % (getNiceColHeader(key)), file=outFile)
    print('</tr>', file=outFile)

    #print out the rest of the rows
    for machineName in testMachineInfo.keys():
        print('<tr><td style="vertical-align: top;"><br>%s</td>' % (machineName), file=outFile)
        tDict = testMachineInfo[machineName]

        for key in specialKeys:
            print('<td style="vertical-align: top;"><br>%s</td>' % (tDict[key]), file=outFile)
        print('</tr>', file=outFile)

    print('''</tbody>
    </table>
    <br>
    <br>

    <hr style="width: 100%; height: 2px;">''')

#------------------------------------------------------------------------------
if __name__=="__main__":
    DBNAME    = argv[1]  #name of the database

    #open the database
    joe = getDBasDict(DBNAME)
    generateHTML(joe)
