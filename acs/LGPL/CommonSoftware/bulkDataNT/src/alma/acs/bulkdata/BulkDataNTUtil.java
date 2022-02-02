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
import java.util.ArrayList;
import java.util.List;


import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.StringSeq;

import org.apache.commons.lang.Validate;

/**
 * The collection of utility methods.
 *
 * @author Takashi Nakamoto
 */
class BulkDataNTUtil {
    /**
     * Convert Java {@link java.time.Duration} to DDS
     * {@link com.rti.dds.infrastructure.Duration_t}.
     *
     * @param d Duration to convert. It must be in the range between 0 and
     *          {@value java.lang.Integer.MAX_VALUE}.999999999 seconds
     *          (inclusive).
     *
     * @return Converted duration.
     *
     * @throws IllegalArgumentException
     * if the given duration is null, negative, or larger than
     * {@value java.lang.Integer.MAX_VALUE}.999999999 seconds.
     */
    public static Duration_t convertDuration(Duration d)
        throws IllegalArgumentException {
        validateDuration(d);
        return new Duration_t((int)d.getSeconds(), d.getNano());
    }

    /**
     * This method validates if the given duration is in the range between
     * 0 and {@value java.lang.Integer.MAX_VALUE}.999999999 seconds (inclusive).
     * If not, this method throws {@link IllegalArgumentException}.
     *
     * @param d Duration to validate.
     *
     * @throws IllegalArgumentException
     * if the given duration is null, negative, or larger than
     * {@value java.lang.Integer.MAX_VALUE}.999999999 seconds.
     */
    public static void validateDuration(Duration d)
        throws IllegalArgumentException {
        Validate.notNull(d);
        
        if (d.isNegative()) {
            String msg
                = String.format("Duration cannot be negative." +
                                "(second = %d, nano = %d)",
                                d.getSeconds(), d.getNano());
            throw new IllegalArgumentException(msg);
        } else if (d.getSeconds() > Integer.MAX_VALUE) {
            String msg 
                = String.format("Duration larger than %d.999999999 seconds " +
                                "is not allowed.", Integer.MAX_VALUE);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Convert the given duration to a human-readable string in seconds.
     * 
     * @param d The duration to convert.
     * 
     * @return The human-readable string of the given duration in seconds.
     */
    public static String convertDurationToString(Duration d) {
        BigDecimal nanos = new BigDecimal(d.getNano());
        BigDecimal secs = new BigDecimal(d.getSeconds());
        BigDecimal total = secs.add(nanos.movePointLeft(9));
        return total.stripTrailingZeros().toPlainString();
    }
    
    /**
     * Convert {@link StringSeq} to {@link List<String>}. 
     * 
     * @param seq StringSeq to convert.
     * @return The result of the conversion.
     */
    public static List<String> convertStringSeqToList(StringSeq seq) {
        List<String> list = new ArrayList<>(seq.size());
        for (Object element: seq) {
            if (element instanceof String) {
                list.add((String)element);
            }
        }
        return list;
    }
}
