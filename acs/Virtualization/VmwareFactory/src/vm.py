#!/usr/bin/python 

# Import system modules
import os
import getpass
import sys
import time
import signal
import socket

# Import Project Modules

import ParseConfig
import Common

# Server listening on PORT 62000
PORT = 62000
# Maximum numbers of Character that can be received
MAXNUMCHARS = 256

# Directory exported to Vmware machines
SOURCE = '/acs_build/'

feedback        = '22222@vmware_factory'
 

#############################################################################################################################
def executeCommand( commandLine, Host):

        # First Release semaphores on remote host 
        Claro2  = SOURCE + "/bin/Claro2 "
        releaseLine = "ssh -x " + rUser + "@"  + host + " " + Claro2 + " -release"
        Common.Log("Releasing semaphores on host " + host + ", run following command -> " + releaseLine )
        status = os.system(releaseLine)
        # To be activated again when the following error will disappear
        # Died at /home/vltuser8/sqam/scripts/crontabs/Semaphor.pm line 31.

        #if status:
        #    Common.Log(" >>> Failed release semaphores with command: " + realeaseLine)

        # Then Create an Internet Socket
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # Connect to the right host/port
        Log ('Connecting to the client HOST ' + Host + ' Port: ' + str(PORT))
        s.connect((Host,PORT))

        # send the command
        bytesSent=s.sendall(commandLine)

        try:
                s.close()
        except:
                print "exception"



#################################################################################################################################################
def buildCommandLine( Task, Machine, feedback): 
#################################################################################################################################################

  
        # Build Claro2 command line 

        # Prepare a set of variable with different arguments to pass to Claro2
        subsystem               = Task['name']
        host                    = Machine['name']
        ruser                   = Task['user']
        baseCamp                = Machine['base']
        baseDest                = Machine['destination']
        release                 = Task['version']
        notify                  = Task['notify']
        notifyTest              = Task['notifyTest']
        extractionEnd           = time.time()
        timeoutThreshold        = str(3600)
        test                    = Task['test']
        instrument              = Machine['instrument']
        excludees               = Task['excludees']
        currentPid              = os.getpid()
        emptyString             = "\"\""

        if excludees == "":
                excludees       = emptyString

        # These four parameters are now fixed but needs to be put in the xml configuration file
        endurance               = 'NULL'
        build                   = 'on'
        valgrind                = 'off'
        remoteHost              = emptyString
        #feedback               = '\'' + feedback +'\''

        # And log them
        Common.Log("=============================================")
        Common.Log("Current Pid = " + str(currentPid))
        Common.Log("=============================================")
        Common.Log("subsystem           = " + subsystem)
        Common.Log("Host                        = " + host)
        Common.Log("Ruser                       = " + ruser) 
        Common.Log("Base Camp           = " + baseCamp)
        Common.Log("Base Dest           = " + baseDest)
        Common.Log("Release             = " + release)
        Common.Log("Notify                      = " + notify)
        Common.Log("Notify Test         = " + notifyTest)
        Common.Log("ExtrEnd             = " + str(extractionEnd))
        Common.Log("TimeTresh           = " + str(timeoutThreshold))
        Common.Log("Test                        = " + str(test))
        Common.Log("Instrument          = " + instrument)
        Common.Log("Valgrind            = " + valgrind)
        Common.Log("Remote Host         = " + remoteHost)
        Common.Log("Excludees           = " + excludees)
        Common.Log("Endurance           = " + endurance)
        Common.Log("Feedback            = " + feedback)
        Common.Log("=============================================")


        # Claro2 log file on the vm host
        LogFile = '/tmp/NRI/Claro2-' + subsystem + '-' + str(currentPid) + '.log'  


        # Main Claro2 path 
        #Claro2  = "/home/vltuser8/sqam/scripts/crontabs/Claro2 -nofork "
        Claro2  = SOURCE + "/bin/Claro2 "
        # First Parameter 
        Parameters = 'ALMA'

        # build List of all arguments 
        for param in [ subsystem, baseCamp, baseDest, release, notify, notifyTest, str(extractionEnd), timeoutThreshold, test, build, instrument, valgrind, remoteHost, excludees, endurance, feedback]:
                Parameters +=  ' ' + param  

    
        # Build Claro2 command Line
        commandLine = Claro2 + ' -nofork ' + Parameters + ' > ' + LogFile + ' 2>&1\n'

        return commandLine


