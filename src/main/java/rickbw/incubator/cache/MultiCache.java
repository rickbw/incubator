/* Copyright 2013–2014 Rick Warren
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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.UncheckedExecutionException;


/**
 * A {@link Cache} that partitions entries into one of multiple delegate
 * {@link Cache}s on insertion, so that different subsets of the key space
 * can have different policies attached to them.
 *
 * Delegate caches are identified by means of a "cache key", which is derived
 * from an element key by means of a provided function. The cache key may be
 * any object, so long as it provides well-behaved
 * {@link Object#equals(Object) equals} and {@link Object#hashCode() hashCode}
 * implementations.
 */
public class MultiCache<K, V> implements Cache<K, V> {

    private final CacheMap asMap = new CacheMap();

    private final Class<K> elementKeyClass;
    private final ImmutableMap<Object, Cache<K, V>> delegates;
    private final Function<? super K, ?> cacheKeyCalc;


    /**
     * @param caches    Delegate caches, identified by their cache keys.
     * @param cacheKeyCalc  A {@link Function} to determine the cache key for
     *          a given element key. This function must be very fast, must
     *          return the same answer for a given element key every time,
     *          must return a key for which a {@link Cache} exists in the
     *          given {@link Map}, and must never return null.
     */
    public MultiCache(
            final Class<K> elementKeyClass,
            final Map<?, ? extends Cache<K, V>> caches,
            final Function<? super K, ?> cacheKeyCalc) {
        this.elementKeyClass = Objects.requireNonNull(elementKeyClass);
        this.delegates = ImmutableMap.copyOf(caches);
        this.cacheKeyCalc = Objects.requireNonNull(cacheKeyCalc);
    }

    @Override
    public @Nullable V getIfPresent(@Nullable final Object elementKey) {
        final Cache<K, V> cache = getCache(elementKey);
        if (cache != null) {
            return cache.getIfPresent(elementKey);
        } else {
            return null;
        }
    }

    @Override
    public @Nonnull V get(final K elementKey, final Callable<? extends V> valueLoader)
    throws ExecutionException {
        final Cache<K, V> cache = getCache(elementKey);
        if (cache != null) {
            return cache.get(elementKey, valueLoader);
        } else {
            throw new UncheckedExecutionException(noCacheForKey(elementKey));
        }
    }

    @Override
    public ImmutableMap<K, V> getAllPresent(final Iterable<?> keys) {
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (final Object elementKey : keys) {
            final Cache<K, V> cache = getCache(elementKey);
            if (cache != null) {
                final V value = cache.getIfPresent(elementKey);
                if (value != null) {
                    builder.put(this.elementKeyClass.cast(elementKey), value);
                }
            }
        }
        return builder.build();
    }

