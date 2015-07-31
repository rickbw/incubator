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
package rickbw.incubator.counter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Preconditions;


/**
 * A fast, reusable Hadoop-style counter.
 */
public final class Counter {

    private final String name;
    private final AtomicLong value;


    public Counter(final String name) {
        this(name, 0L);
    }

    public Counter(final String name, final long initialValue) {
        Preconditions.checkArgument(initialValue >= 0, "Negative initial value");
        this.name = Objects.requireNonNull(name);
        this.value = new AtomicLong(initialValue);
    }

    public String name() {
        return this.name;
    }

    public long get() {
        return this.value.get();
    }

    public void increment() {
        incrementAndGet();
    }

    public void increment(final long delta) {
        incrementAndGet(delta);
    }

    public long incrementAndGet() {
        return this.value.incrementAndGet();
    }

    public long incrementAndGet(final long delta) {
        Preconditions.checkArgument(delta >= 0, "Negative delta");
        return this.value.addAndGet(delta);
    }

    public long getAndIncrement() {
        return this.value.getAndIncrement();
    }

    public long getAndIncrement(final long delta) {
        Preconditions.checkArgument(delta >= 0, "Negative delta");
        return this.value.getAndAdd(delta);
    }

}
