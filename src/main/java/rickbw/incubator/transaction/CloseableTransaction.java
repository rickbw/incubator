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

package rickbw.incubator.transaction;

import javax.transaction.Synchronization;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import com.google.common.base.Preconditions;


/**
 * A wrapper for a Hibernate {@link Transaction} that supports the Java 7
 * try-with-resources structure, for safer operation.
 *
 * For example:
 * <pre>
 *   final SessionFactory mySessionFactory = ...;
 *   try (CloseableSession session = CloseableSession.openSession(mySessionFactory)) {
 *       try (CloseableTransaction tx = session.beginTransaction()) {
 *           // Do stuff...
 *           tx.commit();
 *       }   // If we get here before commit() is called, tx will roll back.
 *   }   // When we get here, session will close automatically.
 * <pre>
 *
 * CloseableTransactions are started with {@link CloseableSession#beginTransaction()}.
 */
public final class CloseableTransaction implements Transaction, AutoCloseable {

    private final Transaction delegate;
    private boolean isCommitted = false;


    @Override
    public void commit() {
        this.delegate.commit();
        this.isCommitted = true;
    }

    @Override
    public void rollback() {
        this.delegate.rollback();
    }

    @Override
    public void begin() {
        this.delegate.begin();
        this.isCommitted = false;
    }

    @Override
    public boolean wasRolledBack() {
        return this.delegate.wasRolledBack();
    }

    @Override
    public boolean wasCommitted() {
        return this.delegate.wasCommitted();
    }

    @Override
    public boolean isActive() {
        return this.delegate.isActive();
    }

    @Override
    public void registerSynchronization(final Synchronization synchronization) {
        this.delegate.registerSynchronization(synchronization);
    }

    @Override
    public void setTimeout(final int seconds) {
        this.delegate.setTimeout(seconds);
    }

    /**
     * If this transaction hasn't yet been committed (see {@link #commit()}),
     * roll back, as with {@link #rollback()}.
     *
     * @throws  HibernateException  if an error occurs.
     */
    @Override
    public void close() {
        if (!this.isCommitted) {
            rollback();
        }
    }

    /**
     * This class doesn't provide any public constructors or factory methods,
     * because it's unsafe to wrap a {@link Transaction} when some statements
     * may already have been executed within it. Instead, this constructor is
     * provided for the sole use of {@link CloseableSession}.
     */
    /*package*/ CloseableTransaction(final Transaction delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

}
