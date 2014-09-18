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
package rickbw.incubator.activity;

import java.lang.reflect.Method;

import com.google.common.base.Preconditions;


/**
 * A unique identifier for an {@link Activity}. It is comprised of several
 * parts:
 * <ol>
 *  <li>A <em>group</em> name. For IDs based on Java identifiers, this will
 *      be a package name.</li>
 *  <li>A <em>type</em> name. For IDs based on Java identifiers, this will be
 *      a simple class name.</li>
 *  <li>An <em>activity</em> name. For IDs based on Java identifiers, this
 *      will be a method name.</li>
 * </ol>
 */
public final class ActivityId {

    private final String groupName;
    private final String typeName;
    private final String activityName;

    /**
     * Keep this around to speed up equals() and toString().
     */
    private final String cachedComboString;
    /**
     * Keep this around to speed up equals() and hashCode().
     */
    private final int cachedHashCode;


    /**
     * @throws NullPointerException     If any argument is null.
     * @throws IllegalArgumentException If any argument is empty.
     */
    public static ActivityId of(
            final String groupName,
            final String typeName,
            final String activityName) {
        return new ActivityId(groupName, typeName, activityName);
    }

    /**
     * @throws NullPointerException     If the argument is null.
     */
    public static ActivityId forMethod(final Method method) {
        return forMethodOfClass(method.getDeclaringClass(), method);
    }

    /**
     * @throws NullPointerException     If any argument is null.
     */
    public static ActivityId forMethodOfClass(final Class<?> klazz, final Method method) {
        final String methodName = method.getName();
        return forMethodName(klazz, methodName);
    }

    /**
     * @throws NullPointerException     If any argument is null.
     * @throws IllegalArgumentException If the string is empty.
     */
    public static ActivityId forMethodName(
            final Class<?> klazz,
            final String methodName) {
        return new ActivityId(klazz.getPackage().getName(), klazz.getSimpleName(), methodName);
    }

    // TODO: Add "parse" factory method

    public String getGroupName() {
        return this.groupName;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public String getActivityName() {
        return this.activityName;
    }

    @Override
    public String toString() {
        return this.cachedComboString;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ActivityId other = (ActivityId) obj;
        if (this.cachedHashCode != other.cachedHashCode) {
            /* This comparison isn't necessary for correctness, but it means
             * that we frequently won't have to do any String comparison.
             */
            return false;
        }
        return this.cachedComboString.equals(other.cachedComboString);
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    /**
     * @throws NullPointerException     If any argument is null.
     * @throws IllegalArgumentException If any argument is empty.
     */
    private ActivityId(
            final String groupName,
            final String typeName,
            final String activityName) {
        Preconditions.checkArgument(groupName.length() > 0);
        this.groupName = groupName;

        Preconditions.checkArgument(typeName.length() > 0);
        this.typeName = typeName;

        Preconditions.checkArgument(activityName.length() > 0);
        this.activityName = activityName;

        this.cachedComboString = this.groupName  + '.' + this.typeName + '.' + this.activityName;
        this.cachedHashCode = this.cachedComboString.hashCode();
    }

}
