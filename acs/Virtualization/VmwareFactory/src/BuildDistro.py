#!/usr/bin/python 

# Import system modules
import os
import getpass
import sys
import time
import signal
import socket

# Import Project Modules

from Common import LogFile

# Import a Configuration File Parser
import ConfigParser

##############################################################################      
def timeStamp():
    # Get a current Time Stamp - Format 
    item = time.localtime()
    data = "%02d.%02d.%d-%02d.%02d.%02d" % (item[2], item[1], item[0], item[3],item[4],item[5])
    return(data)
          
##############################################################################      
def generateStatusPage(vmHost, run, timestamp, Task, Machine, feedback, logFileWeb):
    # Generate VM Builds Status page
    # vmHost can be rhel4 or sl4 currently
    statusDir = SOURCE + '/log/BuildDistro/'
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
    
    s.write('<p>Last Log name is' + ClaroLogFile + " And can be found")
    if vmHost == 'rhel4': 
    	VM = 'VM1'
    if vmHost == 'sl4': 
    	VM = 'VM2'
    htmlAddress = 'http://websqa.hq.eso.org/alma/snapshot' + VM + '/logs/' + logFileWeb
    s.write('  <a href=\"' + htmlAddress +'\">here</a>\n')
    s.write('</center></body>\n')
    s.write('</html>\n')
    
    s.close()
 
##############################################################################      
def executeCommand( commandLine, Host, PORT):
    # Create an Internet Socket
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	# Connect to the right host/port
    print 'Connecting to the client HOST ' + Host + ' Port: ' + str(PORT)
    log.logMsg('Connecting to the client HOST ' + Host + ' Port: ' + str(PORT))
    s.connect((Host,PORT))

	# send the command
    log.logMsg('Sending Command Line\n' + commandLine)
    print 'Sending Command Line\n' + commandLine
    bytesSent=s.sendall(commandLine)
    log.logMsg('Command Line Sent')
    print 'Command Line Sent'
    try:
		s.close()
    except:
        print "Exception"

###############################################################################
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

################################################################################
def checkBuildEnd(Port):
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
    print " Start listening on port  " + str(Port)

	# Waiting for only one connection
    newSocket, address = sock.accept()
    log.logMsg(" Build run terminated on" + str(address))
    print " Build run terminated on" + str(address)
    newSocket.close()
	
	# Stop listening on port 
    log.logMsg(" Stop listening  on port " + str(Port))
    print " Stop listening  on port " + str(Port)
    
    sock.close()

##############################################################################      
def readConf(configFile):
   
    # Read and parse configuration file
    config = ConfigParser.ConfigParser()
    config.read(configFile)
    # Read the directory to be used as base for the build
    BUILDDIR = config.get("general", "buildDir")
    # Read where all the scripts are in CVS
    CVSPATH = config.get("general", "cvsPath")
    # Read the tag to be used for the scripts in CVS
    CVSTAG = config.get("general", "cvsTag")
   
    # dump entire config file
    # It will contain Vmware Machines information
    Vmware = {}
    for section in config.sections():
        # Every Virtual machine is represented by a list
        if section == 'general':
            BUILDDIR = config.get("general", "buildDir")
            CVSPATH   = config.get('general', 'cvspath')
            CVSTAG   = config.get('general', 'cvsTag')
            baseDir   = config.get('general', 'basedir')
            vmwareDir = config.get('general', 'vmwareDir')
            port      = int(config.get('general', 'port'))
            cUser     = config.get('general', 'cuser')
        else:
            # Vmware[section] = {}
            item = {}
            for option in config.options(section):
                #print option, config.get(section, option)
                value = config.get(section, option)
                item[option] = value
            #print item
            Vmware[section] = item
                    
    # Print Entire COnfig File
    #for section in config.sections():
    #   print section
    #  for option in config.options(section):
    #      print " ", option, "=", config.get(section, option)
    

    #print sortedDictValues(config)
    return(BUILDDIR, CVSPATH, CVSTAG, baseDir, vmwareDir, port, cUser, Vmware)

