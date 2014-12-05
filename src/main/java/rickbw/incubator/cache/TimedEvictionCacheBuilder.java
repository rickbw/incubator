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
package rickbw.incubator.cache;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


/**
 * Constructs new {@link Cache} instances that expire their elements
 * incrementally, in multiple time periods instead of just one, as
 * {@link CacheBuilder} does. The simpler behavior of {@code CacheBuilder} is
 * appropriate for applications that cache objects gradually over a long
 * period of time. It is less appropriate for applications that build a large
 * cache within a short period of time. An application of the latter sort
 * tends to also dump its entire cache within a short period of time, and thus
 * experiences periodic volatile performance. This class allows applications
 * to configure multiple periods, each governing only a subset of the cache.
 * This avoids a situation in which the entire cache expires at once.
 *
 * Add as many periods as you like; each is associated with a "size". Any
 * given element will expire from the resulting cache with a probability of
 * {@code size[i]/sum(size[0..n-1])} for each given size {@code i}. For
 * example, if a cache is defined with one period of 3 seconds and a size of
 * 4, and a second period of 5 seconds and a size of 6, then when any given
 * key is added to the cache, there is a 40% chance that the element
 * associated with that key will expire every 3 seconds, and a 60% chance that
 * it will expire every 5 seconds. (As you may guess, prime-numbered sizes are
 * good; they reduce the frequency at which multiple periods expire their
 * elements at the same time. Periods that are multiples of one another are
 * bad; they defeat the whole purpose of this class.)
 */
public final class TimedEvictionCacheBuilder<K, V> {

    private final List<Period> periods = new ArrayList<>();
    private final Class<K> elementKeyClass;
    private Optional<Eviction> evictionPolicy = Optional.absent();


    public static <K, V> TimedEvictionCacheBuilder<K, V> forKeysOfType(final Class<K> elementKeyClass) {
        return new TimedEvictionCacheBuilder<>(elementKeyClass);
    }

    /**
     * It is required to call either this method or {@link #evictAfterWrite()}
     * for each Builder instance before calling {@link #build()} or
     * {@link #build(CacheLoader)}.
     */
    public TimedEvictionCacheBuilder<K, V> evictAfterAccess() {
        Preconditions.checkState(!this.evictionPolicy.isPresent(), "eviction policy already set");
        this.evictionPolicy = Optional.of(Eviction.AFTER_ACCESS);
        return this;
    }

    /**
     * It is required to call either this method or {@link #evictAfterAccess()}
     * for each Builder instance before calling {@link #build()} or
     * {@link #build(CacheLoader)}.
     */
    public TimedEvictionCacheBuilder<K, V> evictAfterWrite() {
        Preconditions.checkState(!this.evictionPolicy.isPresent(), "eviction policy already set");
        this.evictionPolicy = Optional.of(Eviction.AFTER_WRITE);
        return this;
    }

    /**
     * It is required to call this method at least once for each Builder
     * instance before calling {@link #build()} or {@link #build(CacheLoader)}.
     */
    public TimedEvictionCacheBuilder<K, V> period(final long duration, final TimeUnit durationUnit, final int size) {
        this.periods.add(new Period(duration, durationUnit, size));
        return this;
    }

    // TODO: Mirror additional CacheBuilder APIs

    /**
     * @throws IllegalStateException    If neither {@link #evictAfterAccess()}
     *              nor {@link #evictAfterWrite()} has been called, or if no
     *              periods have been added with
     *              {@link #period(long, TimeUnit, int)}.
     */
    public Cache<K, V> build() {
        if (this.periods.isEmpty()) {
            throw new IllegalStateException("no periods defined");
        } else if (this.periods.size() == 1) {
            final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
            configureBuilder(builder, this.periods.get(0));
            return builder.build();
        } else {
            assert this.periods.size() > 1;
            final Map<Period, Cache<K, V>> caches = new IdentityHashMap<>();
            for (final Period period : this.periods) {
                final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
                configureBuilder(builder, period);
                final Cache<K, V> cache = builder.build();
                caches.put(period, cache);
            }
            return new MultiCache<K, V>(
                    this.elementKeyClass,
                    caches,
                    new PeriodSelector<>(this.periods));
        }
    }

