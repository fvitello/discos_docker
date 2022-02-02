package alma.acs.xmlfilestore.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.cosylab.logging.engine.LogEngineException;
import com.cosylab.logging.engine.ACS.ACSRemoteRawLogListener;
import com.cosylab.logging.engine.ACS.LCEngine;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import alma.acs.container.ContainerServices;

public class amqpHandler {
    
	/*
	 * amqp settings, read from CDB
	 */
	private static Boolean amqpConnectionEnabled = true;

	
	private final Logger m_logger;

	
	private static int triesToEnableAmqp = 0;


	/**
	 * Constructor
	 * 
	 * @param myLogger The logger
	 * @param cs the container services
	 * @param enabled: to activate/deactivate the messaging to amqp
	 * @param amqp_host;
	 * @param amqp_user;
	 * @param amqp_password;
	 * @throws LogEngineException 
	 * @throws Exception
	 *  
	 */
	public amqpHandler(Logger myLogger, final ContainerServices cs,LCEngine engine, Boolean enabled,
			final String amqp_host, final String amqp_user,final  String amqp_password, 
			final String origin, final String buildtag) throws IOException, TimeoutException, LogEngineException {
			this.m_logger = cs.getLogger();
	
			ACSRemoteRawLogListener myListener = new ACSRemoteRawLogListener() {
				
				@Override
				public void xmlEntryReceived(String xmlLogString) {
					
					try{
						if (!amqpConnectionEnabled && (triesToEnableAmqp > 3)){
							return ;
						}
						//Transform XML to JSON and sanitize
						JSONObject xmlJSONObj = XML.toJSONObject(xmlLogString);
						JSONObject properJSON = new JSONObject();
						//fix loglevel
						Iterator<?> keys = xmlJSONObj.keys();
						while (keys.hasNext()){
							String LogLeveLKey =  (String)keys.next();
							if (LogLeveLKey.equals("Debug") || LogLeveLKey.equals("Delouse") ||
									LogLeveLKey.equals("Emergency") || LogLeveLKey.equals("Error") ||
									LogLeveLKey.equals("Info") || LogLeveLKey.equals("Notice") ||
									LogLeveLKey.equals("Warning") || LogLeveLKey.equals("Trace") ||
									LogLeveLKey.equals("Critical") 
									){
								properJSON = (JSONObject) xmlJSONObj.get(LogLeveLKey);
								properJSON.put("LogLevel", LogLeveLKey);
							}else{
								// TODO: implement handling of other cases
							}
						}
						
						//append $LOCATION and BUILDTAG to the message
						properJSON.put("Origin", origin);
						properJSON.put("BuildTag", buildtag);
						
						//rename key "content" as "text"
						if (properJSON.has("content")){
							properJSON.put("Text", properJSON.get("content")).toString();
							properJSON.remove("content");
						}else{
							System.out.println("content not found");
						}
						
						//String to be published on amqp
						String jsonPrettyPrintString = properJSON.toString();
	
						//rabbitmq setup
						ConnectionFactory factory = new ConnectionFactory();
						factory.setHost(amqp_host);
						factory.setUsername(amqp_user);
						factory.setPassword(amqp_password);
						final Connection connection = factory.newConnection();
						final Channel channel = connection.createChannel();
						channel.exchangeDeclare(origin, "fanout", true);
						//rabbitmq publish
						//publish to queue
						channel.basicPublish(origin, "", null, jsonPrettyPrintString.getBytes());
						connection.close();
					}catch (IOException | TimeoutException | JSONException e){		
						m_logger
						.severe("Could not publish to amqp. Try: " + triesToEnableAmqp + ": " + e.getMessage());
						triesToEnableAmqp++;
						setAmqpConnectionEnabled(false);
					}
				}
			};
			
			engine = new LCEngine();
			engine.connect(cs.getAdvancedContainerServices().getORB(), null);
			engine.enableAutoReconnection(true);
			if (enabled){
				engine.addRawLogListener(myListener);
			} else{
				engine.close(false);
				m_logger
				.severe("Closing the Logging channel engine.");
			}

		
		
	}
	
	public static void resetConnection(Logger myLogger){
		setAmqpConnectionEnabled(true);
		setTriesToEnableAmqp(0);
		myLogger.info("Setting setAmqpConnectionEnabled to true and setTriesToEnableAmqp to 0 (it was "
		+ getAmqpConnectionEnabled().toString() + " and " + String.valueOf(getTriesToEnableAmqp()) +")");
	}
	
	
	public static Boolean getAmqpConnectionEnabled (){
		return amqpConnectionEnabled ;
	}
	
	public static void setAmqpConnectionEnabled (Boolean state){
		amqpConnectionEnabled = state;
	}
	
	public static int getTriesToEnableAmqp() {
		return triesToEnableAmqp;
	}

	public static void setTriesToEnableAmqp(int myTriesToEnableAmqp) {
		triesToEnableAmqp = myTriesToEnableAmqp;
	}
};



