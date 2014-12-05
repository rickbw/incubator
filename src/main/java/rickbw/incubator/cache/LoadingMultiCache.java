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

import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;


/**
 * A {@link MultiCache} that implements {@link LoadingCache} and wraps other
 * {@link Cache}s that also implement {@link LoadingCache}.
 */
public final class LoadingMultiCache<K, V> extends MultiCache<K, V> implements LoadingCache<K, V> {

    public LoadingMultiCache(
            final Class<K> elementKeyClass,
            final Map<?, ? extends LoadingCache<K, V>> caches,
            final Function<? super K, ?> cacheKeyCalc) {
        super(elementKeyClass, caches, cacheKeyCalc);
    }

    @Override
    public V get(final K key) throws ExecutionException {
        final LoadingCache<K, V> cache = getCacheNonNull(key);
        return cache.get(key);
    }

    @Override
    public V getUnchecked(final K key) {
        final LoadingCache<K, V> cache = getCacheNonNull(key);
        return cache.getUnchecked(key);
    }

    @Override
    public ImmutableMap<K, V> getAll(final Iterable<? extends K> elementKeys)
    throws ExecutionException {
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (final K key : elementKeys) {
            final LoadingCache<K, V> cache = getCache(key);
            if (cache != null) {
                final V value = cache.get(key);
                builder.put(key, value);
            }
        }
        return builder.build();
    }

    @Override
    public void refresh(final K key) {
        final LoadingCache<K, V> cache = getCacheNonNull(key);
        cache.refresh(key);
    }

    @Override
    @Deprecated
    public V apply(final K key) {
        final LoadingCache<K, V> cache = getCacheNonNull(key);
        return cache.apply(key);
    }

    @Override
    protected @Nullable LoadingCache<K, V> getCache(final Object elementKey) {
        return (LoadingCache<K, V>) super.getCache(elementKey);
    }

    @Override
    protected @Nonnull LoadingCache<K, V> getCacheNonNull(final Object elementKey) {
        return (LoadingCache<K, V>) super.getCacheNonNull(elementKey);
    }

}
