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

import alma.acs.bulkdata.ACSBulkData.BulkDataNTFrameTypeSupport;
import alma.acs.logging.AcsLogLevel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import org.apache.commons.lang.Validate;

/**
 * This class wraps DDS Domain Participant and manage its lifecycle for
 * BulkData. It is intended to be used by {@link BulkDataNTStream}.
 *
 * <p>
 * This class is designed in a way that this domain participant can be shared
 * by multiple Streams. For that, this class has a reference count. Once this
 * class is instantiated, the reference count becomes 1. If the other Streams
 * want to use the same domain participant, it must call use() method to
 * declare that that Stream will use this domain participant. Then, the
 * reference count is incremented by 1.
 *
 * <p>
 * When the Stream finishes using this domain participant (normally it happens
 * when the Stream is destroyed), it must call unuse() method. It decrements
 * the reference count by 1, and if it becomes 0, this domain participant
 * is also destroyed.
 *
 * @author Takashi Nakamoto
 */
class BulkDataNTDomainParticipant {
    static final String TYPE_NAME
        = BulkDataNTFrameTypeSupport.get_type_name();

    /**
     * This variable holds the number of Domain Participants that have
     * been created so far. This value intended to be used to identify
     * each Domain Participant within this JVM.
     */
    static final AtomicInteger idCounter = new AtomicInteger();
    
    private final DomainParticipant participant;
    private final Optional<Logger> logger;
    private final String qosLibraryName;
    private final String qosProfileName;
    private final int domainId;
    private final int id;

    private int referenceCount;
    private boolean destroyed;

