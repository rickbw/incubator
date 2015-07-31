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
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;


/**
 * An Activity represents a series of actions carried out by an application.
 * These actions may be executed any number of times; each is represented
 * by an {@link Execution}. An Execution may be parallel or sequential, but as
 * a whole, it has a beginning and an end.
 *
 * An Activity, and all of its Executions, report what they do to an
 * {@link ActivityListener}. That "plug-in" type is the point of extensibility
 * in the Activity framework.
 */
public abstract class Activity implements Executor {

    private final ActivityId id;


    /**
     * Create a new {@link Activity} with the given ID that will report to the
     * given listener.
     *
     * The call to {@link ActivityListener#onActivityInitialized(Activity)}
     * will be made at some time in between the call to this factory method
     * and the completion of the first call to {@link ExecutionBuilder#start()}.
     * The exact time is unspecified.
     *
     * @throws  NullPointerException    If either argument is null.
     */
    public static Activity create(
            final ActivityId id,
            final Supplier<ActivityListener<?, ?>> listener) {
        // XXX: We ought to be able to make this safe...
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Activity activity = new ActivityImpl(id, listener);
        return activity;
    }

    public final ActivityId getId() {
        return this.id;
    }

    /**
     * Prepare to start a new {@link Execution} of this Activity.
     *
     * @see ExecutionBuilder#start()
     */
    public abstract ExecutionBuilder execution();

    /**
     * Run a given task in the current thread within a new {@link Execution}.
     * This is a convenience API, equivalent to creating a new
     * {@link ExecutionBuilder}, using it to wrap the given task, and then
     * running that task.
     *
     * @see #execution()
     * @see ExecutionBuilder#wrap(Runnable)
     */
    @Override
    public final void execute(final Runnable task) {
        execution().wrap(task).run();
    }

    @Override
    public final String toString() {
        // Private subclass masquerades as public class:
        return Activity.class.getSimpleName() + '(' + this.id + ')';
    }

    /*package*/ Activity(final ActivityId id) {
        this.id = Objects.requireNonNull(id);
    }


    /**
     * An {@link Activity} may be run multiple times; this is one of them.
     * It begins with a call to {@link ExecutionBuilder#start()}, and ends
     * with a call to {@link #close()}.
     */
    public static abstract class Execution implements AutoCloseable {
        private final ExecutionId id;

        /*package*/ Execution(final ExecutionId id) {
            this.id = Objects.requireNonNull(id);
        }

        public final ExecutionId getId() {
            return this.id;
        }

        /**
         * @return  The {@link Activity} of which this Execution is an
         *          "instance".
         */
        public abstract Activity getOwner();

        /**
         * The Execution of one {@link Activity} may be a sub-task within the
         * Execution of another Activity. If such is the case with this
         * Execution, return that containing Execution.
         */
        public abstract Optional<Execution> getParent();

        /**
         * Inform the listener of the given failure. Do not attempt to recover
         * from any exception it might throw.
         *
         * @see ActivityListener#onExecutionFailure(Object, Throwable)
         */
        public abstract void failureOccurred(final Throwable failure);

        /**
         * Complete this Execution, and inform the listener. Do not attempt to
         * recover from any exception thrown by the latter.
         *
         * @see ActivityListener#onExecutionCompleted(Object)
         */
        @Override
        public abstract void close();

        @Override
        public final String toString() {
            // Private subclass masquerades as public class:
            return Execution.class.getSimpleName() + '(' + this.id + ')';
        }
    }


    /**
     * Constructs and launches a new {@link Execution}.
     *
     * @see Activity#execution()
     * @see #start()
     */
    public static abstract class ExecutionBuilder {
        /**
         * The {@link ExecutionId} of the new {@link Execution} will be
         * comprised of the {@link ActivityId} of the owning {@link Activity}
         * and of the given "execution name".
         *
         * This is an optional call. If it is not called, in incrementing
         * counter will be used instead.
         */
        public abstract ExecutionBuilder withName(final Object name);

        /**
         * Indicate that the new {@link Execution} should be considered a
         * sub-task within the given Execution.
         *
         * This is an optional call. If it is not called, an attempt will be
         * made to infer a parent Execution based on the currently running
         * thread.
         */
        public abstract ExecutionBuilder within(final Execution parent);

        /**
         * Start a new {@link Execution} and inform the listener. Do not
         * attempt to recover from any exception thrown by the latter.
         *
         * @see ActivityListener#onExecutionStarted(Execution, Object)
         */
        public abstract Execution start();

        /**
         * Wrap the given {@link Runnable} in a new Runnable that starts a new
         * {@link Execution} before running the given task, then closes that
         * Execution afterwards.
         */
        public final Runnable wrap(final Runnable task) {
            final ExecutionBuilder protectedAgainstFutureMutation = deepCopy();
            return new Runnable() {
                @Override
                public void run() {
                    try (Execution exec = protectedAgainstFutureMutation.start()) {
                        task.run();
                    }
                }
            };
        }

        /**
         * Wrap the given {@link Runnable} in a new Runnable that starts a new
         * {@link Execution} before running the given task, then closes that
         * Execution afterwards.
         */
        public final <T> Callable<T> wrap(final Callable<T> task) {
            final ExecutionBuilder protectedAgainstFutureMutation = deepCopy();
            return new Callable<T>() {
                @Override
                public T call() throws Exception {
                    try (Execution exec = protectedAgainstFutureMutation.start()) {
                        return task.call();
                    }
                }
            };
        }

        /*package*/ ExecutionBuilder() {
            // prevent unintended instantiation
        }

        private ExecutionBuilder deepCopy() {
            try {
                return (ExecutionBuilder) clone();
            } catch (final CloneNotSupportedException ex) {
                // ActivityImpl.ExecutionBuilderImpl should implement Cloneable
                throw new AssertionError("unreachable");
            }
        }
    }

}
