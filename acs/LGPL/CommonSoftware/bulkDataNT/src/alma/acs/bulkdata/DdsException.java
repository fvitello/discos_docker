/*
* ALMA - Atacama Large Millimiter Array
* Copyright (c) National Astronomical Observatory of Japan, 2017 
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
* 
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*/
package alma.acs.bulkdata;

import java.util.Optional;
import com.rti.dds.infrastructure.RETCODE_ERROR;

/**
 * This class is the wrapper of the exceptions that are raised from
 * the underlying DDS library.
 *
 * <p>
 * RTI DDS library that BulkData Java API is currently based on throws 
 * RETCODE_ERROR (or its descendant) when some method call of the DDS library
 * fails. RETCODE_ERROR is a subclass of {@link RuntimeException} as of RTI
 * Connext Java API Version 5.1.0 (this is not documented, but confirmed by
 * intentionally causing an error when using the API), but it is not ideal for
 * the application programmers because it is an unchecked exception, and they
 * may not want to forget to catch such exceptions.
 *
 * <p>
 * Moreover, RETCODE_ERROR thrown by some methods do not contain any message
 * in it. Instead, RTI DDS library seems to output the log message to its
 * logging facility. Fortunately, that log message can be redirected and 
 * the application programmer who uses BulkData Java API can set the destination
 * of the log messages by calling
 * {@link BulkDataNTGlobalConfiguration#setLogger(Logger)}. However, the
 * destination can be only one, and you cannot distributes those message to
 * different  places. For example, you may want to change the log destination
 * from one Stream to another, but it is not possible.
 *
 * <p>
 * For the above reasons, this exception wraps RETCODE_ERROR with additional
 * message, which might be helpful in case the exception message thrown by
 * DDS library is empty.
 *
 * <p>
 * For more details about the exceptions thrown by RTI DDS library, see
 * <a href="https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/html/api_java/group__DDSReturnTypesModule.html">its Java API document</a>.
 *
 * <p>
 * This exception also covers the case where a method call of the underlying
 * DDS library returns a failure code (e.g. null) instead of throwing
 * RETCODE_ERROR.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class DdsException extends Exception {
    private final Optional<RETCODE_ERROR> error;
    private final Optional<BulkDataNTStream> stream;
    private final Optional<BulkDataNTFlow> flow;

    /**
     * This constructor instantiates this Exception with the given message
     * and the cause.
     *
     * @param msg The detailed message about why this exception happens.
     * @param cause The instance of RETCODE_ERROR or its descendant thrown
     *              by the underlying DDS library. Specify null if the
     *              underlying DDS library did not raise an RETCODE_ERROR.
     */
    public DdsException(String msg,
                        RETCODE_ERROR cause) {
        this(msg, cause, null, null);
    }

    /**
     * This constructor instantiates this Exception with the given message,
     * the throwable, the stream and the flow.
     *
     * @param msg The detailed message about why this exception happens.
     * @param cause The instance of RETCODE_ERROR or its descendant thrown
     *              by the underlying DDS library. Specify null if the
     *              underlying DDS library did not raise an RETCODE_ERROR.
     * @param stream The Stream associated with this exception. This can be
     *               null if this exception is not associated with a certain
     *               Stream.
     * @param flow The Flow associated with this exception. This can be null
     *             if this exception is not associated with a certain Flow.
     */
    public DdsException(String msg,
                        RETCODE_ERROR cause,
                        BulkDataNTStream stream,
                        BulkDataNTFlow flow) {
        super(msg, cause);
        this.error = Optional.ofNullable(cause);
        this.stream = Optional.ofNullable(stream);
        this.flow = Optional.ofNullable(flow);
    }

    /**
     * This method returns the root error of this exception. This can be
     * empty if the underlying DDS library did not throw a RETCODE_ERROR,
     * but a certain method returns an error code like null.
     *
     * @return The error code of the cause of this exception, or empty
     *         if a method call of the underlying DDS library returns
     *         an error code.
     */
    public Optional<RETCODE_ERROR> getDdsError() {
        return error;
    }

    /**
     * This method returns the Stream that is associated with this exception.
     * This can be empty if this exception is not associated to a certain
     * Stream.
     *
     * @return The associated Stream, or empty if there is not such Stream.
     */
    public Optional<BulkDataNTStream> getStream() {
        return stream;
    }

    /**
     * This method returns the Flow that is associated with this exception.
     * This can be empty if this exception is not associated to a certain
     * Flow.
     *
     * @return The associated Flow, or empty if there is not such Flow.
     */
    public Optional<BulkDataNTFlow> getFlow() {
        return flow;
    }
}