    /**
     * This constructor instantiates the wrapper of the given DDS Domain
     * Participant.
     * 
     * <p>
     * It is intended to be called by only {@link BulkDataNTDomainParticipantFactory}.
     *
     * @param participant Associated DDS Domain Participant. Must not be null.
     * @param qosLibraryName QoS XML Library name (e.g. "BulkDataQoSLibrary").
     *                       It must be neither null nor empty.
     * @param qosProfileName QoS XML profile name
     *                       (e.g. "SenderStreamDefaultQosProfile").
     *                       It must be neither null nor empty.
     * @param domainId Domain ID.
     * @param logger The destination of the log messages that this instance
     *               outputs. It can be empty if you don't need logging.
     *
     * @throws IllegalArgumentException
     * if qosLibraryName is either null or empty, or if qosProfileName is
     * either null or empty.
     * @throws QosXmlNotLoadedException
     * if QoS XML file has not been loaded yet. This could happen if 
     * either {@link BulkDataNTGlobalConfiguration#loadQosXml()} or
     * {@link BulkDataNTGlobalConfiguration#loadQosXml(Path)} has not
     * been called so far.
     * @throws QosLibraryNotExistException
     * if the given library name does not exist.
     * @throws QosProfileNotExistException
     * if the given profile name does not exist in the specified QoS library.
     * @throws DdsException
     * if the underlying DDS library encounters an error while creating
     * a new Domain Participant.
     */
    BulkDataNTDomainParticipant(DomainParticipant participant,
                                String qosLibraryName,
                                String qosProfileName,
                                int domainId,
                                Optional<Logger> logger)
        throws DdsException {
        Validate.notNull(participant);
        Validate.notEmpty(qosLibraryName);
        Validate.notEmpty(qosProfileName);
        Validate.notNull(logger);

        this.id = idCounter.incrementAndGet();
        this.qosLibraryName = qosLibraryName;
        this.qosProfileName = qosProfileName;
        this.domainId = domainId;
        this.logger = logger;
        BulkDataNTDomainParticipantFactory.getInstance();
        this.participant = participant;
        this.referenceCount = 1;
        this.destroyed = false;
        
        try {
            BulkDataNTFrameTypeSupport.register_type(participant, TYPE_NAME);
        } catch (RETCODE_ERROR error) {
            String msg
            = String.format("Failed to register %s type to the Domain " +
                            "Participant." +
                            "(Domain ID: %d, library: %s, " +
                            "profile: %s, Instnace ID: %d).",
                            TYPE_NAME,
                            domainId, qosLibraryName,
                            qosProfileName, id);
            throw new DdsException(msg, error); 
        }
        
        try {
            participant.enable();
        } catch (RETCODE_ERROR error) {
            String baseMsg = "Failed to enable Domain Participant.";
            
            try {
                BulkDataNTFrameTypeSupport.unregister_type(participant,
                                                           TYPE_NAME);
            } catch (RETCODE_ERROR error2) {
                // Can't help...
                baseMsg += " Cleanup process also failed.";
            }
            
            String msg
                = String.format(baseMsg + 
                                "(Domain ID: %d, library: %s, " +
                                "profile: %s, Instnace ID: %d).",
                                domainId, qosLibraryName,
                                qosProfileName, id);
            throw new FailedToEnableException(msg, error);
        }

        String msg = String.format("New domain participant is created. " +
                                   "(Domain ID: %d, library: %s, " +
                                   "profile: %s, Instance ID: %d).",
                                   this.domainId, this.qosLibraryName,
                                   this.qosProfileName, this.id);
        this.logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG, msg));
    }

    /**
     * This method increments the reference count of this Domain Participant.
     * 
     * <p>
     * It is intended to be called by only {@link BulkDataNTDomainParticipantFactory}.
     *
     * @return True if the reference count was successfully incremented. False
     *         this domain participant is already destroyed.
     */
    synchronized boolean use() {
        if (destroyed) {
            return false;
        }
        
        if (referenceCount == Integer.MAX_VALUE) {
            String msg
                = String.format("Reference count of Domain Participant "+
                                "(Instance ID: %d) is overflowed", id);
            throw new RuntimeException(msg);
        }

        referenceCount += 1;

        String msg 
            = String.format("Reference count of Domain Participant (Instance " +
                            "ID: %d) is now incremented to %d.",
                            id, referenceCount) ;
        logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG, msg));
        
        return true;
    }

    /**
     * This method decrements the reference count of this Domain Participant.
     *
     * <p>
     * This method must be called exactly the same amount of times as the
     * number of calls of {@link #use()} plus one before quitting the program.
     * 
     * <p>
     * It is intended to be called only by
     * {@link BulkDataNTDomainParticipantFactory}.
     * 
     * @return true if the reference count reaches 0, and this domain
     *         participant is destroyed.
     *
     * @throws DdsException
     * if underlying DDS library encounters an error when destroying.
     */
    synchronized boolean unuse()
        throws DdsException {
        if (referenceCount <= 0) {
            String msg 
                = String.format("The reference count of Domain Particiapnt " +
                                "(Instance ID: %d) is already %d.",
                                id, referenceCount);
            logger.ifPresent(l -> l.log(AcsLogLevel.ERROR, msg));
            return false;
        }

        referenceCount -= 1;
        String msg 
            = String.format("Reference count of Domain Participant (Instance " +
                            "ID: %d) is now decremented to %d.",
                            id, referenceCount) ;
        logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG, msg));

        if (referenceCount == 0) {
            destroy();
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method destroys this domain participant and releases the allocated
     * resources.
     *
     * @throws DdsException
     * if underlying DDS library encounters an error.
     */
    private synchronized void destroy() throws DdsException {
        try {
            BulkDataNTFrameTypeSupport.unregister_type(participant,
                                                       TYPE_NAME);
            participant.delete_contained_entities();

            String msg
                = String.format("Domain Participant (Instance ID: %d) is " +
                                "destroyed.", id);
            logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG, msg));
            
            destroyed = true;
        } catch (RETCODE_ERROR error) {
            String msg
                = String.format("Failed to destroy Domain Participant " +
                                "(Instnace ID: %d).", id);
            throw new DdsException(msg, error);
        }
    }

    /**
     * Return the QoS library name of  this domain participant.
     *
     * @return QoS library name. Never be null.
     */
    public String getQosLibraryName() {
        return qosLibraryName;
    }

    /**
     * Return the QoS profile name of  this domain participant.
     *
     * @return QoS profile name. Never be null.
     */
    public String getQosProfileName() {
        return qosProfileName;
    }

    /**
     * This method returns the corresponding DDS Domain Participant.
     *
     * @return The instance of DDS Domain Participant;
     */
    DomainParticipant getDdsDomainParticipant() {
        return participant;
    }

    /**
     * Return the Domain ID of  this domain participant.
     *
     * @return Domain ID.
     */
    public int getDomainId() {
        return domainId;
    }
}
