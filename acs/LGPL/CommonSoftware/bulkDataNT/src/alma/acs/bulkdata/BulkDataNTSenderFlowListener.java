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

/**
 * This listener monitors the status changes of {@link BulkDataNTSenderFlow}.
 *
 * @author Takashi Nakamoto
 */
public interface BulkDataNTSenderFlowListener {
    public enum ErrorType {
        /**
         * MINOR TODO: This error is raised when 
         *             <a href="https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/html/api_java/interfacecom_1_1rti_1_1dds_1_1publication_1_1DataWriterListener.html#acc20709601f83dcc0b0a3e82b9c3c10c">on_offered_deadline_missed()</a>
         *             is called on the underlying DataWriter listener,
         *             but the author does not understand in what
         *             condition it actually happens.
         */
        MISSED_OFFERED_DEADLINE,
        
        /**
         * This error indicates that the QoS settings of a Receiver is not
         * compatible with the one of this Sender. This error happens
         * when a new Receiver with incompatible QoS settings tries to 
         * connect to the Flow. This error also happens when this Sender
         * connects to the Flow and finds one or more Receivers already
         * connected to the Flow have incompatible QoS settings. In the 
         * latter case, this error is raised the same number of times as
         * the number of the Receivers with incompatible QoS settings.
         * 
         * <p>
         * The Receivers with incompatible QoS settings will not be counted
         * as connected.
         * 
         * <p>
         * The Sender can ignore this error, but the data will not reach
         * the Receivers with incompatible QoS settings.
         */
        INCOMPATIBLE_QOS,
        
        /**
         * This error is raised when Receivers cannot confirm the liveliness
         * of this Sender.
         * 
         * <p>
         * This can happen when there is a network problem, or if the DDS
         * Domain Participant to which this Sender belongs to terminates.
         * In the latter case, it can be considered as the bug of this 
         * BulkData Java library, so please report it to either the author
         * or the maintainer of the library.
         */
        LIVELINESS_LOST,
    }

    /**
     * This method is invoked when an error happens in the underlying DDS
     * library.
     *
     * @param type The type of the error.
     * @param flow The Sender Flow that caused the error.
     */
    public void onError(ErrorType type,
                        BulkDataNTSenderFlow flow);

    /**
     * This method is called when one or more new Receivers connect to a
     * Sender Flow.
     *
     * @param totalReceivers The current total number of receivers that are
     *                       connected to this Flow.
     * @param flow The connected Flow.
     */
    public void onReceiverConnect(int totalReceivers,
                                  BulkDataNTSenderFlow flow);

    /**
     * This method is called when one or more new Receivers disconnect from
     * a Sender Flow.
     *
     * @param totalReceivers The current total number of receivers that are
     *                       connected to this Flow.
     * @param flow The disconnected Flow.
     */
    public void onReceiverDisconnect(int totalReceivers,
                                     BulkDataNTSenderFlow flow);
}
