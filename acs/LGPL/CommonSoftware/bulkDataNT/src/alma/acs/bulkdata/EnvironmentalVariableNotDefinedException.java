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
 * This exception indicates that a certain environmental variable is not
 * defined.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class EnvironmentalVariableNotDefinedException extends Exception {
    private final String var;

    /**
     * This constructor instantiates this exception with the given
     * environmental variable name.
     *
     * @param var The environmental variable name that was not defined.
     */
    public EnvironmentalVariableNotDefinedException(String var) {
        super(String.format("The environmental variable \"%s\" is not " +
                            "defined.", var));
        this.var = var;
    }

    /**
     * This method returns the name of the environmental variable that
     * was not defined.
     *
     * @return The name of the environmental variable name that was not
     *         defined.
     */
    public String getVariableName() {
        return var;
    }
}
