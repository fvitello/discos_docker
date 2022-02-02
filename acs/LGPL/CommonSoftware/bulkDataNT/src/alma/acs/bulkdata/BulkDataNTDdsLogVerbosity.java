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

import com.rti.ndds.config.LogVerbosity;

import org.apache.commons.lang.Validate;

/**
 * This enum represents the log verbosity of the logging facility of the DDS
 * library.
 *
 * <p>
 * The RTI DDS library has its own logging facility. The logging verbosity
 * of this logging facility can be changed by specifying one of these enum
 * values.
 *
 * @author Takashi Nakamoto
 */
public enum BulkDataNTDdsLogVerbosity {
    /**
     * With this log level, the DDS library will output only error messages.
     * This is the minimum log level. 
     */
    ERROR(0, LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_ERROR),

    /**
     * With this log level, the DDS library will output only error and warning
     * messages.
     */
    WARNING(1, LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_WARNING),

    /**
     * With this log level, errors, warnings and verbose information about
     * the lifecycles of local RTI Connext objects will be logged by the DDS
     * library.
     */
    STATUS_LOCAL(2, LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_LOCAL),

    /**
     * With this log level, errors, warnings and verbose information about
     * the lifecycles of remote RTI Connext objects will be logged by the DDS
     * library.
     */
    STATUS_REMOTE(3, LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_REMOTE),

    /**
     * With this log level, the DDS library will output all log messages 
     * including errors, warnings, verbose information about the lifecycle of
     * local and remote RTI Connext object, and periodic information about
     * RTI Connext threads. This is the maximum log level.
     */
    STATUS_ALL(4, LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);

    private final int logLevel;
    private final LogVerbosity ddsLogVerbosity;

    /**
     * Maximum log level allowed for the environmental variable
     * {@value alma.acs.bulkdata.BulkDataNTGlobalConfiguration#VERBOSITY_VAR_NAME}.
     */
    public static final int MAX_LOG_LEVEL = 4;

    /**
     * Minimum log level allowed for the environmental variable
     * {@value alma.acs.bulkdata.BulkDataNTGlobalConfiguration#VERBOSITY_VAR_NAME}.
     */
    public static final int MIN_LOG_LEVEL = 0;

    /**
     * The constructor of one enum value.
     * 
     * @param logLevel See {@link #getLogLevel()}.
     * @param ddsLogVerbosity See {@link #getDdsLogVerbosity()}.
     */
    private BulkDataNTDdsLogVerbosity(int logLevel,
                                      LogVerbosity ddsLogVerbosity) {
        Validate.notNull(ddsLogVerbosity);
        
        this.logLevel = logLevel;
        this.ddsLogVerbosity = ddsLogVerbosity;

        if (logLevel < MIN_LOG_LEVEL || logLevel > MAX_LOG_LEVEL) {
            String msg 
                = String.format("Log level must be between %d and %d.",
                                MIN_LOG_LEVEL, MAX_LOG_LEVEL);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * This method returns the log level. The returned value corresponds
     * the value in the environmental variable
     * {@value alma.acs.bulkdata.BulkDataNTGlobalConfiguration#VERBOSITY_VAR_NAME}.
     * The returned value is in the range of {@link #MIN_LOG_LEVEL} and
     * {@link #MAX_LOG_LEVEL}.
     *
     * <p>
     * This value is unique among all the verbosity levels.
     *
     * @return The log level between {@link #MIN_LOG_LEVEL} and
     *         {@link #MAX_LOG_LEVEL}.
     */
    public int getLogLevel() {
        return logLevel;
    }

    /**
     * This method returns an enum value of {@link LogVerbosity} that
     * corresponds to the log level returned by {@link #getLogLevel()}.
     * The returned value is intended to be passed to
     * {@link com.rti.ndds.config.Logger#set_verbosity_by_category(LogCategory, LogVerbosity)}.
     * 
     * @return An enum value of {@link LogVerbosity} that corresponds to
     *         the log level returned by {@link #getLogLevel()}.
     */
    public LogVerbosity getDdsLogVerbosity() {
        return ddsLogVerbosity;
    }
}
