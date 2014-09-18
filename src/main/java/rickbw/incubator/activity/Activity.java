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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;


/**
 * An Activity represents a series of actions carried out by an application.
 * These actions may be parallel or sequential, but as a group, they have a
 * beginning and an end.
 *
 * An Activity reports those beginnings and endings to {@link ActivityListener}.
 *
 * Activities may be nested; that is, one Activity may be made up (in whole or
 * in part) of other Activities.
 */
public class Activity implements AutoCloseable {

    private final AtomicReference<State> state = new AtomicReference<>(State.UNSTARTED);
    private final ActivityId id;
    private final ActivityListener listener;
    private volatile Object context = null;


    public ActivityId getId() {
        return this.id;
    }

    /**
     * Start this activity and inform the listener.
     *
     * @throw IllegalStateException If this Activity was already started previously.
     *
     * @see ActivityListener#onStarted(Activity)
     */
    /*package*/ void start() {
        Preconditions.checkState(this.state.getAndSet(State.STARTED) == State.UNSTARTED);
        synchronized (this.listener) {
            this.context = this.listener.onStarted(this);
        }
    }

    /**
     * Inform the listener of the given failure. Do not attempt to recover
     * from any exception it might throw.
     *
     * @see ActivityListener#onFailure(Activity, Throwable, Object)
     */
    public void failureOccurred(final Throwable failure) {
        Preconditions.checkState(this.state.get() == State.STARTED);
        synchronized (this.listener) {
            this.listener.onFailure(this, failure, this.context);
        }
    }

    /**
     * Complete this Activity, and inform the listener. If the listener throws
     * an exception, report it to
     * {@link ActivityListener#onFailure(Activity, Throwable, Object)}. Do not
     * attempt to recover from any exception thrown by the latter.
     *
     * @see ActivityListener#onCompleted(Activity, Object)
     */
    @Override
    public void close() {
        Preconditions.checkState(this.state.getAndSet(State.COMPLETED) == State.STARTED);
        synchronized (this.listener) {
            try {
                this.listener.onCompleted(this, this.context);
            } catch (final Throwable failure) {
                this.listener.onFailure(this, failure, this.context);
            } finally {
                this.context = null;
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + this.id + "; " + this.state + ')';
    }

    /*package*/ Activity(final ActivityId id, final ActivityListener listener) {
        this.id = Objects.requireNonNull(id);
        this.listener = Objects.requireNonNull(listener);
    }

    private enum State { UNSTARTED, STARTED, COMPLETED }

}
