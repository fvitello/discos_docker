package alma.acs.xmlfilestore.common;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Logger;

import com.cosylab.logging.engine.ACS.ACSRemoteRawLogListener;
import com.cosylab.logging.engine.ACS.LCEngine;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import alma.acs.container.ContainerServices;

public class kafkaHandler {
    
	/*
	 * kafka settings, read from CDB
	 */
	private static Boolean kafkaConnectionEnabled = true;
	
	private final Logger m_logger;
	
	private int triesToEnableKafka = 0;


	/**
	 * Constructor
	 * 
	 * @param myLogger The logger
	 * @param cs the container services
	 * @param enabled: to activate/deactivate the messaging to kafka
	 * @param kafka_brokers;
	 * @throws Exception
	 *  
	 */
	public kafkaHandler(Logger myLogger, final ContainerServices cs,LCEngine engine, Boolean enabled,
			final HashMap<String, String> kafkaProps,
			final String origin, final String buildtag) throws Exception {

		this.m_logger = cs.getLogger();
	
			ACSRemoteRawLogListener myListener = new ACSRemoteRawLogListener() {
				//kafka initialization
				KafkaProducer<String, String> myProducer = producer(cs, kafkaProps);	
				@Override
				public void xmlEntryReceived(String xmlLogString) {
					
					try{
						if (!kafkaConnectionEnabled && (triesToEnableKafka > 3)){
							return ;
						}
						//Transform XML to JSON and sanitize
						JSONObject xmlJSONObj = XML.toJSONObject(xmlLogString);
						JSONObject properJSON = new JSONObject();
						//fix loglevel
						Iterator<?> keys = xmlJSONObj.keys();
						while (keys.hasNext()){
							String logLevelKey =  (String)keys.next();
							if (logLevelKey.equals("Debug") || logLevelKey.equals("Delouse") ||
									logLevelKey.equals("Emergency") || logLevelKey.equals("Error") ||
									logLevelKey.equals("Info") || logLevelKey.equals("Notice") ||
									logLevelKey.equals("Warning") || logLevelKey.equals("Trace") ||
									logLevelKey.equals("Critical") 
									){
								properJSON = (JSONObject) xmlJSONObj.get(logLevelKey);
								properJSON.put("LogLevel", logLevelKey);

							}
							else{
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
						//need to dig into the body to search for DATA.
						//It seems to be a nested structure the top level is either a JSONArray or JSONObject
						//Children are JSONObject
                        if (properJSON.has("Data")){
                        	JSONObject aux = new JSONObject();
                        	if (properJSON.get("Data") instanceof JSONObject){
	                        	//it is a JSONObject
	                        	JSONObject myData = properJSON.getJSONObject("Data");
	                      		Iterator<?> dataKeys = myData.keys();
	                      		String newKey = "";
                    			String newValue = "";
	                        		while (dataKeys.hasNext()){
	                        			String currentKey = (String) dataKeys.next();
	                        			if (currentKey.equals("Name")){
	                        				newKey = (String) myData.get(currentKey);
	                        			}
	                        			if (currentKey.equals("content")){
	                        				newValue = (String) myData.get(currentKey);
	                        			}
	                        			if (newKey.length()>0 && newValue.length()>0){
	                        				aux.put(newKey, newValue);
	                        				newKey = "";
	                        				newValue = "";
	                        			}                      		
	                        		}
	                        		properJSON.remove("Data");
	                        		properJSON.put("Data", aux);
                        	}else if (properJSON.get("Data") instanceof JSONArray){
	                        	//deal with the array.
	                        	JSONArray myArray = properJSON.getJSONArray("Data");
	                        	for (int i =0 ; i < myArray.length(); i++){
	                        		//it contains JSONObjects
	                        		JSONObject jsonObjAux = myArray.getJSONObject(i);
	                        		Iterator<?> jsonObjAuxKeys = jsonObjAux.keys();
		                      		String newKey = "";
	                    			String newValue = "";
	                        		while (jsonObjAuxKeys.hasNext()){
	                        			String currentKey = (String) jsonObjAuxKeys.next();
	                        			if (currentKey.equals("Name")){
	                        				newKey = (String)jsonObjAux.get(currentKey);
	                        			}
	                        			if (currentKey.equals("content")){
	                        				newValue = (String) jsonObjAux.get(currentKey);
	                        			}
	                        			if (newKey.length()>0 && newValue.length()>0){
	                        				aux.put(newKey, newValue);
	                        				newKey = "";
	                        				newValue = "";
	                        			}
	                        			
	                        		}
	                        		properJSON.remove("Data");
	                        		properJSON.put("Data", aux);
	                        	}
                        	}else{
                        		m_logger.info("no idea about data class, asking the object itself " + properJSON.get("Data").getClass());
                        	}
                        }

						//String to be published on kafka
						String jsonPrettyPrintString = properJSON.toString();
						
						
						//kafka publish
						myProducer.send(new ProducerRecord<String, String>(origin, jsonPrettyPrintString));

					}catch (Exception e){		
						m_logger
						.severe("Could not publish to kafka. Try: " + triesToEnableKafka + ": " + e.getMessage());
						triesToEnableKafka++;
						setKafkaConnectionEnabled(false);
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
	
	public static KafkaProducer<String, String> producer(final ContainerServices cs, 
			final HashMap<String, String> kafkaProps) throws Exception{
		Logger m_logger = cs.getLogger();
		if (kafkaProps.isEmpty()){
			throw new Exception("since kafka is enabled, you have to configure the brokers at least");
		}else{
			Properties props = new Properties();
		    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		    
		    for (String key: kafkaProps.keySet()){
		    	if (!kafkaProps.get(key).equals("")){
		    		//add relevant ProducerConfig
		    		/* most importants as per kafka
 		             * acks
 		             * bootstrap.servers
		    		 * compression.type
		             * retries
		             * buffer.memory
		             * ssl.truststore.location
		             * ssl.keystore.password
		             * ssl.key.password
		             * ssl.keystore.location
		             * ssl.truststore.password
		    		 */
		    		if (key.equals("acks")){
		    			 props.put(ProducerConfig.ACKS_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: ProducerConfig.ACKS_CONFIG = " + kafkaProps.get(key));
		    		}
		            if (key.equals("bootstrap.servers")){
		            	props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: ProducerConfig.BOOTSTRAP_SERVERS_CONFIG = " + kafkaProps.get(key)); 
		            }
		            if (key.equals("compression.type")){
		            	props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: ProducerConfig.COMPRESSION_TYPE_CONFIG = " + kafkaProps.get(key)); 
		            }
		            if (key.equals("retries")){
		            	props.put(ProducerConfig.RETRIES_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: ProducerConfig.RETRIES_CONFIG = " + kafkaProps.get(key)); 
		            }
		            if (key.equals("buffer.memory")){
		            	props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: ProducerConfig.BUFFER_MEMORY_CONFIG = " + kafkaProps.get(key)); 
		            }
		            //SSL section
		            if (key.equals("ssl.truststore.location")){
		            	props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG = " + kafkaProps.get(key)); 
		            }
		            if (key.equals("ssl.keystore.password")){
		            	props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG = " + kafkaProps.get(key)); 
		            }
		            if (key.equals("ssl.key.password")){
		            	props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: SslConfigs.SSL_KEY_PASSWORD_CONFIG = " + kafkaProps.get(key)); 
		            }
		            if (key.equals("ssl.keystore.location")){
		            	props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG = " + kafkaProps.get(key)); 
		            }
		            if (key.equals("ssl.truststore.password")){
		            	props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaProps.get(key));
		    			 m_logger.info("adding kafka config: SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG = " + kafkaProps.get(key)); 
		            }
		    	}
		    }
		    KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
		    return producer;
		}
	}
	
	
	public void resetConnection(){
		this.setKafkaConnectionEnabled(true);
		this.setTriesToEnableKafka(0);
	}
	
	
	public Boolean getKafkaConnectionEnabled (){
		return kafkaConnectionEnabled ;
	}
	
	public void setKafkaConnectionEnabled (Boolean state){
		kafkaConnectionEnabled = state;
	}
	
	public int getTriesToEnableKafka() {
		return triesToEnableKafka;
	}

	public void setTriesToEnableKafka(int triesToEnableKafka) {
		this.triesToEnableKafka = triesToEnableKafka;
	}
};



