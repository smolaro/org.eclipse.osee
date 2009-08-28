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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.exception.TransactionDoesNotExist;
import org.eclipse.osee.framework.database.core.ConnectionHandlerStatement;
import org.eclipse.osee.framework.database.core.OseeSql;
import org.eclipse.osee.framework.database.core.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoad;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.change.ArtifactChanged;
import org.eclipse.osee.framework.skynet.core.change.AttributeChanged;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.change.ChangeType;
import org.eclipse.osee.framework.skynet.core.change.RelationChanged;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;
import org.eclipse.osee.framework.skynet.core.status.IStatusMonitor;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionIdManager;

/**
 * Acquires changes for either branches or transactions.
 * 
 * @author Jeff C. Phillips
 */
public final class InternalChangeManager {
   private static final boolean DEBUG =
         "TRUE".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.osee.framework.skynet.core/debug/Change"));

   private static InternalChangeManager instance = new InternalChangeManager();

   private InternalChangeManager() {
      super();
   }

   public static InternalChangeManager getInstance() {
      return instance;
   }

   /**
    * @return Returns artifact, relation and attribute changes from a specific artifact
    * @throws OseeCoreException
    */
   Collection<Change> getChangesPerArtifact(Artifact artifact, IStatusMonitor monitor) throws OseeCoreException {
      ArrayList<Change> changes = new ArrayList<Change>();
      Branch branch = artifact.getBranch();
      ArrayList<TransactionId> transactionIds = new ArrayList<TransactionId>();
      recurseBranches(branch, artifact, transactionIds);
      
      for (TransactionId transactionId : transactionIds) {
         changes.addAll(getChanges(null, transactionId, monitor, artifact));
      }
      return changes;
   }

   private void recurseBranches(Branch branch, Artifact artifact, Collection<TransactionId> transactionIds) throws OseeCoreException{
      transactionIds.addAll(getTransactionsPerArtifact(branch, artifact));
      
      if(branch.getParentBranch() != null && branch.hasParentBranch()){
         recurseBranches(branch.getParentBranch(), artifact, transactionIds);
      }
   }
   
   private Collection<TransactionId> getTransactionsPerArtifact(Branch branch, Artifact artifact) throws OseeCoreException {
      Collection<TransactionId> transactionIds = new ArrayList<TransactionId>();

      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      try {
         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_GET_TRANSACTIONS_PER_ARTIFACT),
               branch.getBranchId(), artifact.getArtId());

