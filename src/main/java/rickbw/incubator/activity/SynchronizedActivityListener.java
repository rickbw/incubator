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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import rickbw.incubator.activity.Activity.Execution;


/*package*/ final class SynchronizedActivityListener<AC, EC> implements ActivityListener<AC, EC> {

    private final ActivityListener<AC, EC> delegate;


    public static <AC, EC> ActivityListener<AC, EC> of(final ActivityListener<AC, EC> listener) {
        return (listener instanceof SynchronizedActivityListener)
                ? (SynchronizedActivityListener<AC, EC>) listener
                : new SynchronizedActivityListener<>(listener);
    }

    /**
     * This is different than {@link Suppliers#synchronizedSupplier(Supplier)},
     * because this one synchronizes on the underlying listener, not on the
     * {@link Supplier}.
     */
    public static <AC, EC> Supplier<ActivityListener<AC, EC>> supply(final Supplier<ActivityListener<AC, EC>> supplier) {
        if (supplier instanceof SynchSupplier) {
            @SuppressWarnings("unchecked")
            final SynchSupplier<AC, EC> typed = (SynchSupplier<AC, EC>) supplier;
            return typed;
        } else {
            return new SynchSupplier<>(supplier);
        }
    }

    @Override
    public AC onActivityInitialized(final Activity activity) {
        synchronized (this.delegate) {
            return this.delegate.onActivityInitialized(activity);
        }
    }

    @Override
    public EC onExecutionStarted(final Execution execution, final AC activityContext) {
        synchronized (this.delegate) {
            return this.delegate.onExecutionStarted(execution, activityContext);
        }
    }

    @Override
    public void onExecutionFailure(final EC activityContext, final Throwable failure) {
        synchronized (this.delegate) {
            this.delegate.onExecutionFailure(activityContext, failure);
        }
    }

    @Override
    public void onExecutionCompleted(final EC activityContext) {
        synchronized (this.delegate) {
            this.delegate.onExecutionCompleted(activityContext);
        }
    }

    @Override
    public String toString() {
        return "synchronized(" + this.delegate + ')';
    }

    private SynchronizedActivityListener(final ActivityListener<AC, EC> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }


    private static final class SynchSupplier<AC, EC> implements Supplier<ActivityListener<AC, EC>> {
        private final Supplier<ActivityListener<AC, EC>> delegate;

        public SynchSupplier(final Supplier<ActivityListener<AC, EC>> delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        @Override
        public ActivityListener<AC, EC> get() {
            final ActivityListener<AC, EC> listener = this.delegate.get();
            return of(listener);
        }

        @Override
        public String toString() {
            return "synchronized(" + this.delegate + ')';
        }
    }

}
