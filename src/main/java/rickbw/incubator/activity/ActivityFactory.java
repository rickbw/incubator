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
import java.util.Objects;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;


/**
 * A source of {@link Activity} objects.
 */
public class ActivityFactory {

    private final ImmutableMap<ActivityId, ActivityListener> listeners;
    private final Optional<ActivityListener> defaultListener;


    /**
     * All Activities created by this factory will use the given listener.
     *
     * @throws NullPointerException If the argument is null.
     */
    public ActivityFactory(final ActivityListener listener) {
        this.listeners = ImmutableMap.of();
        this.defaultListener = Optional.of(listener);
    }

    /**
     * Activities created by this factory will use the listener indicated for
     * their respective IDs. An attempt to start an {@link Activity} with an
     * ID not present in the given map will result in an
     * {@link IllegalStateException}.
     *
     * @throws NullPointerException If the argument is null.
     */
    public ActivityFactory(final Map<ActivityId, ActivityListener> listeners) {
        this.listeners = ImmutableMap.copyOf(listeners);
        this.defaultListener = Optional.absent();
    }

    /**
     * Activities created by this factory will use the listener indicated for
     * their respective IDs. An {@link Activity} with an ID not present in the
     * given map will use the given default.
     *
     * @throws NullPointerException If either argument is null.
     */
    public ActivityFactory(
            final Map<ActivityId, ActivityListener> listeners,
            final ActivityListener defaultListener) {
        this.listeners = ImmutableMap.copyOf(listeners);
        this.defaultListener = Optional.of(defaultListener);
    }

    /**
     * Start a new {@link Activity} with the given ID.
     *
     * @throws NullPointerException     If the ID is null.
     * @throws IllegalStateException    If there is no {@link ActivityListener}
     *                                  associated with the ID.
     */
    public Activity startActivity(final ActivityId id) {
        Objects.requireNonNull(id);
        final ActivityListener listener = (this.listeners.containsKey(id))
                ? this.listeners.get(id)
                : this.defaultListener.get();
        final Activity activity = new Activity(id, listener);
        activity.start();
        return activity;
    }

}
