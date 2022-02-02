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

import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * The getter methods of this class return the default configuration values
 * of {@link BulkDataNTSenderFlow}.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTSenderFlowConfigurationDefault
    extends BulkDataNTDdsConfigurationDefault
    implements BulkDataNTSenderFlowConfiguration{
    /**
     * This method returns the default QoS XML profile name for Sender Flows:
     * {@value #DEFAULT_QOS_PROFILE_NAME}
     *
     * @return The default QoS XML profile name for Sender Flows.
     */
    @Override public String getQosProfileName() { return DEFAULT_QOS_PROFILE_NAME; }

    /**
     * The default QoS XML profile name for Sender Flows.
     */
    protected static final String
        DEFAULT_QOS_PROFILE_NAME = "SenderFlowDefaultQosProfile";

    /**
     * This method returns the default timeout for waiting ACK:
     * {@value #DEFAULT_ACK_TIMEOUT_SECONDS} seconds
     *
     * @return The default timeout for waiting ACK.
     */
    @Override public Duration getAckTimeout() { return DEFAULT_ACK_TIMEOUT; }

    /**
     * The default timeout for waiting ACK in seconds.
     */
    protected static final int DEFAULT_ACK_TIMEOUT_SECONDS = 2;

    /**
     * The default timeout for waiting ACK.
     */
    protected static final Duration DEFAULT_ACK_TIMEOUT
        = Duration.ofSeconds(DEFAULT_ACK_TIMEOUT_SECONDS);

    /**
     * This method returns the default timeout for sending frame:
     * {@value #DEFAULT_SEND_FRAME_TIMEOUT_SECONDS} seconds
     *
     * @return The default timeout for sending frame.
     */
    @Override public Duration
        getSendFrameTimeout() { return DEFAULT_SEND_FRAME_TIMEOUT; }

    /**
     * The default timeout for sending frame in seconds.
     */
    protected static final int DEFAULT_SEND_FRAME_TIMEOUT_SECONDS = 5;

    /**
     * The default timeout for sending frame.
     */
    protected static final Duration DEFAULT_SEND_FRAME_TIMEOUT
        = Duration.ofSeconds(DEFAULT_SEND_FRAME_TIMEOUT_SECONDS);

    /**
     * This method returns the default throttling:
     * {@value #DEFAULT_THROTTLING} MB/s.
     *
     * @return The default throttling in MB/s.
     */
    @Override public double getThrottling() { return DEFAULT_THROTTLING; }

    /**
     * The default throttling setting in MB/s.
     */
    protected static final double DEFAULT_THROTTLING = 1024.0; // 1 GB/s

    /**
     * This method returns the default logger for Sender Stream.
     *
     * @return The default logger.
     */
    @Override public Optional<Logger> getLogger() {
        return Optional.of(DEFAULT_LOGGER);
    }
    protected static final Logger DEFAULT_LOGGER
        = Logger.getLogger(BulkDataNTSenderFlow.class.getName());
    // This static variable keeps the reference to this logger. If the
    // reference to the logger is not kept somewhere, that logger may
    // be garbage-collected, and a new instance may be returned in
    // the second call of getLogger(). See Javadoc of Logger.getLogger()
    // for more details.
}

