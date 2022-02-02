import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
 * This filter performs two tasks: 1. convert a request with the acsinstance (easy mode)
 * in a request with the correct host name and port 2. change each instance of the
 * parameters in the jnlp files returned by the servlet
 * 
 * The chain (i.e. the servlet) is executed between 1 and 2
 * 
 * @author A. Caproni 02/12/2003
 * @author M. Schilling
 */
public class ReqParamsFilter implements Filter {

	/* Note:
	 * According to Java Servlet Specification (v2.3),
	 * there is only one filter instance created for each
	 * "<filter>" definition in the deployment descriptor.
	 * In other words, a filter must be threadsafe.
	 */

	
	/** debugging helper */
	protected LogHelper lh;
	
	
	/**
	 * 
	 */
	public void init (FilterConfig config) throws ServletException {
		ServletContext servletContext = config.getServletContext();
		String logLevel = config.getInitParameter("logLevel");
		lh = new LogHelper(this, servletContext, logLevel);
	}

	/**
	 * 
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
		
		Map parameters = getParameters(request);
		customizeJNLP(parameters, request, buffer);
		lh.log(lh.DEBUG, "after filtering: " + buffer.toString());

		response.setContentLength(buffer.length());
		PrintWriter pw = response.getWriter();
		pw.print(buffer.toString());
		pw.flush(); // it is crucial to flush!
		// and it's also crucial NOT to close()
	}

	/**
    * Get the parameters from the request
    * All the paremeters are read, checked and evaluated in this method
    * 
    * The HashMap contains all the parameters (names and values) as they are needed to
    *  produce the right output. If the acsinstance parameter is present, then the
    * "easy" mode is used (and some parameters are not specified and must be evaluated)
    * 
    * @throws exception if an error arises decoding parameters
    */ 
	private Map getParameters (ServletRequest req) throws ServletException {
		HashMap ret = new HashMap(12);
		
		ret.put("acsinstance", null);
		ret.put("host", null);
		
		for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
			String Name = (String) e.nextElement();
			ret.put(Name, (String) req.getParameter(Name));
		}

		// If acsinstance and host are not null (easy mode) 
		// then we have to change all the other parameters
		if (ret.get("acsinstance") != null && ret.get("host") != null) {

			int acs_instance = Integer.parseInt((String) ret.get("acsinstance"));
			String host = (String) ret.get("host");
			
			lh.log(lh.INFORMATIONAL, "request is 'easy mode': generating necessary params based on acsinstance="+acs_instance +" and host="+host);
			
			ret.put("manager_host", host);
			ret.put("interface_repository_host", host);
			ret.put("naming_service_host", host);
			ret.put("dal_host", host);
			ret.put("manager_port", "" + (3000 + acs_instance * 100));
			ret.put("naming_service_port", "" + (3000 + acs_instance * 100 + 1));
			ret.put("interface_repository_port", "" + (3000 + acs_instance * 100 + 4));
			ret.put("dal_port", "" + (3000 + acs_instance * 100 + 12));

			lh.log(lh.INFORMATIONAL, ret);
		}

		// Check the parameters
		if (ret.get("manager_port") == null
				|| ret.get("manager_host") == null
				|| ret.get("naming_service_port") == null
				|| ret.get("naming_service_host") == null
				|| ret.get("interface_repository_port") == null
				|| ret.get("interface_repository_host") == null
				|| ret.get("dal_port") == null
				|| ret.get("dal_host") == null) {
			
			throw new ServletException("Error in the parameters: "+lh.mapAsString(ret));
		}
		
		return ret;
	}

	/** 
	 * Substitute the placeholders with the parameters in request
	 * 
	 * NOTE: to avoid name clash: do not use parameters that begin with the same string
	 * (like host and hostname)
	 *
	 * @return false in case of error
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
