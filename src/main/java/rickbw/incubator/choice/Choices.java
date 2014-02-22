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
