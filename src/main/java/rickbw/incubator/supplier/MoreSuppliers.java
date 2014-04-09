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
package rickbw.incubator.supplier;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.UncheckedExecutionException;


/**
 * Additional utilities for working with Guava {@link Supplier}s, analogous
 * to the {@link Suppliers} utility class.
 */
public final class MoreSuppliers {

    /**
     * Wrap the given {@link Callable} in a {@link Supplier}. If the
     * {@code Callable} throws a {@link RuntimeException}, it will be
     * propagated unchanged. If it throws a checked {@link Exception}, that
     * exception will be wrapped in an {@link UncheckedExecutionException}.
     *
     * The resulting object will be {@link Serializable} if the input is.
     */
    public static <T> Supplier<T> fromCallable(final Callable<? extends T> callable) {
        return new CallableSupplier<>(callable);
    }

    /**
     * Similar to {@link Suppliers#memoize(Supplier)}, but without requiring
     * any locking or synchronization. When calling {@link Supplier#get()},
     * the given {@code Supplier} will be used to initialize the value. Note
     * that {@link Supplier#get()} may be called more than once, in the event
     * that there's contention at initialization time. Note also that the
     * given {@code Supplier} must not return null; if it does,
     * {@link Supplier#get()} will throw {@link NullPointerException}.
     *
     * The resulting object will be {@link Serializable} if the input is.
     */
    public static <T> Supplier<T> memoizeLockless(final Supplier<T> supplier) {
        return (supplier instanceof MemoizingSupplier<?>)
                ? supplier
                : new MemoizingSupplier<>(supplier);
    }

    private MoreSuppliers() {
        // prevent instantiation
    }


    private static final class CallableSupplier<T>
    implements Supplier<T>, Serializable {
        private static final long serialVersionUID = -5384149913110484272L;

        private final Callable<? extends T> callable;

        private CallableSupplier(final Callable<? extends T> callable) {
            this.callable = Preconditions.checkNotNull(callable, "null callable");
        }

        @Override
        public T get() {
            try {
                return this.callable.call();
            } catch (final RuntimeException rex) {
                throw rex;
            } catch (final Exception ex) {
                throw new UncheckedExecutionException(ex);
            }
        }
    }


    private static final class MemoizingSupplier<T>
    implements Supplier<T>, Serializable {
        private static final long serialVersionUID = -7096612043821744330L;

        private final AtomicReference<T> ref = new AtomicReference<>(null);
        private final Supplier<? extends T> newValue;

        public MemoizingSupplier(final Supplier<? extends T> newValue) {
            this.newValue = Preconditions.checkNotNull(newValue);
        }

        @Override
        public T get() {
            /* We only set the value in one direction: from null to
             * initialized. Check first to avoid calling Supplier.get() if
             * that transition has already happened.
             */
            if (this.ref.get() == null) {
                final T suppliedNewValue = this.newValue.get();
                Preconditions.checkNotNull(suppliedNewValue, "supplied value is null");
                /* In case multiple threads get into the "if" concurrently,
                 * compareAndSet() is the backstop.
                 */
                this.ref.compareAndSet(null, suppliedNewValue);
            }
            return this.ref.get();
        }
    }

}
