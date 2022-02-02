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

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.infrastructure.StringSeq;

import alma.acs.logging.AcsLogLevel;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.Validate;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

/**
 * This class holds a singleton instance that wraps
 * {@link com.rti.dds.domain.DomainParticipantFactory}.
 *
 * @author Takashi Nakamoto
 */
class BulkDataNTDomainParticipantFactory {
    private static BulkDataNTDomainParticipantFactory instance;
    
    public synchronized static
        BulkDataNTDomainParticipantFactory getInstance() {
        if (instance == null) {
            instance = new BulkDataNTDomainParticipantFactory();
        }
        return instance;
    }

    private final DomainParticipantFactory participantFactory;
    private final Map<BulkDataNTDomainParticipantIdentifier,
                BulkDataNTDomainParticipant> participantPool;
    private String currentQosXmlContent;

    private BulkDataNTDomainParticipantFactory() {
        // It seems that get_instance() always succeeds and returns non-null
        // instance because the API document does not address any exception
        // or possibility of null as the returned value. So, no error handling
        // code is written here.
        this.participantFactory = DomainParticipantFactory.get_instance();

        // It is unsafe for multiple threads to simultaneously make the first
        // call to DomainParticipantFactory#get_instance() on non-Linux systems.
        // https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/html/api_java/classcom_1_1rti_1_1dds_1_1domain_1_1DomainParticipantFactory.html#aa05a4dab9850ff130765224c22ad6b5f
        //
        // In this library, the thread-safety is assured by making this
        // object singleton, and not calling get_instance() anywhere else.
        
        
        // The pool of Domain Participants.
        participantPool = new HashMap<>();
    }

    /**
     * This method loads QoS XML file from the given {@link InputStream}.
     * 
     * <p>
     * The new QoS settings take effect after the successful execution
     * of this method. The Streams created after this method call will
     * use the new QoS settings.
     *
     * @param inputStream From which QoS XML contents are loaded.
     *                    Must not be null.
     *
     * @throws CannotLoadQosXmlSchemaException
     * if QoS XML schema file cannot be loaded correctly from RTI DDS
     * product distribution.
     * @exception InvalidQosXmlException
     * if the given QoS XML has syntax error(s), or if the validation with the
     * schema fails.
     * @throws IOException
     * if IO error happens while reading XML content from the given input
     * stream.
     * @exception ParserConfigurationException
     * if unknown exception happens while configuring XML parser.
     * @exception TransformerException
     * if unknown exception happens while transforming XML into a string.
     * @exception FailedToGetQosException
     * if the underlying DDS library fails to get QoS of the Domain Participant
     * Factory.
     * @exception FailedToSetQosException
     * if the underlying DDS library fails to set QoS of the Domain Participant
     * Factory.
     */
    synchronized void loadQosXml(InputStream inputStream)
        throws CannotLoadQosXmlSchemaException,
               IOException,
               InvalidQosXmlException,
               ParserConfigurationException,
               TransformerException,
               FailedToGetQosException,
               FailedToSetQosException {
        Validate.notNull(inputStream);

        setQosXml(readQosXml(inputStream));
    }

