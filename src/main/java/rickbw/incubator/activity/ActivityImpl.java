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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;


/**
 * An implementation detail of the public {@link Activity} class.
 */
/*package*/ final class ActivityImpl<AC, EC> extends Activity {

    /**
     * Keeps track of which {@link Activity.Execution}, of which
     * {@link Activity}, was last started in the current thread. This is used
     * to infer the parent Execution for a new Execution, in case none is
     * specified.
     *
     * @see Activity.ExecutionBuilder#within(Execution)
     */
    private static final ThreadLocal<Execution> currentExecution = new ThreadLocal<>();

    /**
     * Use this to control the call to
     * {@link ActivityListener#onActivityInitialized(Activity)}.
     */
    private final AtomicBoolean initializedYet = new AtomicBoolean(false);
    /**
     * In case no name is specified for a new {@link Activity.Execution}.
     *
     * @see Activity.ExecutionBuilder#withName(Object)
     */
    private final AtomicLong defaultExecutionNameCounter = new AtomicLong(0L);
    private final Supplier<ActivityListener<AC, EC>> listener;
    private volatile AC activityContext = null;


    public ActivityImpl(final ActivityId id, final Supplier<ActivityListener<AC, EC>> listener) {
        super(id);
        this.listener = SynchronizedActivityListener.supply(Suppliers.memoize(listener));
    }

    @Override
    public ExecutionBuilder execution() {
        if (!this.initializedYet.getAndSet(true)) {
            this.activityContext = this.listener.get().onActivityInitialized(this);
        }
        return new ExecutionBuilderImpl();
    }


    private final class ExecutionImpl extends Execution {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final Optional<Execution> parent;
        private final EC executionContext;

        private ExecutionImpl(
                final ExecutionId id,
                final Optional<Execution> parent) {
            super(id);

            this.parent = parent.isPresent() ? parent : Optional.fromNullable(currentExecution.get());
            currentExecution.set(this);

            final ActivityListener<AC, EC> lstnr = listener();
            synchronized (lstnr) {
                /* If onExecutionStarted() fails, we can't call onExecutionFailure(),
                 * because we don't have the context.
                 */
                this.executionContext = lstnr.onExecutionStarted(
                        this,
                        ActivityImpl.this.activityContext);
            }
        }

        @Override
        public Activity getOwner() {
            return ActivityImpl.this;
        }

        @Override
        public Optional<Execution> getParent() {
            return this.parent;
        }

        @Override
        public void failureOccurred(final Throwable failure) {
            final ActivityListener<AC, EC> lstnr = listener();
            synchronized (lstnr) {
                lstnr.onExecutionFailure(
                        this.executionContext,
                        failure);
            }
        }

        @Override
        public void close() {
            if (!this.closed.getAndSet(true)) {
                try {
                    final ActivityListener<AC, EC> lstnr = listener();
                    synchronized (lstnr) {
                        /* If onExecutionCompleted() fails, we can't call
                         * onExecutionFailure(), because we have a guarantee not
                         * to call the latter after the former.
                         */
                        lstnr.onExecutionCompleted(this.executionContext);
                    }
                } finally {
                    if (currentExecution.get() == this) {
                        currentExecution.set(this.parent.isPresent() ? this.parent.get() : null);
                    }
                }
            }
        }

        private ActivityListener<AC, EC> listener() {
            return ActivityImpl.this.listener.get();
        }
    }


    private final class ExecutionBuilderImpl extends ExecutionBuilder implements Cloneable {
        private Optional<Object> executionName = Optional.absent();
        private Optional<Execution> parent = Optional.absent();

        @Override
        public ExecutionBuilder withName(final Object newName) {
            this.executionName = Optional.of(newName);
            return this;
        }

        @Override
        public ExecutionBuilder within(final Execution newParent) {
            this.parent = Optional.of(newParent);
            return this;
        }

        @Override
        public Execution start() {
            final Object realExecName = this.executionName.isPresent()
                    ? this.executionName.get()
                    : ActivityImpl.this.defaultExecutionNameCounter.getAndIncrement();
            final ExecutionId execId = new ExecutionId(getId(), realExecName);
            return new ExecutionImpl(execId, this.parent);
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            // No further copying logic necessary: all our state is immutable.
            return super.clone();
        }
    }

}
