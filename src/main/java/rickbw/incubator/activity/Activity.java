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
import java.util.concurrent.atomic.AtomicBoolean;


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
public abstract class Activity {

    private final ActivityId id;


    public static <AC, EC> Activity create(
            final ActivityId id,
            final ActivityListener<AC, EC> listener) {
        return new ActivityImpl<>(id, listener);
    }

    public ActivityId getId() {
        return this.id;
    }

    /**
     * Start this activity and inform the listener. Do not attempt to  recover
     * from any exception thrown by the latter.
     *
     * @see ActivityListener#onExecutionStarted(Object)
     */
    public abstract Execution start();

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + this.id + ')';
    }

    private Activity(final ActivityId id) {
        this.id = Objects.requireNonNull(id);
    }


    public interface Execution extends AutoCloseable {
        Activity getActivity();

        /**
         * Inform the listener of the given failure. Do not attempt to recover
         * from any exception it might throw.
         *
         * @see ActivityListener#onExecutionFailure(Object, Throwable)
         */
        void failureOccurred(final Throwable failure);

        /**
         * Complete this Execution, and inform the listener. Do not attempt to
         * recover from any exception thrown by the latter.
         *
         * @see ActivityListener#onExecutionCompleted(Object)
         */
        @Override
        void close();
    }


    private static final class ActivityImpl<AC, EC> extends Activity {
        private final ActivityListener<AC, EC> listener;
        private final AC activityContext;

        private ActivityImpl(final ActivityId id, final ActivityListener<AC, EC> listener) {
            super(id);
            this.listener = listener;
            this.activityContext = this.listener.onActivityInitialized(this);
        }

        @Override
        public Execution start() {
            synchronized (this.listener) {
                /* If onExecutionStarted() fails, we can't call onExecutionFailure(),
                 * because we don't have the context.
                 */
                final EC context = this.listener.onExecutionStarted(this.activityContext);
                return new ExecutionImpl<EC>(this, context);
            }
        }
    }


    private static final class ExecutionImpl<EC> implements Execution {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final ActivityImpl<?, EC> activity;
        private final EC executionContext;

        private ExecutionImpl(final ActivityImpl<?, EC> activity, final EC executionContext) {
            this.activity = activity;
            this.executionContext = executionContext;
        }

        @Override
        public Activity getActivity() {
            return this.activity;
        }

        @Override
        public void failureOccurred(final Throwable failure) {
            final ActivityListener<?, EC> listener = this.activity.listener;
            synchronized (listener) {
                listener.onExecutionFailure(this.executionContext, failure);
            }
        }

        @Override
        public void close() {
            if (this.closed.getAndSet(true)) {
                final ActivityListener<?, EC> listener = this.activity.listener;
                synchronized (listener) {
                    /* If onExecutionCompleted() fails, we can't call
                     * onExecutionFailure(), because we have a guarantee not
                     * to call the latter after the former.
                     */
                    listener.onExecutionCompleted(this.executionContext);
                }
            }
        }
    }

}
