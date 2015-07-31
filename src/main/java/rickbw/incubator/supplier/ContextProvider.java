/* Copyright 2015 Rick Warren
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
package rickbw.incubator.supplier;

import com.google.common.base.Preconditions;


/**
 * An abstraction for maintaining business context throughout a call stack,
 * without having to pass it everywhere. This implementation maintains the
 * context on a per-thread basis, but this strategy could be made pluggable
 * in the future.
 * <p/>
 * The recommended usage pattern looks like this:
 * <pre><code>
 *  try (ContextProvider.Session s = this.context.begin("Hello")) {
 *      // ...
 *      // ... At some point, 10 levels deep in the call stack:
 *      logger.info("Use context {} for something!", this.context.current());
 *      // ...
 *  }
 * </code></pre>
 * <p/>
 * This is similar to what Hibernate's "current session" capability does,
 * but generalized.
 * <p/>
 * TODO: Integrate with the {@link rickbw.incubator.activity} package in this
 * project.
 */
public class ContextProvider<T> {

    private final ThreadLocal<T> context = new ThreadLocal<>();


    /**
     * Initialize the context for a particular business activity, and
     * return a token that will let you close it again at the end. The current
     * implementation assumes that the scope of Session is a single thread.
     *
     * @throws IllegalArgumentException If the given context is {@code null}.
     * @throws IllegalStateException    If this method is called within the
     *              scope of a previous call.
     */
    public Session begin(final T contextValue) {
        Preconditions.checkArgument(contextValue != null, "Context cannot be null");
        Preconditions.checkState(this.context.get() == null, "Already in a Session");
        this.context.set(contextValue);
        return new Session();
    }

    /**
     * Get the context that was previously set in {@link #begin(Object)}.
     *
     * @throws IllegalStateException    If this method is called outside of
     *              the scope between a call to {@link #begin(Object)} and
     *              the subsequent call to {@link Session#close()}.
     */
    public T current() {
        final T current = this.context.get();
        Preconditions.checkState(current != null, "Not in a Session; call begin() first");
        return current;
    }


    /**
     * Represents a particular business activity, which will eventually
     * complete with a call to {@link #close()}.
     */
    public class Session implements AutoCloseable {
        /**
         * Complete the business activity previously started by
         * {@link ContextProvider#begin(Object)}.
         *
         * @throws IllegalStateException    If this method was already called.
         */
        @Override
        public void close() {
            Preconditions.checkState(context.get() != null, "Already closed");
            context.remove();
        }
    }

}
