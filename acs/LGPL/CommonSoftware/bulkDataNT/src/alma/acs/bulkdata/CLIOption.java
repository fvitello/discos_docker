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
import org.apache.commons.cli.ParseException;

/**
 * This interface has getCLIOption() which returns an instance of
 * Apache Commons CLI Option for command line argument parser.
 */
interface CLIOption {
    /**
     * Returns Apache Commons CLI Option instance for the command line
     * argument parser.
     */
    public Option getCLIOption();

    /**
     * Retrieve the argument of the given option name and set the retrieved
     * argument to this configuration.
     *
     * If the given option does not exist in the command line, this
     * configuration retains the current value, which is equal to the
     * default value unless this method has been ever called.
     *
     * In case of BooleanConfiguration, the meaning of this method is
     * slightly different. If the given option does not exist in the command
     * line, the configuration value is set to false. Otherwise, true.
     *
     * @param cmdLine Result of command line parser. Must not be null.
     * @param opt     Name of the option.
     *
     * @throws ParseException if the option argument is not in an appropriate
     *                        form.
     * @throws IllegalArgumentException if cmdLine is null.
     */
    public void getOptionValue(CommandLine cmdLine) throws ParseException;
}
