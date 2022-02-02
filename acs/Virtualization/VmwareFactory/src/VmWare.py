#!/usr/bin/python 

# Import system modules
import os
import getpass
import sys
import time
import signal
import socket

# Import Project Modules
from Config import *
from Common import LogFile
        
def generateStatusPage(vmHost, run, timestamp, Task, Machine, feedback, logFileWeb):
    # Generate VM Builds Status page
    # vmHost can be one of the following names
    #  - rhel4-acs5
    #  - rhel4-acs6
    #  - sl4-acs5
    #  - sl4-acs6
    possible_Hostnames = [ 'rhel4-acs5', 'rhel4-acs6', 'sl4-acs5', 'sl4-acs6' ]
    
    statusDir = SOURCE + '/log/'
    statusFile = statusDir + '/' +vmHost + "/status.html"
    
    if ((not os.path.isdir(statusDir)) or (not os.access(statusDir, os.W_OK))):
        print "************************************************************************************"
        print "Status directory " + statusDir + " doesn't exist, is not writable or is not a directory" 
        print "************************************************************************************"
        sys.exit(-1)
    
    s = open(statusFile, 'w')
    
  
    s.write('<html>\n')
    s.write('<body <center>')
    s.write('<h1>Vmware machine build status:</h1>')
    if run:
        status = "RUNNING"
    else:
        status = "STOPPED"
        
    s.write('<hr><h2>Host : ' + vmHost)
    s.write('<br>Build status is: ' + status) 
    s.write('<br>Build ' + status + ' On ' + timestamp + '</h2><br>')
    s.write('</center><hr>\n')
    
    # Prepare a set of variable with different arguments to pass to Claro2
    subsystem         = Task['name']
    ruser             = Task['user']
    baseCamp          = Machine['base']
    baseDest          = Machine['destination']
    release           = Task['version']
    timeout           = str(3600)
    test              = Task['test']
    instrument        = Machine['instrument']
    excludees         = Task['excludees']
    currentPid        = os.getpid()
    endurance        = 'NULL'
    build             = 'on'
    valgrind         = 'off'
    
    if run: 
    	s.write( '<br> Current Build Configuration:</br>')
    else:
    	s.write( '<br> Last Build Configuration:</br>')
  
    s.write( '<ul>')
    s.write( '<li>Subsystem:        ' + subsystem )
    s.write( '<li>Remote User:      ' + ruser )
    s.write( '<li>Base Camp:        ' + baseCamp )
    s.write( '<li>Base Destination: ' + baseDest )
    s.write( '<li>release:          ' + release )
    s.write( '<li>Claro2 Timeout:   ' + timeout + ' s' )
    s.write( '<li>Pid:              ' + str(currentPid) )
    s.write( '<li>Endurance:        ' + endurance )
    s.write( '<li>Build:            ' + build )
    s.write( '<li>Vlagrind:         ' + valgrind)
    s.write( '</UL>' )
    
    # Check if hostname is a valid vmware host
    valid = False
    for name in possible_Hostnames:
        if vmHost == name:
            valid = True
            VM=vmHost
            break
    if not valid:
        log.logMsg("Vmware Host  " + vmHost + " not valid: ")
        log.logMsg("Must be one of")
        for name in possible_Hostnames: log.logMsg(name)
        sys.exit()
            
    snapdir   = 'snapshotVM' + VM
    webserver = 'websqa.hq.eso.org'
    webdir    = '/home/sqa-ops/docs/alma/' + snapdir
    print "snapdir = ", snapdir
    print "webdir = ", webdir
    
    htmlAddress = 'http://' + webserver + '/'  + snapdir + '/logs/' + logFileWeb
    
    s.write('<p>Last Log name is' + ClaroLogFile + " And can be found")
    s.write('  <a href=\"' + htmlAddress +'\">here</a>\n')
    s.write('</center></body>\n')
    s.write('</html>\n')
    
    s.close()

    

def executeCommand( commandLine, Host):

	# First call Claro with -release option to free semaphores
	releaseLine = "ssh -x " + rUser + "@"  + host + " " + Claro2 + " -release"
	log.logMsg("Releasing semaphores on host " + host + ", run following command -> " + releaseLine )
	status = os.system(releaseLine)
	# To be activated again when the following error will disappear
	# Died at /home/vltuser8/sqam/scripts/crontabs/Semaphor.pm line 31.

	#if status:
	#    Common.Log(" >>> Failed release semaphores with command: " + realeaseLine)

	# Then Clean up /introot before any build otherwise there will be sapace problems
	# in the virtual machines and the build will fail
	cleanUpLine = "ssh -x " + rUser + "@"  + host + " /bin/rm -rf  /introot/" + rUser + ".*"
	log.logMsg("Releasing semaphores on host " + host + ", run following command -> " + cleanUpLine )
	status = os.system(cleanUpLine)
	if status:
	    Common.Log(" >>> Failed Cleaning up /introot directory status = " + status)

	# Then Create an Internet Socket
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	# Connect to the right host/port
	log.logMsg('Connecting to the client HOST ' + Host + ' Port: ' + str(PORT))
	s.connect((Host,PORT))

	# send the command
	log.logMsg('Sending Command Line\n' + commandLine)
	bytesSent=s.sendall(commandLine)
	log.logMsg('Command Line Sent')
	try:
		s.close()
	except:
		print "Exception"
		
