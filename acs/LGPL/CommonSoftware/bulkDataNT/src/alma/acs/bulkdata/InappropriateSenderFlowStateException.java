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

import org.apache.commons.lang.Validate;

/**
 * This exception indicates that a Sender Flow was commanded to perform
 * a certain operation through a method call, but the operation was rejected
 * because the Sender Flow was not in a correct state for the operation.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class InappropriateSenderFlowStateException extends Exception {
    private final BulkDataNTSenderFlow flow;
    private final BulkDataNTSenderFlow.State state;

    /**
     * Instantiates this exception with the given Sender Flow and the state.
     *
     * @param msg The detailed message.
     * @param flow The Sender Flow that rejected the commanded operation.
     *             Must not be null.
     * @param state The state in which the subjected Sender Flow was.
     *              Must not be null.
     */
    public InappropriateSenderFlowStateException(String msg,
                                                 BulkDataNTSenderFlow flow,
                                                 BulkDataNTSenderFlow.State state) {
        super(msg);
        
        Validate.notNull(flow);
        Validate.notNull(state);

        this.flow = flow;
        this.state = state;
    }

    /**
     * Returns the Sender Flow that rejected the commanded operation.
     *
     * @return The Sender Flow that rejected the commanded operation.
     */
    public BulkDataNTSenderFlow getSenderFlow() {
        return flow;
    }

    /**
     * Returns the state in which the subjected Sender Flow was.
     *
     * @return The state in which the subjected Sender Flow was.
     */
    public BulkDataNTSenderFlow.State getState() {
        return state;
    }

}
