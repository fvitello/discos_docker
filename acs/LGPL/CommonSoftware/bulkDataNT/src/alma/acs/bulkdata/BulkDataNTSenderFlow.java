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

import alma.acs.logging.AcsLogLevel;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import alma.acs.bulkdata.ACSBulkData.BD_PARAM;
import alma.acs.bulkdata.ACSBulkData.BD_DATA;
import alma.acs.bulkdata.ACSBulkData.BD_STOP;
import alma.acs.bulkdata.ACSBulkData.BulkDataNTFrame;
import alma.acs.bulkdata.ACSBulkData.BulkDataNTFrameDataWriter;

import org.apache.commons.lang.Validate;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_TIMEOUT;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriter;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.Publisher;
import com.rti.dds.publication.PublisherQos;
import com.rti.dds.publication.ReliableReaderActivityChangedStatus;
import com.rti.dds.topic.Topic;

/**
 * This class represents one BulkData Flow at the sender side. Your
 * application can send data to BulkData Receivers by calling the
 * methods of a instance of this class.
 *
 * <p>
 * To instantiate this class, call
 * {@link BulkDataNTSenderStream#createFlow(String, Optional)} or
 * {@link BulkDataNTSenderStream#createFlow(String, Optional,
 * BulkDataNTSenderFlowConfiguration)}. It is not allowed to directly
 * instantiate this class by calling constructor (it is not public anyway).
 * 
 * <p>
 * Once you create a new Flow, you can start sending the data. The procedure
 * to send data is shown below:
 * 
 * <ol type="1">
 *    <li> Call {@link #startSend(byte[])} to send a
 *         {@link alma.acs.bulkdata.ACSBulkData.BD_PARAM} frame
 *         with the parameter you specify. A parameter is a byte sequence
 *         of the size between 1 and
 *         {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN} bytes.
 *         The format of the parameter has to be agreed with the receiver
 *         side in the application level.
 *    <li> Call {@link #sendData(byte[])} to send your payload data
 *         the Receivers. You can send a byte sequence of arbitrary
 *         length as long as the JVM allows. If there is sufficient
 *         heap memory, the length of the byte sequence can be up to
 *         {@value java.lang.Integer#MAX_VALUE} bytes. The payload data
 *         is automatically split into small byte sequences of the size up
 *         to {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN} bytes,
 *         and transferred to the Receivers using
 *         {@link alma.acs.bulkdata.ACSBulkData.BD_DATA} frame. Then,
 *         the Sender Flow waits the acknowledgments from all the connected
 *         Receivers.
 *    <li> Repeat the step 2. to send more data to the Receivers if any.
 *    <li> Call {@link #stopSend()} to send
 *         {@link alma.acs.bulkdata.ACSBulkData.BD_STOP} frame, which
 *         informs the Receivers that you finished sending one chunk
 *         of data.
 *    <li> Go back to the step 1. to send more chunks of data, call
 *         {@link BulkDataNTSenderStream#deleteFlow(BulkDataNTSenderFlow)}
 *         to finish sending the data.
 * </ol>
 *
 * <p>
 * {@link #startSend(byte[])}, {@link #sendData(byte[])}, {@link #stopSend()}
 * and {@link #destroy()} which is internally called by
 * {@link BulkDataNTSenderStream#deleteFlow(BulkDataNTSenderFlow)} are allowed
 * to be called only if this Sender Flow is in a certain state. The state
 * transition diagram of a Sender Flow is shown below:
 *
 * <img alt="State Transition Diagram of Sender Flow"
 *      src="doc-files/state_transition_diagram_of_flow.png"
 *      width="75%" height="75%">
 *
 * <p>
 * For your convenience, the sequence diagram of a Sender Flow, which shows
 * how the data is actually propagated to the Receivers, is shown below:
 * 
 * <img alt="Sequence Diagram of Sender Flow"
 *      src="doc-files/sender_flow_sequence.png">
 *      
 * "DDS Data Writer" in the above sequence diagram is the entity provided by
 * the underlying DDS library, which buffers the data and transmit it to
 * the Receivers using DDS technology. Note that the actual behavior of 
 * DDS Data Writer and BulkData Receiver may be different from this diagram.
 * This sequence diagram is provided to help the application programmers to
 * understand how the data is actually sent.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTSenderFlow extends BulkDataNTFlow {
    private final BulkDataNTSenderStream stream;
    private final DomainParticipant participant;
    private final Topic topic;
    private final Publisher publisher;
    private final BulkDataNTFrameDataWriter dataWriter;
    private final BulkDataNTFrame paramFrame;
    private final BulkDataNTFrame dataFrame;
    private final BulkDataNTFrame stopFrame;
    private final Optional<BulkDataNTSenderFlowListener> listener;
    private final BulkDataNTSenderFlowConfiguration conf;
    private final Optional<Logger> logger;
    
    /**
     * This is the thread that is executing {@link #sendData(byte[])}
     * This variable contains a valid thread if the current state is
     * {@link State#BUSY}. Otherwise, this variable holds null. 
     * 
     * <p>
     * This instance variable must be used in synchronized(stateLock)
     * clause. 
     */
    private Thread threadExecutingSendData = null;
    
    /**
     * The data size of the payload in BD_DATA frame that were sent
     * last time. This will be used to adjust the data transmission
     * rate.
     */
    private int dataSizeLastSent;

    /**
     * This is the time when this Flow was supposed to start to send
     * the last BD_DATA frame. This is typically several milliseconds
     * earlier than the actual time when this Flow started to send
     * the last BD_DATA frame due to poor accuracy of sleep function
     * and some other overheads.
     *
     * This variable is null if no BD_DATA frame has been sent so far.
     */
    private Instant lastTimeDataToSend;


    /**
     * This enumeration represents a state of {@link BulkDataNTSenderFlow}.
     */
    public enum State {
        /**
         * In this state, the Flow is ready to accept {@link #startSend(byte[])}.
         *
         * <p>
         * When {@link #startSend(byte[])} method is called, the Flow first
         * transitions to {@link #STARTING} state, and finally to {@link #WAIT}
         * state when {@link #startSend(byte[])} method successfully completes
         * its operation.
         *
         * <p>
         * If {@link #startSend(byte[])} method fails for some reason, the
         * Flow goes back to {@link #STOP} state.
         *
         * <p>
         * {@link #sendData(byte[])} and {@link #stopSend()} methods are not
         * allowed to be called when the Flow is in this state.
         */
        STOP,

        /**
         * This is a transitional state where the Flow is performing
         * {@link #startSend(byte[])} operation. In this state, neither 
         * {@link #startSend(byte[])}, {@link #sendData(byte[])} nor
         * {@link #stopSend()}
         * is allowed.
         */
        STARTING,

        /**
         * In this state, the Flow is ready to accept {@link #sendData(byte[])}
         * or {@link #stopSend()}. If {@link #sendData(byte[])} is called, this
         * Flow transitions to {@link #BUSY} state immediately.
         *
         * <p>
         * If {@link #stopSend()} is called, the Flow transitions to
         * {@link #STOPPING} state, and finally {@link #STOP} state after
         * {@link #stopSend()} operation is successfully completed. Otherwise,
         * the Flow goes back to {@link #WAIT} state.
         *
         * <p>
         * {@link #startSend(byte[])} method is not allowed in this state.
         */
        WAIT,

       /**
        * This is a transitional state where the Flow is performing
        * {@link #stopSend()} operation. In this state, neither
        * {@link #startSend(byte[])}, {@link #sendData(byte[])} nor
        * {@link #stopSend()} is allowed.
        */
       STOPPING,

       /**
        * In this state, the Flow is sending the data passed by
        * {@link #sendData(byte[])}, or idling to adjust the data transmission
        * rate. When the data transmission of the given data is completed,
        * the Flow automatically transitions back to {@link #WAIT} state.
        *
        * <p>
        * In this state, neither {@link #startSend(byte[])},
        * {@link #sendData(byte[])} nor {@link #stopSend()} is allowed.
        *
        * <p>
        * Another thread can interrupt the thread that is executing
        * {@link #sendData(byte[])}. In this case, {@link #sendData(byte[])}
        * may throw {@link java.lang.InterruptedException}, and this Flow
        * transitions back to {@link #WAIT} state.
        */
       BUSY,

       /**
        * When the Flow is destroyed, it transitions to this state.
        *
        * <p>
        * In a strict sense, this Flow is in this state if it has been already
        * destroyed, or if one thread is destroying this Flow right now.
        *
        * <p>
        * In this state, no further operation is permitted. The Flow cannot
        * transition to another state from this state.
        */
       DESTROYED
       };
    private State state;
    private Object stateLock = new Object();

    /**
     * Create a new sender Flow with the given name.
     *
     * @param name Flow name that fulfills the criteria described in
     *             {@link #getName()}.
     * @param stream The instance of parent stream. This must not be null.
     * @param listener The listener that monitors the events of this Sender
     *                 Flow. This can be empty if the application does not need
     *                 to listen to the events.
     * @param conf The configuration of this flow. Specify null to use
     *             the default configuration.
     *
     * @exception InvalidFlowNameException
     * if the given name does not fulfill the criteria described in
     * {@link #getName()}
     * @exception IllegalArgumentException,
     * if either stream or listener is null.
     * @throws QosLibraryNotExistException
     * @throws QosLibraryNotExistException
     * if the QoS library name given by the specified configuration does not
     * exist.
     * @throws QosProfileNotExistException
     * if the QoS profile name given by the specified configuration does not
     * exist in the QoS library.
     * @throws DdsException
     * if the underlying DDS library raises an exception or detects an error
     */
    BulkDataNTSenderFlow(String name,
                         BulkDataNTSenderStream stream,
                         Optional<BulkDataNTSenderFlowListener> listener,
                         BulkDataNTSenderFlowConfiguration conf)
        throws StreamAlreadyDestroyedException,
               InvalidFlowNameException,
               IllegalConfigurationException,
               QosLibraryNotExistException,
               QosProfileNotExistException,
               DdsException {
        super(name, stream);

        Validate.notNull(stream);
        Validate.notNull(listener);

        this.stream = stream;
        this.listener = listener;
        this.state = State.STOP;

        String msg = String.format("When creating Flow \"%s\" in Stream " +
                                   "\"%s\", the Stream was already destroyed.",
                                   name, stream.getName());
        this.participant
            = stream.getDomainParticipant()
            .orElseThrow(() -> new StreamAlreadyDestroyedException(msg, stream));

        // Use default configuration if not explicitly specified.
        if (conf != null) {
            this.conf = new BulkDataNTSenderFlowConfigurationStore(conf);
        } else {
            this.conf = new BulkDataNTSenderFlowConfigurationDefault();
        }

        validateConfiguration(this.conf);

        logger = this.conf.getLogger();

        try {
            String topicName
                = stream.getName() + STREAM_FLOW_NAME_SEPARATOR + name;
            topic = getTopic(this.participant, topicName,
                             this.conf.getQosLibraryName(), this.conf.getQosProfileName(),
                             this);
            publisher = createPublisher(this.participant,
                                        this.conf.getQosLibraryName(),
                                        this.conf.getQosProfileName(),
                                        this);
            Duration_t sendFrameTimeout
                = BulkDataNTUtil.convertDuration(this.conf.getSendFrameTimeout());
            dataWriter = createDataWriter(publisher, topic, this.listener,
                                          this.conf.getQosLibraryName(),
                                          this.conf.getQosProfileName(),
                                          this, sendFrameTimeout);

            paramFrame = createFrame();
            paramFrame.typeOfdata = BD_PARAM.VALUE;
            paramFrame.restDataLength = 0;

            dataFrame  = createFrame();
            dataFrame.typeOfdata = BD_DATA.VALUE;
        
            stopFrame  = createFrame();
            stopFrame.typeOfdata = BD_STOP.VALUE;
            stopFrame.restDataLength = 0;

            dataSizeLastSent = 0;
            lastTimeDataToSend = null;
        } catch (DdsException ex) {
            // Clean up the created DDS entities, then throw the exception.
            deleteDdsEntities();
            state = State.DESTROYED;

            throw ex;
        }
    }

    /**
     * This method returns a topic of the given name. If it does not
     * exist, it creates a new topic of the given name with the appropriate
     * QoS configuration for BulkData.
     *
     * <p>
     * This method is intended to be called from the constructor, so
     * please do not call this method outside the constructor unless
     * you know what exactly you are doing.
     *
     * @throws DdsException
     * if the underlying DDS library fails to create the topic.
     */
    private static Topic getTopic(DomainParticipant participant,
                                  String topicName,
                                  String libraryName,
                                  String profileName,
                                  BulkDataNTFlow flow)
        throws DdsException {
        Duration_t timeout = new Duration_t(0, 5000000); // 5 ms
        BulkDataNTStream stream = flow.getStream();
        String typeName = stream.getDataTypeName();

        // Auxiliary information for the exception.
        @SuppressWarnings("serial")
        Map<String, String> aux = new LinkedHashMap<String, String>() {{
                put("topic",   topicName);
                put("type" ,   typeName);
                put("library", libraryName);
                put("proflie", profileName);
            }};

        Topic t = participant.find_topic(topicName, timeout);
        if (t != null) {
            return t;
        }
        t = participant
            .create_topic_with_profile(topicName, typeName,
                                       libraryName, profileName,
                                       null, StatusKind.STATUS_MASK_NONE);
                                       // we do not use listener
        if (t == null) {
            throwDdsException("Failed to create a new Topic.",
                              null, aux, stream, flow);
        }

        try {
            t.enable();
        } catch (RETCODE_ERROR error) {
            throwDdsException("Failed to enable the Topic.",
                              error, aux, stream, flow,
                              FailedToEnableException.class);
        }

        return t;
    }

    /**
     * This method creates a new publisher with the appropriate QoS
     * configuration for BulkData.
     *
     * <p>
     * This method is intended to be called from the constructor, so
     * please do not call this method outside the constructor unless
     * you know what exactly you are doing.
     *
     * @throws DdsException
     * if the underlying DDS library fails to create the publisher.
     */
    private static Publisher createPublisher(DomainParticipant participant,
                                             String libraryName,
                                             String profileName,
                                             BulkDataNTFlow flow)
        throws DdsException {
        BulkDataNTStream stream = flow.getStream();

        // Auxiliary information for the exception.
        @SuppressWarnings("serial")
        Map<String, String> aux = new LinkedHashMap<String, String>() {{
                put("library", libraryName);
                put("proflie", profileName);
            }};

        // Instantiate DDS publisher
        Publisher p = participant
            .create_publisher_with_profile(libraryName, profileName,
                                           null, // listener is not used.
                                           StatusKind.STATUS_MASK_NONE);
        if (p == null) {
            throwDdsException("Failed to create a new Publisher.",
                              null, aux, stream, flow);
        }

        PublisherQos pubQos = new PublisherQos();
        try {
            p.get_qos(pubQos);
        } catch (RETCODE_ERROR error) {
            throwDdsException("Failed to get QoS of the Publisher.",
                              error, aux, stream, flow,
                              FailedToGetQosException.class);
        }

        try {
            pubQos.entity_factory.autoenable_created_entities = false;
            p.set_qos(pubQos);
        } catch (RETCODE_ERROR error) {
            throwDdsException("Failed to set QoS of the Publisher.",
                              error, aux, stream, flow,
                              FailedToSetQosException.class);
        }

        try {
            p.enable();
        } catch (RETCODE_ERROR error) {
            throwDdsException("Failed to enable the Publisher.",
                              error, aux, stream, flow,
                              FailedToEnableException.class);
        }

        return p;
    }

    /**
     * This method creates a new data writer for BulkData.
     *
     * This method is intended to be called from the constructor, so
     * please do not call this method outside the constructor unless
     * you know what exactly you are doing.
     *
     * @param p An instance of Publisher.
     * @param t An instance of Topic.
     * @param flowListener The listener that monitors the event of this Flow.
     *                     This can be empty if the event monitoring is not
     *                     required.
     * @param libraryName QoS library name to create a new Data Writer.
     * @param profileName QoS profile name to create a new Data Writer.
     * @param flow The associated flow.
     * @param sendFrameTimeout The timeout setting for frame sending.
     *
     * @exception IllegalArgumentException if either one of the given
     *                                     parameters is null.
     * @throws DdsException
     * if the underlying DDS library fails to create the data writer.
     */
    private static BulkDataNTFrameDataWriter
        createDataWriter(Publisher p, Topic t,
                         Optional<BulkDataNTSenderFlowListener> flowListener,
                         String libraryName,
                         String profileName,
                         BulkDataNTSenderFlow flow,
                         Duration_t sendFrameTimeout)
        throws DdsException {
        Validate.notNull(p);
        Validate.notNull(t);
        Validate.notNull(flowListener);

        // Auxiliary information for the exception.
        @SuppressWarnings("serial")
        Map<String, String> aux = new LinkedHashMap<String, String>() {{
                put("library", libraryName);
                put("proflie", profileName);
            }};

        BulkDataNTStream stream = flow.getStream();

        // Instantiate DDS publisher
        BulkDataNTDataWriterListener dwListener
            = new BulkDataNTDataWriterListener(flowListener, flow,
                                               flow.getLogger());

        DataWriter dwRaw
            = p.create_datawriter_with_profile(t, libraryName, profileName,
                                               dwListener,
                                               StatusKind.STATUS_MASK_ALL);
        if (dwRaw == null) {
            throwDdsException("Failed to create a new Data Writer.",
                              null, aux, stream, flow);
        } else if (!(dwRaw instanceof BulkDataNTFrameDataWriter)) {
            throwDdsException("The returned Data Writer is not an instnace " +
                              "of BulkDataNTFrameDataWriter.",
                              null, aux, stream, flow);
        }

        BulkDataNTFrameDataWriter dw = (BulkDataNTFrameDataWriter) dwRaw;

        DataWriterQos dwQos = new DataWriterQos();
        try {
            dw.get_qos(dwQos);
        } catch (RETCODE_ERROR error) {
            throwDdsException("Failed to get QoS of the Data Writer.",
                              error, aux, stream, flow,
                              FailedToGetQosException.class);
        }

        Duration_t timeout = sendFrameTimeout;
        dwQos.reliability.max_blocking_time.sec = timeout.sec;
        dwQos.reliability.max_blocking_time.nanosec = timeout.nanosec;

        try {
            dw.set_qos(dwQos);
        } catch (RETCODE_ERROR error) {
            // Note: It seems that error.getMessage() always (or sometimes?)
            // contains null in case of RETCODE_INCONSISTENT_POLICY, and the
            // detailed error message is provided to DDS logging facility instead.

            throwDdsException("Failed to set QoS of the Data Writer.",
                              error, aux, stream, flow,
                              FailedToSetQosException.class);
        } 

        try {
            dw.enable();
        } catch (RETCODE_ERROR error) {
            throwDdsException("Failed to enable the Data Writer.",
                              error, aux, stream, flow,
                              FailedToEnableException.class);
        }

        return dw;
    }

    /**
     * This method creates a new BulkData frame.
     *
     * This method is intended to be called from the constructor, so
     * please do not call this method outside the constructor unless
     * you know what exactly you are doing.
     */
    private BulkDataNTFrame createFrame() {
        // There is no document on this method, but the generated code
        // of BulkDataNTFrame seems that it does not throw any exception,
        // and it always returns the new instance of BulkDataNTFrame.
        // So, error handling and type checking is not done here.
        BulkDataNTFrame frame = (BulkDataNTFrame)BulkDataNTFrame.create();

        // It is strange that RTI Connext Java API says that setMaximum(int)
        // returns true on success, otherwise false while the return type
        // is defined as "void".
        // https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/html/api_java/interfacecom_1_1rti_1_1dds_1_1util_1_1Sequence.html
        //
        // This code assumes that this method always succeeds, and does not
        // handle any error.
        frame.data.setMaximum(0); // We need to do this to use loaning.

        return frame;
    }

    /**
     * This method sends the given parameter to the connected Receivers in
     * the same Flow.
     * 
     * <p>
     * A parameter is a byte sequence of the size between 1 and
     * {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN} bytes.
     * The format of the parameter is arbitrary, so the format has be
     * agreed with the receiver side in the application level.
     * 
     * <p>
     * The given parameter is transferred to all the connected Receivers
     * in the same Flow using {@link alma.acs.bulkdata.ACSBulkData.BD_PARAM}
     * frame.
     *
     * <p>
     * This method basically does not block the caller for a long time. This
     * method simply writes the given parameter to the buffer in the underlying
     * DDS library and returns quickly. If the buffer is full for some reason
     * (it usually does not happen), this method may block the caller until
     * the send frame timeout is elapsed. The send frame timeout is the value
     * obtained by calling
     * {@link BulkDataNTSenderFlowConfiguration#getSendFrameTimeout()} method
     * of the configuration object that was passed when creating this Sender
     * Flow.
     * 
     * <p>
     * Please note that this method does not wait acknowledgments from the
     * connected Receivers, which means it is not guaranteed that the given
     * parameter is received by all the connected Receivers. You must call
     * {@link #sendData(byte[])} to wait until all the Receivers send back
     * acknowledgments for the parameter that this method sends, AND the data
     * that {@link #sendData(byte[])} sends. 
     *
     * @param param Serialized parameter. The size must not exceed
     *              {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN}
     *              bytes.
     *
     * @throws InappropriateSenderFlowStateException
     * if this Flow is not in STOP state.
     * @throws IllegalArgumentException
     * if the length of the given parameter is larger than
     * {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN} bytes, or
     * if it is empty.
     * @throws SendFrameTimeoutException
     * if the configured timeout has been elapsed before the given parameter
     * is completely written to the send buffer.
     * @throws DdsException
     * if the underlying DDS library encounters an error.
     */
    public void startSend(byte[] param)
        throws InappropriateSenderFlowStateException, 
               IllegalArgumentException,
               SendFrameTimeoutException,
               DdsException {
        if (param.length > FRAME_MAX_LEN) {
            String msg
                = String.format("The given parameter length is %d bytes. " +
                                "It must not be larger than %d bytes.",
                                param.length, FRAME_MAX_LEN);
            throw new IllegalArgumentException(msg);
        } else if (param.length == 0) {
            String msg = "The given parameter is empty. " +
                         "It must be, at least, 1 byte or more.";
            throw new IllegalArgumentException(msg);
        }
        
        // Auxiliary information for the exception.
        @SuppressWarnings("serial")
        Map<String, String> aux = new LinkedHashMap<String, String>() {{
                put("operation", "startSend");
                put("data size", String.valueOf(param.length));
            }};

        synchronized (stateLock) {
            logTraceWithState("startSend(byte[]) is called.", aux);
            validateState(State.STOP, "startSend(byte[])");
            state = State.STARTING;
        }
        
        logNumberOfReceivers("startSend");

        assert paramFrame.typeOfdata == BD_PARAM.VALUE;
        assert paramFrame.restDataLength == 0; // This is always 0 according to
                                               // C++ implementation.
        
        // According to RTI Connext Java API 5.1.0, loan() method may fail,
        // and return boolean value to indicate whether loan() was
        // successfully completed or not, but the return type of this
        // method is defined as void. We cannot help but ignoring
        // the failure case here...
        // https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/html/api_java/classcom_1_1rti_1_1dds_1_1util_1_1AbstractPrimitiveSequence.html#a3d685320eb3216680d1982ce660a0a1a
        paramFrame.data.loan(param, param.length);

        try {
            dataWriter.write(paramFrame, InstanceHandle_t.HANDLE_NIL);
        } catch (RETCODE_TIMEOUT error) {
            transitionTo(State.STOP, State.STARTING);
            throwDdsException("startSend() timed out.",
                              error, aux, stream, this,
                              SendFrameTimeoutException.class);
        } catch (RETCODE_ERROR error) {
            transitionTo(State.STOP, State.STARTING);
            throwDdsException("startSend() failed to due to an error " + 
                              "raised by the underlying DDS library.",
                              error, aux, stream, this);
        }

        // According to RTI Connext Java API 5.1.0, unloan() method may fail,
        // and return boolean value to indicate whether unloan() was
        // successfully completed or not, but the return type of this
        // method is defined as void. We cannot help but ignoring
        // the failure case here...
        // https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/html/api_java/classcom_1_1rti_1_1dds_1_1util_1_1AbstractPrimitiveSequence.html#ac764ac2f50134b498367dc885fab6a2e
        paramFrame.data.unloan();

        transitionTo(State.WAIT, State.STARTING);
    }

    /**
     * This method sends the given binary data to the connected Receivers in the
     * same Flow.
     *
     * <p>
     * This method sends the given data to all the connected Receivers at 
     * the data transmission rate specified by the throttling configuration.
     * You can specify a byte array of arbitrary length as long as the JVM
     * allows. If there is sufficient heap memory, the length of the byte
     * sequence can be up to {@value java.lang.Integer#MAX_VALUE} bytes.
     * 
     * <p>
     * At the end of this method call, this method waits the acknowledgments
     * from the Receivers for all the data that have been previously sent
     * including the frames that were sent by {@link #startSend(byte[])} and
     * {@link #stopSend()}. If this Sender Flow fails to receive all
     * acknowledgments before the timeout (this configuration value is obtained
     * from {@link BulkDataNTSenderFlowConfiguration#getAckTimeout()}) has
     * elapsed, this method throws {@link AckTimeoutException}.
     *
     * <p>
     * MINOR TODO: currently, the internal of DDS communication is not fully
     * understood, but this method perhaps waits the acknowledgments of
     * all the frames that have been previously sent including the BD_STOP
     * frame that was sent in the previous sub-scan. This is probably not
     * what we want. Even if Receivers fail to receive BD_STOP frames,
     * no user data is lost (the BD_STOP frames do not convey any payload),
     * so the Sender would want to send other data.
     * 
     * <p>
     * This issue could be solved by waiting the acknowledgments after
     * a BD_STOP frame is sent, but the behavior of the Sender Flow will
     * be slightly different from C++ version. As of implementing this method,
     * the author gave the priority to the compatibility with C++ version.
     *
     * @param data The serialized data to be sent to the receivers. The whole
     *             array will be sent by this method.
     *
     * @throws InappropriateSenderFlowStateException
     * if this Sender Flow is not in {@link State#WAIT} state.
     * @throws IllegalArgumentException
     * if the given data is empty.
     * @throws InterruptedException
     * if the thread executing this method is interrupted while adjusting
     * the time to send data to respect the throttling configuration. This
     * exception indicates that not all the given data has been sent to
     * the Receivers. Maybe some part of the give data has been already
     * sent to the Receivers, or none of them has been sent.
     * @throws SendFrameTimeoutException
     * if the configured timeout has been elapsed before the given parameter
     * is completely written to the send buffer. This indicates that not
     * all data has been transmitted to the Receivers.
     * @throws AckTimeoutException
     * if the timeout occurs while waiting all the acknowledgments of the
     * frames that have been previously sent before this method call and
     * the frames sent in this method. This method waits the acknowledgments
     * from all the connected Receivers. This exception indicates that
     * the underlying DDS library already knows what data to be sent,
     * and most probably they are already sent to the Receivers, but some
     * of the Receivers may have failed to receive the data. It is possible
     * that all data will be eventually delivered to the Receivers even if
     * this exception occurs.
     * @throws DdsException
     * if the underlying DDS library encounters an error.
     */
    public void sendData(byte[] data)
        throws InappropriateSenderFlowStateException,
               InterruptedException,
               SendFrameTimeoutException,
               AckTimeoutException,
               DdsException {
        Validate.notNull(data);
        if (data.length == 0) {
            throw new IllegalArgumentException("Empty data is specified.");
        }
        
        // Auxiliary information for the exception.
        @SuppressWarnings("serial")
        Map<String, String> aux = new LinkedHashMap<String, String>() {{
                put("operation", "sendData");
                put("data size", String.valueOf(data.length));
            }};

        synchronized (stateLock) {
            logTraceWithState("sendData(byte[]) is called.", aux);
            validateState(State.WAIT, "sendData(byte[])");
            state = State.BUSY;
            threadExecutingSendData = Thread.currentThread();
        }
        
        // The number of bytes that were already sent.
        int sent = 0;

        while (sent < data.length) {
            // The size of data in bytes to be sent in this iteration.
            int toSend =
                (data.length - sent) > FRAME_MAX_LEN ?
                FRAME_MAX_LEN :
                (data.length - sent);
            int restNumOfFrames =
                (data.length - sent - toSend) % FRAME_MAX_LEN == 0 ?
                (data.length - sent - toSend) / FRAME_MAX_LEN :
                (data.length - sent - toSend) / FRAME_MAX_LEN + 1;

            // Note: due to Java restriction, we cannot get only a part
            //       of an array without copying. Unlike C/C++, Java does
            //       allow to increment or decrement the pointer.
            //       For this reason, this BulkData Java library allocates
            //       a new memory in  the heap area of the same size in
            //       total as the given data every time sendData(byte[])
            //       is called. The memory allocation is done every 64000
            //       bytes, and it is likely that this array first goes to
            //       the young generation heap area and will be dereferenced
            //       once write() operation is completed. Typically, the
            //       write() method returns very quickly. Thus, the arrays
            //       that are newly created here is most likely to be garbage-
            //       collected by Minor GC (not Full GC), which takes very
            //       short time in general, and does not have a significant
            //       impact on the applications performance.
            byte[] part = Arrays.copyOfRange(data, sent, sent + toSend);
            assert part.length == toSend;
            
            assert dataFrame.typeOfdata == BD_DATA.VALUE;
            dataFrame.restDataLength = restNumOfFrames;
            dataFrame.data.loan(part, part.length);

            if (lastTimeDataToSend == null) {
                // This is the first time for this Flow to send data.
                // Do not have to adjust the throttling. Send data now!
                lastTimeDataToSend = Instant.now();
            } else if (sent == 0) {
                // This is the first data to send within this method call.
                //
                // Check when the last time this Flow sent data and its size.
                // If enough time has not been elapsed since the last time,
                // this method first sleeps so that the data transmission rate
                // will not exceed the throttling configuration. Otherwise, this
                // method immediately starts to send data.
                Instant absTimeToSend
                    = calculateAbsoluteTimeToSendNextData(lastTimeDataToSend,
                                                          dataSizeLastSent,
                                                          conf.getThrottling());
                Instant now = Instant.now();
                if (absTimeToSend.isAfter(now)) {
                    try {
                        sleepUntil(absTimeToSend);
                    } catch (InterruptedException ex) {
                        // At this point, no data has been sent to the
                        // Receivers yet. If applications want to distinguish
                        // this case from the the case where some part of the
                        // given data is already sent, throw a different type
                        // of exception here.
                        transitionTo(State.WAIT, State.BUSY);
                        throw ex;
                    }
                    lastTimeDataToSend = absTimeToSend;
                } else {
                    lastTimeDataToSend = now;
                }
            } else {
                // Adjust the time to send the next data in a way that the data
                // transmission rate will not exceed the throttling configuration.
                //
                // Note that here "data transmission rate" is the average rate
                // within this method call. This method sends large data frame
                // by frame, and the data transmission rate is kept below the
                // throttling limit by adjusting the time between two frames.
                // The instantaneous data transmission rate may be higher than
                // the throttling limit, which surely happens while sending
                // one frame.
                Instant absTimeToSend
                    = calculateAbsoluteTimeToSendNextData(lastTimeDataToSend,
                                                          dataSizeLastSent,
                                                          conf.getThrottling());
                try {
                    sleepUntil(absTimeToSend);
                } catch (InterruptedException ex) {
                    transitionTo(State.WAIT, State.BUSY);
                    throw ex;
                }
                lastTimeDataToSend = absTimeToSend;
            }

            dataSizeLastSent = toSend;
            
            String msg = String.format("Sending %d bytes. " +
                                       "Rest number of frames is %d" +
                                       "@ %s.",
                                       part.length, restNumOfFrames,
                                       Instant.now().toString());
            logTrace(msg, aux);

            try {
                dataWriter.write(dataFrame, InstanceHandle_t.HANDLE_NIL);
            } catch (RETCODE_TIMEOUT error) {
                transitionTo(State.WAIT, State.BUSY);
                throwDdsException("sendData() timed out.",
                                  error, aux, stream, this,
                                  SendFrameTimeoutException.class);
            } catch (RETCODE_ERROR error) {
                transitionTo(State.WAIT, State.BUSY);
                throwDdsException("sendData() failed to due to an error " + 
                                  "raised by the underlying DDS library.",
                                  error, aux, stream, this);
            }
            
            dataFrame.data.unloan();

            sent += toSend;
        }

        // After we send the last frame, we wait ACKs.
        logTrace(String.format("Waitint for ACKs for %s seconds",
                               BulkDataNTUtil
                               .convertDurationToString(conf.getAckTimeout())),
                 aux);
        
        Duration_t ackTimeout
            = BulkDataNTUtil.convertDuration(conf.getAckTimeout());
        try {
            dataWriter.wait_for_acknowledgments(ackTimeout);
        } catch (RETCODE_TIMEOUT error) {
            transitionTo(State.WAIT, State.BUSY);
            throwDdsException("sendData() timed out before receiving ACKs.",
                              error, aux, stream, this,
                              AckTimeoutException.class);
        } catch (RETCODE_ERROR error) {
            transitionTo(State.WAIT, State.BUSY);
            throwDdsException("sendData() failed while waiting ACKs to due " +
                              "an error raised by the underlying DDS library.",
                              error, aux, stream, this);
        }

        transitionTo(State.WAIT, State.BUSY);
    }

    /**
     * This method sends {@link alma.acs.bulkdata.ACSBulkData.BD_STOP} frame
     * to the connected Receivers in the same Flow.
     * 
     * <p>
     * Your application can notify the Receivers that your application finished
     * sending a certain chunk of data. The definition of "chunk" is arbitrary,
     * and it has to be agreed with the receiver side in the application level.
     * It is even possible that, your application keep sending data for a day
     * without calling this method, but it is your choice.
     * 
     * <p>
     * This method basically does not block the caller for a long time. This 
     * method simply writes a {@link alma.acs.bulkdata.ACSBulkData.BD_STOP}
     * frame to the buffer in the underlying DDS library and returns quickly.
     * Normally, the buffer is not full when calling this method because
     * the previous calls of {@link #sendData(byte[])} waits the
     * acknowledgments from the connected Receivers and the buffer should be
     * empty after all acknowledgments are received by the Sender.
     * 
     * <p>
     * However, for some reason (e.g. the previous call of
     * {@link #sendData(byte[])} timed out), the buffer could be full when
     * this method is called. In that case, the caller may be blocked until
     * sufficient space in the buffer becomes available for the new 
     * {@link alma.acs.bulkdata.ACSBulkData.BD_STOP} frame, or the 
     * send frame timeout is elapsed. The send frame timeout is the 
     * value obtained by calling
     * {@link BulkDataNTSenderFlowConfiguration#getSendFrameTimeout()}
     * method of the configuration object that was passed when creating this
     * Sender Flow.
     *
     * @throws InappropriateSenderFlowStateException
     * if this Sender Flow is not in {@link State#WAIT} state.
     * @throws SendFrameTimeoutException
     * if the configured timeout has been elapsed before
     * {@link alma.acs.bulkdata.ACSBulkData.BD_STOP} frame is completely
     * written to the buffer in the underlying DDS library.
     * @throws DdsException
     * if the underlying DDS library encounters an error.
     */
    public void stopSend()
        throws InappropriateSenderFlowStateException,
               SendFrameTimeoutException,
               DdsException {
        // Auxiliary information for the exception.
        @SuppressWarnings("serial")
        Map<String, String> aux = new LinkedHashMap<String, String>() {{
                put("operation", "stopSend");
            }};

        synchronized (stateLock) {
            logTraceWithState("stopSend() is called.", aux);
            validateState(State.WAIT, "stopData()");
            state = State.STOPPING;
        }

        logNumberOfReceivers("stopSend");

        try {
            assert stopFrame.typeOfdata == BD_STOP.VALUE;
            assert stopFrame.restDataLength == 0;
            assert stopFrame.data.getMaximum() == 0;

            dataWriter.write(stopFrame, InstanceHandle_t.HANDLE_NIL);
        } catch (RETCODE_TIMEOUT error) {
            transitionTo(State.WAIT, State.STOPPING);
            throwDdsException("stopSend() timed out.",
                              error, aux, stream, this,
                              SendFrameTimeoutException.class);
        } catch (RETCODE_ERROR error) {
            transitionTo(State.WAIT, State.STOPPING);
            throwDdsException("stopSend() failed to due to an error " + 
                              "raised by the underlying DDS library.",
                              error, aux, stream, this);
        }

        transitionTo(State.STOP, State.STOPPING);
    }

    /**
     * This method returns the number of receivers that are connected to
     * this Flow.
     *
     * @return The number of receivers that are connected to this Flow.
     *
     * @throws DdsException
     * if the underlying DDS library encounters an error.
     */
    public int getNumberOfReceivers()
        throws DdsException {
        ReliableReaderActivityChangedStatus status
            = new ReliableReaderActivityChangedStatus();

        try {
            dataWriter.get_reliable_reader_activity_changed_status(status);
        } catch (RETCODE_ERROR error) {
            throwDdsException("Failed to obtain the number of the connected " +
                              "receivers.", error);
        }

        return status.active_count;
    }

    /**
     * This waits until the state becomes either {@link State#WAIT} or
     * {@link State#STOP}, and forcibly destroy the Flow. It means that
     * BD_STOP frame may not be sent to the Receivers.
     * 
     * At most, the caller is blocked up to the ACK timeout or the send
     * frame timeout, whichever is longer, plus some overhead.
     * 
     * <p>
     * If the state is already DESTROYED, this method does nothing.
     *
     * @throws InterruptedException
     * if the thread executing this method is interrupted. If this happens,
     * this flow is left as undestroyed.
     * @throws DdsException
     * if the underlying DDS library fails to delete DDS entities. Even if
     * this occurs, this Flow is considered as destroyed. The application
     * can ignore this exception, but may need force quit at the end of
     * the application execution.
     */
    void forceDestroy()
        throws InterruptedException,
               DdsException {
        // Auxiliary information for the exception.
        @SuppressWarnings("serial")
        Map<String, String> aux = new LinkedHashMap<String, String>() {{
                put("operation", "destroy");
            }};

        synchronized (stateLock) {
            logTraceWithState("forceDestroy() is called.", aux);
            
            if (state == State.DESTROYED) {
                return;
            } else if (state == State.BUSY) {
                assert threadExecutingSendData != null;
                // Interrupt the sleep call within sendData() method
                // so that this flow can be destroyed quicker.
                threadExecutingSendData.interrupt();
            }

            // Note that write() and wait_for_acknowledgments()
            // may not be interruptible. So, this thread may need
            // to wait for at most the ACK timeout or the send frame
            // timeout, whichever is longer until the state becomes
            // either STOP, WAIT or DESTROYED.
            while (state != State.STOP &&
                   state != State.WAIT &&
                   state != State.DESTROYED) {
                try {
                    stateLock.wait(1000);

                    // Note: the timeout of wait(int) method call is set to
                    //       1 second just in case the thread that holds
                    //       stateLock forgets to call notifyAll().
                } catch (InterruptedException ex) {
                    // Now the caller gets angry. Let's not try to destroy
                    // this Flow and terminate this thread right now.
                    throw ex;
                }
            }

            if (state == State.DESTROYED) {
                // Another thread already destroyed this Sender Flow
                // while waiting, so do nothing except notify other
                // threads that are waiting.
                stateLock.notifyAll();
                return;
            }

            assert state == State.WAIT || state == State.STOP;
            state = State.DESTROYED;
            stateLock.notifyAll(); // start other threads that are waiting 
                                   // at the above "stateLock.wait()" line.
        }

        deleteDdsEntities();
    }

    /**
     * Release all resources allocated for this flow. The application
     * programmers must not call this function directly. To destroy this
     * Flow call {@link BulkDataNTSenderStream#deleteFlow(BulkDataNTSenderFlow)}
     * to remove a certain Flow individually, or call
     * {@link BulkDataNTSenderStream#destroy()} to destroy all Flows in that
     * Stream.
     *
     * <p>
     * If this Flow has been already destroyed, this method does nothing.
     *
     * @throws InappropriateSenderFlowStateException
     * if this Sender Flow is not in {@link State.STOP} state.
     * @throws DdsException
     * if the underlying DDS library fails to delete DDS entities. Even if
     * this occurs, this Flow is considered as destroyed. The application
     * can ignore this exception, but may need force quit at the end of
     * the application execution.
     */
    void destroy()
        throws InappropriateSenderFlowStateException,
               DdsException {
        // Auxiliary information for the exception.
        @SuppressWarnings("serial")
        Map<String, String> aux = new LinkedHashMap<String, String>() {{
                put("operation", "destroy");
            }};

        synchronized (stateLock) {
            logTraceWithState("destroy() is called.", aux);

            if (state == State.DESTROYED) {
                return;
            }

            validateState(State.STOP, "destroy()");
            state = State.DESTROYED;
            stateLock.notifyAll(); // notify the other threads that this thread
                                   // is going to be destroyed by this thread.
        }

        deleteDdsEntities();
    }
    
    /**
     * This method returns true if this Flow has been already destroyed.
     * 
     * @return True if this Flow has been already destroyed.
     */
    public boolean isDestroyed() {
        synchronized (stateLock) {
            return state == State.DESTROYED;
        }
    }

    /**
     * This method deletes all DDS entities (Topic, Publisher and DataWriter).
     *
     * <p>
     * Even if this method encounters an error in the middle, it tries to delete
     * as many entities as much as possible, then throw {@link DdsException}.
     *
     * @throws DdsException
     * if the underlying DDS library fails to delete DDS entities.
     */
    private synchronized void deleteDdsEntities() throws DdsException {
        List<String> undeleted = new ArrayList<>();
        RETCODE_ERROR firstError = null;

        try {
            if (dataWriter != null) {
                publisher.delete_datawriter(dataWriter);
            }
        } catch (RETCODE_ERROR error) {
            undeleted.add("Data Writer");
            firstError = (firstError == null ? error : firstError);
        }

        try {
            if (publisher != null) {
                participant.delete_publisher(publisher);
            }
        } catch (RETCODE_ERROR error) {
            undeleted.add("Publisher");
            firstError = (firstError == null ? error : firstError);
        }

        try{
            if (topic != null) {
                participant.delete_topic(topic);
            }
        } catch (RETCODE_ERROR error) {
            undeleted.add("Topic");
            firstError = (firstError == null ? error : firstError);
        }

        if (!undeleted.isEmpty()) {
            throwDdsException(String.format("Failed to delete the %s.",
                                            String.join(", ", undeleted)),
                              firstError);
        }
    }

    /**
     * This method returns the current state of this Flow.
     *
     * @return The current state of this Flow.
     */
    public State getState() {
        synchronized(stateLock) {
            return state;
        }
    }


    /**
     * This method calculates the absolute time when this Flow can start to
     * send the next data frame.
     *
     * @param lastTime The absolute time when this Flow was supposed to start to
     *                 send the last data. Note that this is not the actual time
     *                 when this Flow started to send the data.
     * @param dataSize The size of the last data that were already sent in bytes.
     * @param throttle Throttle configuration in MB/s. This value must be between
     *                 122070.0 MB/s and 1e-11 MB/s. See
     *                 BulkDataNTSenderFlowConfiguration#getThrottling() for more
     *                 details why this value needs to be in this range.
     */
    private static Instant calculateAbsoluteTimeToSendNextData(Instant lastTime,
                                                               long dataSize,
                                                               double throttle) {
        Validate.notNull(lastTime);
        if (throttle > 122070.0) {
            throw new IllegalArgumentException("Throttle must not be bigger than " +
                                               "122070.0 MB/s.");
        } else if (throttle < 1e-11) {
            throw new IllegalArgumentException("Throttle must not be smaller than " +
                                               "1e-11 MB/s.");
        }

        double timeToWaitSec = (double)dataSize / (throttle * 1024.0 * 1024.0);
        long timeToWaitNanos = (long)(Math.round(timeToWaitSec * 1e9));
        return lastTime.plusNanos(timeToWaitNanos);
    }

    /**
     * This method sleeps until the given time. This method does nothing
     * if the given time is after the current time.
     *
     * @param time The time until which this method sleeps. It must not be earlier
     *             than (2^63-1) nanoseconds since now.
     *
     * @exception InterruptedException if the sleep function is interrupted.
     */
    private static void sleepUntil(Instant time)
        throws InterruptedException {
        Instant now = Instant.now();
        if (time.isAfter(now)) {
            long timeToSleepNanos = now.until(time, ChronoUnit.NANOS);
            assert timeToSleepNanos > 0;
            TimeUnit.NANOSECONDS.sleep(timeToSleepNanos);
        }
    }

    /**
     * This method validates the given configuration. If any of configuration
     * values are illegal, this method throws IllegalConfigurationException.
     * If all configuration values are valid, this method does nothing.
     *
     * @param conf The configuration to be validated.
     *
     * @throws IllegalConfigurationException
     * if the given configuration contains illegal configuration value(s).
     * @throws QosLibraryNotExistException
     * if the QoS library name given the specified configuration does not exist.
     * @throws QosProfileNotExistException
     * if the QoS profile name given the specified configuration does not exist
     * in the QoS library.
     */
    protected static void validateConfiguration(BulkDataNTSenderFlowConfiguration conf)
        throws IllegalConfigurationException,
               QosLibraryNotExistException,
               QosProfileNotExistException {
        validateQosLibraryName(conf.getQosLibraryName());
        validateQosProfileName(conf.getQosLibraryName(),
                               conf.getQosProfileName());
        validateTimeout("ACK", conf.getAckTimeout());
        validateTimeout("Send frame", conf.getSendFrameTimeout());
        validateThrottling(conf.getThrottling());
        
        if (conf.getLogger() == null) {
            String msg = "Optional<Logger> is null.";
            throw new IllegalConfigurationException(msg);
        }
    }
    
    /**
     * This method validates the given QoS library name. If the given
     * QoS library name is valid, this method nothing. Otherwise,
     * this method throws an exception.
     * 
     * @param name QoS library name to validate.
     * 
     * @throws IllegalConfigurationException
     * if the given name is null
     * @throws QosLibraryNotExistException
     * if the given library name does not exist in the current QoS setting.
     */
    protected static void validateQosLibraryName(String name)
        throws IllegalConfigurationException,
               QosLibraryNotExistException {
        BulkDataNTDomainParticipantFactory factory
        = BulkDataNTDomainParticipantFactory.getInstance();

        if (name == null) {
            String msg = "QoS library name must not be null.";
            throw new IllegalConfigurationException(msg);
        }
        factory.checkExistenceOfLibrary(name);
    }
    
    /**
     * This method validates the given QoS library name. If the given
     * QoS library name is valid, this method nothing. Otherwise,
     * this method throws an exception.
     * 
     * @param libraryName QoS library name.
     * @param profileName QoS profile name.
     * 
     * @throws IllegalConfigurationException
     * if the given name is null
     * @throws QosProfileNotExistException
     * if the given library name does not exist in the current QoS setting.
     */
    protected static void validateQosProfileName(String libraryName,
                                                 String profileName)
        throws IllegalConfigurationException,
               QosProfileNotExistException {
        BulkDataNTDomainParticipantFactory factory
        = BulkDataNTDomainParticipantFactory.getInstance();

        if (libraryName == null) {
            String msg = "QoS library name must not be null.";
            throw new IllegalConfigurationException(msg);
        } else if (profileName == null) {
            String msg = "QoS profile name must not be null.";
            throw new IllegalConfigurationException(msg);
        }

        factory.checkExistenceOfProfile(libraryName, profileName);
    }

    /**
     * This method validates the given timeout. The timeout
     * must be in the range between 0 and 
     * {@value java.lang.Integer#MAX_VALUE}.999999999 seconds (inclusive)
     * to convert Duration_t class. If not, this method throws an exception.
     * Otherwise, this method does nothing.
     * 
     * @param name Name of this timeout. Will be used in the exception message.
     * @param timeout The ACK timeout to validate.
     * 
     * @throws IllegalConfigurationException
     * if the given timeout is null, or if the given time out is not
     * in the prescribed range.
     */
    protected static void validateTimeout(String name, Duration timeout)
        throws IllegalConfigurationException {
        if (timeout == null) {
            String msg = name + " timout must not be null.";
            throw new IllegalConfigurationException(msg);
        } else if (timeout.isNegative()) {
            String msg = name + " timout must not be negative.";
            throw new IllegalConfigurationException(msg);
        } else if (timeout.getSeconds() > Integer.MAX_VALUE) {
            String msg
                = String.format("%s timout must not be longer than"+
                                "%d.999999999", name, Integer.MAX_VALUE);
            throw new IllegalConfigurationException(msg);
        }
    }
    
    /**
     * This method validates the given throttling setting.
     * 
     * <p>
     * The upper limit of the throttling configuration is
     * {@value #MAX_THROTTLING} MB/s to keep the throttling accuracy within
     * {@value #THROTTLING_ACCURACY} %. A big chunk of data is split
     * into small frames by every {@value #FRAME_MAX_LEN} bytes.
     * This means that, at maximum, {@value #MAX_FRAMES_PER_SECOND}
     * frames will be generated per second. {@link BulkDataNTSenderFlow}
     * internally manages the time in nanosecond resolution, which may cause
     * up to {@value #TIMING_ERROR_IN_NANOSECONDS} nanoseconds error per frame. 
     * This error is accumulated frame
     * by frame, and finally it is accumulated up to
     * {@value #MAX_TIMING_ERROR_MILLISECONDS_PER_SECOND} millisecond(s)
     * (= {@value #MAX_FRAMES_PER_SECOND} * {@value #TIMING_ERROR_IN_NANOSECONDS}
     * nanoseconds) per second, hence {@value #THROTTLING_ACCURACY} % accuracy.
     *
     * <p>
     * Note that the throttling accuracy may be deteriorated if the size of
     * the payload in one frame is not
     * {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN}
     * bytes. This may happen if you call
     * {@link #sendData(byte[])} with the data whose
     * size is not aligned to
     * {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN}
     * bytes. The accuracy would be greatly
     * deteriorated especially when
     * {@link #sendData(byte[])} is repeatedly called
     * with small data.
     * 
     * <p>
     * The lower limit of this value is {@value #MIN_THROTTLING} MB/s. This
     * limitation comes from the maximum nanoseconds that
     * {@link BulkDataNTSenderFlow} can wait. {@link BulkDataNTSenderFlow}
     * internally holds the time to wait between two frames in nanosecond
     * resolution, and the variable is 64-bit signed integer. It means that
     * the maximum time that it can wait is {@value java.lang.Long#MAX_VALUE}
     * nanoseconds. Because the maximum payload data size is limited up to
     * {@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN} bytes, the
     * minimum acceptable throttling can be calculated as
     * ({@value alma.acs.bulkdata.BulkDataNTFlow#FRAME_MAX_LEN} *
     * 10<sup>9</sup>) / ({@value java.lang.Long#MAX_VALUE} * 1024 * 1024) =
     * {@value #MIN_THROTTLING} MB/s.
     * 
     * @param throttling The throttling setting to validate.
     * 
     * @throws IllegalConfigurationException
     * if the given throttling setting does not fulfill the prescribed
     * conditions.
     */
    protected static void validateThrottling(double throttling)
        throws IllegalConfigurationException {
        String msg;
        
        if (Double.isNaN(throttling)) {
            msg =
                "Throttling is set to NaN. It must be a finite value.";
            throw new IllegalConfigurationException(msg);
        } else if (throttling < MIN_THROTTLING) {
            // Negative infinity is considered as invalid here.
            msg = String.format("Throttling is set to %f MB/s, but it must " +
                                "be %f or faster.",
                                throttling, MIN_THROTTLING);
            throw new IllegalConfigurationException(msg);
        } else if (throttling > MAX_THROTTLING) {
            // Positive infinity is considered as invalid here.
            msg = String.format("Throttling is set to %f MB/s, but it must " +
                                "be %f or slower.",
                                throttling, MAX_THROTTLING);
            throw new IllegalConfigurationException(msg);
        }
    }
    
    /**
     * The throttling accuracy in %.
     */
    protected static final double THROTTLING_ACCURACY = 0.1;
    
    /**
     * The maximum timing error in nanoseconds. This is the half of
     * the time resolution.
     */
    protected static final double TIMING_ERROR_IN_NANOSECONDS = 0.5;
    
    /**
     * Maximum allowed throttling in MB/s.
     */
    protected static final double MAX_THROTTLING
        = FRAME_MAX_LEN / 1024.0 / 1024.0
        / (TIMING_ERROR_IN_NANOSECONDS * 1e-9)
        * THROTTLING_ACCURACY / 100.0;
    
    /**
     * The maximum number of frames sent per second.
     */
    protected static final double MAX_FRAMES_PER_SECOND
        = MAX_THROTTLING * 1024.0 * 1024.0 / FRAME_MAX_LEN;
    
    /**
     * The maximum accumulated timing error per second.
     */
    protected static final double MAX_TIMING_ERROR_MILLISECONDS_PER_SECOND
        = MAX_FRAMES_PER_SECOND * TIMING_ERROR_IN_NANOSECONDS * 1e-6;
    
    /**
     * Minimum allowed throttling in MB/s.
     */
    protected static final double MIN_THROTTLING
        = FRAME_MAX_LEN * 1e9 / Long.MAX_VALUE / 1024.0 / 1024.0;

    /**
     * This method returns the Sender Stream that this Sender Flow belongs to.
     *
     * @return The Sender Stream that this Sender Flow belongs to.
     */
    public BulkDataNTSenderStream getSenderStream() {
        return stream;
    }


    /**
     * @deprecated This method is here because C++ implementation has an equivalent
     * method. However, the use case of this method is unclear and currently is
     * not implemented. This method will be removed unless there is an explicit
     * request.
     */
    public void dumpStatistics() {
        throw new UnsupportedOperationException();
    };

    /**
     * @deprecated This method is here because C++ implementation has an equivalent
     * method. However, the use case of this method is unclear and currently is
     * not implemented. This method will be removed unless there is an explicit
     * request.
     *
     * @param log not supported
     */
    public void getStatistics(boolean log) {
            throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This method is here because C++ implementation has an equivalent
     * method. However, the use case of this method is unclear and currently is
     * not implemented. This method will be removed unless there is an explicit
     * request.
     *
     * @param log not supported
     * @param flowMethod not supported
     */
    public void getDelayedstatistics(boolean log, int flowMethod) {
            throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This method is here because C++ implementation has an equivalent
     * method. However, the use case of this method is unclear and currently is
     * not implemented. This method will be removed unless there is an explicit
     * request.
     */
    public void statisticsLogs() {
            throw new UnsupportedOperationException();
    }

    /**
     * Returns the logger that this Flow uses.
     * 
     * @return The logger that this Flow uses.
     */
    public Optional<Logger> getLogger() {
        return logger;
    }

    private void throwDdsException(String msg,
                                   RETCODE_ERROR error)
        throws DdsException {
        String s = String.format("%s [stream: %s, flow: %s]",
                                 msg, getStream().getName(), getName());
        throw new DdsException(s, error, getStream(), this);
    }

    private static void throwDdsException(String msg,
                                          RETCODE_ERROR error,
                                          Map<String, String> aux,
                                          BulkDataNTStream stream,
                                          BulkDataNTFlow flow,
                                          Class<? extends DdsException> type)
        throws DdsException {
        Map<String, String> m = new LinkedHashMap<>(aux);
        m.put("stream", stream.getName());
        m.put("flow", flow.getName());

        msg = String.format("%s [%s]", msg,
                            m.entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue())
                            .collect(Collectors.joining(", ")));
        if (type == FailedToEnableException.class) {
            throw new FailedToEnableException(msg, error, stream, flow);
        } else if (type == FailedToGetQosException.class) {
            throw new FailedToGetQosException(msg, error, stream, flow);
        } else if (type == FailedToSetQosException.class) {
            throw new FailedToSetQosException(msg, error, stream, flow);
        } else if (type == SendFrameTimeoutException.class) {
            throw new SendFrameTimeoutException(msg, error, stream, flow);
        } else if (type == AckTimeoutException.class) {
            throw new AckTimeoutException(msg, error, stream, flow);
        } else {
            throw new DdsException(msg, error, stream, flow);
        }
    }

    private static void throwDdsException(String msg,
                                          RETCODE_ERROR error,
                                          Map<String, String> aux,
                                          BulkDataNTStream stream,
                                          BulkDataNTFlow flow)
        throws DdsException {
        throwDdsException(msg, error, aux, stream, flow, DdsException.class);
    }

    /**
     * This method must be called within synchronized(stateLock) clause.
     */
    private void validateState(State expectedState,
                               String operationName)
        throws InappropriateSenderFlowStateException {
        if (state != expectedState) {
            String msg
                = String.format("%s is allowed only when the Sender Flow is " +
                                "in %s state. " +
                                "[stream: %s, flow: %s, state: %s]",
                                operationName, expectedState.name(),
                                getStream().getName(), getName(), state.name());
            throw new InappropriateSenderFlowStateException(msg, this, state);
        }
    }

    private void logInfo(String msg) {
        logger.ifPresent(l -> l.log(AcsLogLevel.INFO, msg));
    }

    private void logError(String msg) {
        logger.ifPresent(l -> l.log(AcsLogLevel.ERROR, msg));
    }
    
    /**
     * This method should be called in synchronized(stateLock) clause.
     */
    private void logTraceWithState(String msg, Map<String, String> aux) {
        Map<String, String> m = new LinkedHashMap<>(aux);
        m.put("stream", stream.getName());
        m.put("flow", getName());
            
        synchronized (stateLock) {
            m.put("state", state.name());
        }

        String msg2;
        msg2 = String.format("%s [%s]", msg,
                             m.entrySet().stream()
                             .map(e -> e.getKey() + ": " + e.getValue())
                             .collect(Collectors.joining(", ")));
        
        logger.ifPresent(l -> l.log(AcsLogLevel.TRACE, msg2));
    }
    
    private void logTrace(String msg, Map<String, String> aux) {
        Map<String, String> m = new LinkedHashMap<>(aux);
        m.put("stream", stream.getName());
        m.put("flow", getName());

        String msg2;
        msg2 = String.format("%s [%s]", msg,
                             m.entrySet().stream()
                             .map(e -> e.getKey() + ": " + e.getValue())
                             .collect(Collectors.joining(", ")));
        
        logger.ifPresent(l -> l.log(AcsLogLevel.TRACE, msg2));
    }

    private void logNumberOfReceivers(String operationName) {
        try {
            int receivers = getNumberOfReceivers();
            if (receivers == 0) {
                logError(String.format("%s invoked without connected " +
                                       "listeners in Sender Flow: %s @ " +
                                       "Stream: %s",
                                       operationName,
                                       getStream().getName(),
                                       getName()));
            } else {
                logInfo(String.format("%s will send data to %d " +
                                      "connected listeners in Sender Flow: " +
                                      "%s @ Stream: %s",
                                      operationName,
                                      receivers,
                                      getStream().getName(),
                                      getName()));
            }
        } catch (DdsException ex) {
            // Ignore this exception. It is not critical to get the number
            // of receivers.
            String msg
                = String.format("Failed to obtian the number of during %s.",
                                operationName);
            logger.ifPresent(l -> l.log(AcsLogLevel.ERROR, msg, ex));
        }
    }
    
    /**
     * This method transitions the state into the specified new 
     * state.
     * 
     * @param newState New state.
     * @param oldState Old state. This is just used to assert the current 
     *                 state.
     */
    private void transitionTo(State newState, State oldState) {
        synchronized (stateLock) {
            assert state == oldState;
            state = newState;
            if (state != State.BUSY) {
                threadExecutingSendData = null;
            }
            
            if (state == State.WAIT ||
                state == State.STOP ||
                state == State.DESTROYED) {
                stateLock.notifyAll();
            }
        }
    }
    
    /**
     * This method returns the configuration object that this Sender Flow
     * uses.
     * 
     * @return The configuration object that this Sender Flow uses.
     */
    public BulkDataNTSenderFlowConfiguration getConfiguration() {
        return conf;
    }
    
}
