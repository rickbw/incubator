package rickbw.incubator.choice;

import com.google.common.base.Function;
import com.google.common.base.Optional;


/**
 * A set of utility methods for dealing with exclusive choices, such as
 * {@link Nothing}, {@link Optional}, and {@link Either}.
 */
public final class Choices {

    public static <F, T> Optional<T> map(
            final Optional<F> from,
            final Function<? super F, ? extends T> func) {
        if (from.isPresent()) {
            final T result = func.apply(from.get());
            return Optional.of(result);
        } else {
            return Optional.absent();
        }
    }

    private Choices() {
        // prevent external instantiation
    }

}
