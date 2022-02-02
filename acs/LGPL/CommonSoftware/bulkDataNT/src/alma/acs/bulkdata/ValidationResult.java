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
 * This class holds the validation result.
 *
 * @author Takashi Nakamoto
 */
class ValidationResult {
    private final boolean isValid;
    private final String  reason;

    /**
     * This constructor instantiates the validation result with the given
     * arguments.
     *
     * @param isValid Validation result.
     * @param reason  A short description about why the validation failed.
     *                This argument is ignored if isValid is true.
     */
    public ValidationResult(boolean isValid, String reason) {
        this.isValid = isValid;
        this.reason  = isValid ? null : reason;
    }

    /**
     * This method returns whether the validation was passed successfully.
     *
     * @return true if the validation was passed successfully.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * This method returns the description about why the validation failed.
     *
     * @return A short description of why the validation failed, or null
     *         if the validation was passed successfully.
     */
    public String getReason() {
        return reason;
    }
}
