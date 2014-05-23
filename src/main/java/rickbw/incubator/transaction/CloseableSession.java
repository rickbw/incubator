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

import java.sql.Connection;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


/**
 * A wrapper for a Hibernate {@link Session} that supports the Java 7
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
 */
public class CloseableSession implements AutoCloseable {

    private final Session delegate;


    /**
     * Open a brand new session, as with {@link SessionFactory#openSession()}.
     *
     * @throws  HibernateException  if an error occurs.
     */
    public static CloseableSession openSession(final SessionFactory sessionFactory) {
        final Session session = sessionFactory.openSession();
        return from(session);
    }

    /**
     * Open a brand new session, as with {@link SessionFactory#openSession(Connection)}.
     *
     * @throws  HibernateException  if an error occurs.
     */
    public static CloseableSession openSession(
            final SessionFactory sessionFactory,
            final Connection connection) {
        final Session session = sessionFactory.openSession(connection);
        return from(session);
    }

    /**
     * Open a brand new session, as with {@link SessionFactory#openSession(Interceptor)}.
     *
     * @throws  HibernateException  if an error occurs.
     */
    public static CloseableSession openSession(
            final SessionFactory sessionFactory,
            final Interceptor interceptor) {
        final Session session = sessionFactory.openSession(interceptor);
        return from(session);
    }

    /**
     * Open a brand new session, as with
     * {@link SessionFactory#openSession(Connection, Interceptor)}.
     *
     * @throws  HibernateException  if an error occurs.
     */
    public static CloseableSession openSession(
            final SessionFactory sessionFactory,
            final Connection connection,
            final Interceptor interceptor) {
        final Session session = sessionFactory.openSession(connection, interceptor);
        return from(session);
    }

    /**
     * Get a wrapper for the current session, obtained as with
     * {@link SessionFactory#getCurrentSession()}.
     * Unlike all of the other sessions returned by the factory methods of
     * this class, those returned by this method will not be automatically
     * closed. That is, {@link #close()} will do nothing.
     *
     * @throws  HibernateException  if an error occurs.
     */
    public static CloseableSession getCurrentSession(final SessionFactory sessionFactory) {
        final Session session = sessionFactory.getCurrentSession();
        return new CloseableSession(session) {
            @Override
            public void close() {
                // don't close shared session
            }
        };
    }

    public static CloseableSession from(final Session session) {
        return new CloseableSession(session);
    }

    /**
     * Start a new session with the given {@link EntityMode} in effect, as
     * by calling {@link Session#getSession(EntityMode)}.
     *
     * @throws  HibernateException  if an error occurs.
     */
    public CloseableSession getSession(final EntityMode mode) {
        final Session session = this.delegate.getSession(mode);
        return from(session);
    }

    /**
     * @throws  HibernateException  if an error occurs.
     */
    public CloseableTransaction beginTransaction() {
        final Transaction tx = this.delegate.beginTransaction();
        return new CloseableTransaction(tx);
    }

    /**
     * Get the underlying Hibernate {@link Session}, in order to perform
     * additional actions.
     *
     * Unfortunately, it's not possible for CloseableSession to implement
     * Session directly, because the return types of {@link Session#close()}
     * and {@link AutoCloseable#close()} are not compatible.
     */
    public Session getDelegateSession() {
        return this.delegate;
    }

    /**
     * @throws  HibernateException  if an error occurs.
     */
    @Override
    public void close() {
        this.delegate.close();
    }

    private CloseableSession(final Session delegate) {
        this.delegate = delegate;
        assert null != this.delegate;
    }

}
