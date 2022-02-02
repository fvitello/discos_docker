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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.Validate;

/**
 * This class represents one string configuration. It ensures that the
 * configuration value is neither null nor empty.
 *
 * @author Takashi Nakamoto
 */
class StringConfiguration implements CLIOption {
    private final String defaultValue;
    private String value;
    private final String argName;
    private final String description;
    private final String optStr;

    /**
     * This constructor defines one string configuration with the given default
     * value.
     *
     * @param defaultValue Default value of this configuration. It must be
     *                     not be null or an empty string.
     * @param argName Argument name that is used when printing the help.
     *                It must not be null or an empty string.
     * @param description Description of this option for printing the help.
     *                    It must not be null.
     * @param optStr Option string (or character) to change this configuration
     *               in the command line option. It must not be null or
     *               an empty string.
     *
     * @throw IllegalArgumentException if the given arguments do not fulfill
     *                                 the prescribed preconditions.
     */
    public StringConfiguration(String defaultValue,
                               String argName,
                               String description,
                               String optStr) {
        Validate.notEmpty(defaultValue);
        Validate.notEmpty(argName);
        Validate.notNull(description);
        Validate.notEmpty(optStr);
        
        ValidationResult result = validate(defaultValue);
        if (!result.isValid()) {
            throw new IllegalArgumentException("The default value \"" +
                                               defaultValue + "\" is illegal. "
                                               + result.getReason());
        }

        this.value        = defaultValue;
        this.defaultValue = defaultValue;
        this.argName      = argName;
        this.description  = description;
        this.optStr       = optStr;
    }

    /**
     * Returns the current value of this configuration.
     *
     * @return Non-null/non-empty string.
     */
    synchronized public String getValue() {
        return value;
    }

    /**
     * Returns the default value of this configuration.
     *
     * @return Non-null/non-empty string.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    @SuppressWarnings("static-access")
    @Override
    public Option getCLIOption() {
        return OptionBuilder
            .withArgName(argName)
            .hasArg()
            .withDescription(description + " Default: '" + defaultValue + "'")
            .create(optStr);
    }

    @Override
    synchronized public void getOptionValue(CommandLine cmdLine)
        throws ParseException {
        Validate.notNull(cmdLine);

        if (cmdLine.hasOption(optStr)) {
            if (cmdLine.getOptionValues(optStr).length > 1) {
                throw new MoreThanOneOptionException(optStr);
            }

            String arg = cmdLine.getOptionValue(optStr);
            assert arg != null;
            if (arg.length() == 0) {
                throw new EmptyStringException(optStr);
            }

            ValidationResult result = validate(arg);
            if (!result.isValid()) {
                throw new IllegalStringException(arg, optStr,
                                                 result.getReason());
            }

            value = arg;
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
