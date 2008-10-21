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

package org.eclipse.osee.framework.skynet.core.artifact;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.OseeConnection;
import org.eclipse.osee.framework.db.connection.OseeDbConnection;
import org.eclipse.osee.framework.db.connection.core.transaction.AbstractDbTxTemplate;
import org.eclipse.osee.framework.db.connection.exception.ConflictDetectionException;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.change.ModificationType;
import org.eclipse.osee.framework.skynet.core.change.TxChange;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.event.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionDetailsType;

/**
 * Commits gammaIds from a Source branch into a destination branch.
 * 
 * @author Jeff C. Phillips
 */
class CommitJob extends Job {
   //destination branch id, source branch id
   private static final String INSERTION =
         "INSERT INTO osee_txs(transaction_id, gamma_id, mod_type, tx_current) SELECT ?, tx1.gamma_id, tx1.mod_type, CASE WHEN tx1.mod_type = 3 THEN " + TxChange.DELETED.getValue() + " WHEN tx1.mod_type = 5 THEN " + TxChange.ARTIFACT_DELETED.getValue() + " ELSE " + TxChange.CURRENT.getValue() + " END ";
   private static final String UPDATE =
         "UPDATE osee_txs tx2 set tx_current = 0 WHERE (tx2.transaction_id, tx2.gamma_id) in (SELECT tx1.transaction_id, tx1.gamma_id ";
   private static final String UPDATE_CURRENT_COMMIT_ATTRIBUTES =
         UPDATE + "FROM osee_txs tx1, osee_tx_details td2, osee_attribute at3, osee_txs tx4, osee_tx_details td5, osee_attribute at6 WHERE tx1.transaction_id = td2.transaction_id AND td2.branch_id = ? AND tx1.gamma_id = at3.gamma_id AND tx1.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND td5.branch_id = ? AND tx4.transaction_id = td5.transaction_id AND td5.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND tx4.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND tx4.gamma_id = at6.gamma_id AND at6.attr_id = at3.attr_id)";

   private static final String COMMIT_ATTRIBUTES =
         INSERTION + "FROM osee_txs tx1, osee_tx_details td2, osee_attribute at3 WHERE tx1.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND tx1.transaction_id = td2.transaction_id AND td2.branch_id = ? AND td2.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND tx1.gamma_id = at3.gamma_id";

   //destination branch id, source branch id
   private static final String UPDATE_CURRENT_COMMIT_RELATIONS =
         UPDATE + "FROM osee_txs tx1, osee_tx_details td2, osee_relation_link rl3, osee_txs tx4, osee_tx_details td5, osee_relation_link rl6 WHERE tx1.transaction_id = td2.transaction_id AND td2.branch_id = ? AND tx1.gamma_id = rl3.gamma_id AND tx1.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND td5.branch_id = ? AND tx4.transaction_id = td5.transaction_id AND td5.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND tx4.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND tx4.gamma_id = rl6.gamma_id AND rl6.rel_link_id = rl3.rel_link_id)";

   private static final String COMMIT_RELATIONS =
         INSERTION + "FROM osee_txs tx1, osee_tx_details td2, osee_relation_link rl3 WHERE tx1.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND tx1.transaction_id = td2.transaction_id AND td2.branch_id = ? AND td2.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND tx1.gamma_id = rl3.gamma_id";

   //destination branch id, source branch id
   private static final String UPDATE_CURRENT_COMMIT_ARTIFACTS =
         UPDATE + "FROM osee_txs tx1, osee_tx_details td2, osee_artifact_version av3, osee_txs tx4, osee_tx_details td5, osee_artifact_version av6 WHERE tx1.transaction_id = td2.transaction_id AND td2.branch_id = ? AND tx1.gamma_id = av3.gamma_id AND tx1.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND td5.branch_id = ? AND tx4.transaction_id = td5.transaction_id AND td5.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND tx4.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND tx4.gamma_id = av6.gamma_id AND av6.art_id = av3.art_id)";

