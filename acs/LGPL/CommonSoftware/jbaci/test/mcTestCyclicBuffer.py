#!/usr/bin/env python
#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) Associated Universities Inc., 2002 
# (c) European Southern Observatory, 2002
# Copyright by ESO (in the framework of the ALMA collaboration)
# and Cosylab 2002, All rights reserved
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
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
# MA 02111-1307  USA
#

from __future__ import print_function
from Acspy.Clients.SimpleClient import PySimpleClient
from sys                        import argv
from sys                        import exit
from TMCDB                      import MonitorCollector
from TMCDB                      import propertySerailNumber
from omniORB                    import any
import MonitorErrImpl
import MonitorErr
import time

# Make an instance of the PySimpleClient
simpleClient = PySimpleClient()

mc = simpleClient.getComponent(argv[1])
cname = "MC_TEST_COMPONENT"

# First test: Nominal test. Some data buffered and recovered
try:
    tc =   simpleClient.getComponent(cname)
    psns =[propertySerailNumber('doubleSeqProp', ['12124']),propertySerailNumber('doubleProp', ['3432535'])]
    mc.registerNonCollocatedMonitoredDeviceWithMultipleSerial(cname, psns)
    tc.reset()
    mc.startMonitoring(cname)
    time.sleep(2)
    mc.stopMonitoring(cname)

except MonitorErr.RegisteringDeviceProblemEx as _ex:
    ex = MonitorErrImpl.RegisteringDeviceProblemExImpl(exception=_ex)
    ex.Print()

data = mc.getMonitorData()

# First log entry: Print results of the first part of the test
print("Nominal test. Number of Devices:", len(data))
for d in data:
    print(d.componentName, d.deviceSerialNumber)
    for blob in d.monitorBlobs:
        print("\t", blob.propertyName, " ", blob.propertySerialNumber, sep='')
        i=0
        for blobData in any.from_any(blob.blobDataSeq):
            print("\t\t", dict(sorted(blobData.items(), reverse=True)), sep='')
            i+=1

# Second test: Cyclic buffering activation. Reseting MC_TEST_COMPONENT at beggining, verification of data loss
try:
    tc.reset()
    mc.startMonitoring(cname)
    time.sleep(220)
    mc.stopMonitoring(cname)

except MonitorErr.RegisteringDeviceProblemEx as _ex:
    ex = MonitorErrImpl.RegisteringDeviceProblemExImpl(exception=_ex)
    ex.Print()

data = mc.getMonitorData()

# Second log entry: Print results of the second part of the test
print("Cyclic buffer test. Number of Devices:", len(data))
for d in data:
    print(d.componentName, d.deviceSerialNumber)
    for blob in d.monitorBlobs:
        print("\t", blob.propertyName, " ", blob.propertySerialNumber, sep='')
        i=0
        for blobData in any.from_any(blob.blobDataSeq):
            print("\t\t", dict(sorted(blobData.items(), reverse=True)), sep='')
            i+=1

# Third test: Verification of cyclic buffer restarted after acquisition (no MC_TEST_COMPONENT reset)
try:
    #tc.reset()
    mc.startMonitoring(cname)
    time.sleep(15)
    mc.stopMonitoring(cname)

except MonitorErr.RegisteringDeviceProblemEx as _ex:
    ex = MonitorErrImpl.RegisteringDeviceProblemExImpl(exception=_ex)
    ex.Print()

data = mc.getMonitorData()

# Third log entry: Print results of the third part of the test
print("Cyclic buffer reset test. Number of Devices:", len(data))
for d in data:
    print(d.componentName, d.deviceSerialNumber)
    for blob in d.monitorBlobs:
        print("\t", blob.propertyName, " ", blob.propertySerialNumber, sep='')
        i=0
        for blobData in any.from_any(blob.blobDataSeq):
            print("\t\t", dict(sorted(blobData.items(), reverse=True)), sep='')
            i+=1

mc.deregisterMonitoredDevice(cname)

#cleanly disconnect
simpleClient.releaseComponent(argv[1])
simpleClient.disconnect()
