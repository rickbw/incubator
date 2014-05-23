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
package rickbw.incubator.hibernate.example;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;


/**
 * This is the view of a "product" that the application sees. It's immutable,
 * and it provides application-relevant {@link #equals(Object)} and
 * {@link #hashCode()} implementations.
 */
public final class ImmutableProduct {

    private final ProductVo state;


    /**
     * Applications shouldn't create Instances directly; they should use a
     * {@link Builder}.
     *
     * @see #newBuilder()
     * @see #toBuilder()
     */
    private ImmutableProduct(final ProductVo source) {
        this.state = new ProductVo(source);
        Preconditions.checkArgument(source.getSerialNumber() != null);
        Preconditions.checkArgument(source.getSomeRequiredAttribute() != null);
        // optional attribute may be null
    }

    /**
     * This is how applications will actually create brand new objects, e.g.
     * to insert as new rows into the database.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * This is how applications "modify" existing entities, e.g. to update in
     * the database.
     */
    public Builder toBuilder() {
        return new Builder(this.state);
    }

    /**
     * Expose the "business key" to the application. The result can never be
     * null.
     *
     * The application may not need to see the synthetic database key at all;
     * this example class doesn't expose it.
     */
    public String getSerialNumber() {
        return this.state.getSerialNumber();
    }

    /**
     * Expose a required attribute to the application. The result can never
     * be null.
     */
    public String getSomeRequiredAttribute() {
        return this.state.getSomeRequiredAttribute();
    }

    /**
     * Expose an optional attribute to the application. Prefer Optional to
     * nullable values.
     */
    public Optional<String> getSomeOptionalAttribute() {
        return Optional.fromNullable(this.state.getSomeOptionalAttribute());
    }

    /**
     * From the application perspective, equality is based on all of the
     * exposed attributes. This is important, because the application may
     * have different objects representing different states of the same row
     * in memory at one time, and it must be able to put objects into
     * {@link Set}s.
     */
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
        final ImmutableProduct other = (ImmutableProduct) obj;
        if (!getSerialNumber().equals(other.getSerialNumber())) {
            return false;
        }
        if (!getSomeRequiredAttribute().equals(other.getSomeRequiredAttribute())) {
            return false;
        }
        if (!getSomeOptionalAttribute().equals(other.getSomeOptionalAttribute())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getSerialNumber().hashCode();
        result = prime * result + getSomeRequiredAttribute().hashCode();
        result = prime * result + getSomeOptionalAttribute().hashCode();
        return result;
    }

    /**
     * The database-visible VO doesn't need any toString(), because the
     * application never sees it. This class should have one, though, and it
     * should be based on the application-visible state.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " [serialNumber=" + this.getSerialNumber()
                + ", someRequiredAttribute=" + this.getSomeRequiredAttribute()
                + ", someOptionalAttribute=" + this.getSomeOptionalAttribute()
                + "]";
    }


    /**
     * Since products are immutable from the application's point of view,
     * when the application wants to insert a brand new record, it will
     * construct it with a Builder.
     */
    public static final class Builder {
        private final ProductVo state;

        private Builder() {
            this.state = new ProductVo();
        }

        private Builder(final ProductVo source) {
            this.state = new ProductVo(source);
        }

        public Builder serialNumber(final String serial) {
            this.state.setSerialNumber(serial);
            return this;
        }

        public Builder someRequiredAttribute(final String attribute) {
            this.state.setSomeRequiredAttribute(attribute);
            return this;
        }

        public Builder someOptionalAttribute(final String attribute) {
            this.state.setSomeOptionalAttribute(attribute);
            return this;
        }

        public ImmutableProduct build() {
            return new ImmutableProduct(this.state);
        }
    }
}