################################################################################################
def sortedDictValues(adict):
    dictkeys = adict.keys()
    dictkeys.sort()
    return [adict[key] for key in dictkeys]

################################################################################################
def checkDir(BaseDir, LogDir, LogFileName, vmWareBooted, LockFile):
    # Initial security checks
    
    # Define a status variable, useful at the beginning to understand if there are other
    # 1) VmWare.py instances
    # 2) vmware machines running as cUser
    # 3) lock file left from some previous script
    StopScript = False
    
    # Identify the current process I
    currentPid		= os.getpid()

    # Current Script Name
    currentName =  os.path.basename(sys.argv[0])
    
    # Check the current username  
    currentUser = getpass.getuser()
    if currentUser != cUser:
        print "************************************************************************************"
        print "You are running the script as the wrong user " + currentUser + " : It should be " + cUser 
        print "************************************************************************************"
        sys.exit(-1)

    # Check if LogDir path exists and is writable directory 
    if (not os.path.isdir(LogDir)) or (not os.access(LogDir, os.W_OK)):
        print "************************************************************************************"
        print "LogDir directory " + LogDir + " doesn't exist, is not writable or is not a directory" 
        print "************************************************************************************"
        sys.exit(-1)
        

    # Check if Log file exists and is writabe
    if not os.access(LogFileName, os.W_OK) and os.path.isfile(LogFileName):
        print "Cannot open Log file: " + LogFileName + " in append mode (permisssion?)"
        sys.exit(-1)
        
        #print "StopScript = " + str(StopScript)
        #print "VmwareBooted = " + str(vmWareBooted)
        # check that no other processes with Vmware.Py running 
        #print "Script prima ps= "
        
    # Check if another "BuildDistro.py" process is running 
    print "Check if another program is running...."
    command = 'ps -ef |grep ' + currentName + ' | grep python |grep -v grep'
    print "Command ran: \n" + command 
    psout = os.popen( command, 'r')
    for machine in psout:
        #print "After ps= " + str(StopScript)
        pid = machine.split()[1]
        if pid != str(currentPid):
            print currentName + " processes  running with pid " + pid
            StopScript = True
            #print "Script = " + str(StopScript)
            if StopScript:
                print "Please Check why an instance of " + currentName + " script is still running"
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


        # Check that same script is not running
    if not os.path.isfile(LockFile):
        lock = open(LockFile, "w")
        # Check if you can write lock file
        if not lock:
            print "Cannot open lockfile " + LockFile + " for writing"
            sys.exit(-1)
        lock.write(str(currentPid));
        lock.close
    else:
        print "***************************************************************"
        print "A lock file has been found under  " + LockFile
        print "***********************************************************************************"
        lock = open(LockFile, "r")
        if lock:
            pid = lock.read();
            print "According to the lock file  there should be a process running with  " + pid + " Pid"
            print "Please kill the process and remove the PID manually before running the script again"
            sys.exit(-1)
        else:
            print "I cannot read lockfile  " + LockFile + " Permission problems? please check before running the script again"
            sys.exit(-1)
    
    # Change Dir to sqaBaseCamp
    if (not os.path.isdir(BaseDir)) or (not os.access(BaseDir, os.W_OK)) :
        print "base directory " + BaseDir + " directory is not existing or it is not accessible"
        os.remove(lockfile)
        sys.exit(-1)
    os.chdir(BaseDir)

