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
 * A subclass of {@link Date} that prevents mutation. Normally, using Joda
 * Time types, or their Java 8 equivalents, would be better. However, if you
 * <em>must</em> use nasty legacy Java {@code Date} objects, and you
 * <em>believe</em> that no one is supposed to change them, then you can
 * enforce that by using this class.
 */
public final class ImmutableDate extends Date {

    private static final long serialVersionUID = -2145042019359276634L;


    /**
     * @return  an {@code ImmutableDate} instant corresponding to the current
     *          moment in time.
     *
     * @see Date#Date()
     */
    public static ImmutableDate now() {
        return new ImmutableDate();
    }

    /**
     * @return  the given object if it is already of type
     *          {@code ImmutableDate}, or a new immutable copy otherwise.
     *
     * @throws  NullPointerException    if the given date is null.
     *
     * @see #nullOrCopyOf(Date)
     */
    public static ImmutableDate copyOf(final Date date) {
        return (date instanceof ImmutableDate)
                ? (ImmutableDate) date
                : new ImmutableDate(date.getTime());
    }

    /**
     * @return  null if the given object is null, the given object itself if
     *          it is already of type {@code ImmutableDate}, or a new
     *          immutable copy of it otherwise.
     *
     * @see #copyOf(Date)
     */
    public static ImmutableDate nullOrCopyOf(final Date date) {
        return (date == null) ? null : copyOf(date);
    }

    /**
     * @return  an {@code ImmutableDate} representing the moment in time
     *          that is the given number of milliseconds after the UTC epoch
     *          (or before it, if the value is negative).
     *
     * @see #atTimeSinceEpoch(long, TimeUnit)
     * @see Date#Date(long)
     */
    public static ImmutableDate atMillisSinceEpoch(final long millis) {
        return new ImmutableDate(millis);
    }

    /**
     * @return  an {@code ImmutableDate} representing the moment in time
     *          that is the given amount of time after the UTC epoch (or
     *          before it, if the value is negative).
     *
     * @throws  NullPointerException    if the given unit is null.
     *
     * @see #atMillisSinceEpoch(long)
     * @see Date#Date(long)
     */
    public static ImmutableDate atTimeSinceEpoch(final long time, final TimeUnit unit) {
        return atMillisSinceEpoch(unit.toMillis(time));
    }

    /**
     * @return  this object: there's no need to copy an immutable object.
     */
    @Override
    public ImmutableDate clone() {
        return this;    // no need to copy
    }

    /**
     * Implements {@code java.util.Date}'s equality contract, with small
     * modifications to provide commutativity.
     * <ol>
     *  <li>Like with the inherited implementation, this class considers its
     *      instances to be equal to any object of exactly type
     *      {@code java.util.Date}, or exactly this type, that has the same
     *      {@link #getTime() millisecond time} value.</li>
     *  <li>Unlike with the inherited implementation, this class never
     *      considers its instances equal to SQL {@link Timestamp}s, because
     *      {@code Timestamp} does not consider its instances to be equal to
     *      concrete {@code java.util.Date}s, and equality should always be
     *      commutative.</li>
     *  <li>Like with the inherited implementation, this class considers
     *      its instances equal to SQL {@link java.sql.Date}s and SQL
     *      {@code java.sql.Times}s with equal millisecond times, even though
     *      they have different implied precisions than
     *      {@code java.util.Date}s, for the same reason. Concrete
     *      {@code java.sql.Date}, {@code java.sql.Time}, and
     *      {@code java.util.Date} instances will consider one another equal
     *      if they have the same millisecond time values, and this class
     *      honors that behavior.</li>
     *  <li>Equality with {@code java.util.Date} subclasses other than those
     *      mentioned here is undefined.</li>
     * </ol>
     */
    @Override
    public boolean equals(final Object obj) {
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

    /**
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setYear(final int year) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setMonth(final int month) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setDate(final int date) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setHours(final int hours) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setMinutes(final int minutes) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setSeconds(final int seconds) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setTime(final long time) {
        throw new UnsupportedOperationException();
    }

    private ImmutableDate() {
        super();        // construct only with factory methods
    }

    private ImmutableDate(final long date) {
        super(date);    // construct only with factory methods
    }

    /**
     * Save the state of this object to a stream (i.e., serialize it).
     *
     * @serialData The value returned by <code>getTime()</code>
     *             is emitted (long).  This represents the offset from
     *             January 1, 1970, 00:00:00 GMT in milliseconds.
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.writeLong(getTime());
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    private void readObject(final ObjectInputStream stream) throws IOException {
        // Mutation! Fortunately, no one can observe us during this time.
        super.setTime(stream.readLong());
    }

}
