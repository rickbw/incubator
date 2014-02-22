package rickbw.incubator.choice;

import static rickbw.incubator.choice.Nothing.nothing;

import java.util.concurrent.Callable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;


/**
 * An immutable pair of elements, in which only one is present. This class is
 * the extension of Guava's {@link Optional} from one choice to two.
 *
 * Unless otherwise specified, the methods of this class neither accept null
 * on their argument lists nor return it. Passing a null with throw
 * {@link NullPointerException}.
 */
public abstract class Either<FIRST, SECOND> {
    /* Design rationale: This class is designed for fast deterministic
     * behavior. After its construction, it does not allocate any new objects,
     * and it does not branch. The former avoids the time overhead of
     * allocation and subsequent garbage collection. The latter, along with
     * very short methods in general, makes the code easier for the JIT to
     * optimize.
     */

    /**
     * The value encapsulated by this Either, guaranteed to be present (i.e.
     * {@link Optional#isPresent()}).
     *
     * <b>Design rationale</b>: Why wrap it in an Optional? We want to be able
     * to provide it as an Optional (see e.g. {@link #firstAsOptional()}), so
     * we have the choice to either store it bare and wrap it in that call, or
     * to store it wrapped and unwrap it in e.g. {@link #get()}. Wrapping
     * requires the allocation of a new heap object and subsequent garbage-
     * collection. Unwrapping requires a call to a trivial accessor method,
     * easily inlined by the JVM. Therefore, we wrap up front, when this
     * Either itself is being allocated anyway, and unwrap on demand later.
     */
    private final Optional<?> value;


    /**
     * Return an Either for which {@link #first()} will return the given
     * object and {@link #second()} will throw {@link IllegalStateException}.
     */
    public static <F, S> Either<F, S> first(final F value) {
        return new First<>(Optional.of(value));
    }

    /**
     * Return an Either for which {@link #second()} will return the given
     * object and {@link #first()} will throw {@link IllegalStateException}.
     */
    public static <F, S> Either<F, S> second(final S value) {
        return new Second<>(Optional.of(value));
    }

    /**
     * @return  Either the first argument, if it is present, or the result
     *          supplied by the second.
     */
    public static <F, S> Either<F, S> firstOrSecond(
            final Optional<F> first,
            final Supplier<S> second) {
        if (first.isPresent()) {
            return new First<>(first);
        } else {
            return second(second.get());
        }
    }

    /**
     * An alternative presentation of an {@link Optional}: as an exclusive
     * choice between two objects, one of them representing the state of
     * absence.
     *
     * @return  Either the given value, if it is present, or {@link Nothing}.
     */
    public static <F> Either<F, Nothing> presentOrNothing(final Optional<F> value) {
        if (value.isPresent()) {
            return new First<>(value);
        } else {
            return second(nothing);
        }
    }

    /**
     * Invoke {@link Callable#call()}, and return Either the result or the
     * exception that was thrown.
     *
     * @see #supply(Supplier)
     */
    public static <T> Either<T, Exception> call(final Callable<T> callable) {
        try {
            final T result = callable.call();
            return first(result);
        } catch (final Exception ex) {
            return second(ex);
        }
    }

    /**
     * Invoke {@link Supplier#get()}, and return Either the result or the
     * exception that was thrown.
     *
     * @see #call(Callable)
     */
    public static <T> Either<T, RuntimeException> supply(final Supplier<T> supplier) {
        try {
            final T result = supplier.get();
            return first(result);
        } catch (final RuntimeException ex) {
            return second(ex);
        }
    }

    /**
     * If {@link #isFirstPresent()}, return the result of {@link #first()},
     * wrapped in an {@link Optional}. Otherwise, return
     * {@link Optional#absent()}.
     */
    public Optional<FIRST> firstAsOptional() {
        // Subclass First overrides this implementation
        return Optional.absent();
    }

    /**
     * If {@link #isSecondPresent()}, return the result of {@link #second()},
     * wrapped in an {@link Optional}. Otherwise, return
     * {@link Optional#absent()}.
     */
    public Optional<SECOND> secondAsOptional() {
        // Subclass Second overrides this implementation
        return Optional.absent();
    }

    /**
     * Return true if the "first" element of this Either is the one that is
     * present. This result is equivalent to {@link #firstAsOptional()}
     * followed by {@link Optional#isPresent()}.
     */
    public final boolean isFirstPresent() {
        return firstAsOptional().isPresent();
    }

    /**
     * Return true if the "second" element of this Either is the one that is
     * present. This result is equivalent to {@link #secondAsOptional()}
     * followed by {@link Optional#isPresent()}.
     */
    public final boolean isSecondPresent() {
        return secondAsOptional().isPresent();
    }