def buildCommandLine( Task, Machine, feedback):	
##############################################################################################################################

  
	# Build Claro2 command line 

	# Prepare a set of variable with different arguments to pass to Claro2
	subsystem 		= Task['name']
	host      		= Machine['name']
	ruser     		= Task['user']
	baseCamp      		= Machine['base']
	baseDest 		= Machine['destination']
	release   		= Task['version']
	notify    		= Task['notify']
	notifyTest		= Task['notifyTest']
	extractionEnd  		= time.time()
	timeoutThreshold 	= str(3600)
	test      		= Task['test']
	instrument      	= Machine['instrument']
	excludees      		= Task['excludees']
	currentPid		= os.getpid()
	emptyString		= "\"\""

	if excludees == "":
		excludees 	= emptyString

	# These four parameters are now fixed but needs to be put in the xml configuration file
	endurance		= 'NULL'
	build 			= 'on'
	valgrind 		= 'off'
	remoteHost 		= emptyString
	#feedback		= '\'' + feedback +'\''

	# And log them
	log.logMsg("=============================================")
	log.logMsg("Current Pid	= " + str(currentPid))
	log.logMsg("=============================================")
	log.logMsg("subsystem 		= " + subsystem)
	log.logMsg("Host			= " + host)
	log.logMsg("Ruser			= " + ruser) 
	log.logMsg("Base Camp		= " + baseCamp)
	log.logMsg("Base Dest		= " + baseDest)
	log.logMsg("Release		= " + release)
	log.logMsg("Notify			= " + notify)
	log.logMsg("Notify Test		= " + notifyTest)
	log.logMsg("ExtrEnd 		= " + str(extractionEnd))
	log.logMsg("TimeTresh 		= " + str(timeoutThreshold))
	log.logMsg("Test			= " + str(test))
	log.logMsg("Instrument		= " + instrument)
	log.logMsg("Valgrind		= " + valgrind)
	log.logMsg("Remote Host		= " + remoteHost)
	log.logMsg("Excludees		= " + excludees)
	log.logMsg("Endurance 		= " + endurance)
	log.logMsg("Feedback 		= " + feedback)
	log.logMsg("=============================================")


	# First Parameter 
	Parameters = 'ALMA'

	# build List of all arguments 
	for param in [ subsystem, baseCamp, baseDest, release, notify, notifyTest, str(extractionEnd), timeoutThreshold, test, build, instrument, valgrind, remoteHost, excludees, endurance, feedback]:
		Parameters +=  ' ' + param  

    
	# Build Claro2 command Line
	commandLine = Claro2 + ' -nofork ' + Parameters + ' > ' + ClaroLogFile + ' 2>&1\n'

	return commandLine
##############################################################################################################################
def checkVmwareUp(Host, Port):
    # Check if a selected port is open on the Vmware machine
    log.logMsg("Checking Host " + Host + " On port " + str(Port))
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
##############################################################################################################################
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
	log.logMsg(" Start listening on port  " + str(Port))

	# Waiting for only one connection
	newSocket, address = sock.accept()
	log.logMsg(" Claro2 run terminated on" + str(address))
	newSocket.close()
	
	# Stop listening on port 
	log.logMsg(" Stop listening  on port " + str(Port))
	
	sock.close()
##############################################################################################################################
def retrieveSoftware(sub, cvsroot, baseCamp, Live):
# Retrieve software from CVS
	log.logMsg("Start of Retrieving software ....")
	if Live:
		os.chdir(baseCamp)
		log.logMsg("Here we start from the " + baseCamp + " Directory")
		command = 'rm -rf ACS'
		log.logMsg("Removing ACS directory -> " + command)
		os.system(command)
		command = 'cvs -d ' + cvsroot + ' co -r'  + sub['version'] + ' ACS/Makefile'
		log.logMsg("Start Retrieving Software: Executing Command -> " + command)
		os.system(command)
		#command =  "export CVSROOT=$ENV{CVSROOT}; cd ACS; make cvs-get-no-lgpl "
		#command =  "cd ACS; make cvs-get-no-lgpl > /dev/null 2>&1"
		#command =  "cd ACS; pwd; make cvs-get-no-lgpl > /dev/null 2>&1"
		command =  "cd ACS; pwd; make cvs-get-no-lgpl > /dev/null 2>&1"
		log.logMsg("Executing Command -> " + command)
		status = os.system(command)
		if status:
			log.logMsg(" >>> Failed to retrieve software " + command)
			log.logMsg("End of Retrieving Software Command -")


