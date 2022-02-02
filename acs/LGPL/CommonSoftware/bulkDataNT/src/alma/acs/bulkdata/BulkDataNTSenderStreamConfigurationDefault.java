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
import java.util.logging.Logger;

/**
 * This class holds the default configuration {@link BulkDataNTSenderStream}.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTSenderStreamConfigurationDefault
    extends BulkDataNTStreamConfigurationDefault
    implements BulkDataNTSenderStreamConfiguration {
    private final static Logger defaultLogger
        = Logger.getLogger(BulkDataNTSenderStream.class.getName());;

    /**
     * This constructor instantiates this configuration object with the
     * default Domain ID. The default Domain ID is obtained from the
     * environmental variable
     * {@value alma.acs.bulkdata.BulkDataNTStreamConfigurationDefault#ENV_VAR_FOR_DOMAIN_ID}.
     *
     * <p>
     * If
     * {@value alma.acs.bulkdata.BulkDataNTStreamConfigurationDefault#ENV_VAR_FOR_DOMAIN_ID}    
     * is not defined, 0 will be used as the Domain ID.
     *
     * @throws IllegalDomainIdException
     * if the environmental variable
     * {@value alma.acs.bulkdata.BulkDataNTStreamConfigurationDefault#ENV_VAR_FOR_DOMAIN_ID}    
     * does not contain a valid 32-bit integer value.
     *
     * @see BulkDataNTStreamConfigurationDefault#BulkDataNTStreamConfigurationDefault()
     */
    public BulkDataNTSenderStreamConfigurationDefault()
        throws IllegalDomainIdException {
        super();
    }

    /**
     * This constructor instantiates this configuration object with the
     * given Domain ID.
     *
     * @param domainId Domain ID for DDS communication.
     *
     * @see BulkDataNTStreamConfigurationDefault#BulkDataNTStreamConfigurationDefault(int)
     */
    public BulkDataNTSenderStreamConfigurationDefault(int domainId) {
        super(domainId);
    }

    /**
     * This method returns the default QoS XML profile name for
     * Sender Streams.
     */
    @Override public String getQosProfileName() { return DEFAULT_QOS_PROFILE_NAME; }
    
    /**
     * The default QoS XML profile name for Sender Streams.
     */
    protected static final String
        DEFAULT_QOS_PROFILE_NAME = "SenderStreamDefaultQosProfile";

    /**
     * This method returns the default logger for {@link BulkDataNTSenderStream}.
     *
     * @return Default logger for {@link BulkDataNTSenderStream}.
     */
    @Override public Optional<Logger> getLogger() {
        return Optional.of(defaultLogger);
    }
}

