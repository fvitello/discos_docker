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
 * This class represents one integer configuration. It ensures that the
 * configuration value is always in a certain range.
 *
 * @author Takashi Nakamoto
 */
class IntegerConfiguration implements CLIOption {
    private int value;
    private final int defaultValue;
    private final int min;
    private final int max;
    private final String argName;
    private final String description;
    private final String optStr;

    /**
     * This constructor defines one integer configuration with the 
     * given range.
     *
     * The given argument must fulfills the following relation:
     *
     *   min <= defaultValue <= max
     * 
     * @param defaultValue Default value of this configuration. It must be
     *                     in the range between min and max.
     * @param min Lower inclusive limit of this configuration.
     * @param max Upper inclusive limit of this configuration.
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
    public IntegerConfiguration(int defaultValue, int min, int max,
                                String argName,
                                String description,
                                String optStr) {
        if (defaultValue < min) {
            throw new IllegalArgumentException("defaultValue must not be less than min.");
        } else if (defaultValue > max) {
            throw new IllegalArgumentException("defaultValue must not be more than max.");
        }
        Validate.notEmpty(argName);
        Validate.notNull(description);
        Validate.notEmpty(optStr);

        this.value        = defaultValue;
        this.defaultValue = defaultValue;
        this.min          = min;
        this.max          = max;
        this.argName      = argName;
        this.description  = description;
        this.optStr       = optStr;
    }

    /**
     * This constructor defines one integer configuration that accepts
     * arbitrary integer value within the range of 32-bit signed integer.
     *
     * @param defaultValue Default value of this configuration.
     * @param argName Argument name that is used when printing the help.
     * @param description Description of this option for printing the help.
     * @param optStr Option string (or character) to change this configuration
     *               in the command line option.
     */
    public IntegerConfiguration(int defaultValue,
                                String argName,
                                String description,
                                String optStr) {
        this(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE,
             argName, description, optStr);
    }

    /**
     * Returns the lower inclusive limit of this configuration.
     *
     * The returned value always fulfills the following relation:
     *
     *   getMin() <= getValue() <= getMax()
     */
    public int getMin() {
        return min;
    }

    /**
     * Returns the upper inclusive limit of this configuration.
     *
     * The returned value always fulfills the following relation:
     *
     *   getMin() <= getValue() <= getMax()
     */
    public int getMax() {
        return max;
    }

    /**
     * Returns the current value of this configuration.
     *
     * The returned value always fulfills the following relation:
     *
     *   getMin() <= getValue() <= getMax()
     */
    synchronized public int getValue() {
        return value;
    }

    /**
     * Returns the default value of this configuration.
     *
     * The returned value always fulfills the following relation:
     *
     *   getMin() <= getDefaultValue() <= getMax()
     */
    public int getDefaultValue() {
        return defaultValue;
    }

    /**
     * Parse the given argument and return the obtained integer.
     *
     * @param arg An argument string that contains an integer value
     *            in decimal notation. It must not be null.
     *
     * @throws NotIntegerArgumentException if the given argument string
     *         does not represent a valid 32-bit signed integer. This
     *         exception is also thrown if the given argument contains
     *         an integer that is out of 32-bit signed integer range.
     * @throws OutOfRangeIntegerArgumentException if the given argument
     *         string contains an integer, but it is not within the
     *         range defined by getMin() and getMax().
     * @throws IllegalArgumentException if arg is null.
     *
     * @return The parsed result.
     */
    synchronized private int parseArgument(String arg)
        throws NotIntegerArgumentException,
               OutOfRangeIntegerArgumentException {
        Validate.notNull(arg);

        int parsedValue;

        try {
            parsedValue = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            throw new NotIntegerArgumentException(arg, optStr);
        }

        if (parsedValue < min || parsedValue > max) {
            throw new OutOfRangeIntegerArgumentException(parsedValue, min,
                                                         max, optStr);
        }

        return parsedValue;
    }

    @SuppressWarnings("static-access")
    @Override
    public Option getCLIOption() {
        return OptionBuilder
            .withArgName(argName)
            .hasArg()
            .withDescription(description + " Default: " + defaultValue)
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

            value = parseArgument(cmdLine.getOptionValue(optStr));
        }
    }
}
