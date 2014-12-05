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

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public class TimedEvictionCacheBuilderExample {

    public static void main(final String... args) {
        final LoadingCache<String, Object> cache = TimedEvictionCacheBuilder.forKeysOfType(String.class)
                // After being written, ...
                .evictAfterWrite()
                // ...20% of values will expire after 17 minutes:
                .period(17, TimeUnit.MINUTES, 20)
                // ...30% will expire after 31 minutes:
                .period(31, TimeUnit.MINUTES, 30)
                // ...and the remaining 50% will expire after 1 hour:
                .period( 1, TimeUnit.HOURS,   50)
                .build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(final String key) {
                        // Imagine something expensive here...
                        return key.trim().toUpperCase();
                    }
                });

        final Object cached = cache.getUnchecked("Hello, World");
        assert cached != null;
    }

}
