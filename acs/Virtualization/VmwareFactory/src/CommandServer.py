
import sys, thread, socket, os, signal

from Common import LogFile

# Global Definitions
HOST='' 
# Server listening on PORT 62000
PORT=62000
# Number of Pending Connections
NPENDCONN=2
# Maximum numbers of Character that can be received in one shot
MAXNUMCHARS=1024
# Shared area 
SOURCE = '/acs_build/'

class Server:
	
	def __init__(self):
		# Define logMsg filename
		self.logDir = '/tmp/NRI/'
		self.serverlogMsgFileName = self.logDir + 'ServerDaemon.log' 
		self.logFile = LogFile(self.serverlogMsgFileName)
		self.logFile.logMsg("Here Start the Server listening on Port: " + str(PORT))
		#print "logMsgFileName  = " + self.serverlogMsgFileName
###################################################################################
	def serverLoop(self, socket):
		while 1:
			#print 'Entering the loop'
			# Receive a Maximum of MAXNUMCHARS characters
			commandLine = ''
			while  commandLine == '' or commandLine[ len(commandLine) - 1 ] != '\n':
				temp = socket.recv(MAXNUMCHARS)
				if len(temp)==0:
					return
				commandLine += temp
			commandLine = commandLine.rstrip()
			self.logFile.logMsg("Executing Command line received :" + commandLine)
	
			#inFile = os.popen(commandLine,mode='r', bufsize='1')
			(outFile,inFile) = os.popen4(commandLine,'r',-1)
			
			outputLines = ""
			if inFile != None:
				outputLines = inFile.read()
	
			if inFile != None:
				ret = inFile.close()
			self.logFile.logMsg("Command Executed")
		socket.close()




	def serverLoop2(self, socket):
		self.logFile.logMsg( 'Entering the loop')
		while 1:
			self.logFile.logMsg( 'Loop iteration del minchio')
			# Receive a Maximum of MAXNUMCHARS characters
			commandLine = ''
			while  commandLine == '' or commandLine[ len(commandLine) - 1 ] != '\n':
				temp = socket.recv(MAXNUMCHARS)
				self.logFile.logMsg("socket.recv:" + temp)
				if len(temp)==0:
					socket.close()
					self.logFile.logMsg( 'Socket Closed')
					break
				else:
					commandLine += temp
					if commandLine[ len(commandLine) - 1 ] == '\n':
						break;
					else:
						self.logFile.logMsg( 'suka')
				#self.logFile.logMsg( 'last '+commandLine[ len(commandLine) - 1 ]+" int "+int(commandLine[ len(commandLine) - 1 ]))
				self.logFile.logMsg( 'Temp cmdLine ['+commandLine+']')
     		commandLine = commandLine.rstrip()
        	self.logFile.logMsg("Executing Command line received :" + commandLine)
         	(outFile,inFile) = os.popen4(commandLine,'r',-1)
          	outputLines = ""
           	if inFile != None:
        	    outputLines = inFile.read()
        	if inFile != None:
        		ret = inFile.close()
          	self.logFile.logMsg("Command Executed")
       
###################################################################################
	def startServer(self):

		self.logFile.logMsg("Starting Daemon")
		self.logFile.logMsg("Now Printing the full environment ")
		for key in os.environ.keys():
			self.logFile.logMsg(key + "=" + os.environ[key])
		# Create an Internet Socket
		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		# Bind to the right port
		s.bind((HOST,PORT))
		# Listen but allows only NPENDCONN Pending Connections
		while 1:
			s.listen(NPENDCONN)
			# Get a Connection
			client, addr = s.accept()
			self.logFile.logMsg( "New connection from IP " + addr[0] + " Port " + str(addr[1]) )
			thread.start_new_thread(self.serverLoop,(client,) )						    
		

# Here starts the main
# Define logMsg directory 
if __name__ == '__main__':
	
	server = Server()
	server.startServer()

