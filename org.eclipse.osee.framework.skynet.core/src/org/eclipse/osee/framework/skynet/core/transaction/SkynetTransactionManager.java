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

package org.eclipse.osee.framework.skynet.core.transaction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.core.KeyedLevelManager;
import org.eclipse.osee.framework.db.connection.core.transaction.DbTransactionEventCompleted;
import org.eclipse.osee.framework.db.connection.core.transaction.IDbTransactionEvent;
import org.eclipse.osee.framework.db.connection.core.transaction.IDbTransactionListener;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.jdk.core.util.ThreadKeyLocal;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;

/**
 * @author Robert A. Fisher
 */
public class SkynetTransactionManager {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(SkynetTransactionManager.class);
   private static final SkynetTransactionManager transactionManager = new SkynetTransactionManager();
   private ThreadKeyLocal<Branch, SkynetTransactionBuilder> transactionBuilder;
   private ThreadKeyLocal<Branch, LevelManager> levelManager;

   public static SkynetTransactionManager getInstance() {
      return transactionManager;
   }

   private SkynetTransactionManager() {
      this.transactionBuilder = new ThreadKeyLocal<Branch, SkynetTransactionBuilder>();

      this.levelManager = new ThreadKeyLocal<Branch, LevelManager>() {
         @Override
         protected LevelManager initialValue() {
            return new LevelManager();
         }

         @Override
         public void remove(Branch branch) {
            if (get(branch) != null) {
               ConnectionHandler.removeDbTransactionListener(get(branch));
            }
            super.remove(branch);
         }

         @Override
         public void set(Branch branch, LevelManager value) {
            if (value != get(branch) && get(branch) != null) {
               ConnectionHandler.removeDbTransactionListener(get(branch));
            }
            super.set(branch, value);
         }
      };
   }

   protected void startBatchLevel(Object key, Branch branch) throws OseeDataStoreException {
      levelManager.get(branch).startTransactionLevel(branch, key);
   }

   protected void setBatchLevelAsSuccessful(Object key, Branch branch) throws SQLException {
      levelManager.get(branch).setTransactionLevelSuccess(key);
   }

   protected void endBatchLevel(Object key, Branch branch) {
      LevelManager manager = levelManager.get(branch);
      if (true != manager.isTransactionLevelSuccess(key)) {
         manager.requestRollback();
      }

      try {
         manager.endTransactionLevel(key);
      } catch (Exception ex) {
         OseeLog.log(SkynetActivator.class, Level.SEVERE, ex);
      }
   }

   protected void startBuilder(Branch branch, IProgressMonitor monitor) throws OseeDataStoreException {
      transactionBuilder.set(branch, new SkynetTransactionBuilder(branch, monitor));
   }

   private void endBuilder(boolean rollback, Branch branch) throws OseeDataStoreException {
      try {
         SkynetTransactionBuilder builder = transactionBuilder.get(branch);
         if (builder != null) {
            if (rollback) {
               builder.terminateBatch();
            } else {
               builder.execute(); // Execute the batch
            }
         }
      } finally {
         // Clear the builder so a new batch can be started\
         // RAF is this going to happen before we get a chance to kick off the events?
         transactionBuilder.set(branch, null);
      }
   }

   /**
    * Check if the current thread has a batch in progress.
    */
   public boolean isInBatch(Branch branch) {
      return transactionBuilder.get(branch) != null || levelManager.get(branch).inLevel();
   }

   /**
    * Retrieves the <code>TransactionBuilder</code> for the current thread. The returned value may be null.<br/><br/>
    * <b>This method is only made available for the internal Skynet system</b>.
    */
   public SkynetTransactionBuilder getTransactionBuilder(Branch branch) {
      return transactionBuilder.get(branch);
   }

   private final class LevelManager extends KeyedLevelManager implements IDbTransactionListener {
      private Branch branch = null;
      private Collection<SkynetTransaction> transactions = new ArrayList<SkynetTransaction>();
      private boolean rollback;

      public LevelManager() {
      }

      @Override
      protected void onInitialEntry() throws OseeDataStoreException {
         super.onInitialEntry();
         this.rollback = false;
         ConnectionHandler.addDbTransactionListener(this);
      }

      public void startTransactionLevel(Branch branch, Object key) throws OseeDataStoreException {
         this.branch = branch;
         boolean initialEntry = super.startTransactionLevel(key);

         if (branch == null) throw new IllegalArgumentException("branch can not be null");
         if (!initialEntry && this.branch != branch) throw new IllegalArgumentException(
               "A leveled batch has already been started for a different branch");

         if (initialEntry) {
            startBuilder(branch, new NullProgressMonitor());
         }
      }

      @Override
      protected void onExitLevelError() {
         requestRollback();
      }

      @Override
      protected void onLastExit() throws OseeDataStoreException {
         try {
            if (!rollback) {
               SkynetTransactionBuilder builder = getTransactionBuilder(branch);
               if (builder != null) {
                  transactions.addAll(builder.getTransactions());
               }
            }
            endBuilder(rollback, branch);
         } finally {
            ConnectionHandler.removeDbTransactionListener(this);
            this.branch = null;
         }
      }

      public void onEvent(IDbTransactionEvent event) {
         if (event instanceof DbTransactionEventCompleted) {
            DbTransactionEventCompleted eventCompleted = (DbTransactionEventCompleted) event;

            if (eventCompleted.isCommitted()) {
               for (SkynetTransaction transaction : transactions) {
                  transaction.kickEvents();
               }
            }
            transactions.clear();
         }
      }

      public void requestRollback() {
         rollback = true;
      }
   }
}