    /**
     * Return the "first" element of this Either, assuming it is present.
     *
     * @throws  IllegalStateException   If the first element is not present
     *                                  (and the second is).
     *
     * @see #isFirstPresent()
     */
    public final FIRST first() {
        return firstAsOptional().get();
    }

    /**
     * Return the "second" element of this Either, assuming it is present.
     *
     * @throws  IllegalStateException   If the second element is not present
     *                                  (and the first one is).
     *
     * @see #isSecondPresent()
     */
    public final SECOND second() {
        return secondAsOptional().get();
    }

    /**
     * Get the "first" element of this Either if {@link #isFirstPresent()} or
     * the "second" element if {@link #isSecondPresent()}. In this case, the
     * application must take responsibility for type checking.
     */
    public final Object get() {
        return this.value.get();
    }

    /**
     * Return an Either in which the positions of the "first" and "second"
     * elements are reversed.
     */
    public abstract Either<SECOND, FIRST> swap();

    /**
     * If the "first" element of this Either is present, apply the first of
     * the given {@link Function} to it. Otherwise, if the "second" element is
     * present, apply the second of the given Functions to it.
     *
     * @return  an Either that encapsulates the result of the only Function
     *          that was run.
     */
    public abstract <TOF, TOS> Either<TOF, TOS> map(
            final Function<? super FIRST, ? extends TOF> firstFunc,
            final Function<? super SECOND, ? extends TOS> secondFunc);

    /**
     * Return the result of applying the given function to the "first" element,
     * if it is present. Otherwise, return {@link Optional#absent()}.
     *
     * @param func  Applied to the "first" element, if it is present.
     */
    public <T> Optional<T> mapFirst(final Function<? super FIRST, ? extends T> func) {
        // Subclass First overrides this implementation
        return Optional.absent();
    }

    /**
     * Return the result of applying the given function to the "second"
     * element, if it is present. Otherwise, return {@link Optional#absent()}.
     *
     * @param func  Applied to the "second" element, if it is present.
     */
    public <T> Optional<T> mapSecond(final Function<? super SECOND, ? extends T> func) {
        // Subclass Second overrides this implementation
        return Optional.absent();
    }

    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder(getClass().getSimpleName());
        buf.append('[');
        if (isFirstPresent()) {
            buf.append("first=").append(first());
        } else {
            buf.append("second=").append(second());
        }
        buf.append(']');
        return buf.toString();
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + firstAsOptional().hashCode();
        result = prime * result + secondAsOptional().hashCode();
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Either<?, ?> other = (Either<?, ?>) obj;
        if (isFirstPresent() != other.isFirstPresent()) {
            /* Allows us to short-circuit calling equals() on the "second"
             * element. Assuming comparing booleans is cheaper than calling
             * equals() on some arbitrary object, this way should be faster.
             */
            return false;
        }
        return firstAsOptional().equals(other.firstAsOptional());
    }

    private Either(final Optional<?> value) {
        this.value = value;
        assert null != this.value;
    }


    private static final class First<FIRST, SECOND> extends Either<FIRST, SECOND> {
        public First(final Optional<FIRST> value) {
            super(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<FIRST> firstAsOptional() {
            return (Optional<FIRST>) get();
        }

        @Override
        public Either<SECOND, FIRST> swap() {
            // Eithers are immutable! No choice but to allocate a new one.
            return new Second<>(firstAsOptional());
        }

        @Override
        public <TOF, TOS> Either<TOF, TOS> map(
                final Function<? super FIRST, ? extends TOF> firstFunc,
                final Function<? super SECOND, ? extends TOS> secondFunc) {
            final TOF result = firstFunc.apply(first());
            return first(result);
        }

        @Override
        public <T> Optional<T> mapFirst(final Function<? super FIRST, ? extends T> func) {
            final T result = func.apply(first());
            return Optional.of(result);
        }
    }


    private static final class Second<FIRST, SECOND> extends Either<FIRST, SECOND> {
        public Second(final Optional<SECOND> value) {
            super(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<SECOND> secondAsOptional() {
            return (Optional<SECOND>) get();
        }

        @Override
        public Either<SECOND, FIRST> swap() {
            // Eithers are immutable! No choice but to allocate a new one.
            return new First<>(secondAsOptional());
        }

        @Override
        public <TOF, TOS> Either<TOF, TOS> map(
                final Function<? super FIRST, ? extends TOF> firstFunc,
                final Function<? super SECOND, ? extends TOS> secondFunc) {
            final TOS result = secondFunc.apply(second());
            return second(result);
        }

        @Override
        public <T> Optional<T> mapSecond(final Function<? super SECOND, ? extends T> func) {
            final T result = func.apply(second());
            return Optional.of(result);
        }
    }

}