#################################################################################################################################################

def checkVmwareUp(Host, Port):
# Check if a selected port is open on the Vmware machine

        # Create an Internet Socket
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # Connect to the right host/port
        try:
                s.connect((Host, Port))
                status = True
        except socket.error:
                status = False
        s.close
        return status

#################################################################################################################################################


def checkClaroEnd(Port):
# Check if a selected port is open on the Vmware machine

        # create a INET socket (type STREAM)
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        
        # Ensure that you can restart your server quickly when it terminates    
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        
        # associate socket 
        sock.bind(('', Port))

        # Set number of clients waiting for connection that can be queued
        sock.listen(1)
        Common.Log(" Start listening on port  " + str(Port))

        # Waiting for only one connection
        newSocket, address = sock.accept()
        Common.Log(" Claro2 run terminated on" + str(address))
        newSocket.close()
        
        # Stop listening on port 
        Common.Log(" Stop listening  on port " + str(Port))
        
        sock.close()

#################################################################################################################################################


def retrieveSoftware(sub, cvsroot, baseCamp, Live):
# Retrieve software from CVS
        Common.Log("Start of Retrieving software ....")
        if Live:
                os.chdir(baseCamp)
                Common.Log("Here we start from the " + baseCamp + " Directory")
                command = 'rm -rf ACS'
                Common.Log("Removing ACS directory -> " + command)
                os.system(command)
                command = 'cvs -d ' + cvsroot + ' co -r'  + sub['version'] + ' ACS/Makefile'
                Common.Log("Start Retrieving Software: Executing Command -> " + command)
                os.system(command)
                #command =  "export CVSROOT=$ENV{CVSROOT}; cd ACS; make cvs-get-no-lgpl "
                #command =  "cd ACS; make cvs-get-no-lgpl > /dev/null 2>&1"
                #command =  "cd ACS; pwd; make cvs-get-no-lgpl > /dev/null 2>&1"
                command =  "cd ACS; pwd; make cvs-get-no-lgpl > /dev/null 2>&1"
                Common.Log("Executing Command -> " + command)
                status = os.system(command)
                if status:
                    Common.Log(" >>> Failed to retrieve software " + command)
                Common.Log("End of Retrieving Software Command -")

                
#################################################################################################################################################



# Here Starts the main

# Lock file - only one version of the scritp is allowed to run
lockfile = '/tmp/VmWarepy.lock'

# Set up Live Action
Live = True

# Turn this flag on if you want to avoid rebooting/shuttding down the VmWare machine
# It will assume the vmware machines are up and running
vmWareBooted = False;

# Remote User (must be present with the right environment on the Vmware
# Virtual Machines
rUser = 'vltuser8'

# Current User (must be the user that run the script and 
# defined on teh factory
cUser = 'sqa-ops'

# Define a status variable, useful at the beginning to understand if there are other
# 1) VmWare.py instances
# 2) vmware machines running as sqa-ops
# 3) lock file left from some previous script
StopScript = False

# Get Current Process id
currentPid = os.getpid()

# Define Log directory and check if is writable
LogDir = SOURCE + 'log/'

# Define Log filename
LogFileName = LogDir + 'VmWarePy.' + str(currentPid)
Common.LogFileName = LogFileName

# Check the current username  
currentUser = getpass.getuser()
if currentUser != cUser:
        print "************************************************************************************"
        print "You are running the script as the wrong user " + currentUser + " : It should be " + cUser 
        print "************************************************************************************"
        sys.exit(-1)


if (not os.path.isdir(LogDir)) or (not os.access(LogDir, os.W_OK)):
        print "************************************************************************************"
        print "LogDir directory " + LogDir + " doesn't exist, is not writable or is not a directory" 
        print "************************************************************************************"
        sys.exit(-1)
                

if not os.access(LogFileName, os.W_OK) and os.path.isfile(LogFileName):
        print "Cannot open Log file: " + LogFileName + " in append mode (permisssion?)"
        sys.exit(-1)

