/*
 * A. Caproni 04/12/2003
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class GetFileServlet extends HttpServlet implements SingleThreadModel {

	
	protected ServletContext servletContext;

	/** debugging helper */
	protected LogHelper lh;


	public void init (ServletConfig config) throws ServletException {
		servletContext = config.getServletContext();
		String logLevel = config.getInitParameter("logLevel");
		lh = new LogHelper(this, servletContext, logLevel);
	}

	public void destroy () {}

	
	protected void service (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		if (!(request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("GET"))) {
			super.service(request, response);
			return;
		}

		lh.log(lh.INFORMATIONAL, "incoming request. forwarding");

		String filename = request.getParameter("file");

RequestDispatcher rd = request.getRequestDispatcher(filename);
rd.forward(request, response);
if (true) return;


		Writer writer = response.getWriter();
		
		String realpath = servletContext.getRealPath(filename);

		// Check if the file exists
		File f = new File(realpath);
		if (!f.exists()) {
			lh.log(lh.FATAL, "File not found: " + realpath);
			response.sendError(404, "File not found: " + filename);
			return;
		}
		// Check if the file is readable
		if (!f.canRead()) {
			lh.log(lh.FATAL, "File not readable: " + realpath);
			response.sendError(400, "File not readable: " + filename);
			return;
		}

		// Set the mime type of the response (it is a jnlp file...)
		response.setContentType(servletContext.getMimeType(realpath));

		// Read the input file and write each line in the response
		BufferedReader inf = new BufferedReader(new FileReader(f));
		String inputLine;
		while ((inputLine = inf.readLine()) != null) {
			writer.write(inputLine);
		}

		lh.log(lh.INFORMATIONAL, "request processed");
	}

}
