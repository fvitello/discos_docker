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

/**
 * This interface defines the required method for the objects that hold
 * configuration for {@link BulkDataNTSenderFlow}.
 *
 * @author Takashi Nakamoto
 */
public interface BulkDataNTSenderFlowConfiguration
    extends BulkDataNTDdsConfiguration {
    /**
     * This method returns ACK timeout. The returned value must be neither
     * null nor negative.
     *
     * <p>
     * {@link BulkDataNTSenderFlow#sendData(byte[])} sends all data to
     * the receivers, and then it blocks the caller until the sender
     * receives the acknowledgments of all the data that have been
     * previously sent (including the frames sent by
     * {@link BulkDataNTSenderFlow#startSend(byte[])} and
     * {@link BulkDataNTSenderFlow#stopSend()}) from all the connected
     * receivers, or the time specified by this configuration is elapsed.
     *
     * @return The ACK timeout configuration in the range between 0 and
     *         {@value java.lang.Integer#MAX_VALUE}.999999999 seconds
     *         (inclusive).
     */
    public Duration getAckTimeout();

    /**
     * This method returns send frame timeout. The returned value must be
     * neither null nor negative.
     *
     * <p>
     * When {@link BulkDataNTSenderFlow#sendData(byte[])} is called, the given
     * data is split into small arrays with the size of up to 
     * {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN} bytes, and
     * calls the write() method of
     * {@link alma.acs.bulkdata.ACSBulkData.BulkDataNTFrameDataWriter}
     * for each array. This method just writes the data to a buffer (called
     * "history" in the document of RTI Connext Java API 5.1.0). Thus,
     * the call of write() method is typically returned very quickly. 
     * However, if the connected receivers do not send back acknowledgments,
     * the associated data will keep staying in the buffer, and finally the
     * buffer may be filled up. In this case, write() method may block the
     * caller until the sender receives the acknowledgments of the data
     * in the buffer.
     * 
     * <p>
     * This timeout configuration determines the maximum block time
     * of the write() call. If this time has been elapsed, the write() call
     * immediately returns and the sender flow throws
     * {@link SendFrameTimeoutException}.
     *
     * <p>
     * Note that {@link BulkDataNTSenderFlow#startSend(byte[])} and
     * {@link BulkDataNTSenderFlow#stopSend()} also call the write() method
     * internally to send special frames, and this timeout configuration
     * is also used by those methods.
     *
     * @return The send frame timeout configuration in the range between 0 and
     *         {@value java.lang.Integer#MAX_VALUE}.999999999 seconds
     *         (inclusive).
     */
    public Duration getSendFrameTimeout();

    /**
     * This method returns the throttling (the maximum limit of the data
     * transmission rate) in MB/s.
     * 
     * <p>
     * {@link BulkDataNTSenderFlow#sendData(byte[])} controls the data
     * transmission rate by adjusting the time to call the write()
     * method of {@link alma.acs.bulkdata.ACSBulkData.BulkDataNTFrameDataWriter}
     * so that the transmission rate will not exceed the specified
     * throttling parameter.
     *
     * <p>
     * Note 1: Here MB means 1048576 (= 1024 x 1024) bytes.
     * 
     * <p>
     * Note 2: this throttling configuration is applied only to the payload
     * sent by {@link BulkDataNTSenderFlow#sendData(byte[])}.
     * Actual transmission rate over the wire can be a bit faster
     * than the specified throttling parameter because of the headers of
     * BulkData frames, frames sent by
     * {@link BulkDataNTSenderFlow#startSend(byte[])} and
     * {@link BulkDataNTSenderFlow#stopSend()}, and headers of lower level
     * protocols.
     * 
     * <p>
     * Note 3: the throttling configuration is applied to the average data
     * transmission rate, so the instantaneous data transmission rate can be
     * faster. The data is sent frame by frame, and the maximum frame
     * size is defined as {@link BulkDataNTSenderFlow#FRAME_MAX_LEN}. For
     * example, if the size of all the frames are 64,000 bytes, and if the
     * throttling configuration is set to 128,000 bytes/s, the time interval
     * between two frames will be 0.5 seconds. However, most probably, at
     * the beginning of each 0.5 seconds interval, the payload in that frame
     * would be sent at once. For example, it could be sent as one UDP
     * packet. So, the instantaneous data transmission rate will be very
     * high when sending one frame.
     * 
     * <p>
     * Note 4: {@link BulkDataNTSenderFlow#sendData(byte[])} controls the
     * data transmission rate by adjusting the time to call the write()
     * method. However, the underlying DDS library may buffer the given data
     * (actually it does). Thus, the actual data transmission rate over the
     * wire can be faster, for example, if the buffered data is sent at once.
     *
     * @return A positive value in MB/s.
     */
    public double getThrottling();
}