# check that no other processes with Vmware.Py running 
if not vmWareBooted: 
        psout = os.popen('ps -ef |grep VmWare.py | grep python |grep -v grep', 'r')
        for machine in psout:
                pid = machine.split()[1]
                if pid != str(currentPid):
                        print "Vmware.Py processes  running with pid " + pid
                        StopScript = True
        if StopScript:  
                print "Exit script because of errors"
                sys.exit(-1)

# check that no other vmware process is running as sqa-ops
if not vmWareBooted: 
        psout = os.popen('ps -u ' + cUser + ' |grep vmware| grep -v grep', 'r')
        results = psout.read().split('\n')
        if len(results) != 1:
                print "One or more vmware processes  are running as user sqa-ops : vmware machines are up check the situation and retry"
                print "Running Processes are:"
                for line in results:
                        StopScript = True
                        print line
        if StopScript: 
                print "Exit script"
                sys.exit(-1)

#Check that same script is not running
if not os.path.isfile(lockfile):
        lock = open(lockfile, "w")
        # Check if you can write lock fiele
        if not lock:
                print "Cannot open lockfile " + lockfile + " for writing"
                sys.exit(-1)
        lock.write(str(currentPid));
        lock.close
else:
        print "***************************************************************"
        print "A lock file has been found under  " + lockfile
        print "***********************************************************************************"
        lock = open(lockfile, "r")
        if lock:
                pid = lock.read();
                print "According to the lock file  there should be a process running with  " + pid + " Pid"
                print "Please kill the process and remove the PID manually before running the script again"
                sys.exit(-1)
        else:
                print "I cannot read lockfile  " + lockfile + " Permission problems? please check before running the script again"
        sys.exit(-1)

print "Vmware.py started, log can be found under " + LogFileName

                
# Where all the results will be in the Vmware Factory 
sqaBaseCamp = SOURCE + '/NRI/ALMA'
Common.Log("All results will be found under  " + sqaBaseCamp)

# Read Configuration File and parse it
Config = SOURCE + "/etc/NRIVMWare.xml" 
#  Check if Config file exists
if (not os.path.isfile(Config)) or (not os.access(Config, os.R_OK)) :
        print "ConfigFile " + Config + " is not existing or it cannot be open in read mode"
        os.remove(lockfile)
        sys.exit(-1)

# Parse XML Configuration file
Machines, Subsystems = ParseConfig.ReadConfigFile(Config)

# define Excludees
excludees = 'ACS/LGPL/Kit/acs:ACS/Web/AcsWebStart:ACS/LGPL/Kit/acstempl:ACS/LGPL/Kit/doc:ACS/LGPL/Kit/vlt:ACS/LGPL/Tools/cmm:ACS/LGPL/Tools/compat:ACS/LGPL/Tools/doxygen:ACS/LGPL/Tools/emacs:ACS/LGPL/Tools/expat:ACS/LGPL/Tools/extjars:ACS/LGPL/Tools/extpy:ACS/LGPL/Tools/loki:ACS/LGPL/Tools/tat:ACS/NO-LGPL/fftw:ACS/LGPL/acsBUILD:ACS/NO-LGPL/cfitsio:ACS/NO-LGPL/sla'


# Change Dir to sqaBaseCamp
if (not os.path.isdir(sqaBaseCamp)) or (not os.access(sqaBaseCamp, os.W_OK)) :
        print "sqaBaseCamp " +sqaBaseCamp + " directory is not existing or it is not accessible"
        os.remove(lockfile)
        sys.exit(-1)

os.chdir(sqaBaseCamp)

