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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;


/**
 * Iterates over an underlying sequence of elements in a random order.
 */
public abstract class Shuffler<E> extends FluentIterable<E> {

    private final Iterable<E> elements;


    public static <E> Shuffler<E> from(final Iterable<E> elements) {
        if (elements instanceof List<?> && elements instanceof RandomAccess) {
            return new RandomAccessListShuffler<>((List<E>) elements);
        } else {
            return null; // TODO
        }
    }

    public static <E extends Comparable<? super E>> Shuffler<E> fromRange(
            final Range<E> range,
            final DiscreteDomain<E> domain) {
        // ATTN: ContiguousSet does not copy all of the elements of the Range!
        final ContiguousSet<E> collectionView = ContiguousSet.create(range, domain);
        return from(collectionView);
    }

    private Shuffler(final Iterable<E> elements) {
        this.elements = Objects.requireNonNull(elements);
    }


    private static final class RandomAccessListShuffler<E> extends Shuffler<E> {
        public RandomAccessListShuffler(final List<E> elements) {
            super(elements);
            Preconditions.checkArgument(
                    elements instanceof RandomAccess,
                    "%s not RandomAccess", elements.getClass());
        }

        @Override
        public Iterator<E> iterator() {
            /* The following looks like recursion, but it's actually not,
             * because fromRange() doesn't ever create a RandomAccess List
             * internally.
             */
            final Shuffler<Integer> indexes = fromRange(
                    Range.closedOpen(0, Iterables.size(super.elements)),
                    DiscreteDomain.integers());
            return indexes.transform(new Function<Integer, E>() {
                @Override
                public E apply(final Integer input) {
                    return Iterables.get(RandomAccessListShuffler.super.elements, input);
                }
            }).iterator();
        }
    }

}
