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
 * This exception indicates that the file format is not as expected.
 * 
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class IllegalFileFormatException extends Exception {
    /**
     * Instantiate this exception with the given message.
     * 
     * @param msg Detailed explanation of which file is considered as illegal,
     *            and why. 
     */
    public IllegalFileFormatException(String msg) {
        super(msg);
    }

    /**
     * Instantiate this exception with the given message and the cause.
     * 
     * @param msg Detailed explanation of which file is considered as illegal,
     *            and why. 
     * @param cause The cause of this exception.            
     */
    public IllegalFileFormatException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