    /**
     * This method sets the given QoS XML contents to the DDS Domain Participant
     * Factory. The given QoS XML takes effect after the successful execution
     * of this method.
     *
     * @param xmlContent A string that contains a complete and validated QoS
     *                   XML file. This is typically the value returned by
     *                   {@link readQosXml(InputStream)}.
     *
     * @exception FailedToGetQosException
     * if the underlying DDS library fails to get QoS of the Domain Participant
     * Factory.
     * @exception FailedToSetQosException
     * if the underlying DDS library fails to set QoS of the Domain Participant
     * Factory.
     * @exception IllegalArgumentException
     * if the given xmlContent is null.
     */
    private synchronized void setQosXml(String xmlContent)
        throws FailedToGetQosException,
        FailedToSetQosException {
        Validate.notNull(xmlContent);

        currentQosXmlContent = xmlContent;
        
        DomainParticipantFactoryQos factoryQos
            = new DomainParticipantFactoryQos();

        try {
            participantFactory.get_qos(factoryQos);
        } catch (RETCODE_ERROR error) {
            String msg = "Failed to get QoS of the Domain Participant Factory.";
            throw new FailedToGetQosException(msg, error);
        }

        // Do not automatically enable the domain participant(s) that
        // will be created from this factory.
        factoryQos.entity_factory.autoenable_created_entities = false;

        // Ignores USER_QOS_PROFILE.xml in the current directory and
        // the environmental variable NDDS_QOS_PROFILES. It makes sure
        // that the external environment does not affect which QoS
        // files will be used in BulkData. Instead, BulkData library
        // searches the QoS file in ACS directory structure.
        factoryQos.profile.ignore_user_profile = true;
        factoryQos.profile.ignore_environment_profile = true;

        // According to RTI Connext Java API, this parameter adjusts the
        // maximum number of Domain Participants. If this settings is
        // set to 1024, about 10 or 11 Domain Participants are allowed
        // to be created. So, this setting "4096" allows to create around
        // 40 to 44 Domain Participants.
        // https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/html/api_java/classcom_1_1rti_1_1dds_1_1infrastructure_1_1SystemResourceLimitsQosPolicy.html
        //
        // The value "4096" is the one used by BulkData C++ library. The
        // author does not understand why this value is chosen, but to keep
        // the compatibility with the C++ implementation, the same value is
        // used here. See bulkDataNTStream.cpp for more details.
        factoryQos.resource_limits.max_objects_per_thread = 4096;

        // Set QoS XML contents.
        factoryQos.profile.url_profile.clear();
        factoryQos.profile.string_profile.clear();
        factoryQos.profile.string_profile.add(xmlContent);

        try {
            participantFactory.set_qos(factoryQos);
        } catch (RETCODE_ERROR error) {
            String msg = "Failed to set QoS of the Domain Participant Factory.";
            throw new FailedToSetQosException(msg, error);
        }
    }