    @Override
    public void put(final K elementKey, final V value) {
        final Cache<K, V> cache = getCacheNonNull(elementKey);
        cache.put(elementKey, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> entries) {
        for (final Map.Entry<? extends K, ? extends V> entry : entries.entrySet()) {
            final K elementKey = entry.getKey();
            final Cache<K, V> cache = getCacheNonNull(elementKey);
            cache.put(elementKey, entry.getValue());
        }
    }

    @Override
    public long size() {
        long totalSize = 0L;
        for (final Cache<?, ?> delegate : this.delegates.values()) {
            totalSize += delegate.size();
        }
        return totalSize;
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return this.asMap;
    }

    @Override
    public void invalidate(final Object elementKey) {
        final Cache<K, V> cache = getCache(elementKey);
        if (cache != null) {
            cache.invalidate(elementKey);
        }
    }

    @Override
    public void invalidateAll(final Iterable<?> keys) {
        for (final Object elementKey : keys) {
            final Cache<K, V> cache = getCache(elementKey);
            if (cache != null) {
                cache.invalidate(elementKey);
            }
        }
    }

    @Override
    public void invalidateAll() {
        for (final Cache<?, ?> delegate : this.delegates.values()) {
            delegate.invalidateAll();
        }
    }

    @Override
    public void cleanUp() {
        for (final Cache<?, ?> delegate : this.delegates.values()) {
            delegate.cleanUp();
        }
    }

    @Override
    public CacheStats stats() {
        long hitCount = 0L;
        long missCount = 0L;
        long loadSuccessCount = 0L;
        long loadExceptionCount = 0L;
        long totalLoadTime = 0L;
        long evictionCount = 0L;
        for (final Cache<?, ?> delegate : this.delegates.values()) {
            final CacheStats stats = delegate.stats();
            hitCount += stats.hitCount();
            missCount += stats.missCount();
            loadSuccessCount += stats.loadSuccessCount();
            loadExceptionCount += stats.loadExceptionCount();
            totalLoadTime += stats.totalLoadTime();
            evictionCount += stats.evictionCount();
        }
        return new CacheStats(
                hitCount,
                missCount,
                loadSuccessCount,
                loadExceptionCount,
                totalLoadTime,
                evictionCount);
    }

    private @Nullable Object cacheKey(@Nullable final Object key) {
        if (this.elementKeyClass.isInstance(key)) {
            return this.cacheKeyCalc.apply(this.elementKeyClass.cast(key));
        } else {
            return null;
        }
    }

    private @Nullable Cache<K, V> getCache(final Object elementKey) {
        @Nullable final Object cacheKey = cacheKey(elementKey);
        @Nullable final Cache<K, V> cache = this.delegates.get(cacheKey);
        return cache;
    }

    private @Nonnull Cache<K, V> getCacheNonNull(final Object elementKey) {
        @Nullable final Cache<K, V> cache = getCache(elementKey);
        if (cache != null) {
            return cache;
        } else {
            throw noCacheForKey(elementKey);
        }
    }

    private static IllegalArgumentException noCacheForKey(final Object elementKey) {
        return new IllegalArgumentException("Unable to identify cache for element key " + elementKey);
    }


    private final class CacheMap extends AbstractMap<K, V> implements ConcurrentMap<K, V> {
        private final Set<Map.Entry<K, V>> entrySet = new CacheSet();

        @Override
        public V get(final Object elementKey) {
            final Cache<K, V> cache = getCache(elementKey);
            if (cache != null) {
                return cache.asMap().get(elementKey);
            } else {
                return null;
            }
        }

        @Override
        public boolean containsKey(final Object elementKey) {
            final Cache<K, V> cache = getCache(elementKey);
            if (cache != null) {
                return cache.asMap().containsKey(elementKey);
            } else {
                return false;
            }
        }

        @Override
        public V put(final K elementKey, final V value) {
            final Cache<K, V> cache = getCacheNonNull(elementKey);
            return cache.asMap().put(elementKey, value);
        }

        @Override
        public V putIfAbsent(final K elementKey, final V value) {
            final Cache<K, V> cache = getCacheNonNull(elementKey);
            return cache.asMap().putIfAbsent(elementKey, value);
        }

        @Override
        public boolean remove(final Object elementKey, final Object value) {
            final Cache<K, V> cache = getCache(elementKey);
            if (cache != null) {
                return cache.asMap().remove(elementKey, value);
            } else {
                return false;
            }
        }

        @Override
        public boolean replace(final K elementKey, final V oldValue, final V newValue) {
            final Cache<K, V> cache = getCacheNonNull(elementKey);
            return cache.asMap().replace(elementKey, oldValue, newValue);
        }

        @Override
        public V replace(final K elementKey, final V value) {
            final Cache<K, V> cache = getCacheNonNull(elementKey);
            return cache.asMap().replace(elementKey, value);
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return this.entrySet;
        }
    }


    private final class CacheSet extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            final Collection<Iterator<Map.Entry<K, V>>> iterators = new ArrayList<>(
                    MultiCache.this.delegates.size());
            for (final Cache<K, V> delegate : MultiCache.this.delegates.values()) {
                iterators.add(delegate.asMap().entrySet().iterator());
            }
            return Iterators.concat(iterators.iterator());
        }

        @Override
        public boolean add(final Map.Entry<K, V> entry) {
            final K elementKey = entry.getKey();
            final Cache<K, V> cache = getCacheNonNull(elementKey);
            return cache.asMap().entrySet().add(entry);
        }

        @Override
        public int size() {
            final long totalSize = MultiCache.this.size();
            if (totalSize <= Integer.MAX_VALUE) {
                return (int) totalSize;
            } else {
                return Integer.MAX_VALUE;
            }
        }
    }

}
