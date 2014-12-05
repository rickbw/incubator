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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * A subclass of {@link Date} that provides for correct, commutative
 * comparison among instances, unlike its superclass. Specifically:
 * <ul>
 *  <li>Instances of this class and concrete instances of
 *      {@code java.util.Date} behave commutatively with respect to
 *      {@link #equals(Object)}, {@link #compareTo(Date)},
 *      {@link #before(Date)}, and {@link #after(Date)}.</li>
 *  <li>Instances of this class and concrete instances of {@link Timestamp}
 *      behave commutatively with respect to {@link #equals(Object)},
 *      {@link #compareTo(Date)}, {@link #before(Date)}, and
 *      {@link #after(Date)}. In particular, unlike concrete instances of
 *      {@code java.util.Date}, instances of this class do <em>not</em>
 *      consider themselves to be equal to any instance of
 *      {@code java.sql.Timestamp}, regardless of the result of
 *      {@link Timestamp#getTime()}, in order to behave consistently with
 *      {@code Timestamp} itself. <em>However</em>, note that this class
 *      therefore has a natural ordering that is inconsistent with
 *      {@link #equals(Object)} in the case where the argument is of type
 *      {@code Timestamp}. This limitation is a direct consequence of the
 *      inconsistent behavior of {@link Date#equals(Object)} and
 *      {@link Timestamp#equals(Object)}.</li>
 *  <li>Instances of this class and of {@link java.sql.Date} and
 *      {@link java.sql.Time} behave commutatively with respect to
 *      {@link #equals(Object)}, {@link #compareTo(Date)},
 *      {@link #before(Date)}, and {@link #after(Date)}. This property bears
 *      mention because of the implied differences in precision among these
 *      types.</li>
 * </ul>
 *
 * This class is serialization-compatible with {@code java.util.Date}.
 */
public class ComparableDate extends Date {

    /**
     * The serialVersionUID from our superclass, {@link Date}.
     * We retain the same value, so that instances of one class may be
     * translated across the serialization boundary.
     *
     * @see #readObject(ObjectInputStream)
     * @see #writeObject(ObjectOutputStream)
     */
    /*package*/ static final long serialVersionUID = 7523967970034938905L;


    /**
     * @return  an {@code ImmutableDate} instant corresponding to the current
     *          moment in time.
     *
     * @see Date#Date()
     */
    public static ComparableDate now() {
        return new ComparableDate();
    }

    /**
     * @return  a deep copy of the given date.
     *
     * @see #nullOrCopyOf(Date)
     */
    public static ComparableDate copyOf(@Nonnull final Date date) {
        return atMillisSinceEpoch(date.getTime());
    }

    /**
     * @return  null if the given object is null or a new deep copy of it
     *          otherwise.
     *
     * @see #copyOf(Date)
     */
    public static ComparableDate nullOrCopyOf(@Nullable final Date date) {
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
    public static ComparableDate atMillisSinceEpoch(final long millis) {
        return new ComparableDate(millis);
    }

    /**
     * @return  a date representing the moment in time that is the given
     *          amount of time after the UTC epoch (or before it, if the value
     *          is negative).
     *
     * @see #atMillisSinceEpoch(long)
     * @see Date#Date(long)
     */
    public static ComparableDate atTimeSinceEpoch(final long time, @Nonnull final TimeUnit unit) {
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
    public ComparableDate clone() {
        return (ComparableDate) super.clone();
    }

    /**
     * Implements {@code java.util.Date}'s equality contract, with small
     * modifications to provide commutativity.
     * <ol>
     *  <li>Like the inherited implementation, this class considers its
     *      instances to be equal to any object of exactly type
     *      {@code java.util.Date}, or of this type, that has the same
     *      {@link #getTime() millisecond time} value.</li>
     *  <li>Unlike the inherited implementation, this class never
     *      considers its instances equal to SQL {@link Timestamp}s, because
     *      {@code Timestamp} does not consider its instances to be equal to
     *      concrete {@code java.util.Date}s, and equality should always be
     *      commutative.</li>
     *  <li>Like the inherited implementation, this class considers
     *      its instances equal to SQL {@link java.sql.Date}s and SQL
     *      {@code java.sql.Times}s with equal millisecond times, even though
     *      they have different implied precisions than
     *      {@code java.util.Date}s, for the same reason. Concrete
     *      {@code java.sql.Date}, {@code java.sql.Time}, and
     *      {@code java.util.Date} instances will consider one another equal
     *      if they have the same millisecond time values, and this class
     *      honors that behavior.</li>
     * </ol>
     *
     * Equality with {@code java.util.Date} subclasses other than those
     * mentioned here is undefined.
     */
    @Override
    public final boolean equals(final Object obj) {
        // Short-circuit super for a speedup:
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        // Default implementation:
        if (!super.equals(obj)) {
            return false;
        }

        /* Exclude SQL Timestamps:
         * (Timestamp instances don't consider themselves equal to
         * java.util.Dates. Be symmetrical.)
         */
        return !(obj instanceof Timestamp);
    }

    /*package*/ ComparableDate() {
        super();
    }

    /*package*/ ComparableDate(final long date) {
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
