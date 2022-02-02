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
import alma.acs.logging.ClientLogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.LogManager;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class holds the configuration of Java version of bulkDataNTGenSender.
 *
 * @author Takashi Nakamoto
 */
class BulkDataNTGenSenderConfigurations {
    private Logger logger;
    private boolean parsed;

    /**
     * This variable holds the command name set by the system property
     * {@link #COMMAND_NAME_PROPERTY_KEY}. If the property is not set,
     * this string will have an empty string. This variable will never
     * be null.
     */
    private String commandName = "";

    /**
     * Key of system property that holds the command name of
     * BulkDataNTGenSender. This property shall be set by the wrapper script
     * that invokes the main methods of {@link BulkDataNTGenSender} class.
     * By changing this property, this standalone command line application
     * will be able to show the usage with the given command name.
     */
    private static final String COMMAND_NAME_PROPERTY_KEY
        = "alma.acs.bulkdata.gen_sender.command_name";

    /**
     * If the command name is set by the system property
     * {@link #COMMAND_NAME_PROPERTY_KEY}, that name is used as the logger's
     * name. If the property is not set or empty, this default logger name
     * will be used.
     */
    private static final String DEFAULT_LOGGER_NAME = "BulkDataNTGenSender";

    protected static final String DEFAULT_STREAM_NAME = "DefaultStream";
    protected static final String STREAM_NAME_OPTION = "s";
    private final StringConfiguration confStreamName
        = new StringConfiguration(DEFAULT_STREAM_NAME,
                                  "streamName",
                                  "Name of stream.",
                                  STREAM_NAME_OPTION) {
                @Override protected ValidationResult validate(String str) {
                    return BulkDataNTStream.getNameValidator().validate(str);
                }
            };
    private final AlphanumericStringConfiguration confQosLibraryName
        = new AlphanumericStringConfiguration("BulkDataQoSLibrary",
                                              "qosLib",
                                              "Name of QoS library name.",
                                              "qos_lib");
    private final AlphanumericStringConfiguration confStartSendParameter
        = new AlphanumericStringConfiguration("defaultParameter",
                                              "param",
                                              "Parameter for startSend().",
                                              "p");
    protected static final String FLOW_NAME_OPTION = "f";
    private final MultipleStringConfiguration confFlowNames
        = new MultipleStringConfiguration("flowName",
                                          "Flow name. Use this " +
                                          "option multiple " +
                                          "times to create " +
                                          " multiple flows.",
                                          FLOW_NAME_OPTION,
                                          true, // this configuration is mandatory
                                          false // duplicated name is not allowed
                                          ) {
                @Override
                protected ValidationResult validate(String str) {
                    return BulkDataNTSenderFlow.getNameValidator()
                        .validate(str);
                }
            };
    private final IntegerConfiguration confDataSize
        = new IntegerConfiguration(65000, 1, Integer.MAX_VALUE,
                                   "dataSize",
                                   "Size of the dummy data to send in bytes.",
                                   "b");
    private final IntegerConfiguration confNumLoops
        = new IntegerConfiguration(1, 1, Integer.MAX_VALUE,
                                   "loops",
                                   "# of loops/iterations.",
                                   "l");
    private final IntegerConfiguration confNumSequences
        = new IntegerConfiguration(1, 1, Integer.MAX_VALUE,
                                   "sequence",
                                   "# of sequences to send (outer loop)",
                                   "L");
    private final DurationConfiguration confSendFrameTimeout
        = new DurationConfiguration(BulkDataNTSenderFlowConfigurationDefault
                                    .DEFAULT_SEND_FRAME_TIMEOUT,
                                    "timeout",
                                    "Send frame timeout in sec.",
                                    "t");
    private final DurationConfiguration confAckTimeout
        = new DurationConfiguration(BulkDataNTSenderFlowConfigurationDefault
                                    .DEFAULT_ACK_TIMEOUT,
                                    "timeout",
                                    "ACK timout in sec.",
                                    "a");
    
