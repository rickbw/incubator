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
package rickbw.incubator.random;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;


/**
 * Encapsulates different means of obtaining {@link Random} instances using
 * {@link Supplier}s. Injecting a Supplier of Random instead of just Random
 * allows, in particular, the substitution of {@link ThreadLocalRandom} for
 * Random, with the consumer being none the wiser.
 */
public final class Randoms {

    /**
     * Keep this instance around as a singleton, to avoid unnecessary memory
     * allocations, and to let the default equals() implementation work.
     */
    private static final Supplier<ThreadLocalRandom> threadLocalSupplier = new Supplier<ThreadLocalRandom>() {
        @Override
        public ThreadLocalRandom get() {
            return ThreadLocalRandom.current();
        }
    };


    /**
     * Create a {@link Supplier} for the given {@link Random}. It will be
     * equal to any other {@code Supplier} created via this method for the
     * same {@code Random}.
     */
    public static Supplier<Random> from(final Random rand) {
        return Suppliers.ofInstance(rand);
    }

    /**
     * Create a {@link Supplier} for a {@link Random} with the given initial
     * seed. The Supplier will be "memoized", such that the same Random will
     * be returned from {@link Supplier#get()} every time. The Supplier will
     * not considered equal to any other Supplier, even one created with the
     * same seed, because a call to {@link Random#setSeed(long)} would cause
     * the Randoms returns by the two Suppliers to return different sequences
     * of values.
     */
    public static Supplier<Random> fromSeed(final long seed) {
        return from(new Random(seed));
    }

    /**
     * Create a {@link Supplier} for a {@link Random} with a default initial
     * seed, as if chosen by the default constructor of that class. The
     * Supplier will be "memoized", such that the same Random will be returned
     * from {@link Supplier#get()} every time. The Supplier will not
     * considered equal to any other Supplier, even one created with the same
     * seed, because a call to {@link Random#setSeed(long)} would cause the
     * Randoms returns by the two Suppliers to return different sequences
     * of values.
     */
    public static Supplier<Random> fromDefaultSeed() {
        return from(new Random());
    }

    public static Supplier<ThreadLocalRandom> threadLocal() {
        return threadLocalSupplier;
    }

    private Randoms() {
        // prevent instantiation
    }

}