   private static final String COMMIT_ARTIFACTS =
         INSERTION + "FROM osee_txs tx1, osee_tx_details td2, osee_artifact_version av3 WHERE tx1.tx_current != " + TxChange.NOT_CURRENT.getValue() + " AND tx1.transaction_id = td2.transaction_id AND td2.branch_id = ? AND td2.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND tx1.gamma_id = av3.gamma_id";

   private static final String UPDATE_MERGE_TRANSACTIONS =
         "UPDATE osee_txs set gamma_id = ?, mod_type = " + ModificationType.MERGED.getValue() + " Where transaction_id = ? and gamma_id = ?";

   private static final String UPDATE_MERGE_TRANSACTION_ID =
         "UPDATE osee_merge set transaction_id = ? Where source_branch_id = ? and dest_branch_id = ?";

   private static final String ARTIFACT_CHANGES =
         "SELECT av1.art_id, ? as branch_id FROM osee_txs tx1, osee_artifact_version av1 WHERE tx1.transaction_id = ? AND tx1.gamma_id = av1.gamma_id UNION ALL SELECT ar1.art_id, ? as branch_id FROM osee_txs tx1, osee_relation_link rl1, osee_artifact ar1 WHERE (rl1.a_art_id = ar1.art_id OR rl1.b_art_id = ar1.art_id) AND tx1.transaction_id = ? AND tx1.gamma_id = rl1.gamma_id";

   private static final String UPDATE_MODIFICATION_ID =
         "UPDATE osee_txs SET mod_type = " + ModificationType.NEW.getValue() + " WHERE mod_type = " + ModificationType.CHANGE.getValue() + " AND (transaction_id, gamma_id) in ((SELECT transaction_id, txs0.gamma_id FROM osee_txs txs0, osee_artifact_version ver0 where txs0.transaction_id = ? and txs0.gamma_id = ver0.gamma_id and ver0.art_id in (SELECT art_id FROM osee_tx_details det1, osee_txs txs1, osee_artifact_version ver1 WHERE det1.branch_id = ? AND det1.transaction_id = txs1.transaction_id AND det1.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND txs1.mod_type = " + ModificationType.NEW.getValue() + " AND txs1.gamma_id = ver1.gamma_id)) UNION (SELECT transaction_id, txs0.gamma_id FROM osee_txs txs0, osee_attribute ver0 where txs0.transaction_id = ? and txs0.gamma_id = ver0.gamma_id and ver0.attr_id in (SELECT attr_id FROM osee_tx_details det1, osee_txs txs1, osee_attribute ver1 WHERE det1.branch_id = ? AND det1.transaction_id = txs1.transaction_id AND det1.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND txs1.mod_type = " + ModificationType.NEW.getValue() + " AND txs1.gamma_id = ver1.gamma_id)) UNION (SELECT transaction_id, txs0.gamma_id FROM osee_txs txs0, osee_relation_link ver0 where txs0.transaction_id = ? and txs0.gamma_id = ver0.gamma_id and (ver0.a_art_id , ver0.b_art_id) in (SELECT a_art_id, b_art_id FROM osee_tx_details det1, osee_txs txs1, osee_relation_link ver1 WHERE det1.branch_id = ? AND det1.transaction_id = txs1.transaction_id AND det1.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND txs1.mod_type = " + ModificationType.NEW.getValue() + " AND txs1.gamma_id = ver1.gamma_id)))";

   private static final String REVERT_DELETED_NEW =
         "SELECT av1.art_id, td1.branch_id FROM osee_txs tx1, osee_txs tx2, osee_tx_details td1, osee_tx_details td2, osee_artifact_version av1, osee_artifact_version av2 WHERE td1.branch_id = ? AND td1.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND td1.transaction_id = tx1.transaction_id AND tx1.mod_type = " + ModificationType.NEW.getValue() + " AND tx1.gamma_id = av1.gamma_id AND td2.branch_id = ? AND td2.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND td2.transaction_id = tx2.transaction_id AND tx2.tx_current = " + TxChange.DELETED.getValue() + " AND tx2.gamma_id = av2.gamma_id AND av1.art_id = av2.art_id";

