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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.Validate;

/**
 * This class represents one configuration that contains multiple string.
 * It ensures that all strings are neither null nor empty.
 *
 * @author Takashi Nakamoto
 */
class MultipleStringConfiguration implements CLIOption {
    private List<String> value;
    private final String argName;
    private final String description;
    private final String optStr;
    private final boolean mandatory;
    private final boolean allowDuplication;

    /**
     * This constructor defines one configuration that contains multiple
     * string values.
     *
     * @param argName Argument name that is used when printing the help.
     *               It must not be null or an empty string.
     * @param description Description of this option for printing the help.
     *                    It must be null.
     * @param optStr Option string (or character) to change this configuration
     *               in the command line option. It must be null or an empty
     *               string.
     * @param mandatory Whether this configuration must be configured by
     *                  command line.
     * @param allowDuplication Whether duplicated strings are allowed.
     *
     * @throw IllegalArgumentException if the given arguments do not fulfill
     *                                 the prescribed preconditions.
     */
    public MultipleStringConfiguration(String argName,
                                       String description,
                                       String optStr,
                                       boolean mandatory,
                                       boolean allowDuplication) {
        Validate.notEmpty(argName);
        Validate.notNull(description);
        Validate.notEmpty(optStr);

        this.value       = Collections.unmodifiableList(new ArrayList<String>());
        this.argName     = argName;
        this.description = description;
        this.optStr      = optStr;
        this.mandatory   = mandatory;
        this.allowDuplication = allowDuplication;
    }

    /**
     * Returns the current value of this configuration.
     *
     * @return List of string values. All strings are neither null nor empty.
     *         Empty list may be returned if getOptionValue has not been ever
     *         called, or if this option is not mandatory.
     */
    synchronized public List<String> getValue() {
        return new ArrayList<String>(value);
    }

    @SuppressWarnings("static-access")
    @Override
    public Option getCLIOption() {
        return OptionBuilder
            .withArgName(argName)
            .hasArg()
            .isRequired(mandatory)
            .withDescription(description)
            .create(optStr);
    }

    @Override
    synchronized public void getOptionValue(CommandLine cmdLine)
        throws ParseException {
        Validate.notNull(cmdLine);

        if (cmdLine.hasOption(optStr)) {
            String[] strs = cmdLine.getOptionValues(optStr);
            
            assert strs != null;
            assert strs.length != 0;

            // Validate each string value.
            for (String str: strs) {
                assert str != null;
                if (str.length() == 0) {
                    throw new EmptyStringException(optStr);
                }

                ValidationResult result = validate(str);
                if (!result.isValid()) {
                    throw new IllegalStringException(str, optStr,
                                                     result.getReason());
                }
            }

            // Find duplication, and throws an Exception when there
            // is duplication.
            if (!allowDuplication) {
                Set<String> set = new HashSet<String>();
                for (String str: strs) {
                    if (set.contains(str)) {
                        throw new DuplicatedStringException(str, optStr);
                    }
                    set.add(str);
                }
            }

            value = Collections.unmodifiableList(Arrays.asList(strs));
        }
    }

    /**
     * This method validates if the given string fulfills certain criteria.
     *
     * This method is intended to be overridden by child classes. In this base
     * class, any String is accepted and the validation result is always
     * success.
     *
     * @param non-null/non-empty string to be validated
     *
     * @return Validation result.
     */
    protected ValidationResult validate(String str) {
        return new ValidationResult(true, null);
    }
}
