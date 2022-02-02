#!/usr/bin/env python

# acaproni	30/10/2003	created
#acaproni	05/11/2003	added the classpath option
#acaproni	26/11/2003	added the check for the signature of a jar file and the -f option

"""
This script reads each jnlp input file to find the .jar file names.
Then each .jar files is read from the input directory, signed and stored in the destination directory.
If the .jar file already exists then its date is compared with the date of the source file before copying and signing

When a file is not found and the -classpath=CLASSPATH option is used then it is searched into the CLASSPATH string before
returning an error


The functioning of the script is:
1. check the command line parameteres
   1.1 check the existence of each source file (jnlp) and directory
2. for each jnlp file
   2.1 build the list of the .jar files specified in jnlp file itself
   2.2 for each jar files
       2.3 check for the existence of the jar file into the source directory
       2.4 check if the dest file already exists and its date is older then that of the source file
       2.5 check if the jar file is already signed
       2.6 sign the jar file (if requested)
       2.7 copy the signed jar file into the dest directory
"""
import sys,string,stat,os
import xml.parsers.expat

def Usage(name):
	""" This function only prints the classic usage"""
	print 'USAGE: '+name+' {-switches} -keystore=file -storepass=passwd -alias=alias -classpath=CLASSPATH [jnlp file] sourceDir destDir'
	print 'Read a list of jar files from the jnlp (xml) files'
	print 'Then this files are signed and copied in the destDir'
	print 'The dest file is overwritten only if the source file is newer then the dest file'
	print "A check about the signature is made before signing each jar file"
	print 'Switches:'
	print '\tp: only print actions (does nothing)'
	print '\ts: do not sign the jar files'
	print '\tv: verbose output'
	print '\tn: do not check the dates of the dest and source jar'
	print '\tf: do not check if the jar file is already signed before signing'
	print '\th: print this help'
	print 'sourceDir: the directory where the source .jar files are stored'
	print 'destDir: the directory where the destination jar signed file are to be stored'
	print 'If -classpath=CLASSPATH is used then whenever a jar file is not found it is searched in the CLASSPATH string'
	print 'If one or more -include=dir option is present, the jar files is searched into the dir directory'
	print 'For -keystore file, -storepass passwd and -alias alias see the jarsigner man pages'

def isToUpdate(file,outfile):
	""" Check if the source file must be copied in the outdir
	The check is made by checking the existence of the destination files
	and comparing the dates"""
	if os.access(outfile,os.F_OK)==1:
		outDate=os.stat(outfile)[stat.ST_MTIME]
	else:
		outDate=0
	inDate=os.stat(file)[stat.ST_MTIME]
	if outDate<=inDate:
		return 1
	else:
		return 0

def isAlreadySigned(file):
	"""Check if the jar file is already signed
	The check is made by executing jarsigner with the -verify switch against the file
	and scanning the output for the "jar verified." string"""
	cmd='jarsigner -verify '+file+' >./jarsigner.tmp'
	os.system(cmd)
	#Read the output
	f = open("./jarsigner.tmp","r")
	data = f.read()
	f.close()
	os.remove('./jarsigner.tmp');
	if string.find(data,'jar verified.')!=-1:
		return 0
	else:
		return 1

def searchInClassPath(file):
	"""Search if class path exists inside the classpath
	If the file is found return the new name"""
	newFile=''
	if pars.searchClassPath()!=1:
		return 0
	files= pars.getClassPathFiles()
	for t in files:
		y = string.split(t,'/')
		if y[len(y)-1]==file:
			newFile=t
	return newFile

def searchInIncludeDirectories(file):
	"""Search for file in une of the include directories
	If the file is found return the new name"""
	if pars.searchIncludeDirs()==0:
		return ''
	dirs = pars.getIncludeDirs()
	for dir in dirs:
		if os.access(dir+'/'+file,os.F_OK)==1:
			return dir+'/'+file
	return ''

def searchSourceFile(file):
	"""Search for the given file
	It first tries to access the file, then tries in the include dirs or in the classpath
	Return the new file name or an empty string"""
	newFile=''
	if string.find(file,'/')==-1:
		jarFileName=file
	else:
		splittedFileName=string.split(file,"/")
		jarFileName=splittedFileName[len(splittedFileName)-1]
	# Check if the source file exists
	if os.access(file,os.F_OK)==1:
		return file
	# Check into the include directories
	if pars.searchIncludeDirs():
		newFile=searchInIncludeDirectories(jarFileName)
		if newFile!='':
			return newFile
	if pars.searchClassPath():
		# Try to search in $CLASSPATH
		newFile=searchInClassPath(jarFileName)
	return newFile
	
