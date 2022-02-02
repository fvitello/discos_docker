import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter to replace string constants loaded from a file.
 * 
 * The chain (i.e. the servlet) is executed between 1 and 2
 * 
 * @author M.Schilling (after a class by A.Caproni)
 */
public class StringConstantsFilter implements Filter {

	/* Note:
	 * According to Java Servlet Specification (v2.3),
	 * there is only one filter instance created for each
	 * "<filter>" definition in the deployment descriptor.
	 * In other words, a filter must be threadsafe.
	 */

	private final String CONSTANTS = "StringConstants.properties";
	
	/** The string constants read from a file */
	private Properties constants;

	/** debugging helper */
	protected LogHelper lh;

	/**
	 */
	public void init (FilterConfig config) throws ServletException {

		constants = new Properties();
		ServletContext servletContext = config.getServletContext(); 

		String logLevel = config.getInitParameter("logLevel");
		lh = new LogHelper(this, servletContext, logLevel);

		
		try {
			InputStream is = servletContext.getResourceAsStream(CONSTANTS);
			constants.load(is);

			lh.log(lh.INFORMATIONAL, "constants in file: "+lh.mapAsString(constants));

		} catch (IOException exc) {
			String msg = "could not find or read "+CONSTANTS;
			lh.log(lh.FATAL, msg, exc);
			throw new ServletException(msg, exc);
		}
	}

	/**
	 */
	public void destroy () {}

	
	/**
	 * 
	 */
	public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain) 
																											throws IOException, ServletException {

		lh.log(lh.INFORMATIONAL,
				"started for "+((HttpServletRequest)request).getRequestURI()+". now waiting for input to filter");
		
		ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
		chain.doFilter(request, responseWrapper);
		// reply from servlet (or other filter) is now in responseWrapper

		StringBuffer buffer = new StringBuffer(responseWrapper.getAsString());
		lh.log(lh.DEBUG, "input available, about to filter: " + buffer.toString());

		customizeJNLP(constants, request, buffer);
		lh.log(lh.DEBUG, "after filtering: " + buffer.toString());

		response.setContentLength(buffer.length());
		PrintWriter pw = response.getWriter();
		pw.print(buffer.toString());
		pw.flush(); // it is crucial to flush!
		// and it's also crucial NOT to close()
	}


	/** 
	 * Substitute the placeholders with the parameters in request
	 * 
	 * NOTE: to avoid name clash: do not use parameters that begin with the same string
	 * (like host and hostname)
	 */
	private void customizeJNLP (Map parameters, ServletRequest req, StringBuffer sb) {

		int pos;

		Object[] parNames = parameters.keySet().toArray();
		for (int t = 0; t < parNames.length; t++) {
			
			while ((pos = sb.indexOf("$$"+parNames[t])) != -1) {
				sb.replace(pos, pos + ("$$"+parNames[t]).length(), (String)parameters.get(parNames[t]));
				
				lh.log(lh.INFORMATIONAL, "subst $$"+parNames[t] +" with "+ parameters.get(parNames[t]) +" at "+ pos);
			}
		}
	}

}