###############################################################################################
def menu(Vmware):
    print "Main Menu to build ACS version on different distributions"
    print "Options are:\n"
    # Build the menu
    answ = []
    for vm in Vmware.keys():
        answ.append(vm)
        print vm + ') ' + Vmware[vm]['name'] + " : " + Vmware[vm]['desc']  
    
    print "All Distributions : 0"
    print "\nPlease select one option: (q to quit)\n"
    # Careful: it reads all the strings but only the first character goes into answer
    char = sys.stdin.read(1)
    
    # Convert all to lowercase
    char = char.lower()
    
    # Alphanumeric character?
    if not char.isalpha():
        print "Please enter an character"
        sys.exit(-1)
    
    if char == 'q':
        print "Quitting"
        sys.exit(0)
        
    if char == '0':
        print "All Distributions"
        sys.exit(0)
    
    for c in answ:
        if char == c:
            return(char)
    print "Sorry but the character  \"" +char + "\" is not a valid answer\n"###############################################################################################
    
#####################################################################
def options(configFile):  
    # 
    import getopt
    
    
    # without options the default is to parse and execute all images 
    # in the configuration file
    answer = "all"
    newConf = False
    allVM   = False
    distClean = True
    
    # Check number of parameters passed
    nparams = len(sys.argv)
    
    #print "Parameters = " + str(nparams)
    # IF no parameters give then menu
    if nparams == 1:      
        allVM = False
        return(configFile, allVM, distClean)  

    try:
        # Possible options 
        # -a or --all : Choose all machines in the configuration files
        # -c or --conf <confFile> : Select a different configuration file
        # -h or --help
        opts, args = getopt.getopt(sys.argv[1:], "ac:nh", ["all","conf=", "noclean", "help"])
    except getopt.GetoptError:
        # print help information and exit:
         usage()
         sys.exit(2)


    for input,param in opts:
        if input in ("-a", "--all"):
            allVM = True
            
        if input in ("-c", "--conf"):
            conf = param
            # Check if configuration file exists and it is readable
            if os.access(conf, os.R_OK) and os.path.isfile(conf):
                configFile = param
            else:
                print "The configuration File: ", param, " is not a file or is not readable"
                usage()
                sys.exit(-2)
                
        if input in ("-n", "--noclean"):
            distClean = False

        if input in ("-h", "--help"):
            usage()
            sys.exit(-2)
    
    return(configFile, allVM, distClean)
    # If it gets there there is some error
    print " You shoudn't get here - Check the code"
    sys.exit(-2)
    
def usage():
    print "******************************************** Usage *************************************"
    print "Usage is :"
    print ""
    print sys.argv[0] + " without parameters shows a menu where you can select single distribution to build"
    print "\t -------------------------------------------------------------------------------"
    print "\t -a or --all : build all VMs"
    print "\t -c or --conf <configuration File> : Use a different Confguration file (not /acsbuild/etc/buildAcs.conf)"
    print "\t -n or --noclean : Do not clean the Distribution directory"
    print "\t -h or --help : print this menu"
    sys.exit(-2)
    
###############################################################################################   
def retrieveSoftware(buildDir, cvsroot, cvspath, cvstag, Live):
    # Retrieve software from CVS
    print "Start of Retrieving software ...."
    log.logMsg("Start of Retrieving software ....")
    if Live:
        # Create the build rirectory if it does not exist already
        if not os.path.exists(buildDir):
            os.mkdir(buildDir)
        os.chdir(buildDir)
        log.logMsg("Here we start from the " + buildDir + " Directory")
        print "Here we start from the " + buildDir + " Directory"
        command = 'rm -rf ACS'
        log.logMsg("Removing ACS directory -> " + command)
        print "Removing ACS directory -> " + command
        os.system(command)
        command = 'cvs -d ' + cvsroot + ' co ' + '-r ' + cvstag + ' ' + cvspath
        log.logMsg("Start Retrieving Software: Executing Command -> " + command)
        print "Start Retrieving Software: Executing Command -> " + command
        status = os.system(command)
        if status:
            log.logMsg(" >>> Failed to retrieve software " + command)
            log.logMsg("End of Retrieving Software Command -")
            print " >>> Failed to retrieve software " + command
            print "End of Retrieving Software Command -"

