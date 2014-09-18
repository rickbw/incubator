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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import rickbw.incubator.activity.Activity.Execution;


/**
 * An immutable composite of multiple {@link ActivityListener}s.
 */
public final class CompositeActivityListener implements ActivityListener<List<Object>, List<Object>> {

    /**
     * Order-preserving! That's important.
     */
    private final ImmutableSet<ActivityListener<?, ?>> listeners;


    public static CompositeActivityListener of(final ActivityListener<?, ?> listener) {
        return (listener instanceof CompositeActivityListener)
                ? (CompositeActivityListener) listener
                : ofAll(ImmutableSet.<ActivityListener<?, ?>>of(listener));
    }

    public static CompositeActivityListener ofAll(final Iterable<ActivityListener<?, ?>> listeners) {
        return new CompositeActivityListener(listeners);
    }

    /**
     * Iterates over all member listeners in forward order.
     */
    @Override
    public List<Object> onActivityInitialized(final Activity activity) {
        // Can't use ImmutableList.Builder, because it doesn't support nulls.
        final List<Object> contextAccumulator = new ArrayList<>();
        for (final ActivityListener<?, ?> listener : this.listeners) {
            final Object context = listener.onActivityInitialized(activity);
            contextAccumulator.add(context);
        }
        return Collections.unmodifiableList(contextAccumulator);
    }

    /**
     * Iterates over all member listeners in forward order.
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Object> onExecutionStarted(final Execution execution, final List<Object> activityContexts) {
        // Can't use ImmutableList.Builder, because it doesn't support nulls.
        final List<Object> contextAccumulator = new ArrayList<>();
        int index = 0;
        for (final ActivityListener listener : this.listeners) {
            final Object activityContext = activityContexts.get(index++);
            final Object executionContext = listener.onExecutionStarted(execution, activityContext);
            contextAccumulator.add(executionContext);
        }
        return Collections.unmodifiableList(contextAccumulator);
    }

    /**
     * Iterates over all member listeners in unspecified order.
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void onExecutionFailure(final List<Object> activityContexts, final Throwable failure) {
        int index = 0;
        for (final ActivityListener listener : this.listeners) {
            final Object listenerContext = activityContexts.get(index++);
            listener.onExecutionFailure(listenerContext, failure);
        }
    }

    /**
     * Iterates over all member listeners in reverse order.
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void onExecutionCompleted(final List<Object> activityContexts) {
        int index = activityContexts.size() - 1;
        for (final ActivityListener listener : this.listeners.asList().reverse()) {
            final Object listenerContext = activityContexts.get(index--);
            listener.onExecutionCompleted(listenerContext);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + this.listeners + ')';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CompositeActivityListener other = (CompositeActivityListener) obj;
        return this.listeners.equals(other.listeners);
    }

    @Override
    public int hashCode() {
        return this.listeners.hashCode();
    }

    private CompositeActivityListener(final Iterable<ActivityListener<?, ?>> listeners) {
        final ImmutableSet.Builder<ActivityListener<?, ?>> builder = ImmutableSet.builder();
        for (final ActivityListener<?, ?> listener : listeners) {
            builder.add(SynchronizedActivityListener.of(listener));
        }
        this.listeners = builder.build();
    }

}
