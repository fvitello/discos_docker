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

import java.util.Map;

/**
 * This exception indicates that some Sender Flows couldn't be destroyed
 * for some reason.
 * 
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class UndestroyedSenderFlowsException extends Exception {
    private final Map<BulkDataNTSenderFlow, Exception> flowsAndReasons;

    /**
     * Construct this exception with given detailed message and the
     * collection of undestroyed Sender Flows and the reasons of why each
     * flow couldn't be destroyed.
     * 
     * @param msg Detailed message.
     * @param flowsAndReasons the collection of undestroyed Sender Flows
     *                        and the reasons of why each flow couldn't be
     *                        destroyed.
     */
    public UndestroyedSenderFlowsException(String msg,
                     Map<BulkDataNTSenderFlow, Exception> flowsAndReasons) {
        super(msg);
        this.flowsAndReasons = flowsAndReasons;
    }
    
    /**
     * This method returns the collection of undestroyed Sender Flows and
     * the reasons of why each flow couldn't be destroyed.
     * 
     * @return The collection of undestroyed Sender Flows and the reasons
     *         of why each flow couldn't be destroyed.
     */
    public Map<BulkDataNTSenderFlow, Exception> getFlowsAndReasons() {
        return flowsAndReasons;
    }
}
