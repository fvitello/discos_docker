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
import org.apache.commons.lang.StringUtils;

/**
 * This class represents n BulkData Flow.
 *
 * <p>
 * This class holds the information that could be the same between 
 * the Sender Flow and the Receiver Flow in the hope that, in some future,
 * BulkDataNTReceiverStream might be implemented.
 *
 * @author Takashi Nakamoto
 */
public abstract class BulkDataNTFlow {
    /**
     * Separator between Stream name and Flow name (e.g. "stream#flow").
     * The concatenated name is used as Topic name of DDS.
     */
    public static final String STREAM_FLOW_NAME_SEPARATOR = "#";

    /**
     * The maximum size of payload in one BulkData frame in bytes.
     */
    public static final int FRAME_MAX_LEN
        = alma.acs.bulkdata.ACSBulkData.FRAME_MAX_LEN.VALUE;

    private final String name;
    private final BulkDataNTStream stream;

    /**
     *
     * @param name Flow name that fulfills the criteria described in
     *             {@link #getName()}.
     * @param stream The instance of the parent stream. This must not be null.
     * @exception InvalidFlowNameException
     * if the given name does not fulfill the criteria described in
     * {@link #getName()}
     */
    BulkDataNTFlow(String name,
                   BulkDataNTStream stream)
        throws InvalidFlowNameException {
        this.name = name;
        this.stream = stream;

        Validate.notNull(stream);

        ValidationResult result = getNameValidator().validate(name);
        if (!result.isValid()) {
            throw new InvalidFlowNameException(name, result.getReason());
        }
    }

    /**
     * This method returns the name of this Flow. The returned name
     * always fulfills the following criteria:
     *
     * <ul>
     * <li>It is neither null nor empty (zero-length).
     * <li>It consists of up to 127 ASCII-printable characters.
     * <li>It does not contain " " (white space) or
     *     {@value #STREAM_FLOW_NAME_SEPARATOR}.
     * </ul>
     *
     * <p>
     * The restriction of the maximum length comes from the maximum length
     * of DDS topic name (255 characters). The topic name consists of
     * Stream name, {@value #STREAM_FLOW_NAME_SEPARATOR} and Flow name. Half
     * of the length of the topic name (127 characters out of 255) is assigned
     * to the Flow name.
     *
     * <p>
     * To avoid unnecessary confusion, non-ASCII characters, non-printable
     * characters and white space are not allowed.
     * 
     * <p>
     * {@value #STREAM_FLOW_NAME_SEPARATOR} is not allowed because it is used as
     * the separator between Stream and Flow names.
     *
     * @return Valid Flow name.
     */
    public String getName() {
        return name;
    }

    /**
     * This methods returns a validator which validates the given Flow
     * name. The criteria of the validation is described in {@link #getName()}.
     *
     * @return Validator which validates the given Flow name.
     */
    static StringValidator getNameValidator() {
        return new StringValidator() {
            @Override public ValidationResult validate(String str) {
                String msg;
                if (str == null) {
                    msg = "Flow name must not be null.";
                    return new ValidationResult(false, msg);
                } else if (str.isEmpty()) {
                    msg = "Flow name must not be empty.";
                    return new ValidationResult(false, msg);
                } else if (!StringUtils.isAsciiPrintable(str)) {
                    msg = "Flow name must consist of only ASCII-printable " +
                          "characters.";
                    return new ValidationResult(false, msg);
                } else if (str.length() > 127) {
                    msg = "Flow name must consist of up to 127 characters.";
                    return new ValidationResult(false, msg);
                } else if (str.contains(" ")) {
                    msg = "Flow name must not contain ' ' (white space).";
                    return new ValidationResult(false, msg);
                } else if (str.contains(STREAM_FLOW_NAME_SEPARATOR)) {
                    msg = "Flow name must not contain '" +
                        STREAM_FLOW_NAME_SEPARATOR + "'.";
                    return new ValidationResult(false, msg);
                } else {
                    return new ValidationResult(true, null);
                }
            }
        };
    }

    /**
     * This method returns the Stream that this Flow belongs to. The returned
     * value is never null.
     *
     * @return The Stream that this Flow belongs to.
     */
    public BulkDataNTStream getStream() {
        return stream;
    }
}
