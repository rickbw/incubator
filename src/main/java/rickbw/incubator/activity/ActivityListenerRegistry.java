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
package rickbw.incubator.activity;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;


public class ActivityListenerRegistry {

    private static ActivityListenerRegistry shared = initiallyEmpty();

    private final ConcurrentMap<ActivityId, CompositeActivityListener> listenersById = Maps.newConcurrentMap();
    private final Optional<ActivityListener<?, ?>> defaultListener;


    public static ActivityListenerRegistry shared() {
        return shared;
    }

    public static ActivityListenerRegistry initiallyEmpty() {
        return new ActivityListenerRegistry(
                ImmutableMap.<ActivityId, ActivityListener<?,?>>of(),
                Optional.<ActivityListener<?, ?>>absent());
    }

    public static ActivityListenerRegistry of(final ActivityListener<?, ?> defaultListener) {
        return new ActivityListenerRegistry(
                ImmutableMap.<ActivityId, ActivityListener<?,?>>of(),
                Optional.<ActivityListener<?, ?>>of(defaultListener));
    }

    public static ActivityListenerRegistry create(final Map<ActivityId, ActivityListener<?, ?>> listenersById) {
        return new ActivityListenerRegistry(
                listenersById,
                Optional.<ActivityListener<?, ?>>absent());
    }

    public static ActivityListenerRegistry createWithDefault(
            final Map<ActivityId, ActivityListener<?, ?>> listenersById,
            final ActivityListener<?, ?> defaultListener) {
        return new ActivityListenerRegistry(
                listenersById,
                Optional.<ActivityListener<?, ?>>of(defaultListener));
    }

    public void add(final ActivityId id, final ActivityListener<?, ?> listener) {
        final CompositeActivityListener newValue = (this.listenersById.containsKey(id))
            ? this.listenersById.get(id).plus(listener)
            : CompositeActivityListener.of(listener);
        this.listenersById.put(id, newValue);
    }

    public Optional<ActivityListener<?, ?>> get(final ActivityId id) {
        Preconditions.checkNotNull(id);
        return (this.listenersById.containsKey(id))
                ? Optional.<ActivityListener<?, ?>>of(this.listenersById.get(id))
                : this.defaultListener;
    }

    public Function<ActivityId, ActivityListener<?, ?>> asFunction() {
        return new Function<ActivityId, ActivityListener<?,?>>() {
            @Override
            public ActivityListener<?, ?> apply(final ActivityId id) {
                // Function interface has explicitly nullable in and out
                return (id == null) ? null : get(id).orNull();
            }
        };
    }

    public Supplier<Optional<ActivityListener<?, ?>>> asSupplier(final ActivityId id) {
        Preconditions.checkNotNull(id);
        return new Supplier<Optional<ActivityListener<?,?>>>() {
            @Override
            public Optional<ActivityListener<?, ?>> get() {
                return ActivityListenerRegistry.this.get(id);
            }
        };
    }

    public Supplier<ActivityListener<?, ?>> asFailableSupplier(final ActivityId id) {
        Preconditions.checkNotNull(id);
        return new Supplier<ActivityListener<?,?>>() {
            @Override
            public ActivityListener<?, ?> get() {
                return ActivityListenerRegistry.this.get(id).get();
            }
        };
    }

    private ActivityListenerRegistry(
            final Map<ActivityId, ActivityListener<?, ?>> listenersById,
            final Optional<ActivityListener<?, ?>> defaultListener) {
        for (final Map.Entry<ActivityId, ActivityListener<?, ?>> entry : listenersById.entrySet()) {
            final ActivityId id = entry.getKey();
            final ActivityListener<?, ?> listener = entry.getValue();
            this.listenersById.put(id, CompositeActivityListener.of(listener));
        }

        this.defaultListener = defaultListener;
    }

}