   private static final String REVERT_DELETED_NEW_ATTRIBUTE =
         "SELECT atr1.attr_id, atr1.art_id, td1.branch_id FROM osee_txs tx1, osee_txs tx2, osee_tx_details td1, osee_tx_details td2, osee_attribute atr1, osee_attribute atr2 WHERE td1.branch_id = ? AND td1.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND td1.transaction_id = tx1.transaction_id AND tx1.mod_type = " + ModificationType.NEW.getValue() + " AND tx1.gamma_id = atr1.gamma_id AND td2.branch_id = ? AND td2.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND td2.transaction_id = tx2.transaction_id AND tx2.tx_current in ( " + TxChange.DELETED.getValue() + " , " + TxChange.ARTIFACT_DELETED.getValue() + " ) AND tx2.gamma_id = atr2.gamma_id AND atr1.attr_id = atr2.attr_id";

   private static final String REVERT_DELETED_NEW_REL_LINK =
         "SELECT rel1.rel_link_id, td1.branch_id, tx2.transaction_id, tx2.gamma_id FROM osee_txs tx1, osee_txs tx2, osee_tx_details td1, osee_tx_details td2, osee_relation_link rel1, osee_relation_link rel2 WHERE td1.branch_id = ? AND td1.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND td1.transaction_id = tx1.transaction_id AND tx1.mod_type = " + ModificationType.NEW.getValue() + " AND tx1.gamma_id = rel1.gamma_id AND td2.branch_id = ? AND td2.tx_type = " + TransactionDetailsType.NonBaselined.getId() + " AND td2.transaction_id = tx2.transaction_id AND tx2.tx_current in ( " + TxChange.DELETED.getValue() + " , " + TxChange.ARTIFACT_DELETED.getValue() + " ) AND tx2.gamma_id = rel2.gamma_id AND rel1.rel_link_id = rel2.rel_link_id";
   private static final String UPDATE_DELETED_NEW =
         "UPDATE osee_txs set tx_current = 0 where gamma_id = ? AND transaction_id = ?";
   private static final String RESET_DELETED_NEW =
         "UPDATE osee_txs set tx_current = CASE WHEN mod_type = 3 THEN 2 WHEN mod_type = 5 THEN 3 ELSE 1 END WHERE gamma_id = ? AND transaction_id = ?";

   private IProgressMonitor monitor;
   private final CommitDbTx commitDbTx;
   private final ConflictManagerExternal conflictManager;

   private static final boolean DEBUG =
         "TRUE".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.osee.framework.skynet.core/debug/Commit"));
   private static final boolean MERGE_DEBUG =
         "TRUE".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.osee.framework.skynet.core/debug/Merge"));