################################################################################################
################################################################################################            
# Here The main starts
#

# Some important debug variables

Live = True

# if the script have to boot vmware machine
vmWareBooted = False

# Default Configuration File can be changed with "-c" option
defaultConfigFile = '/acs_build/etc/buildACS.conf'

# If the Distribution directory has to be cleaned up
# and rebuild from cratch from CVS.
# Any previous build gets deleted in this case.
# Default is True

(configFile, allVM, distClean) = options(defaultConfigFile)


print "Config File in Use: ", configFile
(buildDir, cvsPath, cvsTag, baseDir, VmwareDir, port, cUser, Vmware) = readConf(configFile)

if not allVM:
    answer = menu(Vmware)
else:
    answer = "all"
    
#answer = 'd'
#print "Answer is " + answer
# Define Log File
logDir = baseDir + "/log/"
distroLogFile = logDir + "BuildDistro"
log = LogFile(distroLogFile)
# Set Up the correct CVSROOT environment for retrieve
cvsroot = ':pserver:almamgr@cvssrv.hq.eso.org:/project2/CVS'
os.environ['CVSROOT'] = cvsroot

# Where ACS is build
fullBuildDir = baseDir + '/' + buildDir

# Set Up Lock File
lockFile = "/tmp/BuildDir.lock"

# Recall File name from the Log module
logFileName = log.LogFileName

print "logDir = " + logDir + " Log File Name = " + logFileName

# Do many checks about directories, and files
# example if LogFile exists, is writable, if a lock file is already present
checkDir( baseDir, logDir, logFileName, vmWareBooted, lockFile)

# Retrieve software via cvs# if distClean is False
if distClean:
    print "Clean distribution!"
    retrieveSoftware(fullBuildDir, cvsroot, cvsPath, cvsTag, Live)
else:
    print "No Clean"
    
# hostName,hostName ConfName, makeFileOpts are lists
index    = []
hostName = []
ConfName = []
makeFileOpts =[]
VmwareConfigFile = []
print "You chose the following data"

if answer == "all":
    vm = sortedDictValues(Vmware)
    for machine  in vm:
        hostName.append(machine['host'])
        ConfName.append(machine['name'])
        VmwareConfigFile.append(machine['vmwareconf'])
        makeFileOpts.append(machine['makefileopts'])
    print "Menu Entry   = " + str(index)
    print "Hostnames    = " + str(hostName)
    print "ConfNames    = " + str(ConfName)
    print "Vware Conf   = " + str(VmwareConfigFile)
    print "Make Options = " + str(makeFileOpts)
  
else:
    hostName.append(Vmware[answer]['host'])
    ConfName.append(Vmware[answer]['name'])
    VmwareConfigFile.append(Vmware[answer]['vmwareconf'])
    makeFileOpts.append(Vmware[answer]['makefileopts'])
    print "Configuration Name = " + str(ConfName)
    print "Make File options = " + str(makeFileOpts)
    print "Host Name = " + str(hostName)