    /**
     * This method reads QoS XML file from the given {@link InputStream},
     * validates the content based on the schema provided by RTI DDS library
     * and returns the contents of the QoS XML file as {@link String}.
     *
     * @param inputStream InputStream from which this method reads QoS XML contents.
     *
     * @exception IllegalArgumentException
     * if the given inputStream is null.
     * @throws CannotLoadQosXmlSchemaException
     * if QoS XML schema file cannot be loaded correctly from RTI DDS
     * product distribution.
     * @exception IOException
     * if I/O error happens while getting the data form the given
     * {@link InputStream}.
     * @exception InvalidQosXmlException
     * if the given QoS XML has syntax error(s), or if the validation with the
     * schema fails.
     * @exception ParserConfigurationException
     * if unknown exception happens while configuring XML parser.
     * @exception TransformerException
     * if unknown exception happens while transforming XML into a string.
     */
    private static String readQosXml(InputStream inputStream)
        throws CannotLoadQosXmlSchemaException,
               IOException,
               InvalidQosXmlException,
               ParserConfigurationException,
               TransformerException {
        Validate.notNull(inputStream);

        BulkDataNTQosXmlSchema schema 
            = BulkDataNTQosXmlSchema.getInstance();

        try{
            // Read XML contents from the given InputStream and parse it
            // first. If there is syntax error, the parser may fail.
            DocumentBuilderFactory factory
                = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            // Convert the XML contents to a single String.
            Transformer transformer
                = TransformerFactory.newInstance().newTransformer();
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(doc),
                                  new StreamResult(stringWriter));
            String xmlContent = stringWriter.toString();

            // Finally validate the given XML contents in the String
            // with the schema provided by RTI DDS product distribution.
            schema.getSchema().newValidator()
                .validate(new StreamSource(new StringReader(xmlContent)));

            return xmlContent;
        } catch (SAXException ex) {
            String msg
                = "The given XML content has syntax error(s), or the "
                + "validation with the schema at " + schema.getPath()
                + "failed.";
            throw new InvalidQosXmlException(msg, ex, schema, null);
        }
    }

    /**
     * This method checks the existence of the given library name. If it
     * does not exist, this method throws an exception. If it exists,
     * this method does nothing.
     *
     * @param name Library name to check. Must not be null.
     *
     * @throws QosLibraryNotExistException
     * if the given library name does not exist in the given domain participant
     * factory.
     */
    public synchronized void checkExistenceOfLibrary(String name)
        throws QosLibraryNotExistException {
        Validate.notNull(name);

        StringSeq availableLibraryNameSeq = new StringSeq();
        participantFactory.get_qos_profile_libraries(availableLibraryNameSeq);
        Set<String> availableLibraryNames = new HashSet<>();
        for (int i=0; i < availableLibraryNameSeq.size(); i++) {
            availableLibraryNames.add((String)(availableLibraryNameSeq.get(i)));
        }

        if (!availableLibraryNames.contains(name)) {
            throw new QosLibraryNotExistException(name, availableLibraryNames);
        }
    }

    /**
     * This method checks the existence of the given profile name. If it
     * does not exist, this method throws an exception. If it exists,
     * this method does nothing.
     *
     * @param libraryName Library name.
     * @param name Profile name to check.
     *
     * @throws QosProfileNotExistException
     * if the given profile name does not exist in the given library.
     */
    public synchronized void checkExistenceOfProfile(String libraryName,
                                        String name)
        throws QosProfileNotExistException {
        Validate.notNull(libraryName);
        Validate.notNull(name);

        StringSeq availableProfileNameSeq = new StringSeq();
        participantFactory.get_qos_profiles(availableProfileNameSeq, libraryName);
        Set<String> availableProfileNames = new HashSet<>();
        for (int i=0; i < availableProfileNameSeq.size(); i++) {
            availableProfileNames.add((String)(availableProfileNameSeq.get(i)));
        }

        if (!availableProfileNames.contains(name)) {
            throw new QosProfileNotExistException(name,
                                                  availableProfileNames,
                                                  libraryName);
        }
    }

    /**
     * This method returns a {@link BulkDataNTDomainParticipant} with the given
     * library name, the given profile name and the domain ID.
     * 
     * <p>
     * If such domain participant already exists in the pool, the reference
     * count of the participant is incremented and returned. Otherwise, a
     * new domain participant is created and returned with the reference count
     * of 1.
     * 
     * <p>
     * The caller of this method must call
     * {@link #unuseDomainParticipant(BulkDataNTDomainParticipant)} to decrement
     * the reference count when it does no longer need the returned participant.
     * 
     * @param libraryName QoS library name for the domain participant.
     *                    Must be neither null or empty.
     * @param profileName QoS profile name for the domain participant.
     *                    Must be neither null or empty.
     * @param domainId Domain ID for the domain participant.
     *
     * @throws QosLibraryNotExistException
     * if the given library name does not exist.
     * @throws QosProfileNotExistException
     * if the given profile name does not exist in the specified QoS library.
     * @throws DdsException
     * if the underlying DDS library encounters an error while creating
     * a new Domain Participant.
     * @throws QosXmlNotLoadedException
     * if QoS XML file has not been loaded yet. This could happen if 
     * either {@link BulkDataNTGlobalConfiguration#loadQosXml()} or
     * {@link BulkDataNTGlobalConfiguration#loadQosXml(Path)} has not
     */
    synchronized BulkDataNTDomainParticipant
        useDomainParticipant(String libraryName,
                             String profileName,
                             int domainId)
        throws QosLibraryNotExistException,
               QosProfileNotExistException,
               DdsException,
               QosXmlNotLoadedException {
        Validate.notEmpty(libraryName);
        Validate.notEmpty(profileName);
        
        Optional<Logger> logger
            = BulkDataNTGlobalConfiguration.getInstance().getLogger();
 
        BulkDataNTDomainParticipantIdentifier identifier
            = new BulkDataNTDomainParticipantIdentifier(currentQosXmlContent,
                                                        libraryName,
                                                        profileName,
                                                        domainId);
        if (participantPool.containsKey(identifier)) {
            BulkDataNTDomainParticipant domainParticipant
                = participantPool.get(identifier);
            if (domainParticipant.use()) {
                return domainParticipant;
            }
            
            // The domain participant was already destroyed. Create a new
            // one.
        }

        checkExistenceOfLibrary(libraryName);
        checkExistenceOfProfile(libraryName, profileName);
        DomainParticipant participant;

        try {
            // Obtain Participant QoS with the given library and profile name,
            // and disable the "autoenable" switch to make sure that what
            // the participant will create is always disabled when it is
            // created.
            
            DomainParticipantQos participantQos = new DomainParticipantQos();
            participantFactory.get_participant_qos_from_profile(participantQos,
                                                                libraryName,
                                                                profileName);
            participantQos.entity_factory.autoenable_created_entities = false;
            
            StringSeq initialPeers = participantQos.discovery.initial_peers;
            String msg
                = "List of initial peers: " +
                    String.join(", ",
                                BulkDataNTUtil
                                .convertStringSeqToList(initialPeers));
            logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG, msg));

            participant
                = participantFactory
                .create_participant(domainId, participantQos,
                                    null, // listener won't be used
                                    StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                throw new RETCODE_ERROR("create_participant() returned null.");
            }
        } catch (RETCODE_ERROR error) {
            String msg
                = String.format("The underlying DDS library encounters an " +
                                "error while creating a new Domain " +
                                "Participant." +
                                "(Domain ID: %d, library: %s, " +
                                "profile: %s).",
                                domainId, libraryName,
                                profileName);
            throw new DdsException(msg, error);
        }
        
        try {
            BulkDataNTDomainParticipant bdParticipant
                = new BulkDataNTDomainParticipant(participant, libraryName,
                    profileName, domainId,
                    logger);
            participantPool.put(identifier, bdParticipant);
            return bdParticipant;
        } catch (DdsException ex) {
            // Clean-up
            try {
                participantFactory.delete_participant(participant);
            } catch (RETCODE_ERROR error) {
                throw new DdsException(ex.getMessage() +
                                       " (clean-up also failed)",
                                       ex.getDdsError().orElse(null));                    
            }
            
            throw ex;
        }

    }
    
    /**
     * The caller declares that it does no longer need the given participant
     * so that the reference count is decremented and destroyed if nobody
     * else still uses it.
     * 
     * <p>
     * The caller must use this method instead of
     * {@link BulkDataNTDomainParticipant#unuse()} in order to delete the 
     * reference of the given domain participant from the pool when it is 
     * not used anymore. Otherwise, the pool keeps the reference and it
     * will never be garbage-collected until the application terminates.
     * 
     * @param participant The domain participant that is no longer needed.
     *                    Must not be null.
     */
    synchronized void
        unuseDomainParticipant(BulkDataNTDomainParticipant participant)
        throws DdsException {
        Validate.notNull(participant);
        
        boolean destroyed;
        try {
            destroyed = participant.unuse();
        } catch (DdsException ex) {
            // This participant is not useful anymore. Let's delete from
            // the pool. Note that this exception occurs only when the
            // reference count of the participant became 0, and it failed
            // to be destroyed. So, this participant is not used by anyone,
            // and can be deleted from the pool.
            deleteFromPool(participant);
            throw ex;
        }
        
        if (destroyed) {
            try {
                participantFactory
                    .delete_participant(participant.getDdsDomainParticipant());
            } catch (RETCODE_ERROR error) {
                String msg
                    = String.format("The underlying DDS library failed to " +
                                    "delete the domain participant." +
                                    "(Domain ID: %d, library: %s, " +
                                    "profile: %s).",
                                    participant.getDomainId(),
                                    participant.getQosLibraryName(),
                                    participant.getQosProfileName());
                throw new DdsException(msg, error);
            } finally {
                deleteFromPool(participant);
            }
        }
    }
    
    /**
     * Delete the specified participant from the pool
     * 
     * @param participant The participant to be deleted from the pool.
     */
    private synchronized void
        deleteFromPool(BulkDataNTDomainParticipant participant) {
        for (Map.Entry<BulkDataNTDomainParticipantIdentifier,
                       BulkDataNTDomainParticipant> e:
                       participantPool.entrySet()) {
            if (e.getValue() == participant) {
                participantPool.remove(e.getKey(), e.getValue());
            }
        }   
    }
    
    /**
     * This class is intended to be used as the key of HashMap to identify
     * the set of QoS XML, QoS library name, QoS profile name and Domain ID.
     * If all those values are the same, it is considered that the resultant
     * Domain Participant is the same.
     * 
     * <p>
     * Note that QoS library name, QoS profile name and Domain ID are not
     * sufficient to identify a domain participant because QoS XML may be
     * reloaded by {@link BulkDataNTGlobalConfiguration#loadQosXml()} or
     * {@link BulkDataNTGlobalConfiguration#loadQosXml(java.nio.file.Path)}.
     * Thus, QoS XML contents is also used as the part of the key.
     * 
     * <p>
     * This class is immutable.
     * 
     * <p>
     * TRIVIAL TODO: In this method, the canonicalization of QoS XML is not
     *               done. It means that even if you insert a single space
     *               additionally within a tag (e.g. \<tag\> => \<tag \>),
     *               the two QoS XML is considered as different. This will
     *               not be a significant problem because BulkData Java
     *               library does not expect that the application reloads
     *               QoS XML file so many times. Typically, the application
     *               loads QoS XML file only once at the beginning.
     */
    class BulkDataNTDomainParticipantIdentifier {
        private String qosXmlContent;
        private String qosLibraryName;
        private String qosProfileName;
        private int domainId;
        
        /**
         * Constructor.
         * 
         * @param qosXmlContent The contents of QoS XML.
         * @param qosLibraryName QoS library name.
         * @param qosProfileName QoS profile name.
         * @param domainId Domain ID.
         */
        BulkDataNTDomainParticipantIdentifier (String qosXmlContent,
                                               String qosLibraryName,
                                               String qosProfileName,
                                               int domainId) {
            this.qosXmlContent = qosXmlContent;
            this.qosLibraryName = qosLibraryName;
            this.qosProfileName = qosProfileName;
            this.domainId = domainId;
        }

        /**
         * This is the hashCode() implementation generated by Eclipse Neon.2.
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + domainId;
            result = prime * result
                + ((qosLibraryName == null) ? 0 : qosLibraryName.hashCode());
            result = prime * result
                + ((qosProfileName == null) ? 0 : qosProfileName.hashCode());
            result = prime * result
                + ((qosXmlContent == null) ? 0 : qosXmlContent.hashCode());
            return result;
        }

        /**
         * This is the equal() implementation generated by Eclipse Neon.2.
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            BulkDataNTDomainParticipantIdentifier other = (BulkDataNTDomainParticipantIdentifier) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (domainId != other.domainId) {
                return false;
            }
            if (qosLibraryName == null) {
                if (other.qosLibraryName != null) {
                    return false;
                }
            } else if (!qosLibraryName.equals(other.qosLibraryName)) {
                return false;
            }
            if (qosProfileName == null) {
                if (other.qosProfileName != null) {
                    return false;
                }
            } else if (!qosProfileName.equals(other.qosProfileName)) {
                return false;
            }
            if (qosXmlContent == null) {
                if (other.qosXmlContent != null) {
                    return false;
                }
            } else if (!qosXmlContent.equals(other.qosXmlContent)) {
                return false;
            }
            return true;
        }

        /**
         * This method is generated by Eclipse Neon.2.
         */
        private BulkDataNTDomainParticipantFactory getOuterType() {
            return BulkDataNTDomainParticipantFactory.this;
        }
    }
}