    protected static final String THROTTLING_OPTION = "o"; 
    private static final String confThrottlingDesc
        = String.format("Throttling in MBytes/sec between %e and %f. " +
                        "You may want to set the largest number within " +
                        "this range to effectively disable throtting.",
                        BulkDataNTSenderFlow.MIN_THROTTLING,
                        BulkDataNTSenderFlow.MAX_THROTTLING);
    private final DoubleConfiguration confThrottling
        = new DoubleConfiguration(BulkDataNTSenderFlowConfigurationDefault
                                  .DEFAULT_THROTTLING,
                                  BulkDataNTSenderFlow.MIN_THROTTLING,
                                  BulkDataNTSenderFlow.MAX_THROTTLING,
                                  "throttle",
                                  confThrottlingDesc,
                                  THROTTLING_OPTION);
    private final BooleanConfiguration confNoWaitForKey
        = new BooleanConfiguration("no wait for a key", "n");
    private final BooleanConfiguration confCDPProtocolCompatibility
        = new BooleanConfiguration("CDP protocol compatibility (-p option " +
                                   "ignored and parameter = data size as " +
                                   "unsigned 32-bit integer).",
                                   "c");
    private final PathConfiguration confLoggingPropertiesFile
        = new PathConfiguration("file",
                                "Properties file of java.util.logging in. " +
                                "java.util.Properties format. " + 
                                "If this option is specified, Java standard " +
                                "logging facility will be used instead of  " +
                                "ACS logging facility.",
                                "g");
    
    protected static final String DATA_FILE_OPTION = "r";
    private final PathConfiguration confDataFile
        = new PathConfiguration("file",
                                "Read data from this file instead of sending " +
                                "fake data.",
                                DATA_FILE_OPTION);

    private final CLIOption cliOptions[] = {
        confStreamName,
        confFlowNames,
        confDataSize,
        confDataFile,
        confStartSendParameter,
        confNumLoops,
        confNoWaitForKey,
        confSendFrameTimeout,
        confAckTimeout,
        confThrottling,
        confCDPProtocolCompatibility,
        confNumSequences,
        confQosLibraryName,
        confLoggingPropertiesFile,
    };

    /**
     * This constructor instantiates BulkDataNTGenSenderOptions with
     * default parameters.
     */
    public BulkDataNTGenSenderConfigurations() {
        parsed = false;

        // Obtain command name from the system property.
        // This will be used to display the usage of the command,
        // and also as the logger name.
        commandName
            = System.getProperties().getProperty(COMMAND_NAME_PROPERTY_KEY,
                                                 "");
    }

    /**
     * This method returns the specified BulkData Stream name.
     *
     * @return non-null/non-empty string that only contains ASCII
     *         alphanumeric characters.
     */
    synchronized public String getStreamName() {
        return confStreamName.getValue();
    }

    /**
     * This method returns configured BulkData Flow names. This method may
     * return an empty list of Flow names if the user does not explicitly
     * configure Flow name. The caller must care the case when this method
     * returns an empty list. The strings in the returned list is not null
     * or an empty string. They only contain ASCII alphanumeric characters.
     *
     * @return non-null list of Flow names in String.
     */
    synchronized public List<String> getFlowNames() {
        return confFlowNames.getValue();
    }

    /**
     * This method returns an instance of
     * {@link BulkDataNTSenderStreamConfiguration} which holds the 
     * configuration values set by the command line arguments.
     *
     * @return The configuration of Sender Stream.
     */
    synchronized public BulkDataNTSenderStreamConfiguration
        getSenderStreamConfiguration() 
        throws IllegalDomainIdException {
        String qosLibraryName = confQosLibraryName.getValue();

        return new BulkDataNTSenderStreamConfigurationDefault() {
            @Override public String getQosLibraryName() {
                return qosLibraryName;
            }

            @Override public Optional<Logger> getLogger() {
                return Optional.of(BulkDataNTGenSenderConfigurations.this
                                   .getLogger());
            }
        };
    }

