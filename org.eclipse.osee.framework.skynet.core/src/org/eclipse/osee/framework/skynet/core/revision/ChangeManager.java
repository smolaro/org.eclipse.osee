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
package org.eclipse.osee.framework.skynet.core.revision;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.data.OseeSql;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.status.EmptyMonitor;
import org.eclipse.osee.framework.skynet.core.status.IStatusMonitor;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionIdManager;

/**
 * Public API class for access to change data from branches and transactionIds
 * 
 * @author Jeff C. Phillips
 * @author Donald G. Dunne
 */
public class ChangeManager {

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    * 
    * @param sourceBranch
    * @param baselineTransactionId
    * @return changes
    * @throws OseeCoreException
    */
   public static Collection<Change> getChangesPerTransaction(TransactionId transactionId, IStatusMonitor monitor) throws OseeCoreException {
      return InternalChangeManager.getInstance().getChanges(null, transactionId,
            monitor == null ? new EmptyMonitor() : monitor);
   }

   public static ChangeData getChangeDataPerTransaction(TransactionId transactionId, IStatusMonitor monitor) throws OseeCoreException {
      return new ChangeData(InternalChangeManager.getInstance().getChanges(null, transactionId,
            monitor == null ? new EmptyMonitor() : monitor));
   }

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    * 
    * @param sourceBranch
    * @param baselineTransactionId
    * @return changes
    * @throws OseeCoreException
    */
   public static Collection<Change> getChangesPerBranch(Branch sourceBranch, IStatusMonitor monitor) throws OseeCoreException {
      return InternalChangeManager.getInstance().getChanges(sourceBranch, null,
            monitor == null ? new EmptyMonitor() : monitor);
   }

   public static ChangeData getChangeDataPerBranch(Branch sourceBranch, IStatusMonitor monitor) throws OseeCoreException {
      return new ChangeData(InternalChangeManager.getInstance().getChanges(sourceBranch, null,
            monitor == null ? new EmptyMonitor() : monitor));
   }

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    * 
    * @param sourceBranch
    * @param baselineTransactionId
    * @return changes
    * @throws OseeCoreException
    */
   public static Collection<Change> getChanges(Branch sourceBranch, TransactionId transactionId, IStatusMonitor monitor) throws OseeCoreException {
      return InternalChangeManager.getInstance().getChanges(sourceBranch, transactionId,
            monitor == null ? new EmptyMonitor() : monitor);
   }

   public static ChangeData getChangeData(Branch sourceBranch, TransactionId transactionId, IStatusMonitor monitor) throws OseeCoreException {
      return new ChangeData(getChanges(sourceBranch, transactionId, monitor == null ? new EmptyMonitor() : monitor));
   }

   /**
    * @return true changes exist
    * @throws OseeCoreException
    */
   public static boolean isChangesOnWorkingBranch(Branch workingBranch) throws OseeCoreException {
      return InternalChangeManager.getInstance().isChangesOnWorkingBranch(workingBranch);
   }

   /**
    * For the given list of artifacts determine which transactions (on that artifact's branch) affected that artifact.
    * The branch's baseline transaction is excluded.
    * 
    * @param artifacts
    * @return a map of artifact to collection of TransactionIds which affected the given artifact
    * @throws OseeCoreException
    */
   public static HashCollection<Artifact, TransactionId> getModifingTransactions(Collection<Artifact> artifacts) throws OseeCoreException {
      List<Object[]> insertParameters = new ArrayList<Object[]>(artifacts.size() * 5);

      int queryId = ArtifactLoader.getNewQueryId();
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

      CompositeKeyHashMap<Integer, Branch, Artifact> artifactMap = new CompositeKeyHashMap<Integer, Branch, Artifact>();
      for (Artifact artifact : artifacts) {
         Branch branch = artifact.getBranch();
         artifactMap.put(artifact.getArtId(), branch, artifact);
         int transactionNumber = TransactionIdManager.getlatestTransactionForBranch(branch).getTransactionNumber();
         insertParameters.add(new Object[] {queryId, insertTime, artifact.getArtId(), branch.getBranchId(),
               transactionNumber});

         // for each combination of artifact and its branch hierarchy
         while (branch.hasParentBranch()) {
            transactionNumber = branch.getParentTransactionId().getTransactionNumber();
            branch = branch.getParentBranch();
            insertParameters.add(new Object[] {queryId, insertTime, artifact.getArtId(), branch.getBranchId(),
                  transactionNumber});
         }
      }

      HashCollection<Artifact, TransactionId> transactionMap = new HashCollection<Artifact, TransactionId>();
      try {
         ArtifactLoader.insertIntoArtifactJoin(insertParameters);
         ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
         try {
            chStmt.runPreparedQuery(insertParameters.size() * 2,
                  ClientSessionManager.getSQL(OseeSql.Changes.SELECT_MODIFYING_TRANSACTION), queryId);
            while (chStmt.next()) {
               Branch branch = BranchManager.getBranch(chStmt.getInt("branch_id"));
               Artifact artifact = artifactMap.get(chStmt.getInt("art_id"), branch);
               transactionMap.put(artifact, TransactionIdManager.getTransactionId(chStmt.getInt("transaction_id")));
            }
         } finally {
            chStmt.close();
         }
      } finally {
         ArtifactLoader.clearQuery(queryId);
      }

      return transactionMap;
   }

   /**
    * For the given list of artifacts determine which branches (in the branch hierarchy for that artifact) affected that
    * artifact.
    * 
    * @param artifacts
    * @return a map of artifact to collection of branches which affected the given artifact
    * @throws OseeCoreException
    */
   public static HashCollection<Artifact, Branch> getModifingBranches(Collection<Artifact> artifacts) throws OseeCoreException {
      List<Object[]> insertParameters = new ArrayList<Object[]>(artifacts.size() * 5);
      int queryId = ArtifactLoader.getNewQueryId();
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

      CompositeKeyHashMap<Integer, Branch, Artifact> artifactMap = new CompositeKeyHashMap<Integer, Branch, Artifact>();
      for (Artifact artifact : artifacts) {
         artifactMap.put(artifact.getArtId(), artifact.getBranch(), artifact);
         // for each combination of artifact and all working branches in its hierarchy
         for (Branch workingBranch : artifact.getBranch().getWorkingBranches()) {
            insertParameters.add(new Object[] {queryId, insertTime, artifact.getArtId(), workingBranch.getBranchId(),
                  SQL3DataType.INTEGER});
         }
      }

      HashCollection<Artifact, Branch> branchMap = new HashCollection<Artifact, Branch>();
      try {
         ArtifactLoader.insertIntoArtifactJoin(insertParameters);
         ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
         try {
            chStmt.runPreparedQuery(insertParameters.size() * 2,
                  ClientSessionManager.getSQL(OseeSql.Changes.SELECT_MODIFYING_BRANCHES), queryId);
            while (chStmt.next()) {
               if (chStmt.getInt("tx_count") > 0) {
                  Branch branch = BranchManager.getBranch(chStmt.getInt("branch_id"));
                  Artifact artifact = artifactMap.get(chStmt.getInt("art_id"), branch.getParentBranch());
                  branchMap.put(artifact, branch);
               }
            }
         } finally {
            chStmt.close();
         }
      } finally {
         ArtifactLoader.clearQuery(queryId);
      }

      return branchMap;
   }
}