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

import java.util.Optional;
import java.util.logging.Logger;

import com.rti.dds.domain.DomainParticipant;
import org.apache.commons.lang.StringUtils;

/**
 * This class represents one BulkData Stream. This abstract class
 * is the base of {@link BulkDataNTSenderStream} to handle DDS Domain
 * Participant instance. The functionality of handling DDS Domain
 * Participant was split from {@link BulkDataNTSenderStream}
 * implementation in the hope that, in some future, BulkDataNTReceiverStream
 * might be implemented on top of this class. The intention of this
 * class is to have the base class for both receiver and sender
 * to share the common parts in a similar way as C++ implementation.
 *       
 * @author Takashi Nakamoto
 */
public abstract class BulkDataNTStream {
    private final String name;
    private final BulkDataNTStreamConfiguration conf;
    private final BulkDataNTDomainParticipantFactory factory;
    private final BulkDataNTDomainParticipant participant;
    protected final Optional<Logger> logger;
    private boolean destroyed;

    /**
     * Create a new Stream with the given name and the given configuration.
     *
     * <p>
     * Basically, there is no DDS entity that corresponds to Stream. Stream
     * is a pure concept which groups several flows under a meaningful Stream
     * name. The name is used as a part of the DDS topic name.
     *
     * <p>
     * From the view point of Flow, this object is a convenient entity that
     * holds a DDS Domain Participant from which DDS topics, DDS publishers
     * or subscribers are generated. The Domain Participant can be obtained by
     * calling {@link #getDomainParticipant()}.
     *
     * @param name Stream name that fulfills the criteria described in
     *             {@link #getName()}.
     * @param conf An instance that holds the configuration values for 
     *             the newly created Stream.
     *
     * @exception InvalidStreamNameException
     * if the given name does not fulfill the criteria described in
     * {@link #getName()}
     * @exception QosXmlNotLoadedException
     * if QoS XML file has not been loaded yet. This could happen if
     * either {@link BulkDataNTGlobalConfiguration#loadQosXml()} or
     * {@link BulkDataNTGlobalConfiguration#loadQosXml(Path)} has not
     * been called so far.
     * @throws QosLibraryNotExistException
     * if the library name specified by the configuration does not exist.
     * @throws QosProfileNotExistException
     * if the profile name specified by the configuration does not exist
     * @exception DdsException
     * if the underlying DDS library throws an exception.
     */
    protected BulkDataNTStream(String name,
                               BulkDataNTStreamConfiguration conf)
        throws InvalidStreamNameException,
               QosXmlNotLoadedException,
               QosLibraryNotExistException,
               QosProfileNotExistException,
               DdsException {

        // Copy the configuration in to the memory.
        this.conf = new BulkDataNTStreamConfigurationStore(conf);

        // Note for the programmer who may modify the code of this constructor
        // in the future:
        // 
        // Do not call any method of the argument "conf" hereinafter. Always
        // use "this.conf" instead. This is because the method calls of the
        // given argument "conf" may have a side effect depending on how
        // BulkDataNTStremConfiguration interface is implemented. It may not
        // be immutable. It means that, every time you call some getter
        // methods, the returned value may change, which is not desirable in
        // some cases.
        //
        // For that, all the configuration values are copied to "this.conf" in
        // the above code using BulkDataNTStreamConfigurationStore which is 
        // immutable. The getter methods of that instance always return
        // the same value.

        this.logger = this.conf.getLogger();
        this.destroyed = false;

        ValidationResult result = getNameValidator().validate(name);
        if (!result.isValid()) {
            throw new InvalidStreamNameException(name, result.getReason());
        }

        this.name = name;
        this.factory = BulkDataNTDomainParticipantFactory.getInstance();
        this.participant
            = this.factory.useDomainParticipant(this.conf.getQosLibraryName(),
                                                this.conf.getQosProfileName(),
                                                this.conf.getDomainId());

        // Note: Currently, the domain participant is created per Stream.
        //       This is probably not desirable in case the application
        //       wants to create many Streams because the domain participant
        //       is a heavy-weight object, and having many domain participants
        //       may adversely affect the performance and resource utilization
        //       of the application according to RTI Connext Java API.
        //
        //       However, it is not currently expected that so many Streams
        //       will be created. At least, for new ACA Spectrometer (the
        //       first user of BulkData Java library), only one Stream is
        //       expected to be used. So, 
        //
        //
        // https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/html/api_java/classcom_1_1rti_1_1dds_1_1infrastructure_1_1SystemResourceLimitsQosPolicy.html



        // Note: this method does not validate the Domain ID. DDS-wise any
        //       32-bit integer value is OK as a Domain ID.

        logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG,
                                    "Stream: " + name + " has been created."));
    }

    /**
     * This method returns the name of this Stream. The returned name
     * always fulfills the following criteria:
     *
     * <ul>
     * <li>It is neither null nor empty (zero-length).
     * <li>It consists of up to 127 ASCII-printable characters.
     * <li>It does not contain " " (white space) or
     *     {@value alma.acs.bulkdata.BulkDataNTFlow#STREAM_FLOW_NAME_SEPARATOR}.
     * </ul>
     *
     * <p>
     * The restriction of the maximum length comes from the maximum length
     * of DDS topic name (255 characters). The topic name consists of
     * Stream name,
     * {@value alma.acs.bulkdata.BulkDataNTFlow#STREAM_FLOW_NAME_SEPARATOR} and
     * Flow name. Half of the length of the topic name (127 characters out of
     * 255) is assigned to the Flow name.
     *
     * <p>
     * To avoid unnecessary confusion, non-ASCII characters, non-printable
     * characters and white space are not allowed.
     * 
     * <p>
     * {@value alma.acs.bulkdata.BulkDataNTFlow#STREAM_FLOW_NAME_SEPARATOR} is
     * not allowed because it is used as the separator between Stream and Flow
     * names.
     *
     * @return Valid Stream name.
     */
    public String getName() {
        return name;
    }

    /**
     * This methods returns a validator which validates the given Stream
     * name. The criteria of the validation is described in {@link #getName()}.
     *
     * @return Validator which validates the given Stream name.
     */
    static StringValidator getNameValidator() {
        return new StringValidator() {
            @Override public ValidationResult validate(String str) {
                String msg;
                if (str == null) {
                    msg = "Stream name must not be null.";
                    return new ValidationResult(false, msg);
                } else if (str.isEmpty()) {
                    msg = "Stream name must not be empty.";
                    return new ValidationResult(false, msg);
                } else if (!StringUtils.isAsciiPrintable(str)) {
                    msg = "Stream name must consist of only ASCII-printable " +
                          "characters.";
                    return new ValidationResult(false, msg);
                } else if (str.length() > 127) {
                    msg = "Stream name must consist of up to 127 characters.";
                    return new ValidationResult(false, msg);
                } else if (str.contains(" ")) {
                    msg = "Stream name must not contain ' ' (white space).";
                    return new ValidationResult(false, msg);
                } else if (str.contains(BulkDataNTFlow.
                                        STREAM_FLOW_NAME_SEPARATOR)) {
                    msg = "Stream name must not contain '"+
                        BulkDataNTFlow.STREAM_FLOW_NAME_SEPARATOR + "'.";
                    return new ValidationResult(false, msg);
                } else {
                    return new ValidationResult(true, null);
                }
            }
        };
    }

    /**
     * This method returns the instance of DDS Domain Participant that
     * this Stream uses. The returned instance must not be destroyed by
     * the caller.
     *
     * <p>
     * The returned participant can be used to create a new topic, publisher
     * and subscriber when creating a new Flow.
     *
     * <p>
     * If this Stream was already destroyed, this method may return an empty
     * value.
     *
     * @return DDS Domain Participant of this Stream.
     */
    synchronized Optional<DomainParticipant> getDomainParticipant() {
        if (destroyed) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(participant.getDdsDomainParticipant());
        }
    }

    /**
     * This method returns the type name of RTI DDS for BulkData.
     * It is expected that the returned type name is used to create
     * com.rti.dds.topic.Topic (e.g. the second argument of
     * DomainParticipant#create_topic_with_profile(String, String,
     * String, String, TopicListener, int)).
     */
    String getDataTypeName() {
        return BulkDataNTDomainParticipant.TYPE_NAME;
    }

    /**
     * Release the resources allocated to this Stream. The subclass of
     * this class must call this method when it is destroyed. If this
     * Stream is already destroyed, this method does nothing.
     *
     * @throws DdsException
     * if the underlying DDS library throws an exception.
     * @throws UndestroyedSenderFlowsException
     * this exception never occurs in this class. This exception is defined
     * so that the child classes can throw.
     */
    protected synchronized void destroy()
        throws DdsException,
               UndestroyedSenderFlowsException {
        if (destroyed) {
            logger.ifPresent(l -> l.log(AcsLogLevel.WARNING,
                                        "Stream: " + name + 
                                        " was already destroyed."));
            return;
        }

        factory.unuseDomainParticipant(participant);
        destroyed = true;

        logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG,
                                    "Stream: " + name + 
                                    " has been destroyed."));
    }

    /**
     * @deprecated This method is here because C++ implementation has an equivalent
     * method. However, the use case of this method is unclear and currently is
     * not implemented. This method will be removed unless there is an explicit
     * request.
     */
    public void addDDSQosProfile() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This method is here because C++ implementation has an equivalent
     * method. However, the use case of this method is unclear and currently is
     * not implemented. This method will be removed unless there is an explicit
     * request.
     */
    public void removeDDSQosProfile() {
        throw new UnsupportedOperationException();
    }
}