    /**
     * @throws IllegalStateException    If neither {@link #evictAfterAccess()}
     *              nor {@link #evictAfterWrite()} has been called, or if no
     *              periods have been added with
     *              {@link #period(long, TimeUnit, int)}.
     */
    public LoadingCache<K, V> build(final CacheLoader<? super K, V> loader) {
        if (this.periods.isEmpty()) {
            throw new IllegalStateException("no periods defined");
        } else if (this.periods.size() == 1) {
            final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
            configureBuilder(builder, this.periods.get(0));
            return builder.build(loader);
        } else {
            assert this.periods.size() > 1;
            final Map<Period, LoadingCache<K, V>> caches = new IdentityHashMap<>();
            for (final Period period : this.periods) {
                final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
                configureBuilder(builder, period);
                final LoadingCache<K, V> cache = builder.build(loader);
                caches.put(period, cache);
            }
            return new LoadingMultiCache<K, V>(
                    this.elementKeyClass,
                    caches,
                    new PeriodSelector<>(this.periods));
        }
    }

    private TimedEvictionCacheBuilder(final Class<K> elementKeyClass) {
        this.elementKeyClass = Objects.requireNonNull(elementKeyClass);
    }

    private void configureBuilder(
            final CacheBuilder<Object, Object> builder,
            final Period period) {
        this.evictionPolicy.get().applyExpiration(builder, period);
        // TODO: When we add other Builder properties, set them here.
    }


    private static final class Period {
        public final int size;
        public final long duration;
        public final TimeUnit durationUnit;

        public Period(final long duration, final TimeUnit durationUnit, final int size) {
            this.size = size;
            Preconditions.checkArgument(this.size > 0, "Size must be greater than 0");

            this.duration = duration;
            Preconditions.checkArgument(this.duration > 0, "Duration must be greater than 0");

            this.durationUnit = Objects.requireNonNull(durationUnit);
        }

        @Override
        public String toString() {
            return getClass() + "("
                    + this.duration + " " + this.durationUnit.toString().toLowerCase()
                    + "; size="+ this.size
                    + ")";
        }
    }


    private static enum Eviction {
        AFTER_ACCESS {
            @Override
            public void applyExpiration(final CacheBuilder<?, ?> builder, final Period period) {
                builder.expireAfterAccess(period.duration, period.durationUnit);
            }
        },
        AFTER_WRITE {
            @Override
            public void applyExpiration(final CacheBuilder<?, ?> builder, final Period period) {
                builder.expireAfterWrite(period.duration, period.durationUnit);
            }
        };

        public abstract void applyExpiration(CacheBuilder<?, ?> builder, Period period);
    }


    private static final class PeriodSelector<K> implements Function<K, Period> {
        private static final HashFunction hasher = Hashing.goodFastHash(Integer.SIZE);
        private final ImmutableList<Period> periods;
        private final long totalSize;

        public PeriodSelector(final Iterable<? extends Period> periods) {
            this.periods = ImmutableList.copyOf(periods);
            long size = 0L;
            for (final Period period : this.periods) {
                size += period.size;
            }
            this.totalSize = size;
        }

        /**
         * Imagine all of the periods provided in the constructor laid end to
         * end on a number line, each the length of its size, stretching from
         * zero to the sum of all of the sizes. Hash the given key and mod it
         * with that sum to yield a position along that number line. Then
         * identify the period that covers that portion of the value. This
         * approach has the benefit of being both fast and deterministic for a
         * given key.
         */
        @Override
        public Period apply(final K elementKey) {
            // Rehash for better bit dispersion:
            final int hashCode = hasher.hashInt(elementKey.hashCode()).asInt();
            final long value = hashCode % this.totalSize;
            int i = 0;
            for (int bound = 0; i < this.periods.size() && value >= (bound += this.periods.get(i).size); ++i) {
                // do nothing
            }
            return this.periods.get(i);
        }
    }

}
