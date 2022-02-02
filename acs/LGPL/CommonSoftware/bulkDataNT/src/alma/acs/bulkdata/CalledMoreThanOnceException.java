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
 * This exception indicates that a certain instance method is called more
 * than once.
 *
 * <p>
 * If your instance method is not supposed to be called more than once, but
 * it is called twice, instantiate this exception class and throw it to the
 * caller.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
class CalledMoreThanOnceException extends RuntimeException {
    public CalledMoreThanOnceException() {
        super();
    }

    public String getMessage() {
        StackTraceElement trace[] = getStackTrace();
        if (trace == null || trace.length == 0) {
            return "Unknown method was called more than once.";
        } else {
            return trace[0].getMethodName() + " method of an instance of " + 
                trace[0].getClassName() + " was called more than once.";
        }
    }
}
