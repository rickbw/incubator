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
package rickbw.incubator.atomic;

import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;


/**
 * A thread-safe class representing a lazily-initialized value. The value is
 * guaranteed to not be initialized until it is needed, and then only once,
 * regardless of concurrent access.
 */
public final class LazyReference<T> {

    private final AtomicReference<T> ref = new AtomicReference<>(null);
    private final Supplier<? extends T> newValue;


    /**
     * When calling {@link #get()}, the given {@link Supplier} will be used
     * to initialize the value. Note that {@link Supplier#get()} may be called
     * more than once, in the event that there's contention at initialization
     * time.
     */
    public LazyReference(final Supplier<? extends T> newValue) {
        this.newValue = Preconditions.checkNotNull(newValue);
    }

    /**
     * Initialize the reference from the {@link Supplier}, if necessary, and
     * return it.
     */
    public T get() {
        initialize();
        return this.ref.get();
    }

    /**
     * If the value hasn't been initialized yet, do so now.
     */
    public boolean initialize() {
        boolean set = false;
        if (this.ref.get() == null) {
            final T suppliedNewValue = this.newValue.get();
            set = this.ref.compareAndSet(null, suppliedNewValue);
        }
        return set;
    }

}
