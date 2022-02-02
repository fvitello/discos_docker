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
 * This class holds the configuration of {@link BulkDataNTSenderFlow}
 * in memory.
 *
 * <p>
 * The instance of this class is immutable. The values returned by the getter
 * methods are always the same.
 *
 * @author Takashi Nakamoto
 */
class BulkDataNTSenderFlowConfigurationStore
    implements BulkDataNTSenderFlowConfiguration {

    protected final String qosLibraryName;
    @Override public String getQosLibraryName() { return qosLibraryName; }

    protected final String qosProfileName;
    @Override public String getQosProfileName() { return qosProfileName; }

    protected final Duration ackTimeout;
    @Override public Duration getAckTimeout() { return ackTimeout; }

    protected final Duration sendFrameTimeout;
    @Override public Duration
        getSendFrameTimeout() { return sendFrameTimeout; }

    protected final double throttling;
    @Override public double getThrottling() { return throttling; }

    protected final Optional<Logger> logger;
    @Override public Optional<Logger> getLogger() { return logger; }

    /**
     * This constructor copies all configuration values and keep them in 
     * instance variables.
     *
     * @param src The source instance of {@link BulkDataNTSenderFlowConfiguration}
     *            from which this constructor obtains all the configuration
     *            values through the getter methods.
     */
    public BulkDataNTSenderFlowConfigurationStore(BulkDataNTSenderFlowConfiguration src) {
        this.qosLibraryName = src.getQosLibraryName();
        this.qosProfileName = src.getQosProfileName();
        this.ackTimeout = src.getAckTimeout(); // Duration is immutable.
        this.sendFrameTimeout = src.getSendFrameTimeout();
        this.throttling = src.getThrottling();
        this.logger = src.getLogger();
    }
}
