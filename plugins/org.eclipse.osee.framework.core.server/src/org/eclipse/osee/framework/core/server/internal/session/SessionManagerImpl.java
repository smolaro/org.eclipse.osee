/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.server.internal.session;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.eclipse.osee.cache.admin.Cache;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.data.OseeCredential;
import org.eclipse.osee.framework.core.data.OseeSessionGrant;
import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.server.IAuthenticationManager;
import org.eclipse.osee.framework.core.server.ISession;
import org.eclipse.osee.framework.core.server.ISessionManager;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;

/**
 * @author Roberto E. Escobar
 */
public final class SessionManagerImpl implements ISessionManager {

   private final String serverId;
   private final SessionFactory sessionFactory;
   private final ISessionQuery sessionQuery;
   private final Cache<String, Session> sessionCache;
   private final IAuthenticationManager authenticationManager;
   private final WriteDataAccessor<Session> storeDataAccessor;

   public SessionManagerImpl(String serverId, SessionFactory sessionFactory, ISessionQuery sessionQuery, Cache<String, Session> sessionCache, IAuthenticationManager authenticationManager, WriteDataAccessor<Session> storeDataAccessor) {
      this.serverId = serverId;
      this.sessionFactory = sessionFactory;
      this.sessionQuery = sessionQuery;
      this.sessionCache = sessionCache;
      this.authenticationManager = authenticationManager;
      this.storeDataAccessor = storeDataAccessor;
   }

   @Override
   public OseeSessionGrant createSession(final OseeCredential credential) throws OseeCoreException {
      Conditions.checkNotNull(credential, "credential");
      OseeSessionGrant sessionGrant = null;
      final String newSessionId = GUID.create();
      boolean isAuthenticated = authenticationManager.authenticate(credential);
      if (isAuthenticated) {
         final IUserToken userToken = authenticationManager.asUserToken(credential);

         Callable<Session> callable = new Callable<Session>() {

            @Override
            public Session call() throws Exception {

               String managedByServerId = serverId;
               Date creationDate = GlobalTime.GreenwichMeanTimestamp();
               Session session =
                  sessionFactory.createNewSession(newSessionId, userToken.getUserId(), creationDate, managedByServerId,
                     credential.getVersion(), credential.getClientMachineName(), credential.getClientAddress(),
                     credential.getPort(), creationDate, StorageState.CREATED.name().toLowerCase());

               // if the user is BootStrap we do not want to insert into database since tables may not exist
               if (!SystemUser.BootStrap.equals(userToken)) {
                  storeDataAccessor.create(Collections.singleton(session));
               }

               return session;
            }
         };

         Session session = sessionCache.get(newSessionId, callable);

         sessionGrant = sessionFactory.createSessionGrant(session, userToken, authenticationManager.getProtocol());

      }
      return sessionGrant;
   }

   @Override
   public void releaseSession(String sessionId) throws OseeCoreException {
      releaseSessionImmediate(sessionId);
   }

   @Override
   public void updateSessionActivity(String sessionId, String interactionName) throws OseeCoreException {
      Conditions.checkNotNull(sessionId, "sessionId");
      Session session = getSessionById(sessionId);
      Conditions.checkNotNull(session, "Session", "for id [%s]", sessionId);
      session.setLastInteractionDetails(Strings.isValid(interactionName) ? interactionName : "unknown");
      session.setLastInteractionDate(GlobalTime.GreenwichMeanTimestamp());
      storeDataAccessor.update(Collections.singleton(session));
   }

   @Override
   public Session getSessionById(String sessionId) throws OseeCoreException {
      Conditions.checkNotNull(sessionId, "sessionId");
      return sessionCache.get(sessionId);
   }

   @Override
   public Collection<ISession> getSessionByClientAddress(String clientAddress) throws OseeCoreException {
      Conditions.checkNotNull(clientAddress, "clientAddress");
      Set<ISession> sessions = new HashSet<ISession>();
      for (Session session : sessionCache.getAll()) {
         if (session.getClientAddress().equals(clientAddress)) {
            sessions.add(session);
         }
      }
      return sessions;
   }

   @Override
   public Collection<ISession> getSessionsByUserId(String userId, boolean includeNonServerManagedSessions) throws OseeCoreException {
      Conditions.checkNotNull(userId, "userId");
      Collection<ISession> toReturn = new HashSet<ISession>();
      for (ISession session : getAllSessions(includeNonServerManagedSessions)) {
         if (session.getUserId().equals(userId)) {
            toReturn.add(session);
         }
      }
      return toReturn;
   }

   @Override
   public Collection<ISession> getAllSessions(boolean includeNonServerManagedSessions) throws OseeCoreException {
      Collection<ISession> toReturn = new HashSet<ISession>();
      for (Session session : sessionCache.getAll()) {
         toReturn.add(session);
      }
      if (includeNonServerManagedSessions) {
         ISessionCollector collector = new DefaultSessionCollector(serverId, sessionFactory, toReturn);
         sessionQuery.selectNonServerManagedSessions(collector);
      }
      return toReturn;
   }

   @Override
   public void releaseSessionImmediate(String... sessionIds) throws OseeCoreException {
      Conditions.checkNotNull(sessionIds, "sessionIds");

      Set<Session> sessions = new HashSet<Session>();
      for (String sessionId : sessionIds) {
         Session session = getSessionById(sessionId);
         if (session != null) {
            sessions.add(session);
         }
      }

      if (!sessions.isEmpty()) {
         storeDataAccessor.delete(sessions);
      }
      sessionCache.invalidate(Arrays.asList(sessionIds));
   }

}
