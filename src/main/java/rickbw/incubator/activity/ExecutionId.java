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

import java.util.Objects;

import rickbw.incubator.activity.Activity.Execution;


/**
 * An identifier for a particular {@link Activity.Execution}.
 */
public final class ExecutionId {

    private final ActivityId parentId;
    private final Object executionContext;


    /**
     * @param parentId  The ID of the {@link Activity} that spawned the
     *                  {@link Execution} this ID identifies.
     * @param executionContext  A small, immutable piece of context specific
     *                  to one Execution. Could be a name, an identifier for
     *                  an application-specific resource, or something else.
     *                  May not be null.
     */
    public ExecutionId(final ActivityId parentId, final Object executionContext) {
        this.parentId = Objects.requireNonNull(parentId);
        this.executionContext = Objects.requireNonNull(executionContext);
    }

    public ActivityId getParentId() {
        return this.parentId;
    }

    @Override
    public String toString() {
        // TODO: Cache this to speed it up
        return this.executionContext + "@" + this.executionContext;
    }

    @Override
    public boolean equals(final Object obj) {
        // TODO: Speed this up
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExecutionId other = (ExecutionId) obj;
        if (!this.parentId.equals(other.parentId)) {
            return false;
        }
        if (!this.executionContext.equals(other.executionContext)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        // TODO: Speed this up
        final int prime = 31;
        int result = 1;
        result = prime * result + this.executionContext.hashCode();
        result = prime * result + this.parentId.hashCode();
        return result;
    }

}
