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

import java.nio.file.Path;
import org.apache.commons.lang.Validate;

/**
 * This exception indicates that the given QoS XML file cannot be read
 * for some reason. It might be that the file does not exist, or some
 * IO error happens while reading the file.
 *
 * <p>
 * {@link #getCause()} of this class always returns non-null value. Typically,
 * this exception is caused by {@link java.io.IOException}. It happens if the
 * given QoS XML file does not exist, or if some IO errors happen when reading
 * the file.
 *
 * <p>
 * In some occasion, this exception is raised by other causes. If you want
 * to distinguish the typical IO problem from the others, investigate the
 * type of the cause returned by {@link #getCause()}.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class CannotReadQosXmlException extends Exception {
    private final Path path;

    /**
     * Constructs this exception with the given message and the given cause.
     *
     * @param msg The detailed message.
     * @param cause The cause of this exception. Must not be null.
     * @param path Path from which the method tried to read a QoS XML file.
     *             Must not be null.
     */
    public CannotReadQosXmlException(String msg, Throwable cause, Path path) {
        super(msg, cause);
        Validate.notNull(cause);
        Validate.notNull(path);
        this.path = path;
    }

    /**
     * This methods returns the path from which the method tried to read a QoS
     * XML file.
     *
     * <p>
     * The returned value can be null if QoS XML was not read from a local
     * file system.
     *
     * @return Path from which the method tried to read a QoS XML file.
     */
    public Path getPath() {
        return path;
    }
}
