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

package rickbw.incubator.lens;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Preconditions;


/**
 * A base class for {@link Lens}es of containers that are {@link Cloneable}
 * and mutable. This class takes responsibility for cloning the container, and
 * it delegates the actual get and set to its concrete subclasses using a
 * Template Method pattern.
 */
public abstract class AbstractCloneableLens<T extends Cloneable, M>
extends AbstractMutableLens<T, M> {

    private final Class<? extends T> containerClass;


    protected AbstractCloneableLens(final Class<? extends T> containerClass) {
        this.containerClass = Preconditions.checkNotNull(containerClass);
    }

    /**
     * Unfortunately, Object.clone() isn't public, and {@link Cloneable}
     * doesn't fix that. Therefore, we've got to handle it reflectively.
     */
    @Override
    protected final T deepCopy(final T input) {
        /* Unfortunately, we can't do the clone()-method lookup just once up
         * front, because we might be passed objects of different subclasses
         * of T that have different concrete clone() implementations.
         */
        try {
            final Method cloneMethod = input.getClass().getMethod("clone", (Class[]) null);
            cloneMethod.setAccessible(true);
            final Object cloned = cloneMethod.invoke(input, (Object[]) null);
            return this.containerClass.cast(cloned);
        } catch (final NoSuchMethodException ex) {
            // unreachable: clone() defined in Object
            throw new AssertionError(ex);
        } catch (final IllegalAccessException ex) {
            /* We called setAccessible(true) above. Either that call
             * succeeded, and we won't get IllegalAccessException, or that
             * call will have already thrown SecurityException.
             */
            throw new AssertionError(ex);
        } catch (final InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof CloneNotSupportedException) {
                throw new IllegalStateException(input + " isn't cloneable", cause);
            } else {
                // Unreachable: no other checked exception can be thrown.
                throw new AssertionError(ex);
            }
        }
    }

}
