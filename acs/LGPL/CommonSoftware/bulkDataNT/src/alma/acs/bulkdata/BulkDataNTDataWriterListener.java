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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

import alma.acs.logging.AcsLogLevel;

import com.rti.dds.publication.AcknowledgmentInfo;
import com.rti.dds.publication.DataWriter;
import com.rti.dds.publication.DataWriterListener;
import com.rti.dds.publication.OfferedDeadlineMissedStatus;
import com.rti.dds.publication.OfferedIncompatibleQosStatus;
import com.rti.dds.publication.LivelinessLostStatus;
import com.rti.dds.publication.PublicationMatchedStatus;
import com.rti.dds.publication.ReliableWriterCacheChangedStatus;
import com.rti.dds.publication.ReliableReaderActivityChangedStatus;
import com.rti.dds.infrastructure.Cookie_t;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.Locator_t;

import org.apache.commons.lang.Validate;

/**
 * This listener class listens to the events raised by DDS Data Writer
 * and translate each event to an event that an instance of
 * {@link BulkDataNTSenderFlowListener} can receive.
 *
 * @author Takashi Nakamoto
 */
class BulkDataNTDataWriterListener implements DataWriterListener {
    private final Optional<Logger> logger;
    private final Optional<BulkDataNTSenderFlowListener> flowListener;
    private final BulkDataNTSenderFlow flow;

    @SuppressWarnings("unused")
    private int sumUnacknowledgedSample;
    private int maxUnacknowledgedSample;

    /**
     * Constructs this listener with the given Sender Flow listener.
     *
     * @param flowListener The Sender Flow listener that wants to receive
     *                     the events from this listener. Can be empty
     *                     if it is not necessary to transfer the events
     *                     to the Sender Flow listener.
     * @param flow         The Sender Flow that is associated with the 
     *                     Data Writer. Must not be null.
     * @param logger       The logger to log the events received.
     */
    BulkDataNTDataWriterListener(Optional<BulkDataNTSenderFlowListener> flowListener,
                                 BulkDataNTSenderFlow flow,
                                 Optional<Logger> logger) {
        Validate.notNull(flowListener);
        Validate.notNull(flow);
        Validate.notNull(logger);

        this.flowListener = flowListener;
        this.flow = flow;
        this.sumUnacknowledgedSample = 0;
        this.maxUnacknowledgedSample = 0;
        this.logger = logger;
    }

