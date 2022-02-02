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
 * This exception indicates that a Flow with the given name does not exist
 * in the Stream, or the given Flow does not belong to that Stream.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class FlowNotExistException extends Exception {
    private final BulkDataNTStream stream;
    private final String flowName;

    /**
     * Instantiate this exception with the given Stream and Flow name.
     * 
     * @param stream The Stream.
     * @param flowName The flow name that was not found in the Stream.
     */
    public FlowNotExistException(BulkDataNTStream stream,
                                 String flowName) {
        super("Flow \"" + flowName + "\" does not exist in Stream \"" + 
              stream.getName() + "\".");

        this.stream = stream;
        this.flowName = flowName;
    }

    /**
     * Returns the Stream that does not contain the Flow with the given name.
     *
     * @return The stream.
     */
    public BulkDataNTStream getStream() {
        return stream;
    }

    /**
     * Returns the flow name that was not found in the Stream.
     *
     * @return The flow name that was not found in the Stream.
     */
    public String getFlowName() {
        return flowName;
    }
}
