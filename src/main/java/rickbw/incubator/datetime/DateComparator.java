/* Copyright 2014 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rickbw.incubator.datetime;

import java.util.Comparator;
import java.util.Date;


/**
 * Tests if one date is before, equal to, or after another. It considers only
 * the millisecond values of the two dates, regardless of whether those
 * objects would consider one another to be equal. (For example,
 * {@link java.sql.Timestamp}s do not consider themselves equal to other
 * {@link Date}s.) Unlike {@link Date#compareTo(Date)}, it returns the correct
 * result when comparing a {@link Date} with a {@link java.sql.Timestamp}
 * that differs by less than one second.
 */
public abstract class DateComparator implements Comparator<Date> {

    private static final DateComparator ascending = new DateComparator() {
        @Override
        public int compare(final Date lhs, final Date rhs) {
            final long leftTime = lhs.getTime();
            final long rightTime = rhs.getTime();
            // Don't just subtract the times, to avoid underflow.
            if (leftTime < rightTime) {
                return -1;
            } else if (leftTime == rightTime) {
                return 0;
            } else {
                return 1;
            }
        }
    };

    private static final DateComparator descending = new DateComparator() {
        @Override
        public int compare(final Date lhs, final Date rhs) {
            return -ascending.compare(lhs, rhs);
        }
    };


    public static DateComparator ascending() {
        return ascending;
    }

    public static DateComparator descending() {
        return descending;
    }

    private DateComparator() {
        // prevent external instantiation
    }

}
