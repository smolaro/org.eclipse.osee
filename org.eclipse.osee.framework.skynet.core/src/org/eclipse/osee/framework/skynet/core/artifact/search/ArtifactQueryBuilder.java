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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.data.OseeSql;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.db.connection.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoad;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.ISearchConfirmer;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactQueryBuilder {
   private final HashMap<String, NextAlias> nextAliases = new HashMap<String, NextAlias>();
   private final StringBuilder sql = new StringBuilder(1000);
   private final List<Object> queryParameters = new ArrayList<Object>();
   private List<String> guids;
   private List<String> hrids;
   private String guidOrHrid;
   private final AbstractArtifactSearchCriteria[] criteria;
   private final Branch branch;
   private int artifactId;
   private Collection<Integer> artifactIds;
   private final Collection<ArtifactType> artifactTypes;
   private final boolean allowDeleted;
   private final ArtifactLoad loadLevel;
   private boolean count = false;
   private boolean emptyCriteria = false;
   private boolean firstTable = true;
   private final boolean tableOrderForward;
   private final TransactionId transactionId;

   /**
    * @param artId
    * @param branch
    * @param allowDeleted set whether deleted artifacts should be included in the resulting artifact list
    */
   public ArtifactQueryBuilder(int artId, Branch branch, boolean allowDeleted, ArtifactLoad loadLevel) {
      this(null, artId, null, null, null, branch, null, allowDeleted, loadLevel, true);
   }

   /**
    * search for artifacts with the given ids
    * 
    * @param artifactIds list of artifact ids
    * @param branch
    * @param allowDeleted set whether deleted artifacts should be included in the resulting artifact list
    */
   public ArtifactQueryBuilder(Collection<Integer> artifactIds, Branch branch, boolean allowDeleted, ArtifactLoad loadLevel) {
      this(artifactIds, 0, null, null, null, branch, null, allowDeleted, loadLevel, true);
      emptyCriteria = artifactIds.size() == 0;
   }

   public ArtifactQueryBuilder(List<String> guidOrHrids, Branch branch, ArtifactLoad loadLevel) {
      this(null, 0, guidOrHrids, null, null, branch, null, false, loadLevel, true);
      emptyCriteria = guidOrHrids.size() == 0;
   }

   public ArtifactQueryBuilder(List<String> guidOrHrids, Branch branch, boolean allowDeleted, ArtifactLoad loadLevel) {
      this(null, 0, guidOrHrids, null, null, branch, null, allowDeleted, loadLevel, true);
      emptyCriteria = guidOrHrids.size() == 0;
   }

   public ArtifactQueryBuilder(List<String> guidOrHrids, TransactionId transactionId2, boolean allowDeleted, ArtifactLoad loadLevel) {
      this(null, 0, guidOrHrids, null, null, transactionId2.getBranch(), transactionId2, allowDeleted, loadLevel, true);
      emptyCriteria = guidOrHrids.size() == 0;
   }

   public ArtifactQueryBuilder(String guidOrHrid, Branch branch, boolean allowDeleted, ArtifactLoad loadLevel) {
      this(null, 0, null, ensureValid(guidOrHrid), null, branch, null, allowDeleted, loadLevel, true);
   }

   public ArtifactQueryBuilder(ArtifactType artifactType, Branch branch, ArtifactLoad loadLevel, boolean allowDeleted) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, null, allowDeleted, loadLevel, true);
   }

   public ArtifactQueryBuilder(Collection<ArtifactType> artifactTypes, Branch branch, ArtifactLoad loadLevel) {
      this(null, 0, null, null, artifactTypes, branch, null, false, loadLevel, true);
      emptyCriteria = artifactTypes.size() == 0;
   }

   public ArtifactQueryBuilder(Branch branch, ArtifactLoad loadLevel, boolean allowDeleted) {
      this(null, 0, null, null, null, branch, null, allowDeleted, loadLevel, false);
   }

   public ArtifactQueryBuilder(Branch branch, ArtifactLoad loadLevel, boolean allowDeleted, AbstractArtifactSearchCriteria... criteria) {
      this(null, 0, null, null, null, branch, null, allowDeleted, loadLevel, true, criteria);
      emptyCriteria = criteria.length == 0;
   }

   public ArtifactQueryBuilder(Branch branch, ArtifactLoad loadLevel, List<AbstractArtifactSearchCriteria> criteria) {
      this(null, 0, null, null, null, branch, null, false, loadLevel, true, toArray(criteria));
      emptyCriteria = criteria.size() == 0;
   }

   public ArtifactQueryBuilder(ArtifactType artifactType, Branch branch, ArtifactLoad loadLevel, AbstractArtifactSearchCriteria... criteria) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, null, false, loadLevel, true, criteria);
      emptyCriteria = criteria.length == 0;
   }

   public ArtifactQueryBuilder(ArtifactType artifactType, Branch branch, ArtifactLoad loadLevel, List<AbstractArtifactSearchCriteria> criteria) {
      this(null, 0, null, null, Arrays.asList(artifactType), branch, null, false, loadLevel, true, toArray(criteria));
      emptyCriteria = criteria.size() == 0;
   }

   private ArtifactQueryBuilder(Collection<Integer> artifactIds, int artifactId, List<String> guidOrHrids, String guidOrHrid, Collection<ArtifactType> artifactTypes, Branch branch, TransactionId transactionId, boolean allowDeleted, ArtifactLoad loadLevel, boolean tableOrderForward, AbstractArtifactSearchCriteria... criteria) {
      this.artifactTypes = artifactTypes;
      this.branch = branch;
      this.criteria = criteria;
      this.loadLevel = loadLevel;
      this.allowDeleted = allowDeleted;
      this.guidOrHrid = guidOrHrid;
      this.artifactId = artifactId;
      this.tableOrderForward = tableOrderForward;
      this.transactionId = transactionId;
      if (artifactIds != null && !artifactIds.isEmpty()) {
         if (artifactIds.size() == 1) {
            this.artifactId = artifactIds.iterator().next();
         } else {
            this.artifactIds = artifactIds;
         }
      }

      if (guidOrHrids != null && !guidOrHrids.isEmpty()) {
         if (guidOrHrids.size() == 1) {
            this.guidOrHrid = guidOrHrids.get(0);
         } else {
            hrids = new ArrayList<String>();
            guids = new ArrayList<String>();
            for (String id : guidOrHrids) {
               if (GUID.isValid(id)) {
                  guids.add(id);
               } else {
                  hrids.add(id);
               }
            }
         }
      }

      nextAliases.put("osee_txs", new NextAlias("txs"));
      nextAliases.put("osee_tx_details", new NextAlias("txd"));
      nextAliases.put("osee_artifact", new NextAlias("art"));
      nextAliases.put("osee_artifact_version", new NextAlias("arv"));
      nextAliases.put("osee_attribute", new NextAlias("att"));
      nextAliases.put("osee_relation_link", new NextAlias("rel"));
   }

   private static AbstractArtifactSearchCriteria[] toArray(List<AbstractArtifactSearchCriteria> criteria) {
      return criteria.toArray(new AbstractArtifactSearchCriteria[criteria.size()]);
   }

   private static String ensureValid(String id) {
      if (id == null) {
         throw new IllegalArgumentException("The id can not be null.");
      }
      return id;
   }

   private String getArtifactSelectSql() throws OseeCoreException {
      if (count) {
         sql.append("SELECT%s count(%s.art_id) FROM ");
      } else {
         sql.append("SELECT%s %s.art_id, %s.branch_id FROM ");
      }

      if (criteria.length > 0) {
         for (AbstractArtifactSearchCriteria x : criteria) {
            x.addToTableSql(this);
         }
      }

      String artAlias, artvAlias, txsAlias, txdAlias;
      if (tableOrderForward) {
         artAlias = appendAliasedTable("osee_artifact");
         artvAlias = appendAliasedTable("osee_artifact_version");
         txsAlias = appendAliasedTable("osee_txs");
         txdAlias = appendAliasedTable("osee_tx_details");
      } else {
         txdAlias = appendAliasedTable("osee_tx_details");
         txsAlias = appendAliasedTable("osee_txs");
         artvAlias = appendAliasedTable("osee_artifact_version");
         artAlias = appendAliasedTable("osee_artifact");
      }
      sql.append("\n");

      sql.append(" WHERE ");

      if (artifactId != 0) {
         sql.append(artAlias);
         sql.append(".art_id=? AND ");
         addParameter(artifactId);
      }

      if (artifactIds != null) {
         sql.append(artAlias);
         sql.append(".art_id IN (" + Collections.toString(",", artifactIds) + ") AND ");
      }
      if (artifactTypes != null) {
         sql.append(artAlias);
         sql.append(".art_type_id");
         if (artifactTypes.size() == 1) {
            sql.append("=? AND ");
            addParameter(artifactTypes.iterator().next().getArtTypeId());
         } else {
            sql.append(" IN (");
            for (ArtifactType artifactType : artifactTypes) {
               sql.append(artifactType.getArtTypeId());
               sql.append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") AND ");
         }
      }

      if (guidOrHrid != null) {
         if (GUID.isValid(guidOrHrid)) {
            sql.append(artAlias);
            sql.append(".guid=? AND ");
         } else {
            sql.append(artAlias);
            sql.append(".human_readable_id=? AND ");
         }
         addParameter(guidOrHrid);
      }

      if (guids != null && guids.size() > 0) {
         sql.append(artAlias);
         sql.append(".guid IN ('" + Collections.toString("','", guids) + "') AND ");
      }
      if (hrids != null && hrids.size() > 0) {
         sql.append(artAlias);
         sql.append(".human_readable_id IN ('" + Collections.toString("','", hrids) + "') AND ");
      }

      sql.append("\n");
      if (criteria.length > 0) {
         criteria[0].addToWhereSql(this);
         sql.append("\n");
         for (int i = 1; i < criteria.length; i++) {
            AbstractArtifactSearchCriteria leftCriteria = criteria[i - 1];
            AbstractArtifactSearchCriteria rightCriteria = criteria[i];
            leftCriteria.addJoinArtId(this, false);
            sql.append("=");
            rightCriteria.addJoinArtId(this, true);
            sql.append(" AND ");
            rightCriteria.addToWhereSql(this);
            sql.append("\n");
         }
         criteria[criteria.length - 1].addJoinArtId(this, false);
         sql.append("=");
         sql.append(artAlias);
         sql.append(".art_id AND ");
      }

      sql.append(artAlias);
      sql.append(".art_id=");
      sql.append(artvAlias);
      sql.append(".art_id AND ");
      sql.append(artvAlias);
      sql.append(".gamma_id=");
      sql.append(txsAlias);
      sql.append(".gamma_id AND ");

      if (transactionId != null) {
         sql.append(txsAlias);
         sql.append(".transaction_id <= ?");
         addParameter(transactionId.getTransactionNumber());
      } else {
         sql.append(txsAlias);
         sql.append(".tx_current");

         if (allowDeleted) {
            sql.append(" IN (");
            sql.append(TxChange.CURRENT.getValue());
            sql.append(", ");
            sql.append(TxChange.DELETED.getValue());
            sql.append(")");
         } else {
            sql.append("=");
            sql.append(TxChange.CURRENT.getValue());
         }
      }

      sql.append(" AND ");
      addBranchTxSql(txsAlias, txdAlias);

      List<String> paramList = new ArrayList<String>();
      paramList.add(OseeSql.isHintsAllowed() ? ClientSessionManager.getSQL(OseeSql.QUERY_BUILDER_HINT) : "");
      if (count) {
         paramList.add(artAlias);
      } else {
         paramList.add(artAlias);
         //         paramList.add(txdAlias);
         paramList.add(txdAlias);
      }
      return String.format(sql.toString(), paramList.toArray());
   }

   public void append(String sqlSnippet) {
      sql.append(sqlSnippet);
   }

   public void addParameter(Object data) {
      queryParameters.add(data);
   }

   public void addTxSql(String txsAlias, String txdAlias, boolean historical) {
      if (!historical) {
         addCurrentTxSql(txsAlias);
      }
      addBranchTxSql(txsAlias, txdAlias);
      sql.append(" AND ");
   }

   private void addCurrentTxSql(String txsAlias) {
      sql.append(txsAlias);
      sql.append(".tx_current=1 AND ");
   }

   private void addBranchTxSql(String txsAlias, String txdAlias) {
      sql.append(txsAlias);
      sql.append(".transaction_id=");
      sql.append(txdAlias);
      sql.append(".transaction_id");
      if (branch != null) {
         sql.append(" AND ");
         sql.append(txdAlias);
         sql.append(".branch_id=?");
         addParameter(branch.getBranchId());
      }
   }

   public String appendAliasedTable(String table) {
      if (firstTable) {
         firstTable = false;
      } else {
         sql.append(',');
      }
      sql.append(table);
      sql.append(' ');
      String alias = nextAliases.get(table).getNextAlias();
      sql.append(alias);
      return alias;
   }

   private class NextAlias {
      String aliasPrefix;
      int aliasSuffix;

      public NextAlias(String aliasPrefix) {
         this.aliasPrefix = aliasPrefix;
         this.aliasSuffix = 1;
      }

      public String getNextAlias() {
         return aliasPrefix + aliasSuffix++;
      }
   }

   public List<Artifact> getArtifacts(int artifactCountEstimate, ISearchConfirmer confirmer) throws OseeCoreException {
      return internalGetArtifacts(artifactCountEstimate, confirmer, false);
   }

   public List<Artifact> reloadArtifacts(int artifactCountEstimate) throws OseeCoreException {
      return internalGetArtifacts(artifactCountEstimate, null, true);
   }

   public Artifact reloadArtifact() throws OseeCoreException {
      if (emptyCriteria) {
         throw new ArtifactDoesNotExist("received an empty list in the criteria for this search");
      }
      Collection<Artifact> artifacts = internalGetArtifacts(1, null, true);

      if (artifacts.size() == 0) {
         throw new ArtifactDoesNotExist(getSoleExceptionMessage(artifacts.size()));
      }
      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist(getSoleExceptionMessage(artifacts.size()));
      }
      return artifacts.iterator().next();
   }

   private List<Artifact> internalGetArtifacts(int artifactCountEstimate, ISearchConfirmer confirmer, boolean reload) throws OseeCoreException {
      if (emptyCriteria) {
         return java.util.Collections.emptyList();
      }
      List<Artifact> artifacts =
            ArtifactLoader.getArtifacts(getArtifactSelectSql(), queryParameters.toArray(), artifactCountEstimate,
                  loadLevel, reload, confirmer, null, allowDeleted);
      clearCriteria();
      return artifacts;
   }

   private void clearCriteria() throws OseeDataStoreException {
      if (this.criteria != null) {
         for (AbstractArtifactSearchCriteria critiri : criteria) {
            critiri.cleanUp();
         }
      }
   }

   public void selectArtifacts(int queryId, int artifactCountEstimate, CompositeKeyHashMap<Integer, Integer, Object[]> insertParameters, TransactionId transactionId) throws OseeCoreException {
      ArtifactLoader.selectArtifacts(queryId, insertParameters, getArtifactSelectSql(), queryParameters.toArray(),
            artifactCountEstimate, transactionId);
      clearCriteria();
   }

   public int countArtifacts() throws OseeCoreException {
      if (emptyCriteria) {
         return 0;
      }

      count = true;
      try {
         return ConnectionHandler.runPreparedQueryFetchInt(0, getArtifactSelectSql(), queryParameters.toArray());
      } finally {
         clearCriteria();
      }
   }

   public Artifact getArtifact() throws OseeCoreException {
      if (emptyCriteria) {
         throw new ArtifactDoesNotExist("received an empty list in the criteria for this search");
      }
      Collection<Artifact> artifacts = getArtifacts(1, null);

      if (artifacts.size() == 0) {
         throw new ArtifactDoesNotExist(getSoleExceptionMessage(artifacts.size()));
      }
      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist(getSoleExceptionMessage(artifacts.size()));
      }
      return artifacts.iterator().next();
   }

   private String getSoleExceptionMessage(int artifactCount) {
      StringBuilder message = new StringBuilder(250);
      if (artifactCount == 0) {
         message.append("ArtifactQueryBuilder: No artifact found");
      } else {
         message.append(artifactCount);
         message.append(" artifacts found");
      }
      if (artifactTypes != null) {
         message.append(" with type(s): ");
         message.append(artifactTypes);
      }
      if (artifactId != 0) {
         message.append(" with id \"");
         message.append(artifactId);
         message.append("\"");
      }
      if (guidOrHrid != null) {
         message.append(" with id \"");
         message.append(guidOrHrid);
         message.append("\"");
      }
      if (criteria.length > 0) {
         message.append(" with criteria \"");
         message.append(Arrays.deepToString(criteria));
         message.append("\"");
      }
      message.append(" on branch \"");
      message.append(branch);
      message.append("\"");
      return message.toString();
   }
}