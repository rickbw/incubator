package rickbw.incubator.random;

import java.util.Iterator;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;


/**
 * A presentation of an endless sequence of random values based on
 * {@link Iterable}. This abstraction allows application code to depend solely
 * on Iterable (or {@link Iterator}) rather than on {@link Random}, making it
 * easier to swap out different value-generation schemes. For example, it
 * might be convenient to inject fixed sequences of values for testing
 * purposes.
 *
 * Underlying values are generated by an instance of {@link Random}.
 */
public final class RandomSequence {

    /**
     * TODO: Add support for Java 7 ThreadLocalRandom. One possible approach
     * would be to add a level of indirection, a "Random Provider", one
     * implementation of which would wrap a plain Random and another of which
     * would call <code>ThreadLocalRandom.current()</code>.
     */
    private final Random random;


    public static RandomSequence newSequence() {
        return newSequenceUsing(new Random());
    }

    public static RandomSequence newSequenceWithSeed(final long seed) {
        return newSequenceUsing(new Random(seed));
    }

    public static RandomSequence newSequenceUsing(final Random random) {
        return new RandomSequence(random);
    }

    public FluentIterable<Integer> integers() {
        return this.intIterable;
    }

    public FluentIterable<Integer> integersZeroTo(final int upperBoundExclusive) {
        return new BoundedIntIterable(upperBoundExclusive);
    }

    public FluentIterable<Double> fractions() {
        return this.doubleIterable;
    }

    public FluentIterable<Double> guassians() {
        return this.guassianIterable;
    }

    public FluentIterable<Boolean> booleans() {
        return this.booleanIterable;
    }

    public FluentIterable<Long> longs() {
        return this.longIterable;
    }

    private RandomSequence(final Random random) {
        this.random = Preconditions.checkNotNull(random);
    }


    private static abstract class RandomIterator<T> implements Iterator<T> {
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class BoundedIntIterable extends FluentIterable<Integer> {
        private final int upperBoundExclusive;

        private final RandomIterator<Integer> iterator = new RandomIterator<Integer>() {
            @Override
            public Integer next() {
                return RandomSequence.this.random.nextInt(upperBoundExclusive);
            }
        };

        public BoundedIntIterable(final int upperBoundExclusive) {
            this.upperBoundExclusive = upperBoundExclusive;
            Preconditions.checkArgument(this.upperBoundExclusive > 0, "Upper bound must be > 0");
        }

        @Override
        public Iterator<Integer> iterator() {
            return this.iterator;
        }
    }

    /**
     * TODO: Lazily initialize this (and similar) field(s).
     */
    private final FluentIterable<Integer> intIterable = new FluentIterable<Integer>() {
        private final RandomIterator<Integer> iterator = new RandomIterator<Integer>() {
            @Override
            public Integer next() {
                return RandomSequence.this.random.nextInt();
            }
        };

        @Override
        public Iterator<Integer> iterator() {
            return this.iterator;
        }
    };

    private final FluentIterable<Long> longIterable = new FluentIterable<Long>() {
        private final RandomIterator<Long> iterator = new RandomIterator<Long>() {
            @Override
            public Long next() {
                return RandomSequence.this.random.nextLong();
            }
        };

        @Override
        public Iterator<Long> iterator() {
            return this.iterator;
        }
    };

    private final FluentIterable<Double> doubleIterable = new FluentIterable<Double>() {
        private final RandomIterator<Double> iterator = new RandomIterator<Double>() {
            @Override
            public Double next() {
                return RandomSequence.this.random.nextDouble();
            }
        };

        @Override
        public Iterator<Double> iterator() {
            return this.iterator;
        }
    };

    private final FluentIterable<Double> guassianIterable = new FluentIterable<Double>() {
        private final RandomIterator<Double> iterator = new RandomIterator<Double>() {
            @Override
            public Double next() {
                return RandomSequence.this.random.nextGaussian();
            }
        };

        @Override
        public Iterator<Double> iterator() {
            return this.iterator;
        }
    };

    private final FluentIterable<Boolean> booleanIterable = new FluentIterable<Boolean>() {
        private final RandomIterator<Boolean> iterator = new RandomIterator<Boolean>() {
            @Override
            public Boolean next() {
                return RandomSequence.this.random.nextBoolean();
            }
        };

        @Override
        public Iterator<Boolean> iterator() {
            return this.iterator;
        }
    };

}