class Params:
	"""The object of this class manages (read, check and return) the parameters
	on the command line"""

	def __init__(self,cmdLine):
		"""The constructor
		the parameter is the command line list"""
		#The jnlp (xml) input files
		self.jnlpFiles=[]

		#The name of the program
		self.prgName=''

		#The input and output directories
		self.inDir=''
		self.outDir=''
		
		# Read the program name
		pname = string.split(cmdLine[0],'/')
		self.prgName=pname[len(pname)-1]

		# If an error is encountered while reading the parameters of
		# the command line then this variable is changed
		self.error=''

		#The parameters for jarsigner
		self.keystore=''
		self.storepass=''
		self.alias=''

		# The variables to search for the jar files
		self.classpath=''
		self.includeDirs = []

		#Initialize the dictiony of the switches with default values
		self.switches = {
			"sign" : 1, # The jar files are to be signed
			"verbose" : 0, #Verbose output
			"print" : 0, # Only print actions (do nothing)
			"check" :1, # Check the date before updating a jar file
			"checkSignature" : 1, #Check if a file is already signed before signing
			"help" : 0, # Help requested
		}

		# Scans all the word in the command line
		for a in range(1,len(cmdLine)):
			str = cmdLine[a] # The parameter 
			if string.find(str,'=')!=-1:
				splittedstr=string.split(str,'=')
				par=splittedstr[0]
				val=splittedstr[1]
				if par=='-keystore':
					self.keystore=val
					# Check if the file exists
					if os.access(self.keystore,os.F_OK)!=1:
						self.error='Keystore file not found ('+self.keystore+')'
						return
				elif par=='-storepass':
					self.storepass=val
				elif par=='-alias':
					self.alias=val
				elif par=='-classpath':
					self.classpath=val
				elif par=='-include':
					self.includeDirs.append(val)
				continue
			if str[0]=='-' and len(str)>1:
				# One or more switches
				for t in range(1,len(str)):
					if str[t]=='p':
						self.switches["print"]=1
					elif str[t]=='n':
						self.switches["check"]=0
					elif str[t]=='v':
						self.switches["verbose"]=1
					elif str[t]=='s':
						self.switches["sign"]=0
					elif str[t]=='f':
						self.switches["checkSignature"]=0
					elif str[t]=='h':
						self.switches["help"]=1
					else:
						self.error='Unrecognized switch ('+str[t]+')'
						return
				continue
			# The word is not a switch so it may be a jnpl (xml) or a directory
			# Check if the file or directory exists
			if os.access(str,os.F_OK)!=1:
				self.error='File or directory not found: ('+str+')'
				return
			else:
				# Check if the parameter is a directory	name
				mode=os.stat(str)[stat.ST_MODE]
				if stat.S_ISDIR(mode):
					if self.inDir=='':
						self.inDir=str
						if self.inDir[len(self.inDir)-1]!='/':
							self.inDir=self.inDir+'/'
					elif self.outDir=='':
						self.outDir=str
						if self.outDir[len(self.outDir)-1]!='/':
							self.outDir=self.outDir+'/'
					else:
						self.error='Too many directories in command line ('+str+')'
				else:
					# This is a file (no check is done here about its format - xml)
					self.jnlpFiles.append(str)

	def CheckParams(self):
		"""Check if there are errors on the parameters in the command line
		In this version the constructor checks for some errors and set may set the error variable

		Return an empty string if the parameters are right otherwise a brief explanation of the error"""
		# Inhibit all the error messages if the user requests help
		if self.switches["help"]==1:
			return ''
		if self.error!='':
			return self.error
		# Make some other check
		if (self.keystore=='' or self.storepass=='' or self.alias=='') and self.switches["sign"]==1:
			self.error='Invalid parameters for jarsigner'
		return self.error

	def verbose(self):
		return self.switches["verbose"]
	def sign(self):
		return self.switches["sign"]
	def printOnly(self):
		return self.switches["print"]
	def checkDate(self):
		return self.switches["check"]
	def checkSignature(self):
		return self.switches["checkSignature"]
	def printHelp(self):
		return self.switches["help"]

	def searchClassPath(self):
		if self.classpath=='':
			return 0
		else:
			return 1

	def getClassPathFiles(self):
		return string.split(self.classpath,':')

	def searchIncludeDirs(self):
		if len(self.includeDirs)==0:
			return 0
		else:
			return 1

	def getIncludeDirs(self):
		return self.includeDirs

	def getCmdName(self):
		return self.prgName

	def getInputFiles(self):
		return self.jnlpFiles

	def getSourceDir(self):
		return self.inDir
	def getDestDir(self):
		return self.outDir

	def getKeystore(self):
		return self.keystore
	def getStorepass(self):
		return self.storepass
	def getAlias(self):
		return self.alias