# Here Starts the main


# Define a status variable, useful at the beginning to understand if there are other
# 1) VmWare.py instances
 # 2) vmware machines running as cUser
 # 3) lock file left from some previous script
StopScript = False

# Identify the current process ID
currentPid		= os.getpid()

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
	
# Define Log File the LogFile class witll
# add -time "-timeStamp()" to the name
vmwarePyLogFile = SOURCE + 'log/NRIVmware/VmwarePy'
#print "Calling class"
log = LogFile(vmwarePyLogFile)

if not os.access(LogFileName, os.W_OK) and os.path.isfile(LogFileName):
	print "Cannot open Log file: " + LogFileName + " in append mode (permisssion?)"
	sys.exit(-1)



#print "StopScript = " + str(StopScript)
#print "VmwareBooted = " + str(vmWareBooted)
# check that no other processes with Vmware.Py running 
#print "Script prima ps= "
psout = os.popen('ps -ef |grep VmWare.py | grep python |grep -v grep', 'r')
for machine in psout:
	#print "Script dopo ps= " + str(StopScript)
	pid = machine.split()[1]
	if pid != str(currentPid):
		print "Vmware.Py processes  running with pid " + pid
		StopScript = True
		#print "Script = " + str(StopScript)
		if StopScript:
			print "Check why script is still running"
			sys.exit(-1)

# check that no other vmware process is running as cUser
if not vmWareBooted: 
	psout = os.popen('ps -u ' + cUser + ' |grep vmware| grep -v grep', 'r')
	results = psout.read().split('\n')
	
	if len(results) != 1:
		print "One or more vmware processes  are running as user " + cUser + ": vmware machines are up check the situation and retry"
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

# Where all the results will be in the Vmware Factory 
sqaBaseCamp = SOURCE + 'NRI/ALMA'
log.logMsg("All results will be found under  " + sqaBaseCamp)

# define Excludees
#excludees = 'ACS/LGPL/Kit/acs:ACS/Web/AcsWebStart:ACS/LGPL/Kit/acstempl:ACS/LGPL/Kit/doc:ACS/LGPL/Kit/vlt:ACS/LGPL/Tools/cmm:ACS/LGPL/Tools/compat:ACS/LGPL/Tools/doxygen:ACS/LGPL/Tools/emacs:ACS/LGPL/Tools/expat:ACS/LGPL/Tools/extjars:ACS/LGPL/Tools/extpy:ACS/LGPL/Tools/loki:ACS/LGPL/Tools/tat:ACS/NO-LGPL/fftw:ACS/LGPL/acsBUILD:ACS/NO-LGPL/cfitsio:ACS/NO-LGPL/sla'


# Change Dir to sqaBaseCamp
if (not os.path.isdir(sqaBaseCamp)) or (not os.access(sqaBaseCamp, os.W_OK)) :
	print "sqaBaseCamp " +sqaBaseCamp + " directory is not existing or it is not accessible"
	os.remove(lockfile)
	sys.exit(-1)
os.chdir(sqaBaseCamp)

print "Vmware.py started, log can be found under " + log.LogFileName
print "Build Status can be found under: " 
print "for RedHat Enterprise and ACS 5 http://websqa.hq.eso.org/alma/snapshotVMrhel4-acs5/logs/status.html"
print "for RedHat Enterprise and ACS 6 http://websqa.hq.eso.org/alma/snapshotVMrhel4-acs6/logs/status.html"
print "for Scientific Linux  and ACS 5 http://websqa.hq.eso.org/alma/snapshotVMsl4-acs5/logs/status.html"
print "for Scientific Linux  and ACS 6 http://websqa.hq.eso.org/alma/snapshotVMsl4-acs6/logs/status.html"