# Loop over the Virtual Machines
for item in Machines:
        Common.Log('============================================================================================================================')
        # This is how the virtual machine is called
        host =  item['name']

        # VM configuration file, including path
        VmwareConfigFile =  item['config']
        # Check if configuration file really exists
        if not os.path.isfile(VmwareConfigFile):
                Common.Log("Could not open " + VmwareConfigFile + " Skipping the Virtual Machine\n")
                continue

        Common.Log('\tMachine Name: ' + host ) 
        Common.Log('\tVmware Config File:\t ' + VmwareConfigFile )
        Common.Log('\tTEST:\t ' + item['test'] + '\t\n')
        
        # Loop on different subsystems ( for the time being only ACS )
        for sub in Subsystems:
                subs = sub['name']
                Common.Log("\tSubSystem "      +  subs)
                Common.Log("\t\tVERSION:\t"    +  sub['version'])
                Common.Log("\t\tRETRIEVE:\t"   +  sub['retrieve'])
                Common.Log("\t\tREPOSITORY:\t" +  sub['repository'])

        # Set Up the correct CVSROOT environment
        cvsroot = ':pserver:almamgr@cvssrv.hq.eso.org:'+ sub['repository']
        os.environ['CVSROOT'] = cvsroot

        if sub['retrieve'] and sub['retrieve'] != "off": 
            retrieve = True
        else: 
            retrieve = False
        
        Common.Log("Retrieve = " + str(retrieve))

        # If the option is selected in the XML configuration file then, extract the software from the CVS repository
        if retrieve:
            retrieveSoftware(sub, cvsroot, item['base'] , Live)

        # Now boot the VM, if the vmWarerBooted flag has not been set
        if not vmWareBooted:
                Common.Log("Now booting the VM " + host + ".... ") 
                # If needed os.environ['DISPLAY'] = ":2.0"
                command = '/usr/bin/vmware -xq ' + VmwareConfigFile + ' &'
                if Live: 
                        Common.Log("Live option enabled")
                        Common.Log('Executing Command -> ' + command)
                        status = os.system(command)
                        if status:  
                                Common.Log("Failed to launch virtual machine with config file"  +  VmwareConfigFile )

                # Wait until Vmware machine is up : 
                # At the moment t simply checks every 5 seconds if some process is listening on port 22 (ssh)
                # To be changed when we'll have some vmware API to use 
                Common.Log("Waiting until  Vmware machine is up ...")
                if Live: 
                        while True:
                                if  checkVmwareUp(host, PORT):
                                        Common.Log("******** Host " + host + " is up ***********")
                                        break
                                else:
                                        Common.Log("Waiting... for Host " + host)
                                        time.sleep(5)
        

        # When the machine is up then spawn Claro
        feedbackport    = int(feedback.split('@')[0])
        # Build the right Claro2 command line
        Common.Log("Here start buildCommandLine routine")
        commandLine = buildCommandLine(sub, item, feedback)
        # Send the commandLine command to the right VM host
        Common.Log("Command Line Returned by buildClaroCommandLine subroutine is :\n" + commandLine)
        Common.Log("Here start the executeCommand subroutine ")
        executeCommand( commandLine, host)
        # Start Listening on port "feedbackport" until Claro2 is finished
        Common.Log("Here start checkClaroEnd routine on port " + str(feedbackport) )
        checkClaroEnd(feedbackport)
        Common.Log("Here stop checkClaroEnd on port " + str(feedbackport) )

        # Shutdown the VM
        if not vmWareBooted:
                Common.Log("Now Shutting Down the VM .... ")
                # If needed os.environ['DISPLAY'] = ":2.0"
                command = '/usr/bin/ssh -q -l root ' + host + ' /sbin/shutdown -h now'
                if Live: 
                        Common.Log("Executing Command -> " + command)
                        status = os.system(command)
                        if status:  
                                Common.Log("Failed to ShutDown virtual machine with config file" + VmwareConfigFile + " Status = ", status)
                        # added because with RTOS kernel every power managment feature in the kernel is gono
                        # vmware machine cannot poweroff so vm machines are always up
                        # Workaround: wait 5 minutes and then kill the process
                        Common.Log("Waiting 5 minutes before killing the " + host + " virtual machine")
                        time.sleep(300)
                        
                        psout = os.popen('ps -fu sqa-ops |grep vmware | grep -v grep ', 'r')
                        
                        for machine in psout:
                            pid = machine.split()[1]
        
Common.Log('======================================================================================================================================')
Common.Log('Script VmWare.py terminated, removing lock file')
os.remove(lockfile)
Common.Log('======================================================================================================================================')