class jnlpInputFile:
	"""Each input file is an object of this class
	A jnlp file is an xml that contains a list of jar files (in the tag jar)"""

	def __init__(self,name):
		"""The constructor
		name is the name of the jnlp file"""
		# The name of the file
		self.fileName=name
		# The list of the file
		self.jarFilesList=[]
		self.readFile()

	def startElemHandler(self,name,attrs):
		if name=='jar':
			self.jarFilesList.append(attrs["href"])

	def endElemHandler(self,name):
		pass

	def readFile(self):
		"""Read the xml file and create the list of the jar files"""
		# Open the file
		f = open(self.fileName,"r")
		data = f.read()
		f.close()
		# Init the parser
		p = xml.parsers.expat.ParserCreate()
		p.StartElementHandler = self.startElemHandler
		p.EndElementHandler = self.endElemHandler
		# Parse the file
		p.Parse(data)

	def getJarFiles(self):
		return self.jarFilesList

pars=Params(sys.argv)
cmdLineError=pars.CheckParams()
if cmdLineError!='':
	print 'ERROR: '+cmdLineError
	sys.exit(-1)

# The switch -h is activated?
if pars.printHelp():
	Usage(pars.getCmdName())
	sys.exit(0)

if pars.verbose():
	# Print some info
	print '\nVerbose mode ON'
	print 'Files to process:',pars.getInputFiles()
	print 'Source dir   :',pars.getSourceDir()
	print 'Include dirs :',pars.getIncludeDirs()
	print 'Dest dir     :',pars.getDestDir()
	if pars.searchClassPath():
		print 'Using CLASSPATH:',pars.getClassPathFiles()
	if pars.sign():
		print 'jarsigner, keystore:',pars.getKeystore()
		print 'jarsigner, storepass:',pars.getStorepass()
		print 'jarsigner, alias:',pars.getAlias()
		print 'Dest files will be signed'
	else:
		print 'Dest files will NOT be signed'
	if pars.printOnly():
		print 'Print actions only (do nothing)'
	if pars.checkDate()==0:
		print 'Check of dates inhibited'
	if pars.checkSignature()==0:
		print 'Check of signatures inhibited'

for inFile in pars.getInputFiles():
	if pars.verbose():
		print 'Processing',inFile,'...'
	jnlp=jnlpInputFile(inFile)
	for jarFile in jnlp.getJarFiles():
		if pars.verbose():
			print '\t',jarFile
		# Check if the source file exists
		file=searchSourceFile(pars.getSourceDir()+jarFile)
		if file=='':
			print 'File ',pars.getSourceDir()+jarFile,'requested by',inFile,'not found!'
		else:
			if pars.verbose():
				print '\t\tfound',file
			if (pars.checkDate() and isToUpdate(file,pars.getDestDir()+jarFile)) or not pars.checkDate():
				# build the command
				if (not pars.sign()) or (pars.checkSignature() and not isAlreadySigned(file)):
					# Copy the source into the dest
					cmd = 'cp '+file+' '+pars.getDestDir()+'lib'
				else:
					cmd ='jarsigner -signedjar '+pars.getDestDir()+jarFile+' -keystore '+pars.getKeystore()+' -storepass '+pars.getStorepass()+' '+file+' '+pars.getAlias()
				if pars.printOnly() or pars.verbose():
					print '\t\t',cmd
				if not pars.printOnly():
					os.system(cmd)
			else:
				if pars.verbose():
					print '\t\tNo update needed'
				
	if pars.verbose():
		print '...done.'