    /**
     * This method returns an instance of
     * {@link BulkDataNTSenderFlowConfiguration} which holds the 
     * configuration values set by the command line arguments.
     *
     * @return The configuration of Sender Flow.
     */
    synchronized public BulkDataNTSenderFlowConfiguration
        getSenderFlowConfiguration() {
        String qosLibraryName = confQosLibraryName.getValue();
        Duration ackTimeout = confAckTimeout.getValue(); // Duration is immutable.
        Duration sendFrameTimeout = confSendFrameTimeout.getValue();
        double throttling = confThrottling.getValue();

        return new BulkDataNTSenderFlowConfigurationDefault() {
            @Override public String
                getQosLibraryName() { return qosLibraryName; }
            @Override public Duration
                getAckTimeout() { return ackTimeout; }
            @Override public Duration
                getSendFrameTimeout() { return sendFrameTimeout; }
            @Override public double
                getThrottling() { return throttling; }

            @Override public Optional<Logger> getLogger() {
                return Optional.of(BulkDataNTGenSenderConfigurations.this
                                   .getLogger());
            }
        };
    }

    /**
     * This method returns QoS XML library name.
     *
     * @return Name of QoS XML library.
     */
    synchronized public String getQosLibraryName() {
        return confQosLibraryName.getValue();
    }

    /**
     * This method returns the parameter string that shall be passed
     * to {@link BulkDataNTSenderFlow#startSend(byte[])}.
     *
     * @return Parameter string.
     */
    synchronized public String getStartSendParameter() {
        return confStartSendParameter.getValue();
    }

    /**
     * This method returns the data size in bytes. {@link BulkDataNTGenSender}
     * will send the specified size of data (if {@link #getDataFile()} is null)
     * N times (N is the number of loops = {@link #getNumLoops()}) between
     * a pair of startSend() and stopSend() calls.
     *
     * @return a positive integer value.
     */
    synchronized public int getDataSize() {
        return confDataSize.getValue();
    }

    /**
     * This method returns the number of loops.
     *
     * <p>
     * The returned value represents the number of sendData() calls within
     * one sequence (the number of sequence can be obtained by
     * {@link #getNumSequences()}. These sendData() calls will be done 
     * between a pair of {@link startSend(byte[])} and {@link stopSend()}
     * calls.
     *
     * <p>
     * The dummy data whose size is {@link #getDataSize()} or the data in
     * the file specified by {@link #getDataFile()} are sent this number
     * of times.
     *
     * @return a positive integer value.
     */
    synchronized public int getNumLoops() {
        return confNumLoops.getValue();
    }

    /**
     * This method returns the number of sequences.
     *
     * <p>
     * In one sequence, {@link #startSend(byte[])} will be called once,
     * {@link sendData(byte[])} is called N times (N is the number
     * returned by {@link #getNumLoops()} and {@link #stopSend()} will
     * be called once.
     *
     * <p>
     * If this method returns more than one, it means that the above
     * sequence will repeated the specified number of times.
     *
     * @return a positive integer value.
     */
    synchronized public int getNumSequences() {
        return confNumSequences.getValue();
    }

    /**
     * This method returns send frame timeout. The returned value is neither
     * infinite nor auto duration.
     *
     * @return Send frame timeout. Neither infinite nor auto.
     *
     * @see BulkDataNTSenderFlowConfiguration#getSendFrameTimeout()
     */
    synchronized public Duration getSendFrameTimeout() {
        return confSendFrameTimeout.getValue();
    }

    /**
     * This method returns ACK timeout. The returned value is neither
     * infinite nor auto duration.
     *
     * @return ACK timeout. Neither infinite nor auto.
     *
     * @see BulkDataNTSenderFlowConfiguration#getAckTimeout()
     */
    synchronized public Duration getAckTimeout() {
        return confAckTimeout.getValue();
    }