         while (chStmt.next()) {
            transactionIds.add(TransactionIdManager.getTransactionId(chStmt.getInt("transaction_id")));
         }
      } finally {
         chStmt.close();
      }

      return transactionIds;
   }

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    */
   Collection<Change> getChanges(Branch sourceBranch, TransactionId transactionId, IStatusMonitor monitor) throws OseeCoreException {
      return getChanges(sourceBranch, transactionId, monitor, null);
   }

   /**
    * Acquires artifact, relation and attribute changes from a source branch since its creation.
    * 
    * @param sourceBranch
    * @param baselineTransactionId
    * @return
    * @throws OseeCoreException
    */
   private Collection<Change> getChanges(Branch sourceBranch, TransactionId transactionId, IStatusMonitor monitor, Artifact specificArtifact) throws OseeCoreException {
      ArrayList<Change> changes = new ArrayList<Change>();
      Set<Integer> artIds = new HashSet<Integer>();
      Set<Integer> newAndDeletedArtifactIds = new HashSet<Integer>();
      boolean historical = sourceBranch == null;
      long totalTime = System.currentTimeMillis();

      monitor.startJob("Find Changes", 100);
      if (DEBUG) {
         System.out.println(String.format("\nChange Manager: getChanges(%s, %s)", sourceBranch, transactionId));
      }

      loadNewOrDeletedArtifactChanges(sourceBranch, transactionId, artIds, changes, newAndDeletedArtifactIds, monitor,
            specificArtifact);
      loadAttributeChanges(sourceBranch, transactionId, artIds, changes, newAndDeletedArtifactIds, monitor,
            specificArtifact);
      loadRelationChanges(sourceBranch, transactionId, artIds, changes, newAndDeletedArtifactIds, monitor,
            specificArtifact);

      Branch branch = historical ? transactionId.getBranch() : sourceBranch;

      if (historical) {
         for (Change change : changes) {
            change.setBranch(branch);
         }
      }

      monitor.setSubtaskName("Loading Artifacts from the Database");
      long time = System.currentTimeMillis();
      if (!artIds.isEmpty()) {
         int queryId = ArtifactLoader.getNewQueryId();
         Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

         List<Object[]> insertParameters = new LinkedList<Object[]>();
         for (int artId : artIds) {
            insertParameters.add(new Object[] {queryId, insertTime, artId, branch.getBranchId(),
                  historical ? transactionId.getTransactionNumber() : SQL3DataType.INTEGER});
         }
         ArtifactLoader.loadArtifacts(queryId, ArtifactLoad.FULL, null, insertParameters, true, historical, true);
      }

      if (DEBUG) {
         System.out.println(String.format("     Loaded %d Artifacts in %s", artIds.size(), Lib.getElapseString(time)));
         System.out.println(String.format("Change Manager: Found all of the Changes in %s\n",
               Lib.getElapseString(totalTime)));
      }
      monitor.done();
      return changes;
   }

   /**
    * @param sourceBranch
    * @param changes
    * @throws TransactionDoesNotExist
    * @throws BranchDoesNotExist
    * @throws OseeDataStoreException
    */
   private void loadNewOrDeletedArtifactChanges(Branch sourceBranch, TransactionId transactionId, Set<Integer> artIds, ArrayList<Change> changes, Set<Integer> newAndDeletedArtifactIds, IStatusMonitor monitor, Artifact specificArtifact) throws OseeCoreException {

      Map<Integer, ArtifactChanged> artifactChanges = new HashMap<Integer, ArtifactChanged>();
      boolean hasBranch = sourceBranch != null;
      TransactionId fromTransactionId;
      TransactionId toTransactionId;
      long time = System.currentTimeMillis();
      if (DEBUG) {
         System.out.println(String.format("     Gathering New or Deleted Artifacts on %s",
               hasBranch ? "Branch: " + sourceBranch : "Transaction: " + transactionId));
      }

      monitor.setSubtaskName("Gathering New or Deleted Artifacts");
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      try {

         if (hasBranch) { //Changes per a branch
            Pair<TransactionId, TransactionId> branchStartEndTransaction =
                  TransactionIdManager.getStartEndPoint(sourceBranch);

            fromTransactionId = branchStartEndTransaction.getFirst();
            toTransactionId = branchStartEndTransaction.getSecond();

            chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_BRANCH_ARTIFACT),
                  sourceBranch.getBranchId());
         } else { //Changes per a transaction
            toTransactionId = transactionId;

            if (specificArtifact != null) {
               chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_TX_ARTIFACT_FOR_SPECIFIC_ARTIFACT),
                     toTransactionId.getTransactionNumber(), specificArtifact.getArtId());
               fromTransactionId = toTransactionId;
            } else {
               chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_TX_ARTIFACT),
                     toTransactionId.getTransactionNumber());
               fromTransactionId = TransactionIdManager.getPriorTransaction(toTransactionId);
            }
         }
         int count = 0;
         while (chStmt.next()) {
            count++;
            int artId = chStmt.getInt("art_id");
            ModificationType modificationType = ModificationType.getMod(chStmt.getInt("mod_type"));

            ArtifactChanged artifactChanged =
                  new ArtifactChanged(sourceBranch, chStmt.getInt("art_type_id"), chStmt.getInt("gamma_id"), artId,
                        toTransactionId, fromTransactionId, modificationType, ChangeType.OUTGOING, !hasBranch);

            //We do not want to display artifacts that were new and then deleted
            //The only was this could happen is if the artifact was in here twice
            //since the sql only returns new or deleted artifacts
            if (!artifactChanges.containsKey(artId)) {
               artIds.add(artId);
               changes.add(artifactChanged);
               artifactChanges.put(artId, artifactChanged);
               //The same artifact can have this change many times on a branch. We only want to capture that it occurred once in the change report
            } else if (modificationType != ModificationType.INTRODUCED) {
               changes.remove(artifactChanges.get(artId));
               newAndDeletedArtifactIds.add(artId);
            }
         }
         if (DEBUG) {
            System.out.println(String.format("        Found %d Changes in %s", count, Lib.getElapseString(time)));
         }
         monitor.updateWork(25);
      } finally {
         chStmt.close();
      }
   }

   /**
    * @param sourceBranch
    * @param changes
    * @throws OseeCoreException
    */
   private void loadRelationChanges(Branch sourceBranch, TransactionId transactionId, Set<Integer> artIds, ArrayList<Change> changes, Set<Integer> newAndDeletedArtifactIds, IStatusMonitor monitor, Artifact specificArtifact) throws OseeCoreException {
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      TransactionId fromTransactionId;
      TransactionId toTransactionId;

      monitor.setSubtaskName("Gathering Relation Changes");
      try {
         boolean hasBranch = sourceBranch != null;
         long time = System.currentTimeMillis();
         if (DEBUG) {
            System.out.println(String.format("     Gathering Relation Changes on %s",
                  hasBranch ? "Branch: " + sourceBranch : "Transaction: " + transactionId));
         }
         //Changes per a branch
         if (hasBranch) {
            chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_BRANCH_RELATION),
                  sourceBranch.getBranchId());

            Pair<TransactionId, TransactionId> branchStartEndTransaction =
                  TransactionIdManager.getStartEndPoint(sourceBranch);

            fromTransactionId = branchStartEndTransaction.getFirst();
            toTransactionId = branchStartEndTransaction.getSecond();
         }//Changes per a transaction
         else {
            toTransactionId = transactionId;

            if (specificArtifact != null) {
               chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_TX_RELATION_FOR_SPECIFIC_ARTIFACT),
                     transactionId.getTransactionNumber(), specificArtifact.getArtId(), specificArtifact.getArtId());
               fromTransactionId = transactionId;
            } else {
               chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_TX_RELATION),
                     transactionId.getTransactionNumber());
               fromTransactionId = TransactionIdManager.getPriorTransaction(toTransactionId);
            }
         }

         int count = 0;
         while (chStmt.next()) {
            count++;
            int aArtId = chStmt.getInt("a_art_id");
            int bArtId = chStmt.getInt("b_art_id");
            int relLinkId = chStmt.getInt("rel_link_id");

            if (!newAndDeletedArtifactIds.contains(aArtId) && !newAndDeletedArtifactIds.contains(bArtId)) {
               ModificationType modificationType = ModificationType.getMod(chStmt.getInt("mod_type"));
               String rationale = modificationType != ModificationType.DELETED ? chStmt.getString("rationale") : "";
               artIds.add(aArtId);
               artIds.add(bArtId);

               changes.add(new RelationChanged(sourceBranch, chStmt.getInt("art_type_id"), chStmt.getInt("gamma_id"),
                     aArtId, toTransactionId, fromTransactionId, modificationType, ChangeType.OUTGOING, bArtId,
                     relLinkId, rationale, chStmt.getInt("a_order"), chStmt.getInt("b_order"),
                     RelationTypeManager.getType(chStmt.getInt("rel_link_type_id")), !hasBranch));
            }
         }
         if (DEBUG) {
            System.out.println(String.format("        Found %d Changes in %s", count, Lib.getElapseString(time)));
         }
         monitor.updateWork(25);
      } finally {
         chStmt.close();
      }
   }

   /**
    * @param sourceBranch
    * @param changes
    * @throws TransactionDoesNotExist
    * @throws BranchDoesNotExist
    * @throws OseeDataStoreException
    */
   private void loadAttributeChanges(Branch sourceBranch, TransactionId transactionId, Set<Integer> artIds, ArrayList<Change> changes, Set<Integer> newAndDeletedArtifactIds, IStatusMonitor monitor, Artifact specificArtifact) throws OseeCoreException {
      Map<Integer, Change> attributesWasValueCache = new HashMap<Integer, Change>();
      Map<Integer, ModificationType> artModTypes = new HashMap<Integer, ModificationType>();
      Set<Integer> modifiedArtifacts = new HashSet<Integer>();
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      boolean hasBranch = sourceBranch != null;
      long time = System.currentTimeMillis();

      monitor.setSubtaskName("Gathering Attribute Changes");
      if (DEBUG) {
         System.out.println(String.format("     Gathering Attribute Changes on %s",
               hasBranch ? "Branch: " + sourceBranch : "Transaction: " + transactionId));
      }
      TransactionId fromTransactionId;
      TransactionId toTransactionId;
      boolean hasSpecificArtifact = specificArtifact != null;

      for (Change change : changes) {// cache in map for performance look ups
         artModTypes.put(change.getArtId(), change.getModificationType());
      }
      //Changes per a branch
      if (hasBranch) {
         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_BRANCH_ATTRIBUTE_IS),
               sourceBranch.getBranchId());

         Pair<TransactionId, TransactionId> branchStartEndTransaction =
               TransactionIdManager.getStartEndPoint(sourceBranch);

         fromTransactionId = branchStartEndTransaction.getFirst();
         toTransactionId = branchStartEndTransaction.getSecond();
      }//Changes per transaction number
      else {
         toTransactionId = transactionId;
         if (hasSpecificArtifact) {
            chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_TX_ATTRIBUTE_IS_FOR_SPECIFIC_ARTIFACT),
                  transactionId.getTransactionNumber(), specificArtifact.getArtId());
            fromTransactionId = transactionId;
         } else {
            chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.CHANGE_TX_ATTRIBUTE_IS),
                  transactionId.getTransactionNumber());
            fromTransactionId = TransactionIdManager.getPriorTransaction(toTransactionId);
         }
      }
      loadIsValues(sourceBranch, artIds, changes, newAndDeletedArtifactIds, monitor, attributesWasValueCache,
            artModTypes, modifiedArtifacts, chStmt, hasBranch, time, fromTransactionId, toTransactionId,
            hasSpecificArtifact);
      loadAttributeWasValues(sourceBranch, transactionId, artIds, monitor, attributesWasValueCache, hasBranch);
   }

   private void loadIsValues(Branch sourceBranch, Set<Integer> artIds, ArrayList<Change> changes, Set<Integer> newAndDeletedArtifactIds, IStatusMonitor monitor, Map<Integer, Change> attributesWasValueCache, Map<Integer, ModificationType> artModTypes, Set<Integer> modifiedArtifacts, ConnectionHandlerStatement chStmt, boolean hasBranch, long time, TransactionId fromTransactionId, TransactionId toTransactionId, boolean hasSpecificArtifact) throws OseeDataStoreException, OseeArgumentException, OseeTypeDoesNotExist {
      ModificationType artModType;
      AttributeChanged attributeChanged;

      try {
         int count = 0;
         while (chStmt.next()) {
            count++;
            int attrId = chStmt.getInt("attr_id");
            int artId = chStmt.getInt("art_id");
            int sourceGamma = chStmt.getInt("gamma_id");
            int attrTypeId = chStmt.getInt("attr_type_id");
            int artTypeId = chStmt.getInt("art_type_id");
            String isValue = chStmt.getString("is_value");
            ModificationType modificationType = ModificationType.getMod(chStmt.getInt("mod_type"));

            if (artModTypes.containsKey(artId)) {
               artModType = artModTypes.get(artId);
            } else {
               artModType = ModificationType.MODIFIED;
            }

            //This will be false iff the artifact was new and then deleted
            if (!newAndDeletedArtifactIds.contains(artId)) {
               // Want to add an artifact changed item once if any attribute was modified && artifact was not
               // NEW or DELETED and these chnages are not for a specific artifact
               if (artModType == ModificationType.MODIFIED && !modifiedArtifacts.contains(artId) && !hasSpecificArtifact) {
                  ArtifactChanged artifactChanged =
                        new ArtifactChanged(sourceBranch, artTypeId, -1, artId, toTransactionId, fromTransactionId,
                              ModificationType.MODIFIED, ChangeType.OUTGOING, !hasBranch);

                  changes.add(artifactChanged);
                  modifiedArtifacts.add(artId);
               }

               //ModTypes will be temporarily set to new and then revised for based on the existence of a was value
               if (modificationType == ModificationType.MODIFIED && artModType != ModificationType.INTRODUCED) {
                  modificationType = ModificationType.NEW;
               }

               attributeChanged =
                     new AttributeChanged(sourceBranch, artTypeId, sourceGamma, artId, toTransactionId,
                           fromTransactionId, modificationType, ChangeType.OUTGOING, isValue, "", attrId, attrTypeId,
                           artModType, !hasBranch);

               changes.add(attributeChanged);
               attributesWasValueCache.put(attrId, attributeChanged);
               artIds.add(artId);
            }
         }

         if (DEBUG) {
            System.out.println(String.format("        Found %d Changes in %s", count, Lib.getElapseString(time)));
         }
         monitor.updateWork(13);
         monitor.setSubtaskName("Gathering Was values");
      } finally {
         chStmt.close();
      }
   }

   /**
    * @param sourceBranch
    * @param transactionId
    * @param artIds
    * @param monitor
    * @param attributesWasValueCache
    * @param hasBranch
    * @throws OseeCoreException
    * @throws OseeDataStoreException
    */
   private void loadAttributeWasValues(Branch sourceBranch, TransactionId transactionId, Set<Integer> artIds, IStatusMonitor monitor, Map<Integer, Change> attributesWasValueCache, boolean hasBranch) throws OseeCoreException, OseeDataStoreException {
      if (!artIds.isEmpty()) {
         int count = 0;
         long time = System.currentTimeMillis();
         int sqlParamter; // Will either be a branch id or transaction id
         Branch wasValueBranch;
         String sql;

         if (hasBranch) {
            wasValueBranch = sourceBranch;
            sql = ClientSessionManager.getSql(OseeSql.CHANGE_BRANCH_ATTRIBUTE_WAS);
            sqlParamter = wasValueBranch.getBranchId();
         } else {
            wasValueBranch = transactionId.getBranch();
            sql = ClientSessionManager.getSql(OseeSql.CHANGE_TX_ATTRIBUTE_WAS);
            sqlParamter = transactionId.getTransactionNumber();
         }

         int queryId = ArtifactLoader.getNewQueryId();
         Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();
         List<Object[]> datas = new LinkedList<Object[]>();
         ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();

         try {
            // insert into the artifact_join_table
            for (int artId : artIds) {
               datas.add(new Object[] {queryId, insertTime, artId, wasValueBranch.getBranchId(), SQL3DataType.INTEGER});
            }
            ArtifactLoader.insertIntoArtifactJoin(datas);
            chStmt.runPreparedQuery(sql, sqlParamter, queryId);
            int previousAttrId = -1;

            while (chStmt.next()) {
               count++;
               int attrId = chStmt.getInt("attr_id");

               if (previousAttrId != attrId) {
                  String wasValue = chStmt.getString("was_value");
                  if (attributesWasValueCache.containsKey(attrId) && attributesWasValueCache.get(attrId) instanceof AttributeChanged) {
                     AttributeChanged changed = (AttributeChanged) attributesWasValueCache.get(attrId);

                     if (changed.getArtModType() != ModificationType.NEW) {
                        if (changed.getModificationType() != ModificationType.DELETED && changed.getModificationType() != ModificationType.ARTIFACT_DELETED) {
                           changed.setModType(ModificationType.MODIFIED);
                        }
                        changed.setWasValue(wasValue);
                     }
                  }
                  previousAttrId = attrId;
               }
            }
         } finally {
            ArtifactLoader.clearQuery(queryId);
            chStmt.close();
         }
         if (DEBUG) {
            System.out.println(String.format("        Loaded %d was values in %s", count, Lib.getElapseString(time)));
         }
         monitor.updateWork(12);
      }
   }

   boolean isChangesOnWorkingBranch(Branch workingBranch) throws OseeCoreException {
      Pair<TransactionId, TransactionId> transactionToFrom = TransactionIdManager.getStartEndPoint(workingBranch);
      if (transactionToFrom.getFirst().equals(transactionToFrom.getSecond())) {
         return false;
      }
      return true;
   }
}