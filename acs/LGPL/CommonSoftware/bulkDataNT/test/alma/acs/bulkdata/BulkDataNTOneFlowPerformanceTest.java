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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;

/**
 * This tiny program measures the performance of BulkDataNTSender in Java
 * with one Stream and one Flow.
 *
 * TODO: document this program more.
 */
class BulkDataNTOneFlowPerformanceTest {
    /**
     * This method calculates the data transfer rate based on the given
     * data transmission start time, end time and the data size.
     *
     * @param start Start time of the data transmission.
     * @param end   End time of the data transmission.
     * @param dataSize Data size in bytes.
     *
     * @return Calculated data transfer rate in MB/s.
     */
    public static double calculateDataRate(Instant start,
                                           Instant end,
                                           long dataSize) {
        Validate.notNull(start);
        Validate.notNull(end);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time is after end time.");
        }
        if (dataSize <= 0) {
            throw new IllegalArgumentException("dataSize must be positive.");
        }

        long timeNanos = start.until(end, ChronoUnit.NANOS);
        double dataRate
            = ((double)dataSize / 1024.0 / 1024.0)
            / ((double)timeNanos * 1e-9);
        return dataRate;
    }

    public static void main(String[] args) throws Exception {
        try {
            BulkDataNTGlobalConfiguration.getInstance().loadQosXml();
            BulkDataNTSenderStreamConfiguration streamConf
                = new BulkDataNTSenderStreamConfigurationDefault();
            BulkDataNTSenderStream stream
                = new BulkDataNTSenderStream("TestStream", streamConf);

            BulkDataNTSenderFlowConfiguration flowConf
                = new BulkDataNTSenderFlowConfigurationDefault() {
                        // Throttling is set to 10.0 MB/s. This is the
                        // current value in CDB for ACA Correlator.
                        @Override public double getThrottling() { return 10.0; }
                    };
            BulkDataNTSenderFlow flow
                = stream.createFlow("FULL_RESOLUTION", Optional.empty(), flowConf);

            // The data size of one integration in bytes.
            int frameSize = 36 * 1024 * 1024;

            // The integration time in seconds. The resolution of the
            // integration time must be 1 millisecond.
            double integrationTime = 10.0;

            // The number of integrations in one sub-scan.
            int numOfIntegrations = 60;

            // The number of sub-scans in one sub-scan sequence.
            int numOfSubScans = 3;

            // The number of sub-scan sequences;
            int numOfSubScanSequences = 5;

            // The time gap between two sub-scan sequences in seconds.
            // This number is to set to 0.0 considering the worst case
            // scenario (there is no chance to do Full GC between 
            // two subscan sequences).
            double subScanSequenceInterval = 0.0;

            // This is the absolute time when the current on-going
            // integration finishes. This performance test assumes
            // that the spectrum data of that integration turn to
            // be available at that time, and this test starts
            // sending that spectrum data after that.
            Instant absTimeToFinishIntegration = null;

            // The parameter to send in BD_PARAM frame.
            String startSendParameter = "DefaultParameter";

            // Dummy data for one integration.
            //
            // This data is not generated every integration in order
            // to measure the memory profile of BulkData library only.
            byte[] integrationData = new byte[frameSize];
            for (int i = 0; i < frameSize; i++) {
                integrationData[i] = (byte)(i % 256);
            }

            for (int iSubScanSequence = 0;
                 iSubScanSequence < numOfSubScanSequences;
                 iSubScanSequence++) {
                System.out.println("Starting sub-scan sequence #" + 
                                   iSubScanSequence + ".");

                // Assume that the current time is the start time of
                // this sub-scan sequence. The absolute time when
                // the first integration of the first sub-scan is
                // expected to finish in (integrationTime) seconds.
                absTimeToFinishIntegration
                    = Instant.now()
                    .plusMillis((long)(integrationTime * 1000.0));

                for (int iSubScan = 0;
                     iSubScan < numOfSubScans;
                     iSubScan++) {
                    System.out.println("Starting sub-scan #" + 
                                       (iSubScanSequence * numOfSubScans +
                                        iSubScan) + ".");

                    for (int iIntegration = 0;
                         iIntegration < numOfIntegrations;
                         iIntegration++) {

                        System.out.println("Start integration #" +
                                           iIntegration + ".");

                        // Wait until absTimeToFinishIntegration.
                        Instant now = Instant.now();
                        if (absTimeToFinishIntegration.isAfter(now)) {
                            try {
                                long timeToSleepMilliseconds
                                    = now.until(absTimeToFinishIntegration,
                                                ChronoUnit.MILLIS);
                                TimeUnit.MILLISECONDS
                                    .sleep(timeToSleepMilliseconds);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException("Performance test "+
                                                           "is interrupted.");
                            }
                        }

                        System.out.println("Finished integration #" +
                                           iIntegration + ".");

                        // Set the absolute time when the next integration
                        // finishes.
                        absTimeToFinishIntegration
                            = absTimeToFinishIntegration
                            .plusMillis((long)(integrationTime * 1000.0));

                        if (iIntegration == 0) {
                            flow.startSend(startSendParameter.
                                           getBytes(StandardCharsets.US_ASCII));
                        }

                        System.out.println("Start the data transmission of " +
                                           "integration #" + iIntegration + ".");
                        Instant startTimeOfIntegrationDataSend = Instant.now();

                        flow.sendData(integrationData);

                        Instant endTimeOfIntegrationDataSend = Instant.now();
                        double  dataRateOfIntegration 
                            = calculateDataRate(startTimeOfIntegrationDataSend,
                                                endTimeOfIntegrationDataSend,
                                                integrationData.length);
                        System.out.println("Finished the data transmission of " +
                                           "integration #" + iIntegration + ". (" +
                                           dataRateOfIntegration + " MB/s)");
                    }

                    flow.stopSend();
                    // TODO: measure data transfer rate per sub-scan.
                }

                Thread.sleep((long)(subScanSequenceInterval * 1000));
            }

            stream.destroy();
        } catch (InvalidStreamNameException ex) {
            // TODO: report error to test system
            throw ex;
        } catch (DuplicatedFlowNameException ex) {
            // TODO: report error to test system
            throw ex;
        } catch (InvalidFlowNameException ex) {
            // TODO: report error to test system
            throw ex;
        }
    }
}
