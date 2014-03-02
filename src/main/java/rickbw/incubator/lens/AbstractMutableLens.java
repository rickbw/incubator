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


/**
 * A base class for {@link Lens}es of containers that are mutable. This class
 * uses a Template Method pattern to isolate a deep-copying step and an update
 * step.
 */
public abstract class AbstractMutableLens<T, M> implements Lens<T, M> {

    @Override
    public final T set(final T container, final M member) {
        final T cloned = deepCopy(container);
        modifyCopy(container, member);
        return cloned;
    }

    /**
     * Update the state of the given container, which is a clone of the one
     * passed into {@link #set(Object, Object)}.
     */
    protected abstract void modifyCopy(T copiedContainer, M newMember);

    /**
     * @return  a deep copy of the given object, created in a manner specific
     *          to the concrete subclass of this class.
     */
    protected abstract T deepCopy(final T input);

}
