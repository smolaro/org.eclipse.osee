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
package org.eclipse.osee.framework.skynet.core.usage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;

/**
 * @author Robert A. Fisher
 */
public class UsageLog {
   private static final String INSERT_TO_LOG =
         "INSERT INTO osee_usage (user_id, time, event_id, details) VALUES (?,?,?,?)";
   private static UsageLog instance = null;

   private final boolean usageLoggingEnabled;
   private Collection<UsageEntry> log;
   private int exceptionCount;
   private int userId;

   private UsageLog() {
      this.usageLoggingEnabled = OseeProperties.getInstance().isUsageLoggingEnabled();
      this.log = new LinkedList<UsageEntry>();
      this.exceptionCount = 0;

      new Kicker(this).start();
   }

   public static boolean isInstantiated() {
      return instance != null;
   }

   public static UsageLog getInstance() {
      if (instance == null) {
         instance = new UsageLog();
         instance.userId = -1;
         try {
            instance.userId = SkynetAuthentication.getUser().getArtId();
         } catch (Exception ex) {
            instance.log.add(new ExceptionEntry(ex));
         }
      }
      return instance;
   }

   public void addEntry(UsageEntry entry) {
      if (usageLoggingEnabled) {
         // Exceptions can spiral out of control, only log first few
         if (entry instanceof ExceptionEntry) {
            if (exceptionCount > 9) return;
            exceptionCount++;
         }

         log.add(entry);
      }
   }

   public void writeOutLog() throws SQLException {
      if (log.isEmpty()) {
         return;
      }

      List<Object[]> data = new ArrayList<Object[]>(log.size());
      for (UsageEntry entry : log) {
         data.add(new Object[] {SQL3DataType.INTEGER, userId, SQL3DataType.TIMESTAMP, entry.getEventTime(),
               SQL3DataType.INTEGER, entry.getEventOrdinal(), SQL3DataType.VARCHAR, entry.getDetails()});
      }

      log.clear();
      ConnectionHandler.runPreparedUpdateBatch(INSERT_TO_LOG, data);
   }

   private static class Kicker extends Thread {
      private static final long ONE_HOUR = 1000 * 60 * 60;
      private final UsageLog log;
      private boolean running;

      public Kicker(final UsageLog log) {
         super();
         setDaemon(true);
         this.log = log;
         this.running = true;
      }

      @Override
      public void run() {
         while (running) {
            try {
               sleep(ONE_HOUR);
            } catch (InterruptedException ex) {
               // If interrupted, then we'll just exit
               running = false;
            }
            try {
               log.writeOutLog();
            } catch (SQLException ex) {
               running = false;
               OseeLog.log(SkynetActivator.class.getName(), Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
         }
      }
   }
}
