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
 * This exception indicates that the given QoS XML file has some error.
 * It can be a syntax error as XML, or it cannot be validated correctly
 * by the schema.
 *
 * <p>
 * The reason why it was considered that there is an error can be obtained
 * by {@link #getCause()}.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class InvalidQosXmlException extends Exception {
    private final BulkDataNTQosXmlSchema schema;
    private final Path path;

    /**
     * Constructs this exception with the given message and the given cause.
     *
     * @param msg The detailed message.
     * @param cause The cause of this exception. Must not be null.
     * @param schema XML Schema that validated the QoS XML file. Specify null
     *               if the validation or parsing is done by anything else.
     * @param path Path from which the XML content was read. Specify null
     *             if the XML content was not read from a file in a local
     *             file system.
     */
    public InvalidQosXmlException(String msg, Throwable cause,
                                  BulkDataNTQosXmlSchema schema,
                                  Path path) {
        super(msg, cause);
        Validate.notNull(cause);
        Validate.notNull(schema);

        this.schema = schema;
        this.path = path;
    }

    /**
     * This method returns the schema that validated the given QoS XML.
     *
     * @return The schema that validated the given QoS XML. Can be null
     *         if the validation or parsing was not done by a schema.
     */
    public BulkDataNTQosXmlSchema getSchema() {
        return schema;
    }

    /**
     * This method returns the path from which the QoS XML file was read.
     *
     * @return The path from which the QoS XML file was read. Can be null
     *         if the QoS XML content was not read from a file in a local
     *         file system.
     */
    public Path getPath() {
        return path;
    }
}
