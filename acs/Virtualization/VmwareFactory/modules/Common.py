import time

class LogFile:
    def __init__(self , LogFileName):
        self.LogFileName = LogFileName + '-' + self.timeStamp() 
        
        #print "LogFile Name = " + self.LogFileName
        
    def timeStamp(self):
        # Get a current Time Stamp - Format 
        item = time.localtime()
        data = "%02d.%02d.%d-%02d.%02d.%02d" % (item[2], item[1], item[0], item[3],item[4],item[5])
        return(data)
        
    def logMsg(self, message):
        #print "Enterstring = "
        # Open logMsg file and write message string to it appending a time stamp
        TM = self.timeStamp()
        # Prepare logMsg line
        s = "%(timestamp)s %(msg)s\n" % { "timestamp": TM, "msg": message }
        #print "String = " + s
        # Open Log file in append mode, write message and close it
        self.logFile = open(self.LogFileName, "a")
        self.logFile.write(s)
        self.logFile.close()
        
if __name__ == '__main__':
    logfile = LogFile("/tmp/log.txt")
    logfile.logMsg("test")
    del(logfile)