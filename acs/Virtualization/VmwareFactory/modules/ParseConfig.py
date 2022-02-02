#!/usr/bin/python


# XML Parser
import xml.parsers.expat

# OS and SYS Modules
import os, sys

def char_ConfigFile(data):
	"""Handler for parsing with expat"""
	pass

def start_ConfigFile(name, attrs):
	"""Handler for parsing with expat"""

 	if name == "NRIVM":
		RootElement = 'NRIVM'
	elif name == "machines":
		pass
	elif name == "host":
		Machines.append(attrs)
	elif name == "subsystems":
		pass
	elif name == "subsy":
		#print "Subsystem =", name
		Subsystems.append(attrs)
	else:
		print "Name =", name
		print "Error: bad XML Config file"
			

def end_ConfigFile(name):
	"""Handler for parsing with expat"""
	#print "END: \tname=", name 

def ReadConfigFile(xmlConfigFile):

	# Few Global Variables
	global RootElement, Subsystems, Machines 

	Subsystems = []
	Machines   = []
	
	
	# Create new Parser Object
	parser = xml.parsers.expat.ParserCreate()
	
	# Set the Handlers
	parser.StartElementHandler  = start_ConfigFile
	parser.EndElementHandler    = end_ConfigFile
	parser.CharacterDataHandler = char_ConfigFile
	# Open the XML configuration file
	Config = open(xmlConfigFile, 'r')
	# Start parsing
	parser.ParseFile(Config)

	return Machines, Subsystems
