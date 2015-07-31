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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * A subclass of {@link Timestamp} that provides for correct, commutative
 * comparison among {@link Date}s. This class is serialization-compatible with
 * {@code Timestamp}.
 */
public class ComparableTimestamp extends Timestamp {

    /**
     * The serialVersionUID from our superclass, {@link Date}.
     */
    private static final long serialVersionUID = 7523967970034938905L;


    /**
     * @return  a deep copy of the given date.
     *
     * @throws  NullPointerException    if the given date is null.
     *
     * @see #nullOrCopyOf(Date)
     */
    public static ComparableTimestamp copyOf(final Date date) {
        return atMillisSinceEpoch(date.getTime());
    }

    /**
     * @return  null if the given object is null or a new deep copy of it
     *          otherwise.
     *
     * @see #copyOf(Date)
     */
    public static ComparableTimestamp nullOrCopyOf(final Date date) {
        return (date == null) ? null : copyOf(date);
    }

    /**
     * @return  a date representing the moment in time that is the given
     *          number of milliseconds after the UTC epoch (or before it, if
     *          the value is negative).
     *
     * @see #atTimeSinceEpoch(long, TimeUnit)
     * @see Date#Date(long)
     */
    public static ComparableTimestamp atMillisSinceEpoch(final long millis) {
        return new ComparableTimestamp(millis);
    }

    /**
     * @return  a date representing the moment in time that is the given
     *          amount of time after the UTC epoch (or before it, if the value
     *          is negative).
     *
     * @throws  NullPointerException    if the given unit is null.
     *
     * @see #atMillisSinceEpoch(long)
     * @see Date#Date(long)
     */
    public static ComparableTimestamp atTimeSinceEpoch(final long time, final TimeUnit unit) {
        return atMillisSinceEpoch(unit.toMillis(time));
    }

    /**
     * Tests if this date is before the specified date, consistent with
     * {@link #compareTo(Date)}.
     */
    @Override
    public final boolean before(final Date when) {
        return compareTo(when) < 0;
    }

    /**
     * Tests if this date is after the specified date, consistent with
     * {@link #compareTo(Date)}.
     */
    @Override
    public final boolean after(final Date when) {
        return compareTo(when) > 0;
    }

    /**
     * Tests if this date is before, equal to, or after the specified date.
     * The result is consist with the comparisons from
     * {@link DateComparator#ascending()}.
     *
     * <em>Note</em> that this class has a natural ordering that is
     * inconsistent with {@link #equals(Object)} in the case where the given
     * date is a {@link java.sql.Timestamp}.
     */
    @Override
    public final int compareTo(final Date anotherDate) {
        return DateComparator.ascending().compare(this, anotherDate);
    }

    @Override
    public ComparableTimestamp clone() {
        return (ComparableTimestamp) super.clone();
    }

    /*package*/ ComparableTimestamp(final long date) {
        super(date);
    }

    /**
     * Save the state of this object to a stream (i.e., serialize it).
     *
     * @serialData The value returned by <code>getTime()</code>
     *             is emitted (long).  This represents the offset from
     *             January 1, 1970, 00:00:00 GMT in milliseconds.
     */
    /*package*/ final void writeObject(final ObjectOutputStream stream)
    throws IOException {
        stream.writeLong(getTime());
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    /*package*/ final void readObject(final ObjectInputStream stream)
    throws IOException {
        // Mutation! Fortunately, no one can observe us during this time.
        super.setTime(stream.readLong());
    }

}
