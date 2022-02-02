import Common, ParseConfig
import os, sys

global PORT, MAXNUMCHARS
global lockfile
global SOURCE, feedback, Live, vmWareBooted, rUser, cUser
global currentPid, ConfigFile, Machines, Subsystem
global LogDir, LogdirVM, LogFileName, LogFile, Claro2

# Server listening on PORT 62000

PORT = 62000

# Maximum numbers of Character that can be received
MAXNUMCHARS = 256

# Lock file - only one version of the scritp is allowed to run
lockfile = '/tmp/VmWarepy.lock'

# Directory exported to Vmware machines
SOURCE = '/acs_build/'

feedback    = '22222@vmware_factory'
 
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

# Main Claro2 path 
 #Claro2  = "/home/vltuser8/sqam/scripts/crontabs/Claro2 -nofork "
Claro2  = SOURCE + "src/Claro2 "
# Define Log directory and check if is writable
LogDir = SOURCE + 'log/'
    
# Get Current Process id
currentPid = os.getpid()

# Read Configuration File and parse it
ConfigFile = SOURCE + "/etc/NRIVMWare.xml" 
#  Check if Config file exists
if ( not os.path.isfile( ConfigFile ) ) or ( not os.access( ConfigFile, os.R_OK ) ) :
    print "ConfigFile " + ConfigFile + " is not existing or it cannot be open in read mode"
    sys.exit( -1 )
# Parse XML Configuration file
Machines, Subsystems = ParseConfig.ReadConfigFile(ConfigFile)

  
# Define Log filename
LogFileName = LogDir + 'VmWarePy.' + str( currentPid )
#Common.LogFileName = LogFileName

#return currentPid, Machines, Subsystems, LogFileName
   
