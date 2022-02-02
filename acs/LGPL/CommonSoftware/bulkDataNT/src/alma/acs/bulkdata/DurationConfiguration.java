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

import java.math.BigDecimal;
import java.time.Duration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.Validate;

/**
 * This class represents one duration configuration. It ensures that the
 * configuration value is always in the range between 0 and
 * {@value java.lang.Integer.MAX_VALUE}.999999999 seconds (inclusive).
 *
 * @author Takashi Nakamoto
 */
class DurationConfiguration implements CLIOption {
    private Duration value;
    private final Duration defaultValue;
    private final String argName;
    private final String description;
    private final String optStr;

    /**
     * This constructor defines one duration configuration with
     * a finite value.
     * 
     * @param defaultValue Default value of this configuration. The duration
     *                     must be in the range of 0 and
     *                     {@value java.lang.Integer.MAX_VALUE}.999999999 seconds
     :                     (inclusive).
     * @param argName Argument name that is used when printing the help.
     *                It must not be null or an empty string.
     * @param description Description of this option for printing the help.
     *                    It must not be null.
     * @param optStr Option string (or character) to change this configuration
     *               in the command line option. It must not be null or
     *               an empty string.
     *
     * @throw IllegalArgumentException
     * if the given arguments do not fulfill the prescribed preconditions.
     */
    public DurationConfiguration(Duration defaultValue,
                                 String argName,
                                 String description,
                                 String optStr) {
        BulkDataNTUtil.validateDuration(defaultValue);
        Validate.notEmpty(argName);
        Validate.notNull(description);
        Validate.notEmpty(optStr);

        this.value        = defaultValue; // Duration is immutable
        this.defaultValue = this.value;
        this.argName      = argName;
        this.description  = description;
        this.optStr       = optStr;
    }

    /**
     * Returns the current value of this configuration. The returned duration
     * is never negative.
     */
    synchronized public Duration getValue() {
        return value;
    }

    /**
     * Returns the default value of this configuration. The returned value
     * is never negative.
     */
    public Duration getDefaultValue() {
        return defaultValue;
    }

    /**
     * Parse the given argument and return the obtained duration value.
     *
     * @param arg An argument string that contains a duration value. It must
     *            not be null.
     *
     * @throws InvalidDurationArgumentException if the given argument string
     *         does not represent a valid duration value.
     * @throws IllegalArgumentException if arg is null.
     *
     * @return The parsed result.
     */
    synchronized private Duration parseArgument(String arg)
        throws InvalidDurationArgumentException {
        Validate.notNull(arg);

        BigDecimal val;
        try {
            val = new BigDecimal(arg);
        } catch (NumberFormatException ex) {
            String msg =
                "The given argument \"" + arg + "\" for \"" + optStr +
                "\" option is not a number.";
            throw new InvalidDurationArgumentException(msg);
        }

        BigDecimal min = new BigDecimal("0");
        BigDecimal max = new BigDecimal("2147483647.999999999");
        int maxScale = 9;

        if (val.compareTo(min) < 0 || val.compareTo(max) > 0) {
            String msg =
                "The given argument \"" + arg + "\" for \"" + optStr +
                "\" option is out of range [" + min + ":" + max + "].";
            throw new InvalidDurationArgumentException(msg);
        } else if (val.scale() > maxScale) {
            String msg = 
                "The scale (the nubmer of the digits to the right of the " +
                "decimal point) of the given argument \"" + arg + "\" for \"" +
                optStr + "\" option is more than " + maxScale + ".";
            throw new InvalidDurationArgumentException(msg);
        }

        BigDecimal[] tmp = val.divideAndRemainder(BigDecimal.valueOf(1));
        BigDecimal sec = tmp[0].setScale(0);
        BigDecimal nano = tmp[1]
            .multiply(BigDecimal.valueOf(1000000000))
            .setScale(0);

        try {
            return Duration.ofSeconds(sec.intValueExact(),
                                      nano.intValueExact());
        } catch (ArithmeticException ex) {
            // This must not happen, but this method throws
            // InvalidDurationArgumentException just in case.
            String msg =
                "The given argument \"" + arg + "\" for \"" + optStr +
                "\" option cannot be parsed.";
            throw new InvalidDurationArgumentException(msg);
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public Option getCLIOption() {
        return OptionBuilder
            .withArgName(argName)
            .hasArg()
            .withDescription(description +
                             " Default: " +
                             BulkDataNTUtil
                             .convertDurationToString(defaultValue) +
                             " seconds")
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