   public CommitJob(Branch toBranch, Branch fromBranch, boolean archiveBranch, boolean forceCommit) throws OseeCoreException {
      super("\nCommitting Branch: " + fromBranch.getBranchName());
      conflictManager = new ConflictManagerExternal(toBranch, fromBranch);

      if (DEBUG) {
         System.out.println(String.format("Commiting Branch %s into Branch %s", fromBranch.getBranchId(),
               toBranch.getBranchId()));
      }

      if (conflictManager.remainingConflictsExist() && !forceCommit) {
         if (DEBUG) {
            System.out.println(String.format("  FAILED: Found %d unresolved conflicts",
                  conflictManager.getRemainingConflicts().size()));
         }
         throw new ConflictDetectionException(
               "Trying to commit " + fromBranch.getBranchName() + " into " + toBranch.getBranchName() + " when " + conflictManager.getRemainingConflicts().size() + " conflicts still exist");
      }
      commitDbTx = new CommitDbTx(fromBranch, toBranch, archiveBranch, conflictManager);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   protected IStatus run(IProgressMonitor monitor) {
      this.monitor = monitor;

      IStatus toReturn = Status.CANCEL_STATUS;
      try {
         commitDbTx.execute();
         toReturn = Status.OK_STATUS;
      } catch (Exception ex) {
         toReturn = new Status(Status.ERROR, SkynetActivator.PLUGIN_ID, Status.OK, ex.getLocalizedMessage(), ex);
      }
      return toReturn;
   }

   private final class CommitDbTx extends AbstractDbTxTemplate {
      private int newTransactionNumber;
      private final Branch toBranch;
      private final Branch fromBranch;
      private final boolean archiveBranch;
      private boolean success = true;
      private int fromBranchId = -1;
      private List<Object[]> relLinks;
      private List<Object[]> attributes;
      private List<Object[]> artifacts;

      private CommitDbTx(Branch fromBranch, Branch toBranch, boolean archiveBranch, ConflictManagerExternal conflictManager) {
         super();
         this.toBranch = toBranch;
         this.fromBranch = fromBranch;
         this.newTransactionNumber = -1;
         this.archiveBranch = archiveBranch;
         relLinks = new ArrayList<Object[]>();
         attributes = new ArrayList<Object[]>();
         artifacts = new ArrayList<Object[]>();
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.osee.framework.ui.plugin.util.db.AbstractDbTxTemplate#handleTxWork()
       */
      @Override
      protected void handleTxWork() throws OseeCoreException {
         monitor.beginTask("Acquire from branch transactions", 100);

         User userToBlame = SkynetAuthentication.getUser();

         long time = System.currentTimeMillis();
         long totalTime = time;
         int count = 0;
         //Load new and deleted artifact so that they can be compressed out of the commit transaction

         ConnectionHandlerStatement chStmt = null;
         try {
            chStmt =
                  ConnectionHandler.runPreparedQuery(REVERT_DELETED_NEW, fromBranch.getBranchId(),
                        fromBranch.getBranchId());

            while (chStmt.next()) {
               ArtifactPersistenceManager.getInstance().revertArtifact(chStmt.getInt("branch_id"),
                     chStmt.getInt("art_id"));
               count++;
            }
         } finally {
            ConnectionHandler.close(chStmt);
         }
         if (DEBUG) {
            System.out.println(String.format(
                  "   Reverted %d Artifacts in %s to avoid committing new and deleted artifacts", count,
                  Lib.getElapseString(time)));
         }
         chStmt = null;
         time = System.currentTimeMillis();
         count = 0;
         try {
            chStmt =
                  ConnectionHandler.runPreparedQuery(REVERT_DELETED_NEW_ATTRIBUTE, fromBranch.getBranchId(),
                        fromBranch.getBranchId());

            while (chStmt.next()) {
               ArtifactPersistenceManager.getInstance().revertAttribute(chStmt.getInt("branch_id"),
                     chStmt.getInt("art_id"), chStmt.getInt("attr_id"));
               count++;
            }
         } finally {
            ConnectionHandler.close(chStmt);
         }
         if (DEBUG) {
            System.out.println(String.format(
                  "   Reverted %d Attributes in %s to avoid committing new and deleted attributes", count,
                  Lib.getElapseString(time)));
         }
         //TODO Need to add in the Relation Link filter in the same mold as the above two filters. 
         //Added it in but not with a revert. just set tx_current to zero so it doesn't get committed
         time = System.currentTimeMillis();
         OseeConnection connection = OseeDbConnection.getConnection();
         try {
            chStmt =
                  ConnectionHandler.runPreparedQuery(connection, REVERT_DELETED_NEW_REL_LINK, fromBranch.getBranchId(),
                        fromBranch.getBranchId());

            while (chStmt.next()) {
               relLinks.add(new Object[] {chStmt.getInt("gamma_id"), chStmt.getInt("transaction_id")});
               if (DEBUG) {
                  System.out.println(String.format(
                        "    Setting transaction to 0 where gamma_id = %d and transaction_id = %d",
                        chStmt.getInt("gamma_id"), chStmt.getInt("transaction_id")));
               }
            }
            if (relLinks.size() > 0) {
               ConnectionHandler.runPreparedUpdate(connection, UPDATE_DELETED_NEW, relLinks);
            }
         } finally {
            ConnectionHandler.close(chStmt);
         }
         if (DEBUG) {
            System.out.println(String.format(
                  "   Set Tx_current to 0 for %d Relation Links in %s to avoid committing new and deleted attributes",
                  relLinks.size(), Lib.getElapseString(time)));
         }
         time = System.currentTimeMillis();
         if (fromBranch != null) {
            newTransactionNumber =
                  BranchPersistenceManager.addCommitTransactionToDatabase(connection, toBranch, fromBranch, userToBlame);
            fromBranchId = fromBranch.getBranchId();
            AccessControlManager.getInstance().removeAllPermissionsFromBranch(connection, fromBranch);
         } else {
            //Commit transaction instead of a branch
         }
         if (DEBUG) {
            System.out.println(String.format("   Added commit transaction [%d] into the DB in %s",
                  newTransactionNumber, Lib.getElapseString(time)));
         }

         monitor.worked(25);
         monitor.setTaskName("Commit transactions");

         time = System.currentTimeMillis();
         //Set the tx_current on the destination branch to 0 for the attributes that will be updated
         int insertCount =
               ConnectionHandler.runPreparedUpdate(connection, UPDATE_CURRENT_COMMIT_ATTRIBUTES,
                     toBranch.getBranchId(), fromBranchId);
         if (DEBUG) {
            count = insertCount;
            System.out.println(String.format(
                  "   Updated %d TX_Current values on Destination Branch for Attributes in %s", count,
                  Lib.getElapseString(time)));
         }

         time = System.currentTimeMillis();
         //Add the new attribute value to the destination branch
         insertCount +=
               ConnectionHandler.runPreparedUpdate(connection, COMMIT_ATTRIBUTES, newTransactionNumber, fromBranchId);
         if (DEBUG) {
            System.out.println(String.format("   Commited %d Attributes in %s", insertCount - count,
                  Lib.getElapseString(time)));
            count = insertCount;
         }

         time = System.currentTimeMillis();
         insertCount +=
               ConnectionHandler.runPreparedUpdate(connection, UPDATE_CURRENT_COMMIT_ARTIFACTS, toBranch.getBranchId(),
                     fromBranchId);
         if (DEBUG) {
            System.out.println(String.format(
                  "   Updated %d TX_Current values on Destination Branch for Artifacts in %s", insertCount - count,
                  Lib.getElapseString(time)));
            count = insertCount;
         }

         time = System.currentTimeMillis();
         insertCount +=
               ConnectionHandler.runPreparedUpdate(connection, COMMIT_ARTIFACTS, newTransactionNumber, fromBranchId);
         if (DEBUG) {
            System.out.println(String.format("   Commited %d Artifacts in %s", insertCount - count,
                  Lib.getElapseString(time)));
            count = insertCount;
         }

         time = System.currentTimeMillis();
         insertCount +=
               ConnectionHandler.runPreparedUpdate(connection, UPDATE_CURRENT_COMMIT_RELATIONS, toBranch.getBranchId(),
                     fromBranchId);
         if (DEBUG) {
            System.out.println(String.format(
                  "   Updated %d TX_Current values on Destination Branch for Relations in %s", insertCount - count,
                  Lib.getElapseString(time)));
            count = insertCount;
         }

         time = System.currentTimeMillis();
         insertCount +=
               ConnectionHandler.runPreparedUpdate(connection, COMMIT_RELATIONS, newTransactionNumber, fromBranchId);
         if (DEBUG) {
            System.out.println(String.format("   Commited %d Relations in %s", insertCount - count,
                  Lib.getElapseString(time)));
            count = insertCount;
         }

         //Change all modifications on artifacts/relation/attributes that are modified but should be new, because both new'd 
         //and modified on the same branch.
         time = System.currentTimeMillis();
         ConnectionHandler.runPreparedUpdate(connection, UPDATE_MODIFICATION_ID, newTransactionNumber, fromBranchId,
               newTransactionNumber, fromBranchId, newTransactionNumber, fromBranchId);
         if (DEBUG) {
            System.out.println(String.format("   Updated modification types for new and modified to modified in %s",
                  Lib.getElapseString(time)));
         }

         //add in all merge branch changes over any other source branch changes.
         time = System.currentTimeMillis();
         if (conflictManager.originalConflictsExist()) {
            count = 0;
            for (Conflict conflict : conflictManager.getOriginalConflicts()) {
               if (conflict.statusResolved()) {
                  count++;
                  if (MERGE_DEBUG) {
                     System.out.println(String.format(
                           "     Using Merge value for Artifact %d item %s, setting gamma id to %d where it was %d",
                           conflict.getArtifact().getArtId(), conflict.getChangeItem(), conflict.getMergeGammaId(),
                           conflict.getSourceGamma()));
                  }
                  ConnectionHandler.runPreparedUpdate(connection, UPDATE_MERGE_TRANSACTIONS,
                        conflict.getMergeGammaId(), newTransactionNumber, conflict.getSourceGamma());
                  conflict.setStatus(Conflict.Status.COMMITTED);
               }
            }
            if (DEBUG) {
               System.out.println(String.format("    Added %d Merge Values in %s", count, Lib.getElapseString(time)));
            }

            time = System.currentTimeMillis();
            //insert transaction id into the branch table
            ConnectionHandler.runPreparedUpdate(connection, UPDATE_MERGE_TRANSACTION_ID, newTransactionNumber,
                  fromBranch.getBranchId(), toBranch.getBranchId());
            if (DEBUG) {
               System.out.println(String.format("   Updated the Merge Transaction Id in the conflict table in %s",
                     Lib.getElapseString(time)));
            }
         }

         if (relLinks.size() > 0) {
            ConnectionHandler.runPreparedUpdate(connection, RESET_DELETED_NEW, relLinks);
         }

         time = System.currentTimeMillis();
         if (insertCount > 0) {
            Object[] dataList =
                  new Object[] {toBranch.getBranchId(), newTransactionNumber, toBranch.getBranchId(),
                        newTransactionNumber};
            // reload the committed artifacts since the commit changed them on the destination branch
            ArtifactLoader.getArtifacts(ARTIFACT_CHANGES, dataList, 400, ArtifactLoad.FULL, true, null, null, true);
            if (DEBUG) {
               System.out.println(String.format("   Reloaded the Artifacts after the commit in %s",
                     Lib.getElapseString(time)));
            }

            if (archiveBranch) {
               fromBranch.archive();
            }

         } else {
            throw new IllegalStateException(" A branch can not be commited without any changes made.");
         }

         if (DEBUG) {
            System.out.println(String.format("Commit Completed in %s", Lib.getElapseString(totalTime)));
         }
         success = true;
         monitor.done();
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.ui.plugin.util.db.AbstractDbTxTemplate#handleTxFinally()
       */
      @Override
      protected void handleTxFinally() throws Exception {
         super.handleTxFinally();
         if (success) {
            OseeEventManager.kickBranchEvent(this, BranchEventType.Committed, fromBranchId);
         }
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.osee.framework.ui.plugin.util.db.AbstractDbTxTemplate#handleTxException(java.lang.Exception)
       */
      @Override
      protected void handleTxException(Exception ex) throws Exception {
         super.handleTxException(ex);
         success = false;
      }
   }
}