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

/**
 * The getter methods of this class return the default configuration
 * values of {@link BulkDataNTStream}.
 *
 * @author Takashi Nakamoto
 */
public abstract class BulkDataNTStreamConfigurationDefault
    extends BulkDataNTDdsConfigurationDefault
    implements BulkDataNTStreamConfiguration {
    private final static Logger defaultLogger
        = Logger.getLogger(BulkDataNTStream.class.getName());
    private final int domainId;

    /**
     * The environmental variable name from which Domain ID is determined.
     *
     * <p>
     * The integer value in this environmental variable is used as Domain ID.
     * This is the same behavior as BulkData C++ library. See the comments
     * in {@code BulkDataNTStream::createDDSParticipant()} in bulkDataNTStream.cpp
     * for more details about why this environmental variable is used as the
     * Domain ID.
     */
    protected static final String ENV_VAR_FOR_DOMAIN_ID = "ACS_INSTANCE";

    /**
     * This constructor instantiates this configuration object with the
     * default Domain ID. The default Domain ID is obtained from the
     * environmental variable {@value ENV_VAR_FOR_DOMAIN_ID}.
     *
     * <p>
     * If {@value ENV_VAR_FOR_DOMAIN_ID} is not defined, 0 will be used
     * as the Domain ID.
     *
     * @throws IllegalDomainIdException
     * if the environmental variable {@value ENV_VAR_FOR_DOMAIN_ID} does not
     * contain a valid 32-bit integer value.
     */
    public BulkDataNTStreamConfigurationDefault()
        throws IllegalDomainIdException {
        Optional<Logger> logger = getLogger();
        
        int did;

        try {
            did = readAcsInstanceEnvVar();
            String msg
                = String.format("Found %s=%d, using it as DDS Domain ID.",
                                ENV_VAR_FOR_DOMAIN_ID, did);
            logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG, msg));
        } catch (EnvironmentalVariableNotDefinedException ex) {
            // If the environmental variable is not defined, use default value
            // 0 for the Domain ID.
            did = 0;
            String msg
                = String.format("The environmental variable %s is not " +
                                "defined. Use the default value %d as DDS " +
                                "Domain ID.",
                                ENV_VAR_FOR_DOMAIN_ID, did);
            logger.ifPresent(l -> l.log(AcsLogLevel.DEBUG, msg));
        } catch (IllegalDomainIdException ex) {
            // If not a valid value is set to the environmental variable,
            // notify the caller.
            throw ex;
        }
        
        this.domainId = did;
    }

    /**
     * This constructor instantiates this configuration object with the
     * given Domain ID.
     *
     * @param domainId Domain ID for DDS communication.
     */
    public BulkDataNTStreamConfigurationDefault(int domainId) {
        this.domainId = domainId;
    }

    /**
     * This method returns the Domain ID.
     *
     * @return Domain ID.
     */
    @Override public int getDomainId() { return domainId; }

    /**
     * This method reads the value in the environmental variable
     * {@value ENV_VAR_FOR_DOMAIN_ID} and returns the value as an integer.
     *
     * @return The integer value in the environmental variable
     *         {@value ENV_VAR_FOR_DOMAIN_ID}.
     *
     * @throws EnvironmentalVariableNotDefinedException
     * if the environmental variable {@value ENV_VAR_FOR_DOMAIN_ID}
     * is not defined.
     * @throws IllegalDomainIdException
     * if the environmental variable {@value ENV_VAR_FOR_DOMAIN_ID}
     * does not contain a 32-bit integer value.
     */
    protected static int readAcsInstanceEnvVar()
        throws EnvironmentalVariableNotDefinedException,
               IllegalDomainIdException {
        String value
            = BulkDataNTGlobalConfiguration.getenv(ENV_VAR_FOR_DOMAIN_ID);
        if (value != null) {
            try {                
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                String msg
                    = String.format("Found %s=%s, but it cannot be converted " +
                                    " to a 32-bit integer",
                                    ENV_VAR_FOR_DOMAIN_ID, value);
                throw new IllegalDomainIdException(msg, value);
            }
        } else {
            throw new EnvironmentalVariableNotDefinedException(ENV_VAR_FOR_DOMAIN_ID);
        }
    }

    /**
     * This method returns the default logger for {@link BulkDataNTStream}.
     *
     * @return Default logger for {@link BulkDataNTStream}.
     */
    @Override public Optional<Logger> getLogger() {
        return Optional.of(defaultLogger);
    }
}
