/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 *
 *    Created on Sep 21, 2005
 *
 */
package alma.acs.xmlfilestore.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import alma.acs.component.ComponentImplBase;
import alma.acs.component.ComponentLifecycleException;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogLevel;
import alma.acs.xmlfilestore.common.QueueFileHandler;
import alma.acs.xmlfilestore.common.amqpHandler;
import alma.acs.xmlfilestore.common.kafkaHandler;
import alma.cdbErrType.CDBFieldDoesNotExistEx;



import com.cosylab.logging.engine.ACS.LCEngine;

import alma.xmlFileStore.LogsXmlStoreOperations;

/**
 * @author hmeuss
 * 
 * Important notice:
 * 
 * All methods, which are used to process incoming logs have to be added to
 * methodsNotLogged in class. alma.xmlstore.LoggerImpl.LoggerHelper. (In the
 * moment, this is xmlEntryReceived.) In addition, the method
 * requiresOrbCentralLogSuppression() in alma.xmlstore.LoggerImpl.LoggerHelper
 * must be overwritten, so that it returns true. If this is not done, we will
 * run into a vicious circle of log messages.
 * 
 *  
 */
public class XmlFileStoreLoggerImpl extends ComponentImplBase implements LogsXmlStoreOperations
	{
	
	/**
	 * Container services
	 */
	private ContainerServices cs;
	
	/**
	 * The logger
	 */
	private Logger m_logger;
	
	/**
	 *  The logging client engine that connects to the logging client
	 */
	private LCEngine engine;
	
	/**
	 * The name of the java property to set the log level
	 */
	static public final String MINLOGLEVEL_PROPNAME = "alma.acs.extras.archivelogger.minloglevel";
	
	/**
	 * The default min log level
	 */
	static public final int DEFAULTMINLOGLEVEL = AcsLogLevel.INFO.intValue();
	
	/**
	 * The name of the java property to set the max size of each file of logs
	 */
	static public final String MAXFILESIZE_PROPNAME = "alma.acs.extras.archivelogger.maxFileSize";
	
	/**
	 * The default max size of each file of logs
	 */
	static public final int DEFAULTMAXFILESIZE = 134217728;
	
	/**
	 * The name of the java property to set the max number of file of logs
	 */
	static public final String MAXNUMBEROFFILES_PROPNAME = "alma.acs.extras.archivelogger.maxNumberFiles";
	
	/**
	 * The default max number of file of logs
	 */
	static public final int DEFAULTMAXNUMBEROFFILES = 750;
	
	/**
	 * The name of the java property to set the path to save the file of logs
	 */
	static public final String LOGDIR_PROPNAME = "alma.acs.extras.archivelogger.logDir";
	
	/**
	 * The default max number of file of logs
	 */
	static public String DEFAULLOGDIR = "/mnt/gas02/data1/AcsLogs-8.1";
	
	/**
	 * The log level is initialized from the value assigned to the 
	 * {@link #MINLOGLEVEL_PROPNAME} java property or to the value of
	 * {@link #DEFAULTMINLOGLEVEL}
	 */
	private final AtomicInteger archiveLogLevel = 
			new AtomicInteger(Integer.getInteger(XmlFileStoreLoggerImpl.MINLOGLEVEL_PROPNAME, XmlFileStoreLoggerImpl.DEFAULTMINLOGLEVEL));

    private static String BUILDTAG = "";
	
	String isamqpEnabled = "false";
	static public Boolean amqpConnectionEnabled = true;
	String iskafkaEnabled = "false";
	static public Boolean kafkaConnectionEnabled = true;
	/**
	 *  Constructor
	 */
	public XmlFileStoreLoggerImpl() {
		super();
	}
    
	private void setBUILDTAG(){
		String filePath = System.getenv("ACSROOT") + "/BUILD_TAG";
		try{
			BufferedReader br = new BufferedReader(new FileReader (filePath));
			StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        line = br.readLine();
		    }
		    BUILDTAG = sb.toString().replace("\n", "");
		    br.close();
		    
		}catch (FileNotFoundException e){
			String errorMessage = filePath + " is not available";
			BUILDTAG = "BUILDTAG not found";
			m_logger.severe(errorMessage);
			cs.getAlarmSource().setAlarm("Logging", cs.getName(), 2, true);
		}catch (IOException e ){
			String errorMessage = "There's an exception " + e.getMessage();
			BUILDTAG = "BUILDTAG not found";
			m_logger.severe(errorMessage);
			cs.getAlarmSource().setAlarm("Logging", cs.getName(), 2, true);
		}
		
	}
	/**
	 * Life cycle
	 * @see alma.acs.component.ComponentLifecycle#initialize()
	 */
	@Override
	public void initialize(ContainerServices containerServices)
			throws ComponentLifecycleException {
		super.initialize(containerServices);
		cs = containerServices;
		m_logger = cs.getLogger();
		
		try {
			//com.cosylab.CDB.DAL dal = getContainerServices().getCDB();
			com.cosylab.CDB.DAL dal = cs.getCDB();
			com.cosylab.CDB.DAO dao = dal.get_DAO_Servant("alma/" + this.m_instanceName);
			
			String logFileDir = dao.get_string("logFileDir");
			//should be true or false (notice lower case)
			m_logger.info("log dir from CBD: " + logFileDir );
			DEFAULLOGDIR = logFileDir;
			
			String logFilePath = System.getProperty(XmlFileStoreLoggerImpl.LOGDIR_PROPNAME, XmlFileStoreLoggerImpl.DEFAULLOGDIR);
			File f = new File(logFilePath);
			if (!f.exists()) {
				f.mkdirs();
			}
			
			int fileMax = Integer.getInteger(XmlFileStoreLoggerImpl.MAXNUMBEROFFILES_PROPNAME, XmlFileStoreLoggerImpl.DEFAULTMAXNUMBEROFFILES);
			int fileSizeLimit = Integer.getInteger(XmlFileStoreLoggerImpl.MAXFILESIZE_PROPNAME, XmlFileStoreLoggerImpl.DEFAULTMAXFILESIZE);
			if (fileMax < 1 || fileSizeLimit < 100000 ) {
				StringBuilder str = new StringBuilder(XmlFileStoreLoggerImpl.MAXNUMBEROFFILES_PROPNAME);
				str.append(" must be greater then 1 and ");
				str.append(XmlFileStoreLoggerImpl.MAXFILESIZE_PROPNAME);
				str.append(" must be greater then 100000");
				throw new ComponentLifecycleException(str.toString());
			}
			setBUILDTAG();
			StringBuilder str = new StringBuilder("Will save log files in : ");
			str.append(logFilePath);
			str.append(" (max log file size: ");
			str.append(fileSizeLimit);
			str.append(", max # log files: " );
			str.append(fileMax);
			str.append(')');
			m_logger.info(str.toString());
			
			connectToLoggingChannel(logFilePath, fileMax, fileSizeLimit);
			
			// read config from CDB to send to amqp
				dal = cs.getCDB();
				dao = dal.get_DAO_Servant("alma/" + this.m_instanceName);
			
				try{
					isamqpEnabled = dao.get_string("amqp_enabled");
					//should be true or false (notice lower case)
					m_logger.info("amqp_enabled: " + isamqpEnabled );
					m_logger.info("amqpConnectionEnabled: " + amqpConnectionEnabled );
					String amqp_user="";
					String amqp_password="";
					String amqp_host="";
					String routing_key= System.getenv("LOCATION");//"APE1";
	
		
					if (isamqpEnabled.equals("true")){
						amqp_user = dao.get_string("amqp_user");
						amqp_password = dao.get_string("amqp_password");
						amqp_host = dao.get_string("amqp_host");
						
						//lets' make a check for the routing key: it should have been set to $LOCATION
						m_logger.info("amqp is enabled. Will try to connect as: " + amqp_user + " to " + amqp_host + " using the routing_key " + routing_key );
						
						//this instance takes care of publishing messages on amqp
						amqpHandler myHandler = new amqpHandler(m_logger, containerServices, engine, amqpConnectionEnabled, amqp_host, amqp_user, amqp_password, routing_key, BUILDTAG);
						myHandler.resetConnection(m_logger);
					}
				
					//kafka
					iskafkaEnabled = dao.get_string("kafka_enabled");
					//should be true or false (notice lower case)
					m_logger.info("kafka_enabled: " + iskafkaEnabled );
					m_logger.info("kafkaConnectionEnabled: " + kafkaConnectionEnabled );
					String kafka_bootstrap_servers="";
	
	
		
					if (iskafkaEnabled.equals("true")){
						HashMap<String, String> kafkaProps = new HashMap<>();
						kafka_bootstrap_servers = dao.get_string("kafka_bootstrap.servers");
						//TMCD/CDB compatibility issue
						String allProps = dao.get_string("_attributes");
						String[] arrayProps = allProps.split(",");
						for (String eachProp:arrayProps){
							if (eachProp.startsWith("kafka_", 0) && !(eachProp.endsWith("enabled"))){
								m_logger.info("adding property "+ eachProp.replace("kafka_", ""));
								kafkaProps.put(eachProp.replace("kafka_", ""),dao.get_string(eachProp)) ;
							}
						}
						
						//lets' make a check for the topic in kafka: it should have been set to $LOCATION
						m_logger.info("kafka is enabled. Will try to connect to " + kafka_bootstrap_servers + " to the topic " + routing_key );
						
						//kafka initialization 
						kafkaHandler myKafka = new kafkaHandler(m_logger, containerServices, engine, kafkaConnectionEnabled, 
								kafkaProps, routing_key, BUILDTAG);
					}
		
				}catch (CDBFieldDoesNotExistEx e){
					String errorMessage = "could not set up amqp/kafka: "+ e.getMessage();
					m_logger.severe(errorMessage);
					cs.getAlarmSource().setAlarm("Logging", cs.getName(), 2, true);	
				}
		} catch (Throwable t) {
			String errorMessage = "could not configure component: " + t.getMessage();
			m_logger.severe(errorMessage);
			cs.getAlarmSource().setAlarm("Logging", cs.getName(), 2, true);
			throw new ComponentLifecycleException(errorMessage,t);
		}
	}

    /**
	 * 
	 * @param logFilePath
	 * @param fileMax
	 * @param fileSizeLimit
	 * @throws ComponentLifecycleException
	 */
	private void connectToLoggingChannel(String logFilePath, int fileMax,
			int fileSizeLimit) throws ComponentLifecycleException {
		try {
			// connect to LoggingChannel
			QueueFileHandler queueFileHandler = new QueueFileHandler(cs, logFilePath, fileMax, fileSizeLimit,"log","Logging");
			engine = new LCEngine(queueFileHandler);
			engine.connect(cs.getAdvancedContainerServices().getORB(), null);
			engine.enableAutoReconnection(true);
		} catch (Throwable e) {
			m_logger
					.severe("Could not initialize connection to logging channel.");
			cs.getAlarmSource().setAlarm("Logging", cs.getName(), 2, true);
			throw new ComponentLifecycleException(e);
		}
	}

	/**
	 * Life cycle
	 * @see alma.acs.component.ComponentLifecycle#cleanUp()
	 */
	@Override
	public void cleanUp() throws alma.maciErrType.wrappers.AcsJComponentCleanUpEx {
		super.cleanUp();
		if (m_logger.isLoggable(Level.FINE)) m_logger.fine("cleaning up");
		engine.close(true);
	}

	@Override
	public short getArchiveLevel() {
		return this.archiveLogLevel.shortValue();
	}

	@Override
	public void setArchiveLevel(short newLevel) {
		this.archiveLogLevel.set(newLevel);
	}
	
	public void resetAmqp() {
		isamqpEnabled = "true";
		amqpHandler.resetConnection(m_logger);
		
	}
}