    @Override
	public void on_offered_deadline_missed (DataWriter writer,
                                            OfferedDeadlineMissedStatus status) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               "OfferedDeadlineMissed " +
                               "(total_count = %d, total_count_change = %d)",
                               flow.getStream().getName(),
                               flow.getName(),
                               status.total_count,
                               status.total_count_change));

        // Note: not all the values in "status" variable is transferred to
        //       the Sender Flow listener.
        flowListener.ifPresent(l -> l.onError(BulkDataNTSenderFlowListener.ErrorType.
                                              MISSED_OFFERED_DEADLINE,
                                              flow));
    }

    @Override
	public void on_offered_incompatible_qos (DataWriter writer,
                                             OfferedIncompatibleQosStatus status) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               "OfferedIncompatibleQoS " +
                               "(total_count = %d, total_count_change = %d," +
                               " last_policy_id = %s)",
                               flow.getStream().getName(),
                               flow.getName(),
                               status.total_count,
                               status.total_count_change,
                               status.last_policy_id.name()));

        // Note: not all the values in "status" variable is transferred to
        //       the Sender Flow listener.
        flowListener.ifPresent(l -> l.onError(BulkDataNTSenderFlowListener.ErrorType.
                                              INCOMPATIBLE_QOS,
                                              flow));
    }

    @Override
	public void on_liveliness_lost (DataWriter writer, 
                                    LivelinessLostStatus status) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               "LivelinessLost " +
                               "(total_count = %d, total_count_change = %d)",
                               flow.getStream().getName(),
                               flow.getName(),
                               status.total_count,
                               status.total_count_change));

        // Note: not all the values in "status" variable is transferred to
        //       the Sender Flow listener.
        flowListener.ifPresent(l -> l.onError(BulkDataNTSenderFlowListener.ErrorType.
                                              LIVELINESS_LOST,
                                              flow));
    }

    @Override
	public void on_publication_matched (DataWriter writer, 
                                        PublicationMatchedStatus status) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               "PublicationMatched " +
                               "(total_count = %d, total_count_change = %d," +
                               " current_count = %d, current_count_peak = %d," +
                               " current_count_change = %d)",
                               flow.getStream().getName(),
                               flow.getName(),
                               status.total_count,
                               status.total_count_change,
                               status.current_count,
                               status.current_count_peak,
                               status.current_count_change));
        // Does nothing.
    }

    @Override
	public void on_reliable_writer_cache_changed(DataWriter writer,
                                                 ReliableWriterCacheChangedStatus status) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               "ReliableWriterCacheChanged " +
                               "(empty_reliable_writer_cache.total_count = %d," +
                               " empty_reliable_writer_cache.total_count_change = %d," +
                               " full_reliable_writer_cache.total_count = %d," +
                               " full_reliable_writer_cache.total_count_change = %d," +
                               " low_watermark_reliable_writer_cache.total_count = %d," +
                               " low_watermark_reliable_writer_cache.total_count_change = %d," +
                               " high_watermark_reliable_writer_cache.total_count = %d," +
                               " high_watermark_reliable_writer_cache.total_count_change = %d," +
                               " unacknowledged_sapmle_count = %d," +
                               " unacknolwedged_sample_count_peak = %d)",
                               flow.getStream().getName(),
                               flow.getName(),
                               status.empty_reliable_writer_cache.total_count,
                               status.empty_reliable_writer_cache.total_count_change,
                               status.full_reliable_writer_cache.total_count,
                               status.full_reliable_writer_cache.total_count_change,
                               status.low_watermark_reliable_writer_cache.total_count,
                               status.low_watermark_reliable_writer_cache.total_count_change,
                               status.high_watermark_reliable_writer_cache.total_count,
                               status.high_watermark_reliable_writer_cache.total_count_change,
                               status.unacknowledged_sample_count,
                               status.unacknowledged_sample_count_peak));

        // Note: this code is here because the equivalent thing is done in
        //       C++ version, but it seems that the calculation result is
        //       not used anywhere else. If the application programmer wants
        //       to use this unacknowledged sample count, a new method needs
        //       to be defined. Also this method has to consider the case
        //       where sumUnacknowledgedSample is overflowed.
        sumUnacknowledgedSample += status.unacknowledged_sample_count;
        maxUnacknowledgedSample
            = Math.max(status.unacknowledged_sample_count,
                       maxUnacknowledgedSample);
    }

    @Override
	public void on_reliable_reader_activity_changed(DataWriter writer,
                                                    ReliableReaderActivityChangedStatus status) {
        int i;

        if (status.active_count_change > 0) {
            for(i = 0; i < status.active_count_change; i++) {
                logInfo(String.format("A new receiver has connected to flow: "+
                                      "%s of the stream: %s. Total alive " +
                                      "connection(s): %d",
                                      flow.getName(),
                                      flow.getStream().getName(),
                                      status.active_count));
                flowListener
                    .ifPresent(l -> l.onReceiverConnect(status.active_count,
                                                        flow));
            }
        } else {
            for(i = status.active_count_change; i < 0; i++) {
                logInfo(String.format("A new receiver has disconnected to " +
                                      "flow: %s of the stream: %s. Total " +
                                      "alive connection(s): %d",
                                      flow.getName(),
                                      flow.getStream().getName(),
                                      status.active_count));
                flowListener
                    .ifPresent(l -> l.onReceiverDisconnect(status.active_count,
                                                           flow));
            }
        }
    }

    @Override
	public void on_instance_replaced(DataWriter writer,
                                     InstanceHandle_t handle) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] InstanceReplaced",
                               flow.getStream().getName(),
                               flow.getName()));

        // MINOR TODO: clarify what this event is, judge if this is an error
        //             that should be propagated to the Sender Flow listener.
    }

    @Override
    public void on_application_acknowledgment(DataWriter writer,
                                              AcknowledgmentInfo ackInfo) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               " ApplicationAcknowledgement " +
                               "(valid_response_data = %b," +
                               " response_data = %s)",
                               flow.getStream().getName(),
                               flow.getName(),
                               ackInfo.valid_response_data,
                               new String(ackInfo.response_data.value
                                          .toArrayByte(null),
                                          StandardCharsets.US_ASCII)));

        // Note: 
        // It seems that this method is not documented in RTI Connext 5.1.0
        // Java API, but since 5.2.0. In either case, it is currently not
        // known how useful this event is. So, do nothing for now.
    }

    @Override
    public void on_sample_removed(DataWriter writer,
                                  Cookie_t cookie) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               " SampleRemoved " +
                               "(cookie.value = %s)",
                               flow.getStream().getName(),
                               flow.getName(),
                               new String(cookie.value
                                          .toArrayByte(null),
                                          StandardCharsets.US_ASCII)));

        // Note: 
        // It seems that this method is not documented in RTI Connext 5.1.0
        // Java API. It is currently not known how useful this event is. So,
        // do nothing for now.
    }

    @Override
    public void on_data_return(DataWriter writer,
                               Object obj,
                               Cookie_t cookie) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               " DataReturn " +
                               "(object = %s, cookie.value = %s)",
                               flow.getStream().getName(),
                               flow.getName(),
                               obj.toString(),
                               new String(cookie.value
                                          .toArrayByte(null),
                                          StandardCharsets.US_ASCII)));

        // Note: 
        // It seems that this method is not documented in RTI Connext 5.1.0
        // Java API. It is currently not known how useful this event is. So,
        // do nothing for now.
    }

    @Override
    public Object on_data_request(DataWriter writer, Cookie_t cookie) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               " DataRequest " +
                               "(cookie.value = %s)",
                               flow.getStream().getName(),
                               flow.getName(),
                               new String(cookie.value
                                          .toArrayByte(null),
                                          StandardCharsets.US_ASCII)));

        // Note: 
        // It seems that this method is not documented in RTI Connext 5.1.0
        // Java API. It is currently not known how useful this event is. So,
        // do nothing for now.

        // TOOD: do not know what to return.
        return null;
    }

    @Override
    public void on_destination_unreachable(DataWriter writer,
                                           InstanceHandle_t handle,
                                           Locator_t locator) {
        logDebug(String.format("[Stream \"%s\", Flow \"%s\"] " +
                               " DestinationUnreachable " +
                               "(locator.kind = %d," +
                               " locator.address = %s," + 
                               " locator.port = %d)",
                               flow.getStream().getName(),
                               flow.getName(),
                               locator.kind,
                               String.format("%032x",
                                             new BigInteger(1, locator.address)),
                               locator.port));

        // Note: 
        // It seems that this method is not documented in RTI Connext 5.1.0
        // Java API. It is currently not known how useful this event is. So,
        // do nothing for now.
    }

    private void logDebug(String msg) {
        logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG, msg));
    }

    private void logInfo(String msg) {
        logger.ifPresent(l -> l.log(AcsLogLevel.INFO, msg));
    }
}
