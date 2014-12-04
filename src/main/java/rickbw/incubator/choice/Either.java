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
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;


/**
 * An immutable pair of elements, in which only one is present. This class is
 * the extension of Guava's {@link Optional} from one choice to two.
 *
 * This class may be used to represent an exclusive choice between a
 * "successful" result and a "failure" result, as an alternative to throwing
 * an exception. In such cases, by convention, the successful result is should
 * be placed on the right side, and the failure on the left.
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
     * to provide it as an Optional (see e.g. {@link #left()}), so
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
    public static <L, R> Either<L, R> ofLeft(final L value) {
        return new Left<>(Optional.of(value));
    }

    /**
     * Return an Either for which {@link #right()} will return the given
     * object and {@link #left()} will throw {@link IllegalStateException}.
     */
    public static <L, R> Either<L, R> ofRight(final R value) {
        return new Right<>(Optional.of(value));
    }

    /**
     * @return  Either the "right" argument, if it is present, or the result
     *          supplied by the "left".
     */
    public static <L, R> Either<L, R> rightOrLeft(
            final Supplier<L> left,
            final Optional<R> right) {
        if (right.isPresent()) {
            return new Right<>(right);
        } else {
            return ofLeft(left.get());
        }
    }

    /**
     * An alternative presentation of an {@link Optional}: as an exclusive
     * choice between two objects, one of them representing the state of
     * absence.
     *
     * @return  Either the given value, if it is present, or {@link Nothing}.
     */
    public static <R> Either<Nothing, R> presentOrNothing(final Optional<R> value) {
        if (value.isPresent()) {
            return new Right<>(value);
        } else {
            return ofLeft(nothing);
        }
    }

    /**
     * Invoke {@link Callable#call()}, and return Either the result or the
     * exception that was thrown.
     *
     * @throws Error    If the given {@link Callable} throws it.
     *
     * @see #supply(Supplier)
     */
    public static <R> Either<Exception, R> call(final Callable<R> callable) {
        try {
            final R result = callable.call();
            return ofRight(result);
        } catch (final Exception ex) {
            return ofLeft(ex);
        }
    }

    /**
     * Invoke {@link Supplier#get()}, and return Either the result or the
     * exception that was thrown.
     *
     * @throws Error    If the given {@link Callable} throws it.
     *
     * @see #call(Callable)
     */
    public static <R> Either<RuntimeException, R> supply(final Supplier<R> supplier) {
        try {
            final R result = supplier.get();
            return ofRight(result);
        } catch (final RuntimeException ex) {
            return ofLeft(ex);
        }
    }

    /**
     * @return  Return the "left" side of this Either, if it is present, or
     *          otherwise {@link Optional#absent()}.
     */
    public Optional<LEFT> left() {
        // Subclass Left overrides this implementation
        return Optional.absent();
    }

    /**
     * @return  Return the "right" side of this Either, if it is present, or
     *          otherwise {@link Optional#absent()}.
     */
    public Optional<RIGHT> right() {
        // Subclass Right overrides this implementation
        return Optional.absent();
    }

    /**
     * Get the "left" element of this Either if {@code left().isPresent()} or
     * the "right" element if {@code right().isPresent()}. In this case, the
     * application must take responsibility for type checking.
     *
     * @see #left()
     * @see #right()
     * @see Optional#isPresent()
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
     * present, apply the second of the given Functions to it. In either case,
     * the result will be of type <code>TO</code>; return it.
     */
    public final <TO> TO fold(
            final Function<? super LEFT, ? extends TO> leftFunc,
            final Function<? super RIGHT, ? extends TO> rightFunc) {
        @SuppressWarnings("unchecked")
        final TO result = (TO) transform(leftFunc, rightFunc).get();
        return result;
    }

    /**
     * If the "left" element of this Either is present, apply the first of
     * the given {@link Function}s to it. Otherwise, if the "right" element is
     * present, apply the second of the given Functions to it.
     *
     * @return  an Either that encapsulates the result of the only Function
     *          that was run.
     *
     * @see Optional#transform(Function)
     */
    public abstract <TOL, TOR> Either<TOL, TOR> transform(
            final Function<? super LEFT, ? extends TOL> leftFunc,
            final Function<? super RIGHT, ? extends TOR> rightFunc);

    /**
     * Return the result of applying the given function to the "left" element,
     * if it is present. Otherwise, retain the existing value on the right.
     *
     * @see Optional#transform(Function)
     */
    public final <TO> Either<TO, RIGHT> transformLeft(final Function<? super LEFT, ? extends TO> func) {
        return transform(func, Functions.<RIGHT>identity());
    }

    /**
     * Return the result of applying the given function to the "right"
     * element, if it is present. Otherwise, retain the existing value on the
     * left.
     *
     * @see Optional#transform(Function)
     */
    public final <TO> Either<LEFT, TO> transformRight(final Function<? super RIGHT, ? extends TO> func) {
        return transform(Functions.<LEFT>identity(), func);
    }

    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder(getClass().getSimpleName());
        buf.append('(');
        final Optional<LEFT> left = left();
        if (left.isPresent()) {
            buf.append("left=").append(left.get());
        } else {
            final Optional<RIGHT> right = right();
            assert right.isPresent();
            buf.append("right=").append(right.get());
        }
        buf.append(')');
        return buf.toString();
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + left().hashCode();
        result = prime * result + right().hashCode();
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
        /* One of these comparisons will be against Optional.absent(), and
         * will thus be very fast:
         */
        if (!left().equals(other.left())) {
            return false;
        }
        if (!right().equals(other.right())) {
            return false;
        }
        return true;
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
        public Optional<LEFT> left() {
            return (Optional<LEFT>) get();
        }

        @Override
        public Either<RIGHT, LEFT> swap() {
            // Eithers are immutable! No choice but to allocate a new one.
            return new Right<>(left());
        }

        @Override
        public <TOL, TOR> Either<TOL, TOR> transform(
                final Function<? super LEFT, ? extends TOL> leftFunc,
                final Function<? super RIGHT, ? extends TOR> rightFunc) {
            final TOL result = leftFunc.apply(left().get());
            return ofLeft(result);
        }
    }


    private static final class Right<LEFT, RIGHT> extends Either<LEFT, RIGHT> {
        public Right(final Optional<RIGHT> value) {
            super(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<RIGHT> right() {
            return (Optional<RIGHT>) get();
        }

        @Override
        public Either<RIGHT, LEFT> swap() {
            // Eithers are immutable! No choice but to allocate a new one.
            return new Left<>(right());
        }

        @Override
        public <TOL, TOR> Either<TOL, TOR> transform(
                final Function<? super LEFT, ? extends TOL> leftFunc,
                final Function<? super RIGHT, ? extends TOR> rightFunc) {
            final TOR result = rightFunc.apply(right().get());
            return ofRight(result);
        }
    }

}
