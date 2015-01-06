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
package rickbw.incubator.token;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Comparator;

import com.google.common.base.Preconditions;


/**
 * A base class for types that are "integer-ish": typically represented as
 * <code>int<code>s or <code>long<code>s, but that do not represent numeric
 * quantities. A numeric ID is a common example.
 *
 * Subclasses should be final or abstract: {@link #equals(Object)} will treat
 * objects of different concrete classes as unequal. Subclasses should also
 * be immutable and should not introduce additional state.
 *
 * @param <SELF> The concrete subclass. This generic parameter allows us to
 *        restrict comparisons to objects of the same concrete type.
 */
public abstract class Intish<SELF> implements Comparable<Intish<SELF>>, Serializable {

    private static final long serialVersionUID = 5689439022865184267L;

    private static final Comparator<Long> ascendingComparator = new Comparator<Long>() {
        @Override
        public int compare(final Long lhs, final Long rhs) {
            // Careful of overflow!
            return (lhs > rhs) ? -1 : (rhs > lhs) ? 1 : 0;
        }
    };

    private static final Comparator<Long> descendingComparator = new Comparator<Long>() {
        @Override
        public int compare(final Long lhs, final Long rhs) {
            return -ascending().compare(lhs, rhs);
        }
    };

    private static final Comparator<Long> notComparableComparator = new Comparator<Long>() {
        @Override
        public int compare(final Long lhs, final Long rhs) {
            throw new UnsupportedOperationException();
        }
    };

    private final long value;
    private transient Behavior<SELF> behavior = null;


    protected Intish(final long value) {
        getBehavior().validateBounds(value);
        this.value = value;
    }


    public final long intValue() {
        return this.value;
    }


    @Override
    public int compareTo(final Intish<SELF> rhs) {
        return getBehavior().compare(this, rhs);
    }


    @Override
    public boolean equals(final Object other) {
        if (null == other) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return (this.value == ((Intish<?>) other).value);
    }


    @Override
    public final int hashCode() {
        return (int) this.value;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + this.value + ')';
    }


    /**
     * Used by subclasses to customize their validation and comparison
     * behavior.
     */
    protected static BehaviorBuilder behavior() {
        return new BehaviorBuilder();
    }


    /**
     * @return A convenience {@link Comparator} that sorts values in
     *         ascending order.
     *
     * @see Behavior
     */
    protected static Comparator<Long> ascending() {
        return ascendingComparator;
    }


    /**
     * @return A convenience {@link Comparator} that sorts values in
     *         descending order.
     *
     * @see Behavior
     */
    protected static Comparator<Long> descending() {
        return descendingComparator;
    }


    /**
     * @return A convenience {@link Comparator} that throws
     * {@link UnsupportedOperationException}.
     *
     * @see Behavior
     */
    protected static Comparator<Long> notComparable() {
        return notComparableComparator;
    }


    /**
     * Return the behavior object to use. This method may be called at
     * "unsafe" times, including possibly during constructor execution.
     * Therefore, the subclass's constructor may not have run yet. The
     * recommended practice is to store the Behavior in a static final
     * variable, and return a reference to that.
     *
     * Design rationale: During normal construction, a Behavior object could
     * be passed by the subclass constructor, because it is invoked first.
     * However, during deserialization, the superclass is initialized first.
     * Either the subclass must be given the opportunity to provide a
     * Behavior at that time, or the Behavior itself must be serialized
     * alongside the data. This would be unfortunate, since it would
     * multiply the size of serialized instances many-fold, to no purpose, as
     * the Behavior's state should be the same for all instances of the same
     * concrete subclass.
     *
     * @return a subclass-defined constant
     */
    protected abstract Behavior<SELF> getBehaviorUnsafe();


    private Behavior<SELF> getBehavior() {
        if (null == this.behavior) {
            this.behavior = getBehaviorUnsafe();
        }
        return this.behavior;
    }


    private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        getBehavior().validateBounds(this.value);
    }


    protected static final class BehaviorBuilder {
        private long minValue = Long.MIN_VALUE;
        private long maxValue = Long.MAX_VALUE;
        private Comparator<Long> comparator = ascending();

        /**
         * Minimum allowable value, inclusive. Default is
         * {@link Long#MIN_VALUE}.
         */
        public BehaviorBuilder lowerBound(final long newValue) {
            this.minValue = newValue;
            return this;
        }

        /**
         * Maximum allowable value, inclusive. Default is
         * {@link Long#MAX_VALUE}.
         */
        public BehaviorBuilder upperBound(final long newValue) {
            this.maxValue = newValue;
            return this;
        }

        /**
         * Used for comparisons with other {@link Intish} instances. See
         * {@link Intish#compareTo(Intish)} and
         * {@link Intish#notComparable()}.
         */
        public BehaviorBuilder comparator(final Comparator<Long> newComparator) {
            if (null == newComparator) {
                throw new IllegalArgumentException("null comparator");
            }
            this.comparator = newComparator;
            return this;
        }

        public <SELF> Behavior<SELF> build() {
            return new Behavior<SELF>(this.minValue, this.maxValue, this.comparator);
        }
    }


    protected static final class Behavior<SELF> implements Comparator<Intish<SELF>> {
        private final long minValue;
        private final long maxValue;
        private final Comparator<Long> comparator;

        private Behavior(final long minValue, final long maxValue, final Comparator<Long> comparator) {
            if (maxValue < minValue) {
                throw new IllegalArgumentException("max " + maxValue + " < " + " min " + minValue);
            }
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.comparator = Preconditions.checkNotNull(comparator);
        }

        // Private so Intish subclasses can't see it.
        private void validateBounds(final long value) {
            if (value < this.minValue || value > this.maxValue) {
                throw new IllegalArgumentException("value " + value + " is not in [" + this.minValue + ", " + this.maxValue + ']');
            }
        }

        @Override
        public int compare(final Intish<SELF> lhs, final Intish<SELF> rhs) {
            return this.comparator.compare(lhs.value, rhs.value);
        }
    }

}
