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

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The value object, from the point of view of Hibernate. Application code
 * should never see this. It should be 100% idiomatic Hibernate: public
 * getters and setters, nullable fields, etc. We can use annotations instead
 * of XML without fear of tangling, since this class is purely a Hibernate
 * implementation artifact.
 */
@Entity
@Table(name = "PRODUCTS_ALL")
/*package*/ class ProductVo {

    private String serialNumber = null;
    private long id = 0L;
    private String someRequiredAttribute = null;
    private String someOptionalAttribute = null;


    /**
     * Hibernate requires a no-argument constructor. Hibernate's the only
     * caller, so this is the only constructor we need.
     */
    public ProductVo() {
        // nothing to do
    }

    /**
     * The "business key" for this type. It determines entity identity from
     * the perspective of the application.
     */
    @Column(name = "SERIAL_NUMBER", unique = true, nullable = false)
    public String getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * The database key for the entity: likely a synthetic value. May or may
     * not be meaningful to the application in any way.
     */
    @Id
    @GeneratedValue
    @Column(name = "PRODUCT_ID", unique = true, nullable = false)
    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    /**
     * An attribute that is constrained to be non-null in the database.
     */
    @Column(name = "REQUIRED_ATTR", nullable = false)
    public String getSomeRequiredAttribute() {
        return this.someRequiredAttribute;
    }

    public void setSomeRequiredAttribute(final String someRequiredAttribute) {
        this.someRequiredAttribute = someRequiredAttribute;
    }

    /**
     * An attribute that is allowed to be null in the database.
     */
    @Column(name = "OPTIONAL_ATTR", nullable = true)
    public String getSomeOptionalAttribute() {
        return this.someOptionalAttribute;
    }

    public void setSomeOptionalAttribute(final String someOptionalAttribute) {
        this.someOptionalAttribute = someOptionalAttribute;
    }

    /**
     * The Hibernate documentation recommends that {@code equals()} and
     * {@link #hashCode()} be based on the business key of the type. It
     * maintains just a single copy of an object for each value of that
     * key value in its session cache.
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
        final ProductVo other = (ProductVo) obj;
        return Objects.equals(this.serialNumber, other.serialNumber);
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hashCode(this.serialNumber);
    }

}
