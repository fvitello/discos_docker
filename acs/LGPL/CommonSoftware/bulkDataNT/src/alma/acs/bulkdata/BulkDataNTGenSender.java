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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.io.IOUtils;

/**
 * This class provides the main method of BulkDataNTGenSender, which is a 
 * generic command line tool to send dummy or user-specified data to one
 * more BulkData Receivers. This is the Java version of bulkDataNTGenSender
 * implemented in C++.
 * 
 * <p>
 * This class is not intended to be used by application programmers. This
 * class is marked as public to show this javadoc to explain the usage of
 * this command line tool. 
 * 
 * <h2>Usage</h2>
 * <p>
 * This command line tool is able to create one Stream and multiple Flows
 * in it, and send the same data to all the Flows. Most of the command line
 * options provided by this command line tool is compatible with the options
 * provided by C++ version of bulkDataNTGenSender. Incompatible options are
 * described below.
 * 
 * <p>
 * You first need to specify, at least, one Flow name to use this tool. Use 
 * {@value alma.acs.bulkdata.BulkDataNTGenSenderConfigurations#FLOW_NAME_OPTION}
 * option to specify a Flow name. To create multiple Flow names, use 
 * {@value alma.acs.bulkdata.BulkDataNTGenSenderConfigurations#FLOW_NAME_OPTION}
 * option multiple times. This Java version does not accepts multiple flow
 * names separated by comma ",".
 * 
 * <p>
 * Unless you explicitly specify the Stream name,
 * {@value alma.acs.bulkdata.BulkDataNTGenSenderConfigurations#DEFAULT_STREAM_NAME}
 * is used as the Stream name by default. To specify the Stream name, use
 * {@value alma.acs.bulkdata.BulkDataNTGenSenderConfigurations#STREAM_NAME_OPTION}
 * option.
 * 
 * <p>
 * The data transfer rate can be adjusted by the throttling option
 * {@value alma.acs.bulkdata.BulkDataNTGenSenderConfigurations#THROTTLING_OPTION}.
 * This option limits the maximum data transfer rate. See
 * {@link BulkDataNTSenderFlowConfiguration#getThrottling()} for more details
 * about the actual meaning of throttling. Unlike C++ version of
 * bulkDataNTGenSender, you cannot specify 0.0 to disable throttling. Instead,
 * specify a large number to effectively disable the throttling.
 * 
 * <p>
 * All the other options are basically compatible with C++ version of
 * bulkDataNTGenSender except that this Java version may validate the
 * command line options more strictly. For example, empty Stream name 
 * is not allowed.
 * 
 * <p>
 * To see all the available options, execute the main method with no command
 * line argument. The help is shown in the console.
 * 
 * <h2>Data format</h2>
 *
 * <p>
 * You can specify the data to send to the receivers using 
 * {@value alma.acs.bulkdata.BulkDataNTGenSenderConfigurations#DATA_FILE_OPTION}
 * option. Please prepare the data to send as a file and specify the file
 * to this option. The file format is described below:
 * 
 * <table border="1">
 * <caption>Data format for BulkDataNTGenSender</caption>
 * <tr>
 *   <th>Address</th>
 *   <th>Length</th>
 *   <th>Valid data range</th>
 *   <th>Description</th>
 * </tr>
 * <tr>
 *   <td>0x00000000</td>
 *   <td>1 byte</td>
 *   <td>0x01 ~ 0xff</td>
 *   <td>The length of the parameter.</td>
 * </tr>
 * <tr>
 *   <td>0x00000001 ~ 0x000000nn</td>
 *   <td>as specified in the address 0x00000000</td>
 *   <td>any byte sequence </td>
 *   <td>Byte sequence of the parameter to send to the receivers before
 *       sending the payload.</td>
 * </tr>
 * <tr>
 *   <td>0x000000nn + 1 ~ 0xmmmmmmmm</td>
 *   <td>1 or more bytes</td>
 *   <td>any byte sequence</td>
 *   <td>Byte sequence of the payload to send to the receivers. 0xmmmmmmmm
 *       is the last address of the file.</td>
 * </tr>
 * </table>
 * 
 * <p>
 * This is the same format that C++ version of bulkDataNTGenSender uses.
 * 
 * <p>
 * Note that the given payload data is first copied to the heap in JVM as
 * a single array, but Java allows up to
 * {@value java.lang.Integer#MAX_VALUE} elements in one array. So, you cannot
 * specify the payload data larger than {@value java.lang.Integer#MAX_VALUE}
 * bytes. The maximum size of the payload data is also limited by the 
 * size of the heap of JVM. In case there is not enough heap space, you
 * will see {@link java.lang.OutOfMemoryError}.
 *   
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTGenSender {
    /**
     * Standard log level for this application.
     */
    private static final Level level = Level.INFO;

    /**
     * Maximum value of 32-bit unsigned integer (2^32 - 1).
     */
    private static long maxUnsignedInt32 = 4294967295L;

    /**
     * Dump all recognized configuration to the standard output.
     * 
     * @param confs Configurations to dump.
     */
    @SuppressWarnings("unused")
    private static void dumpConfigurations(BulkDataNTGenSenderConfigurations confs) {
        System.out.println("Stream name             : "
                           + confs.getStreamName());
        System.out.println("Flow names              : "
                           + StringUtils.join(confs.getFlowNames(), " "));
        System.out.println("Data Size               : "
                           + confs.getDataSize()
                           + " bytes");
        System.out.println("QoS Library Name        : "
                           + confs.getQosLibraryName());
        System.out.println("# of loops              : "
                           + confs.getNumLoops());
        System.out.println("# of outer loops        : "
                           + confs.getNumSequences());
        System.out.println("Parameter for sendStart : "
                           + confs.getStartSendParameter());
        System.out.println("No wait for a key       : "
                           + confs.getNoWaitForKey());
        System.out.println("CDP Protocol Compatible : "
                           + confs.getCDPProtocolCompatibility());
        System.out.println("Send frame timeout      : "
                           + confs.getSendFrameTimeout() + " seconds");
        System.out.println("ACK timeout             : "
                           + confs.getAckTimeout());
        System.out.println("Throttling              : "
                           + confs.getThrottling() + " MBytes/s");
    }
    
    static class ParameterAndData {
        final byte[] param;
        final byte[] data;
        
        public ParameterAndData(byte[] param, byte[] data) {
            this.param = param;
            this.data  = data;
        }
        
        public byte[] getParam() {
            return param;
        }
        
        public byte[] getData() {
            return data;
        }
    }

    /**
     * This method reads the specified file to load the parameter and
     * the data to send.
     *  
     * @param dataFile File to read.
     * 
     * @return The read parameter and the data in byte arrays.
     * 
     * @throws IllegalFileFormatException
     * if the file format in the specified file is illegal.
     * @throws IOException
     * if I/O error happens when reading the specified file.
     */
    private static ParameterAndData readDataFromFile(File dataFile)
        throws IllegalFileFormatException,
               IOException {
        byte[] data;
        byte[] param;
        int paramLen;
        
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(dataFile));
            paramLen = input.readUnsignedByte();
        } catch (EOFException ex) {
            String msg 
                = String.format("The specified file has no data: %s",
                                dataFile.toString());
            if (input != null) {
                input.close();
            }
            throw new IllegalFileFormatException(msg, ex);
        }
        
        if (paramLen == 0) {
            input.close();
            String msg
                = String.format("The first byte of the specified file is " +
                                "0x00. It means that no parameter is given, " +
                                "but it is not allowed: %s",
                                dataFile.toString());
            throw new IllegalFileFormatException(msg);
        }

        try {
            // Read the parameter length and the parameter first.
            param = new byte[paramLen];
            input.readFully(param);
        } catch (EOFException ex) {
            String msg
                = String.format("The first byte of the specified file is " +
                                "0x%02x, which means that the length of the " +
                                "parameter is %s bytes, but the file size " +
                                "is smaller than %s bytes: %s",
                                paramLen, paramLen, paramLen + 1,
                                dataFile.toString()); 
            input.close();
            throw new IllegalFileFormatException(msg, ex);
        }

        // Read the rest of file as the data to send.
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        IOUtils.copy(input, dataStream);
        data = dataStream.toByteArray();
        input.close();

        // Note: the entire byte arrays in the file is loaded to the
        //       memory here. This is because the same data will be
        //       repeatedly sent. This is the same behavior as C++
        //       implementation. For large data that cannot be loaded
        //       to the memory, we need another implementation.
        
        if (data.length == 0) {
            String msg
                 = String.format("The specified file does not contain any " +
                                 "payload data: %s", dataFile.toString());
            throw new IllegalFileFormatException(msg); 
        }
        
        return new ParameterAndData(param, data);
    }

    private static void sendLoop(List<BulkDataNTSenderFlow> flows,
                                 BulkDataNTGenSenderConfigurations confs,
                                 byte[] param,
                                 byte[] data)
    	throws Exception {
    	
    	Logger logger = confs.getLogger();

        long totalDataSize = data.length * confs.getNumLoops();

        double sumThroughput = 0.0;
        double sumTotalThroughput = 0.0;
        Map<BulkDataNTSenderFlow, Double> throughputSums
            = new HashMap<BulkDataNTSenderFlow, Double>();

        // Outer loop
        for (int iSeq = 1; iSeq <= confs.getNumSequences(); iSeq++) {
            System.out.println("Outer Loop: " + iSeq + " of " +
                               confs.getNumSequences());

            // Call startSend() for each flow.
            for (BulkDataNTSenderFlow flow: flows) {
                if (confs.getCDPProtocolCompatibility()) {
                    logger.log(level,
                               String.format("Going to send parameter (= " +
                                             "dataSize*nb_loops): '%d' to " + 
                                             "flow: '%s' to %d receiver(s)",
                                             totalDataSize,
                                             flow.getName(),
                                             flow.getNumberOfReceivers()));

                    // Within 32-bit unsigned integer range.
                    assert totalDataSize <= maxUnsignedInt32;

                    // Serialize the integer into 4 bytes data in
                    // little endian in the same way as C++ implementation.
                    ByteBuffer paramBuffer = ByteBuffer.allocate(8);
                    paramBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    paramBuffer.putLong(totalDataSize);
                    flow.startSend(Arrays.copyOfRange(paramBuffer.array(),
                                                      0, 4));
                } else {
                    logger.log(level,
                               String.format("Going to send parameter: '%s' " +
                                             "to flow: '%s' to %d receiver(s)",
                                             new String(param,
                                                        StandardCharsets.US_ASCII),
                                             flow.getName(),
                                             flow.getNumberOfReceivers()));

                    // Assume that the parameter is given in US-ASCII.

                    flow.startSend(param);
                }
                throughputSums.put(flow, 0.0);
            }
            sumThroughput = 0.0;

            // Inner loop: call sendData() repeatedly for each flow.
            for (int iLoop = 1; iLoop <= confs.getNumLoops(); iLoop++) {
                System.out.println("Loop: " + iLoop + " of " +
                                   confs.getNumLoops());

                for (BulkDataNTSenderFlow flow: flows) {
                    logger.log(level,
                               String.format("Going to send [%d/%d - %d/%d]: " +
                                             "%d Bytes of data to flow: '%s' " +
                                             "to %d receiver(s)",
                                             iSeq, confs.getNumSequences(),
                                             iLoop, confs.getNumLoops(),
                                             data.length,
                                             flow.getName(),
                                             flow.getNumberOfReceivers()));

                    long startTime = System.nanoTime();
                    flow.sendData(data);
                    long elapsedTimeNano = System.nanoTime() - startTime;

                    double throughput
                        = ((double)(data.length) / 1024.0 / 1024.0 *
                           1e9 / (double)elapsedTimeNano);

                    logger.log(level,
                               String.format("Outer Loop [%d/%d] Inner loop " +
                                             "[%d/%d] Transfer rate for flow " +
                                             "'%s': %f MBytes/sec",
                                             iSeq, confs.getNumSequences(),
                                             iLoop, confs.getNumLoops(),
                                             flow.getName(), throughput));


                    sumThroughput += throughput;
                    sumTotalThroughput += throughput;
                    throughputSums.put(flow,
                                       throughputSums.get(flow) + 
                                       throughput);
                }
            }

            // Show average transfer rate and call stopSend() for each flow.
            for (BulkDataNTSenderFlow flow: flows) {
                logger.log(level,
                           String.format("Outer loop [%d/%d]: Average " + 
                                         "transfer rate for flow '%s': %f " +
                                         "MBytes/sec",
                                         iSeq, confs.getNumSequences(),
                                         flow.getName(),
                                         (throughputSums.get(flow)
                                          / confs.getNumLoops())));
                logger.log(level,
                           String.format("Going to send stop to flow: '%s' " +
                                         "to %d receiver(s)",
                                         flow.getName(),
                                         flow.getNumberOfReceivers()));

                flow.stopSend();
            }

            logger.log(level,
                       String.format("\033[0;34m Outer loop [%d/%d]: " + 
                                     "Average transfer rate for all the " +
                                     "flow(s): %f MBytes/sec \033[0m",
                                     iSeq, confs.getNumSequences(),
                                     (sumThroughput
                                      / (confs.getNumLoops() * flows.size()))));
        }

        logger.log(level,
                   String.format("\033[0;31m Average transfer rate for all " +
                                 "the flow(s): %f MBytes/sec \033[0m",
                                 (sumTotalThroughput
                                  / (confs.getNumSequences() *
                                     confs.getNumLoops() *
                                     flows.size()))));
    }

    private static void mainLoop(BulkDataNTGenSenderConfigurations confs)
        throws Exception {
        boolean recreate = true;
        boolean waitKeyInput = ! confs.getNoWaitForKey();
        Scanner stdinScanner = new Scanner(System.in);
        
        File dataFile = confs.getDataFile();
        byte[] data;
        byte[] param;
        if (dataFile == null) {
            // Create fake data.
            data = new byte[confs.getDataSize()];
            for (int i=0; i<data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            // Here we assume that the parameter string contains only
            // ASCII characters.
            param = 
                confs.getStartSendParameter()
                .getBytes(StandardCharsets.US_ASCII);
        } else {
            ParameterAndData paramAndData = readDataFromFile(dataFile);
            param = paramAndData.getParam();
            data = paramAndData.getData();
        }
        
        recreateLoop: while (recreate) {
            // Create a new Stream and new Flows.
            List<BulkDataNTSenderFlow> flows
                = new ArrayList<BulkDataNTSenderFlow>();

            BulkDataNTSenderStream stream
                = new BulkDataNTSenderStream(confs.getStreamName(),
                                             confs.getSenderStreamConfiguration());

            // Make sure that Stream is destroyed when this application
            // is forcibly interrupted by Ctrl + C or for other reasons.
            Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        try {
                            stream.destroy();
                        } catch (Exception ex) {
                            // We cannot help much here. Just output the 
                            // exception and terminate the application.
                            Logger logger = confs.getLogger();
                            logger.log(AcsLogLevel.ERROR,
                                       "Failed to destroy Stream: " +
                                       stream.getName(),
                                       ex);
                        }
                    }
                });

            try {
                for (String flowName: confs.getFlowNames()) {
                    flows.add(stream.createFlow(flowName,
                                                Optional.empty(),
                                                confs.getSenderFlowConfiguration()));
                }

                // Print out what we have created.
                System.out.println("The following " + stream.getFlowNumber() +
                                   " flows has/have been created:[ " + 
                                   stream.getFlowNames().stream()
                                   .collect(Collectors.joining(" ")) +
                                   " ] on stream: " + stream.getName());

                if (waitKeyInput) {
                    System.out.println("press ENTER to send data " +
                                       "(start/data/stop) to connected " +
                                       "receivers ...");
                    stdinScanner.nextLine();
                } else {
                    // Here we wait 10 seconds because bulkDataNTGenSender
                    // implemented in C++ does so.
                    TimeUnit.SECONDS.sleep(10);
                }

                sendLoop: while (true) {
                    sendLoop(flows, confs, param, data);

                    // We exit this program after sending data once
                    // if the user asks not to wait the key input.
                    if (!waitKeyInput) {
                        recreate = false;
                        break sendLoop;
                    }

                    System.out.println("press 'r' for re-send data, " +
                                       "'c' for re-create stream+flow(s), " + 
                                       "and any other key for exit + ENTER");
                    String cmd = stdinScanner.nextLine();
                    if (cmd.equals("r")) {
                        // Repeat the sendLoop.
                    } else if (cmd.equals("c")) {
                        // Recreate the stream and flows by going back
                        // to recreateLoop.
                        recreate = true;
                        break sendLoop;
                    } else {
                        // Exit this application.
                        recreate = false;
                        break sendLoop;
                    }
                } // sendLoop
            } catch (FileNotFoundException ex) {
                System.err.println("Failed to read " +
                                   ex.getMessage());
                break recreateLoop;
            } catch (Exception ex) {
            	// Even if an exception happens, the exception is simply
                // thrown to the caller as this is the test program, and
                // can immediately quit when it encounters an error.
                throw ex;
            } finally {
                stream.destroy();
            }
        }
    }

    public static void main(String[] args)
        throws Exception {
        try {
            BulkDataNTGenSenderConfigurations confs = new BulkDataNTGenSenderConfigurations();
            if (!confs.parseCommandLineArguments(args)) {
                System.exit(1);
            }
            //dumpConfigurations(confs);
    
            BulkDataNTGlobalConfiguration globalConf
                = BulkDataNTGlobalConfiguration.getInstance();
            globalConf.setLogger(confs.getLogger());
            globalConf.loadQosXml();
            try {
                globalConf.setDdsLogVerbosityFromEnvVar();
            } catch (InvalidEnvironmentalVariableException ex) {
                // Use WARNING level if BULKDATA_NT_DEBUG is not defined or
                // contains an invalid value.
                globalConf.setDdsLogVerbosity(BulkDataNTDdsLogVerbosity.WARNING);
            }
    
            // Check if the data size that will be sent within one loop is
            // within 32-bit unsigned integer range.
            long totalDataSize
                = (long)(confs.getDataSize()) * (long)(confs.getNumLoops());
            if (confs.getCDPProtocolCompatibility() &&
                totalDataSize > maxUnsignedInt32) {
                System.err.println("The data size between startSend and " +
                                   "stopSend is configured to be " +
                                   totalDataSize + "(= " +
                                   confs.getDataSize() + " x " +
                                   confs.getNumLoops() + " bytes, but it is too " +
                                   "big for CDB protocol compatibility mode." +
                                   "Up to (2^32 - 1) = " + maxUnsignedInt32 +
                                   "bytes are supported.");
                System.exit(1);
            }
    
            mainLoop(confs);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("\n" + ex.getMessage());
            System.exit(1);
        }
    }
}
