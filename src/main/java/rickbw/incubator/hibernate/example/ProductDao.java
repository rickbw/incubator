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

import org.hibernate.SessionFactory;

import com.google.common.base.Preconditions;


public class ProductDao {

    private final SessionFactory sessionFactory;


    public ProductDao(final SessionFactory sessionFactory) {
        this.sessionFactory = Preconditions.checkNotNull(sessionFactory);
    }

    public void insert(final ImmutableProduct product) {
        this.sessionFactory.getCurrentSession().save(product.getHibernateVo());
    }

    public void update(final ImmutableProduct product) {
        this.sessionFactory.getCurrentSession().update(product.getHibernateVo());
    }

    public void upsert(final ImmutableProduct product) {
        final ProductVo merged = (ProductVo) this.sessionFactory.getCurrentSession().merge(product.getHibernateVo());
        product.setUpdatedProductVo(merged);
    }

}
