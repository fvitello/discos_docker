
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * PENDING: verify getWriter() works as well as getOutputStream()
 * 
 * @author A. Caproni (as CharResponseWrapper, 05/12/2003)
 * @author M. Schilling
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream streamableBuffer;
	private int contentLength;
	private String contentType;

	public ResponseWrapper(HttpServletResponse response) {
		super(response);
		streamableBuffer = new ByteArrayOutputStream(8192);
	}

	
	// ======================================================
	// API for the target-webresource that fills the buffer
	
	/**
	 * Important: This returns an auto-flush PrintWriter,
	 * but auto-flushing only occurs on println(), printf(),
	 * and format().
	 * If you use print() or write(), you have to flush
	 * the PrintWriter explicitly! Also note you must not
	 * close the PrintWriter after flushing!
	 * 
	 * @return a PrintWriter you typically need to flush 
	 */ 
	public PrintWriter getWriter () {
		return new PrintWriter(getOutputStream(), true);
	}
	
	/* 
   msc (apr2006): It is crucial to implement this method, too.
   Otherwise it is not possible to connect neither Tomcat's DefaultServlet nor Sun's
   JnlpDownloadServlet to a Filter based on this response wrapper: these servlets
   use response.getOutputStream() instead of response.getWriter() as our servlet does:
   
   java.lang.IllegalStateException: getWriter() has already been called for this response
      at org.apache.coyote.tomcat4.CoyoteResponse.getOutputStream(CoyoteResponse.java:524)
  */
	public ServletOutputStream getOutputStream () {
		return new FilterServletOutputStream(streamableBuffer);
	}

	public void setContentType (String type) {
		this.contentType = type;
		super.setContentType(type);
	}

	public void setContentLength (int length) {
		this.contentLength = length;
		super.setContentLength(length);
	}
	
	
	// ======================================================
	// API for the Filter that reads the buffer
	
	public byte[] getAsBytes() {
		return streamableBuffer.toByteArray();
	}

	public String getAsString() {
		return streamableBuffer.toString();
	}

	public int getContentLength () {
		return contentLength;
	}

	public String getContentType () {
		return contentType;
	}


	// ======================================================
	// Helper class
	
	protected class FilterServletOutputStream extends ServletOutputStream {

		private DataOutputStream stream;

		public FilterServletOutputStream(OutputStream output) {
			stream = new DataOutputStream(output);
		}

		public void write (int b) throws IOException {
			stream.write(b);
		}

		public void write (byte[] b) throws IOException {
			stream.write(b);
		}

		public void write (byte[] b, int off, int len) throws IOException {
			stream.write(b, off, len);
		}
	}
	
}
