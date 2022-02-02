import java.util.Map;

import javax.servlet.ServletContext;


/**
 * Util class for debugging
 * 
 * @author mschilli
 */
public class LogHelper {
	
	private String client;
	private ServletContext servletContext;
	private int loglevel;

	public final int NONE = 0;
	public final int FATAL = 1;
	public final int WARNING = 2;
	public final int INFORMATIONAL = 3;
	public final int DEBUG = 4;
	
	private int levelFromName (String name) {
		if ("NONE".equalsIgnoreCase(name))
			return NONE;
		if ("FATAL".equalsIgnoreCase(name))
			return FATAL;
		if ("WARNING".equalsIgnoreCase(name))
			return WARNING;
		if ("INFORMATIONAL".equalsIgnoreCase(name))
			return INFORMATIONAL;
		if ("DEBUG".equalsIgnoreCase(name))
			return DEBUG;
		return 0;
	}
	
	// =================================================
	// API
	
	public LogHelper(Object client, ServletContext servletContext, String loglevel) {
		this.client = client.getClass().getName()+"@"+client.hashCode();
		this.servletContext = servletContext;
		this.loglevel = levelFromName(loglevel);
	}
	
	public void log (int level, String msg, Throwable thr) {
		if (level <= loglevel) {
			servletContext.log(client+": "+msg, thr);
		}
	}	
	
	public void log (int level, String msg) {
		if (level <= loglevel) {
			servletContext.log(client+": "+msg);
		}
	}
	
	public void log (int level, Map map) {
		if (level <= loglevel)
			log(level, mapAsString(map));
	}	
	
	public String mapAsString (Map map) {
		StringBuffer ret = new StringBuffer(500);
		Object[] parNames = map.keySet().toArray();
		for (int t = 0; t < parNames.length; t++) {
			ret.append(parNames[t] + "=" + map.get(parNames[t]) + "; ");
		}
		return ret.toString();
	}

	
}