# Iterate over  or more machines
nVms = len(hostName)
currentVM = 0 
for vmWare in VmwareConfigFile:
    print '======================================================================================================================================'
    log.logMsg("====================================================================================================================================") 
    print "Machine number " + str(currentVM + 1)
    log.logMsg("Machine number " + str(currentVM + 1))
    
    # Choose the right Vmware machine to boot
    vmPath = VmwareDir + vmWare
    print "Vmware Configuration File = " + vmPath
    conf = ConfName[currentVM]
    host = hostName[currentVM] 
    makeOpts = makeFileOpts[currentVM]
    
    # Now boot the VM, if the vmWarerBooted flag has not been set
    if not vmWareBooted:
        print "Now Booting the VM " + host + ".... "
        log.logMsg("Now booting the VM " + host + ".... ") 
        command = '/usr/bin/vmware -xq ' + vmPath + ' &'
        if Live: 
            log.logMsg("Live option enabled")
            log.logMsg('Executing Command -> ' + command)
            print 'Executing Command -> ' + command
            status = os.system(command)
            if status:  
                log.logMsg("Failed to launch virtual machine with config file"  +  vmPath )

		# Wait until Vmware machine is up : 
		# At the moment t simply checks every 5 seconds if some process is listening on port 22 (ssh)
		# To be changed when we'll have some vmware API to use 
        log.logMsg("Waiting until  Vmware machine is up ...")
        if Live: 
            while True:
                if  checkVmwareUp(host, port):
                    log.logMsg("******** Host " + host + " is up ***********")
                    break
                else:
                    log.logMsg("Waiting... for Host " + host)
                    time.sleep(5)
    currentVM = currentVM +1
    # here start the command preparation
    feedbackport = 10000
    now = timeStamp()
    buildLogFile = logDir + host + "/make-" + conf + "-" +  str(now)
    command = "cd " + fullBuildDir + cvsPath + " ; " + "/usr/bin/make " + makeOpts + " >& " + buildLogFile + " ; telnet vmware_factory " + str(feedbackport)  + "\n"
    print command
    executeCommand( command, host, port)
                    
                    
    # Start Listening on port "feedbackport" until Claro2 is finished
    log.logMsg("Here start checkBuildEnd routine on port " + str(feedbackport) )
    print "Here start checkBuildEnd routine on port " + str(feedbackport) 
    print "FeedbackPort = " + str(feedbackport)
    checkBuildEnd(feedbackport)
    log.logMsg("Here stop checkBuildEnd on port " + str(feedbackport) )
    print "Here stop checkBuildEnd on port " + str(feedbackport) 

    # Log on the Web when build finished
    # status = False
    # timestamp = log.timeStamp()
    # generateStatusPage(host, status, timestamp, sub, item, feedback, LogFileWeb )
    
    # Shutdown the VM
    if not vmWareBooted:
        log.logMsg("Now Shutting Down the VM .... ")
        print "Now Shutting Down the VM .... "
        command = '/usr/bin/ssh -q -l root ' + host + ' /sbin/shutdown -h now'
        if Live: 
            log.logMsg("Executing Command -> " + command)
            print "Executing Command -> " + command
            status = os.system(command)
            if status:  
                log.logMsg("Failed to ShutDown virtual machine with config file" + vmPath + " Status = " + status)
                print "Failed to ShutDown virtual machine with config file" + vmPath + " Status = " +  status
            # added because with RTOS kernel every power managment feature in the kernel is gono
                        # vmware machine cannot poweroff so vm machines are always up
            # Workaround: wait 5 minutes and then kill the process
            print "Waiting 3 minutes before killing the " + host + " virtual machine"
            log.logMsg("Waiting 3 minutes before killing the " + host + " virtual machine")
            time.sleep(60)
            
            log.logMsg("3 minutes passed: Executing the following command to identify processes to kill: ")
            print "3 minutes passed: Executing the following command to identify processes to kill: "
            command = 'ps -fu ' + cUser + ' | grep vmware | grep -v grep '
            print command
            log.logMsg(command)
            psout = os.popen(command, 'r')
            
            for machine in psout:
                pid = machine.split()[1] 
                print "killing Process n.:" + pid
                log.logMsg("Killing process n. " + pid)
                print "Killing process n. " + pid
                os.system("kill -9 " + pid)
    print '======================== End machine =========================================================================================='
    log.logMsg("=========================End Machine =========================================================================================================") 
print '======================================================================================================================================'
print 'Script BuildDistro.py terminated, removing lock file'
log.logMsg('======================================================================================================================================')
log.logMsg('Script BuildDistro.py terminated, removing lock file')
os.remove(lockFile)
print '======================================================================================================================================'
log.logMsg('======================================================================================================================================')

# __oOo__