    /**
     * This method returns the throttling in MB/s.
     *
     * @return The throttling value in MB/s.
     *
     * @see BulkDataNTSenderFlowConfiguration#getThrottling()
     */
    synchronized public double getThrottling() {
        return confThrottling.getValue();
    }
    
    /**
     * This method returns whether the application should not wait for a key
     * input to start sending data.
     *
     * @return True if the application is supposed to wait for a key input
     *         to start sending data.
     */
    synchronized public boolean getNoWaitForKey() {
        return confNoWaitForKey.getValue();
    }

    /**
     * This method returns whether the data sent from this application
     * must be compatible with CDP protocol.
     *
     * If this method returns true, this application must send startSend
     * frame with the data size as 32-bit unsigned integer (little endian)
     * instead of parameter name.
     *
     * If this method returns false, this application must send startSend
     * frame with parameter name returned by getStartSendParameter().
     */
    synchronized public boolean getCDPProtocolCompatibility() {
        return confCDPProtocolCompatibility.getValue();
    }


    /**
     * This method returns the data file that the user specified. It can be
     * null if the user did not explicitly specify the data file, which means
     * dummy data can be used.
     *
     * @return The data file that the user specified, or null if the user did
     *         not explicitly specify it.
     */
    synchronized public File getDataFile() {
        return confDataFile.getValue();
    }

    /**
     * This method parses the given command line arguments and sets the
     * obtained arguments to this instance. After the invocation of this
     * method, the getter methods of this instance will return the
     * option that the user specified in the command line argument.
     * For the arguments that the user did not explicitly specify, the
     * getter methods will return the default value.
     * 
     * This method must not be called twice or more. If it is called
     * more than once, this method throws one of RuntimeException.
     *
     * This method prints out the help if the given command line is
     * invalid and returns false.
     *
     * @return true if the given command line is valid.
     */
    synchronized public boolean parseCommandLineArguments(String[] args) {
        if (parsed) {
            throw new CalledMoreThanOnceException();
        }

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        for (CLIOption cliopt: cliOptions) {
            options.addOption(cliopt.getCLIOption());
        }

        try {
            CommandLine cmdLine = parser.parse(options, args);

            for (CLIOption cliopt: cliOptions) {
                cliopt.getOptionValue(cmdLine);
            }
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();

            String optionSyntax
                = " -f flowName1 [-f flowName2 ...] [options]";

            formatter.printHelp(commandName + optionSyntax, "",
                                options, "\n" + ex.getMessage());
            return false;
        }
 
        parsed = true;
        return true;
    }

    /**
     * This method returns the logger for {@link BulkDataNTGenSender}.
     *
     * @return Logger for {@link BulkDataNTGenSender}
     */
    synchronized public Logger getLogger() {
        if (logger != null) {
            return logger;
        }

        String loggerName
            = commandName.isEmpty() ? DEFAULT_LOGGER_NAME : commandName;

        File propertiesFile = confLoggingPropertiesFile.getValue();
        if (propertiesFile == null) {
            // This seems to be the standard way of getting logger for a 
            // standalone application that is not running in ACS component.
            // See ACS/Documents/Logging_and_Archiving.doc for more details.
            logger = ClientLogManager.getAcsLogManager()
                .getLoggerForApplication(loggerName, true);
        } else {
            try (InputStream is = new FileInputStream(propertiesFile)) {
                LogManager.getLogManager().readConfiguration(is);
                logger = Logger.getLogger(loggerName);
                logger.log(AcsLogLevel.INFO,
                           "The logging properties are successfully read " +
                           "from " + propertiesFile.getPath());
            } catch (Exception ex) {
                logger = Logger.getLogger(loggerName);
                logger.log(AcsLogLevel.WARNING,
                           "The specified logging properties file: " +
                           propertiesFile.getPath() + " cannot be read. " + 
                           "JDK's defualt logging settings will be used.");
            }
        }
        return logger;
    }
}
