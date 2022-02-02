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
 * This class represents one double configuration. It ensures that the
 * configuration value is always in a certain range. It also ensures that
 * the value is not NaN, -Inf or +Inf.
 *
 * @author Takashi Nakamoto
 */
class DoubleConfiguration implements CLIOption {
    private double value;
    private final double defaultValue;
    private final double min;
    private final double max;
    private final String argName;
    private final String description;
    private final String optStr;

    /**
     * This constructor defines one double configuration with the 
     * given range.
     *
     * The given argument must fulfills the following relation:
     *
     *   min <= defaultValue <= max
     * 
     * @param defaultValue Default value of this configuration. This 
     *                     must not be NaN or infinite. It must be
     *                     in the range between min and max.
     * @param min Lower inclusive limit of this configuration. It must
     *            not be NaN or infinite.
     * @param max Upper inclusive limit of this configuration. It must
     *            not be NaN or infinite.
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
    public DoubleConfiguration(double defaultValue, double min, double max,
                                String argName,
                                String description,
                                String optStr) {
        if (Double.isNaN(defaultValue)) {
            throw new IllegalArgumentException("defaultValue must not be NaN.");
        } else if (Double.isInfinite(defaultValue)) {
            throw new IllegalArgumentException("defaultValue must not be infinite.");
        } else if (Double.isNaN(min)) {
            throw new IllegalArgumentException("min must not be NaN.");
        } else if (Double.isInfinite(min)) {
            throw new IllegalArgumentException("min must not be infinite.");
        } else if (Double.isNaN(max)) {
            throw new IllegalArgumentException("max must not be NaN.");
        } else if (Double.isInfinite(max)) {
            throw new IllegalArgumentException("max must not be infinite.");
        } else if (defaultValue < min) {
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
     * This constructor defines one double configuration that accepts
     * arbitrary finite double value within the range of IEEE 754
     * double precision floating point type.
     *
     * @param defaultValue Default value of this configuration.
     * @param argName Argument name that is used when printing the help.
     * @param description Description of this option for printing the help.
     * @param optStr Option string (or character) to change this configuration
     *               in the command line option.
     */
    public DoubleConfiguration(double defaultValue,
                                String argName,
                                String description,
                                String optStr) {
        this(defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE,
             argName, description, optStr);
    }

    /**
     * Returns the lower inclusive limit of this configuration.
     *
     * The returned value always fulfills the following relation:
     *
     *   getMin() <= getValue() <= getMax()
     */
    public double getMin() {
        return min;
    }

    /**
     * Returns the upper inclusive limit of this configuration.
     *
     * The returned value always fulfills the following relation:
     *
     *   getMin() <= getValue() <= getMax()
     */
    public double getMax() {
        return max;
    }

    /**
     * Returns the current value of this configuration.
     *
     * The returned value always fulfills the following relation:
     *
     *   getMin() <= getValue() <= getMax()
     */
    synchronized public double getValue() {
        return value;
    }

    /**
     * Returns the default value of this configuration.
     *
     * The returned value always fulfills the following relation:
     *
     *   getMin() <= getDefaultValue() <= getMax()
     */
    public double getDefaultValue() {
        return defaultValue;
    }

    /**
     * Parse the given argument and return the obtained double value.
     *
     * @param arg An argument string that contains a double value. It must
     *            not be null.
     *
     * @throws NotDoubleArgumentException if the given argument string
     *         does not represent a valid double value.
     * @throws OutOfRangeDoubleArgumentException if the given argument
     *         string contains a double value, but it is not within the
     *         range defined by getMin() and getMax().
     * @throws IllegalArgumentException if arg is null.
     *
     * @return The parsed result.
     */
    synchronized private double parseArgument(String arg)
        throws NotDoubleArgumentException,
               OutOfRangeDoubleArgumentException {
        Validate.notNull(arg);

        double parsedValue;

        try {
            parsedValue = Double.parseDouble(arg);
            if (Double.isNaN(parsedValue) ||
                Double.isInfinite(parsedValue)) {
                throw new NotDoubleArgumentException(arg, optStr);
            }
        } catch (NumberFormatException ex) {
            throw new NotDoubleArgumentException(arg, optStr);
        }

        if (parsedValue < min || parsedValue > max) {
            throw new OutOfRangeDoubleArgumentException(parsedValue, min,
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