# Loop over the Virtual Machines
for item in Machines:
   	log.logMsg('============================================================================================================================')
	# This is how the virtual machine is called
	host =  item['name']
	# And this is the subsystem
	subsystem = item['name']
	# VM configuration file, including path
	VmwareConfigFile =  item['config']
	# Check if configuration file really exists
	if not os.path.isfile(VmwareConfigFile):
      		log.logMsg("Could not open " + VmwareConfigFile + " Skipping the Virtual Machine\n")
      		continue

   	log.logMsg('\tMachine Name: ' + host ) 
	log.logMsg('\tVmware Config File:\t ' + VmwareConfigFile )
	log.logMsg('\tTEST:\t ' + item['test'] + '\t\n')
	
	# Loop on different subsystems ( for the time being only ACS )
	for sub in Subsystems:
		subs = sub['name']
		log.logMsg("\tSubSystem "      +  subs)
		log.logMsg("\t\tVERSION:\t"    +  sub['version'])
		log.logMsg("\t\tRETRIEVE:\t"   +  sub['retrieve'])
		log.logMsg("\t\tREPOSITORY:\t" +  sub['repository'])
		# Claro2 log file on the vm host
    	ClaroLogFile = LogDir  + host + '/Claro2-' + subsystem + '-' + log.timeStamp() + '.log'  
    	LogFileWeb = 'Claro2-' + subsystem + '-' + log.timeStamp() + '.log' 

	# Set Up the correct CVSROOT environment
	cvsroot = ':pserver:almamgr@cvssrv.hq.eso.org:'+ sub['repository']
	os.environ['CVSROOT'] = cvsroot

	if sub['retrieve'] and sub['retrieve'] != "off": 
	    retrieve = True
	else: 
	    retrieve = False
	
	log.logMsg("Retrieve = " + str(retrieve))

	# If the option is selected in the XML configuration file then, extract the software from the CVS repository
	if retrieve:
	    retrieveSoftware(sub, cvsroot, item['base'] , Live)

	# Now boot the VM, if the vmWarerBooted flag has not been set
	if not vmWareBooted:
		log.logMsg("Now booting the VM " + host + ".... ") 
	    	# If needed os.environ['DISPLAY'] = ":2.0"
		command = '/usr/bin/vmware -xq ' + VmwareConfigFile + ' &'
	    	if Live: 
			log.logMsg("Live option enabled")
			log.logMsg('Executing Command -> ' + command)
			status = os.system(command)
			if status:  
				log.logMsg("Failed to launch virtual machine with config file"  +  VmwareConfigFile )

		# Wait until Vmware machine is up : 
		# At the moment t simply checks every 5 seconds if some process is listening on port 22 (ssh)
		# To be changed when we'll have some vmware API to use 
	    	log.logMsg("Waiting until  Vmware machine is up ...")
	    	if Live: 
		 	while True:
				if  checkVmwareUp(host, PORT):
					log.logMsg("******** Host " + host + " is up ***********")
					break
				else:
					log.logMsg("Waiting... for Host " + host)
					time.sleep(5)
	

        # When the machine is up then spawn Claro
	feedbackport	= int(feedback.split('@')[0])
	# Build the right Claro2 command line
	log.logMsg("Here start buildCommandLine routine")
	commandLine = buildCommandLine(sub, item, feedback)
	# Send the commandLine command to the right VM host
	log.logMsg("Command Line Returned by buildClaroCommandLine subroutine is :\n" + commandLine)
	log.logMsg("Here start the executeCommand subroutine ")
	executeCommand( commandLine, host)
	
	# Write a new status page
	status = True
	timestamp = log.timeStamp()
	generateStatusPage(host, status, timestamp, sub, item, feedback, LogFileWeb )
	
	# Start Listening on port "feedbackport" until Claro2 is finished
	log.logMsg("Here start checkClaroEnd routine on port " + str(feedbackport) )
	#print "FeedbackPort = " + str(feedbackport)
	checkClaroEnd(feedbackport)
	log.logMsg("Here stop checkClaroEnd on port " + str(feedbackport) )

	# Log on the Web when build finished
	status = False
	timestamp = log.timeStamp()
	generateStatusPage(host, status, timestamp, sub, item, feedback, LogFileWeb )
	
	# Shutdown the VM
	if not vmWareBooted:
		log.logMsg("Now Shutting Down the VM .... ")
	    	# If needed os.environ['DISPLAY'] = ":2.0"
		command = '/usr/bin/ssh -q -l root ' + host + ' /sbin/shutdown -h now'
		if Live: 
			log.logMsg("Executing Command -> " + command)
			status = os.system(command)
			if status:  
				log.logMsg("Failed to ShutDown virtual machine with config file" + VmwareConfigFile + " Status = ", status)
			# added because with RTOS kernel every power managment feature in the kernel is gono
                        # vmware machine cannot poweroff so vm machines are always up
			# Workaround: wait 5 minutes and then kill the process
			log.logMsg("Waiting 5 minutes before killing the " + host + " virtual machine")
			time.sleep(300)
			
			log.logMsg("5 minutes passed: Executing the following command: ")
			command = 'ps -fu ' + cUser + ' | grep vmware | grep -v grep '
			log.logMsg(command)
			psout = os.popen(command, 'r')
			
			for machine in psout:
				pid = machine.split()[1]
				log.logMsg("Killing process n. " + pid)
				os.system("kill -9 " + pid)

	
log.logMsg('======================================================================================================================================')
log.logMsg('Script VmWare.py terminated, removing lock file')
os.remove(lockfile)
log.logMsg('======================================================================================================================================')
