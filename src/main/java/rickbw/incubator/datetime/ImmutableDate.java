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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * A subclass of {@link Date} that prevents mutation. Normally, using Joda
 * Time types, or their Java 8 equivalents, would be better. However, if you
 * <em>must</em> use nasty legacy Java {@code Date} objects, and you
 * <em>believe</em> that no one is supposed to change them, then you can
 * enforce that by using this class.
 */
public final class ImmutableDate extends ComparableDate {

    /**
     * Retain the same value as our superclass, so that instances of one class
     * may be translated across the serialization boundary.
     *
     * @see ComparableDate#readObject(java.io.ObjectInputStream)
     * @see ComparableDate#writeObject(java.io.ObjectOutputStream)
     */
    private static final long serialVersionUID = ComparableDate.serialVersionUID;


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
     * @see #nullOrCopyOf(Date)
     */
    public static ImmutableDate copyOf(@Nonnull final Date date) {
        return (date instanceof ImmutableDate)
                ? (ImmutableDate) date
                : atMillisSinceEpoch(date.getTime());
    }

    /**
     * @return  null if the given object is null, the given object itself if
     *          it is already of type {@code ImmutableDate}, or a new
     *          immutable copy of it otherwise.
     *
     * @see #copyOf(Date)
     */
    public static ImmutableDate nullOrCopyOf(@Nullable final Date date) {
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
     * @see #atMillisSinceEpoch(long)
     * @see Date#Date(long)
     */
    public static ImmutableDate atTimeSinceEpoch(final long time, @Nonnull final TimeUnit unit) {
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

}
