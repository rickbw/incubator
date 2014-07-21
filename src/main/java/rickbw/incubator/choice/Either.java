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
public abstract class Either<LEFT, RIGHT> {
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
     * to provide it as an Optional (see e.g. {@link #leftAsOptional()}), so
     * we have the choice to either store it bare and wrap it in that call, or
     * to store it wrapped and unwrap it in e.g. {@link #get()}. Wrapping
     * requires the allocation of a new heap object and subsequent garbage-
     * collection. Unwrapping requires a call to a trivial accessor method,
     * easily inlined by the JVM. Therefore, we wrap up front, when this
     * Either itself is being allocated anyway, and unwrap on demand later.
     */
    private final Optional<?> value;


    /**
     * Return an Either for which {@link #left()} will return the given
     * object and {@link #right()} will throw {@link IllegalStateException}.
     */
    public static <F, S> Either<F, S> left(final F value) {
        return new Left<>(Optional.of(value));
    }

    /**
     * Return an Either for which {@link #right()} will return the given
     * object and {@link #left()} will throw {@link IllegalStateException}.
     */
    public static <F, S> Either<F, S> right(final S value) {
        return new Right<>(Optional.of(value));
    }

    /**
     * @return  Either the "left" argument, if it is present, or the result
     *          supplied by the "right".
     */
    public static <F, S> Either<F, S> leftOrRight(
            final Optional<F> left,
            final Supplier<S> right) {
        if (left.isPresent()) {
            return new Left<>(left);
        } else {
            return right(right.get());
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
            return new Left<>(value);
        } else {
            return right(nothing);
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
            return left(result);
        } catch (final Exception ex) {
            return right(ex);
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
            return left(result);
        } catch (final RuntimeException ex) {
            return right(ex);
        }
    }

    /**
     * If {@link #isLeftPresent()}, return the result of {@link #left()},
     * wrapped in an {@link Optional}. Otherwise, return
     * {@link Optional#absent()}.
     */
    public Optional<LEFT> leftAsOptional() {
        // Subclass Left overrides this implementation
        return Optional.absent();
    }

    /**
     * If {@link #isRightPresent()}, return the result of {@link #right()},
     * wrapped in an {@link Optional}. Otherwise, return
     * {@link Optional#absent()}.
     */
    public Optional<RIGHT> rightAsOptional() {
        // Subclass Right overrides this implementation
        return Optional.absent();
    }

    /**
     * Return true if the "left" element of this Either is the one that is
     * present. This result is equivalent to {@link #leftAsOptional()}
     * followed by {@link Optional#isPresent()}.
     */
    public final boolean isLeftPresent() {
        return leftAsOptional().isPresent();
    }

    /**
     * Return true if the "right" element of this Either is the one that is
     * present. This result is equivalent to {@link #rightAsOptional()}
     * followed by {@link Optional#isPresent()}.
     */
    public final boolean isRightPresent() {
        return rightAsOptional().isPresent();
    }

    /**
     * Return the "left" element of this Either, assuming it is present.
     *
     * @throws  IllegalStateException   If the left element is not present
     *                                  (and the right is).
     *
     * @see #isLeftPresent()
     */
    public final LEFT left() {
        return leftAsOptional().get();
    }

    /**
     * Return the "right" element of this Either, assuming it is present.
     *
     * @throws  IllegalStateException   If the right element is not present
     *                                  (and the left one is).
     *
     * @see #isRightPresent()
     */
    public final RIGHT right() {
        return rightAsOptional().get();
    }

    /**
     * Get the "left" element of this Either if {@link #isLeftPresent()} or
     * the "right" element if {@link #isRightPresent()}. In this case, the
     * application must take responsibility for type checking.
     */
    public final Object get() {
        return this.value.get();
    }

    /**
     * Return an Either in which the positions of the "left" and "right"
     * elements are reversed.
     */
    public abstract Either<RIGHT, LEFT> swap();

    /**
     * If the "left" element of this Either is present, apply the first of
     * the given {@link Function}s to it. Otherwise, if the "right" element is
     * present, apply the second of the given Functions to it.
     *
     * @return  an Either that encapsulates the result of the only Function
     *          that was run.
     */
    public abstract <TOF, TOS> Either<TOF, TOS> map(
            final Function<? super LEFT, ? extends TOF> leftFunc,
            final Function<? super RIGHT, ? extends TOS> rightFunc);

    /**
     * Return the result of applying the given function to the "left" element,
     * if it is present. Otherwise, return {@link Optional#absent()}.
     *
     * @param func  Applied to the "left" element, if it is present.
     */
    public <T> Optional<T> mapLeft(final Function<? super LEFT, ? extends T> func) {
        // Subclass Left overrides this implementation
        return Optional.absent();
    }

    /**
     * Return the result of applying the given function to the "right"
     * element, if it is present. Otherwise, return {@link Optional#absent()}.
     *
     * @param func  Applied to the "right" element, if it is present.
     */
    public <T> Optional<T> mapRight(final Function<? super RIGHT, ? extends T> func) {
        // Subclass Right overrides this implementation
        return Optional.absent();
    }

    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder(getClass().getSimpleName());
        buf.append('[');
        if (isLeftPresent()) {
            buf.append("left=").append(left());
        } else {
            buf.append("right=").append(right());
        }
        buf.append(']');
        return buf.toString();
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftAsOptional().hashCode();
        result = prime * result + rightAsOptional().hashCode();
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
        if (isLeftPresent() != other.isLeftPresent()) {
            /* Allows us to short-circuit calling equals() on the "right"
             * element. Assuming comparing booleans is cheaper than calling
             * equals() on some arbitrary object, this way should be faster.
             */
            return false;
        }
        return leftAsOptional().equals(other.leftAsOptional());
    }

    private Either(final Optional<?> value) {
        this.value = value;
        assert null != this.value;
    }


    private static final class Left<LEFT, RIGHT> extends Either<LEFT, RIGHT> {
        public Left(final Optional<LEFT> value) {
            super(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<LEFT> leftAsOptional() {
            return (Optional<LEFT>) get();
        }

        @Override
        public Either<RIGHT, LEFT> swap() {
            // Eithers are immutable! No choice but to allocate a new one.
            return new Right<>(leftAsOptional());
        }

        @Override
        public <TOF, TOS> Either<TOF, TOS> map(
                final Function<? super LEFT, ? extends TOF> leftFunc,
                final Function<? super RIGHT, ? extends TOS> rightFunc) {
            final TOF result = leftFunc.apply(left());
            return left(result);
        }

        @Override
        public <T> Optional<T> mapLeft(final Function<? super LEFT, ? extends T> func) {
            final T result = func.apply(left());
            return Optional.of(result);
        }
    }


    private static final class Right<LEFT, RIGHT> extends Either<LEFT, RIGHT> {
        public Right(final Optional<RIGHT> value) {
            super(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<RIGHT> rightAsOptional() {
            return (Optional<RIGHT>) get();
        }

        @Override
        public Either<RIGHT, LEFT> swap() {
            // Eithers are immutable! No choice but to allocate a new one.
            return new Left<>(rightAsOptional());
        }

        @Override
        public <TOF, TOS> Either<TOF, TOS> map(
                final Function<? super LEFT, ? extends TOF> leftFunc,
                final Function<? super RIGHT, ? extends TOS> rightFunc) {
            final TOS result = rightFunc.apply(right());
            return right(result);
        }

        @Override
        public <T> Optional<T> mapRight(final Function<? super RIGHT, ? extends T> func) {
            final T result = func.apply(right());
            return Optional.of(result);
        }
    }

}
