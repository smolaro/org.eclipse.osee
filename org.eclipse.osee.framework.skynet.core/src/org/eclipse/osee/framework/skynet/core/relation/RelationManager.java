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
package org.eclipse.osee.framework.skynet.core.relation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.db.connection.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoad;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;

/**
 * @author Ryan D. Brooks
 */
public class RelationManager {
   // the branch is accounted for because Artifact.equals includes the branch in the comparison
   private static final CompositeKeyHashMap<Artifact, RelationType, List<RelationLink>> relationsByType =
         new CompositeKeyHashMap<Artifact, RelationType, List<RelationLink>>(1024);

   private static final HashMap<Artifact, List<RelationLink>> artifactToRelations =
         new HashMap<Artifact, List<RelationLink>>(1024);

   private static final int LINKED_LIST_KEY = -1;

   private static RelationLink getLoadedRelation(Artifact artifact, int aArtifactId, int bArtifactId, RelationType relationType) {
      List<RelationLink> selectedRelations = relationsByType.get(artifact, relationType);
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            if (!relation.isDeleted() && relation.getAArtifactId() == aArtifactId && relation.getBArtifactId() == bArtifactId) {
               return relation;
            }
         }
      }
      return null;
   }

   /**
    * This method should never be called by application code.
    * 
    * @param relationType
    * @param aArtifactId
    * @param bArtifactId
    * @param aBranch
    * @param bBranch
    * @return
    */
   public static RelationLink getLoadedRelation(RelationType relationType, int aArtifactId, int bArtifactId, Branch aBranch, Branch bBranch) {
      Artifact artifactA = ArtifactCache.getActive(aArtifactId, aBranch);
      Artifact artifactB = ArtifactCache.getActive(bArtifactId, bBranch);

      RelationLink relation = null;
      if (artifactA != null) {
         relation = getLoadedRelation(artifactA, aArtifactId, bArtifactId, relationType);
      }
      if (artifactB != null && relation == null) {
         relation = getLoadedRelation(artifactB, aArtifactId, bArtifactId, relationType);
      }
      return relation;
   }

   /**
    * Store the newly instantiated relation from the perspective of relationSide in its appropriate order
    * 
    * @param relation
    * @param relationSide
    */
   public static void manageRelation(RelationLink relation, RelationSide relationSide) {
      Artifact artifact =
            ArtifactCache.getActive(relation.getArtifactId(relationSide), relation.getBranch(relationSide));

      if (artifact != null) {
         List<RelationLink> artifactsRelations = artifactToRelations.get(artifact);
         if (artifactsRelations == null) {
            artifactsRelations = new CopyOnWriteArrayList<RelationLink>();
            artifactToRelations.put(artifact, artifactsRelations);
         }
         if (artifactsRelations.contains(relation)) {
            return;
         }

         artifactsRelations.add(relation);

         List<RelationLink> selectedRelations = relationsByType.get(artifact, relation.getRelationType());
         if (selectedRelations == null) {
            selectedRelations = new CopyOnWriteArrayList<RelationLink>();
            relationsByType.put(artifact, relation.getRelationType(), selectedRelations);
         }
         selectedRelations.add(relation);
      }
   }

   private static List<Artifact> getRelatedArtifacts(Artifact artifact, RelationType relationType, RelationSide relationSide) throws OseeCoreException {
      List<RelationLink> selectedRelations = null;
      if (relationType == null) {
         selectedRelations = artifactToRelations.get(artifact);
      } else {
         selectedRelations = relationsByType.get(artifact, relationType);
      }

      if (selectedRelations == null) {
         return Collections.emptyList();
      }

      int queryId = ArtifactLoader.getNewQueryId();
      CompositeKeyHashMap<Integer, Integer, Object[]> insertParameters =
            new CompositeKeyHashMap<Integer, Integer, Object[]>((int) (selectedRelations.size() * 1.25) + 1);
      List<Artifact> relatedArtifacts = new ArrayList<Artifact>(selectedRelations.size());

      if (relationType == null) {
         addRelatedArtifactIds(queryId, artifact, relatedArtifacts, insertParameters, selectedRelations,
               RelationSide.OPPOSITE);
      } else {
         addRelatedArtifactIds(queryId, artifact, relatedArtifacts, insertParameters, selectedRelations, relationSide);
      }

      if (insertParameters.size() > 0) {
         ArtifactLoader.loadArtifacts(queryId, ArtifactLoad.FULL, null, insertParameters.values(), false, false, false);
      }

      //now that bulk loading is done, put the artifacts in the right order and return them
      relatedArtifacts.clear();
      for (RelationLink relation : selectedRelations) {
         if (!relation.isDeleted()) {
            try {
               if (relationSide == null) {
                  Artifact art = relation.getArtifactOnOtherSide(artifact);
                  //	               if(!art.isDeleted()){
                  relatedArtifacts.add(art);
                  //	               }
               } else {
                  // only select relations where the related artifact is on relationSide
                  // (and thus on the side opposite of "artifact")
                  if (relation.getSide(artifact) != relationSide) {
                     Artifact art = relation.getArtifact(relationSide);
                     //	            	  if(!art.isDeleted()){
                     relatedArtifacts.add(art);
                     //		              }	                  
                  }
               }
            } catch (ArtifactDoesNotExist ex) {
               OseeLog.log(SkynetActivator.class, Level.WARNING, ex.getMessage(), ex);
            }
         }
      }
      return relatedArtifacts;
   }

   private static void addRelatedArtifactIds(int queryId, Artifact artifact, Collection<Artifact> relatedArtifacts, CompositeKeyHashMap<Integer, Integer, Object[]> insertParameters, List<RelationLink> relations, RelationSide side) {
      if (relations == null) {
         return;
      }
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

      for (RelationLink relation : relations) {
         if (!relation.isDeleted()) {
            RelationSide resolvedSide = null;
            if (side == RelationSide.OPPOSITE) {
               resolvedSide = relation.getSide(artifact).oppositeSide();
            } else {
               if (relation.getSide(artifact) != side) {
                  resolvedSide = side;
               }
            }
            if (resolvedSide != null) {
               int artId = relation.getArtifactId(resolvedSide);
               int branchId = relation.getBranch(resolvedSide).getBranchId();
               Artifact relatedArtifact = ArtifactCache.getActive(artId, branchId);
               if (relatedArtifact == null) {
                  insertParameters.put(artId, branchId, new Object[] {queryId, insertTime, artId, branchId,
                        SQL3DataType.INTEGER});
               } else {
                  relatedArtifacts.add(relatedArtifact);
               }
            }
         }
      }
   }

   public static Set<Artifact> getRelatedArtifacts(Collection<? extends Artifact> artifacts, int depth, IRelationEnumeration... relationEnums) throws OseeCoreException {
      int queryId = ArtifactLoader.getNewQueryId();
      CompositeKeyHashMap<Integer, Integer, Object[]> insertParameters =
            new CompositeKeyHashMap<Integer, Integer, Object[]>(artifacts.size() * 8);
      Set<Artifact> relatedArtifacts = new HashSet<Artifact>(artifacts.size() * 8);
      Collection<Artifact> newArtifactsToSearch = new ArrayList<Artifact>(artifacts);
      Collection<Artifact> newArtifacts = new ArrayList<Artifact>();
      int oldArtifactCount = -1;
      for (int i = 0; i < depth && oldArtifactCount != relatedArtifacts.size(); i++) {
         oldArtifactCount = relatedArtifacts.size();
         insertParameters.clear();
         newArtifacts.clear();
         for (Artifact artifact : newArtifactsToSearch) {
            List<RelationLink> selectedRelations = null;

            if (relationEnums.length == 0) {
               selectedRelations = artifactToRelations.get(artifact);
               addRelatedArtifactIds(queryId, artifact, newArtifacts, insertParameters, selectedRelations,
                     RelationSide.OPPOSITE);
            } else {
               for (IRelationEnumeration relationEnum : relationEnums) {
                  selectedRelations = relationsByType.get(artifact, relationEnum.getRelationType());
                  addRelatedArtifactIds(queryId, artifact, newArtifacts, insertParameters, selectedRelations,
                        relationEnum.getSide());
               }
            }
         }

         if (insertParameters.size() > 0) {
            newArtifacts.addAll(ArtifactLoader.loadArtifacts(queryId, ArtifactLoad.FULL, null,
                  insertParameters.values(), false, false, false));
         }
         newArtifactsToSearch.clear();
         newArtifactsToSearch.addAll(newArtifacts);
         relatedArtifacts.addAll(newArtifacts);
      }
      return relatedArtifacts;
   }

   public static List<Artifact> getRelatedArtifactsAll(Artifact artifact) throws OseeCoreException {
      return getRelatedArtifacts(artifact, null, null);
   }

   public static List<Artifact> getRelatedArtifacts(Artifact artifact, RelationType relationType) throws OseeCoreException {
      return getRelatedArtifacts(artifact, relationType, null);
   }

   public static List<Artifact> getRelatedArtifacts(Artifact artifact, IRelationEnumeration relationEnum) throws OseeCoreException {
      return getRelatedArtifacts(artifact, relationEnum.getRelationType(), relationEnum.getSide());
   }

   private static Artifact getRelatedArtifact(Artifact artifact, RelationType relationType, RelationSide relationSide) throws OseeCoreException {
      List<Artifact> artifacts = getRelatedArtifacts(artifact, relationType, relationSide);

      if (artifacts.size() == 0) {
         throw new ArtifactDoesNotExist(String.format("There is no artifact related to %s by a relation of type %s",
               artifact, relationType));
      }

      if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist(
               String.format(
                     "There are %s artifacts related to \"%s\" by a relation of type \"%s\" on side %s instead of the expected 1.",
                     artifacts.size(), artifact, relationType, relationSide.toString()));
      }
      return artifacts.get(0);
   }

   public static Artifact getRelatedArtifact(Artifact artifact, IRelationEnumeration relationEnum) throws OseeCoreException {
      return getRelatedArtifact(artifact, relationEnum.getRelationType(), relationEnum.getSide());
   }

   public static int getRelatedArtifactsCount(Artifact artifact, RelationType relationType, RelationSide relationSide) {
      List<RelationLink> selectedRelations = relationsByType.get(artifact, relationType);

      int artifactCount = 0;
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            if (!relation.isDeleted()) {
               if (relationSide == null) {
                  artifactCount++;
               } else {
                  // only select relations where the related artifact is on the side specified by relationEnum
                  // (and thus on the side opposite of "artifact")
                  if (relation.getSide(artifact) != relationSide) {
                     artifactCount++;
                  }
               }
            }
         }
      }

      return artifactCount;
   }

   /**
    * @param artifact
    * @deprecated
    */
   @Deprecated
   public static void prepareRelationsForReload(Artifact artifact) {
      //This is inappropriate to use as references held to links by other applications will continue to exist.
      artifactToRelations.remove(artifact);
      for (RelationType type : RelationTypeManager.getValidTypes(artifact.getArtifactType(), artifact.getBranch())) {
         relationsByType.remove(artifact, type);
      }
   }

   public static boolean hasDirtyLinks(Artifact artifact) {
      List<RelationLink> selectedRelations = artifactToRelations.get(artifact);
      if (selectedRelations == null) {
         return false;
      }
      for (RelationLink relation : selectedRelations) {
         if (relation.isDirty()) {
            return true;
         }
      }
      return false;
   }

   /**
    * @param artifact
    * @param relationType if not null persists the relations of this type, otherwise persists relations of all types
    */
   public static void persistRelationsFor(Artifact artifact, RelationType relationType) throws OseeCoreException {
      List<RelationLink> selectedRelations;
      if (relationType == null) {
         selectedRelations = artifactToRelations.get(artifact);
      } else {
         selectedRelations = relationsByType.get(artifact, relationType);
      }

      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            if (relation.isDirty()) {
               RelationPersistenceManager.makePersistent(relation);

               try {
                  Artifact artifactOnOtherSide = relation.getArtifactOnOtherSide(artifact);
                  List<RelationLink> otherSideRelations =
                        relationsByType.get(artifactOnOtherSide, relation.getRelationType());
                  for (int i = 0; i < otherSideRelations.size(); i++) {
                     if (relation.equals(otherSideRelations.get(i))) {
                        if (i + 1 < otherSideRelations.size()) {
                           RelationLink nextRelation = otherSideRelations.get(i + 1);
                           if (nextRelation.isDirty()) {
                              RelationPersistenceManager.makePersistent(nextRelation);
                           }
                        }
                     }
                  }
               } catch (ArtifactDoesNotExist ex) {
                  OseeLog.log(
                        RelationManager.class,
                        Level.SEVERE,
                        String.format(
                              "Unable to to persist other side relation order because the artifact on the other side of [%s, %s] doesn't exist. ",
                              artifact.toString(), relation.toString()), ex);
               }

            }
         }
      }
   }

   public static List<RelationLink> getRelationsAll(Artifact artifact, boolean includeDeleted) {
      List<RelationLink> selectedRelations = artifactToRelations.get(artifact);

      if (selectedRelations == null) {
         return Collections.emptyList();
      }

      List<RelationLink> relations = new ArrayList<RelationLink>(selectedRelations.size());
      for (RelationLink relation : selectedRelations) {
         if (!relation.isDeleted() || includeDeleted) {
            relations.add(relation);
         }
      }
      return relations;
   }

   public static List<RelationLink> getRelations(Artifact artifact, RelationType relationType, RelationSide relationSide) {
      List<RelationLink> selectedRelations = relationsByType.get(artifact, relationType);
      if (selectedRelations == null) {
         return Collections.emptyList();
      }

      List<RelationLink> relations = new ArrayList<RelationLink>(selectedRelations.size());

      for (RelationLink relation : selectedRelations) {
         if (!relation.isDeleted()) {
            if (relationSide == null) {
               relations.add(relation);
            } else {
               // only select relations where the related artifact is on the side specified by relationEnum
               // (and thus on the side opposite of "artifact")
               if (relation.getSide(artifact) != relationSide) {
                  relations.add(relation);
               }
            }
         }
      }
      return relations;
   }

   /**
    * @param relationType
    * @param artifactA
    * @param artifactB
    * @param rationale
    * @throws OseeCoreException
    */
   public static void addRelation(RelationType relationType, Artifact artifactA, Artifact artifactB, String rationale) throws OseeCoreException {
      RelationLink relation = getLoadedRelation(artifactA, artifactA.getArtId(), artifactB.getArtId(), relationType);

      if (relation == null) {
         ensureRelationCanBeAdded(relationType, artifactA, artifactB);
         relation = new RelationLink(artifactA, artifactB, relationType, rationale);
         relation.setDirty();

         setDefaultRelationOrder(relation, artifactA, artifactB);

         RelationManager.manageRelation(relation, RelationSide.SIDE_A);
         RelationManager.manageRelation(relation, RelationSide.SIDE_B);

         try {
            OseeEventManager.kickRelationModifiedEvent(RelationManager.class, RelationModType.Added, relation,
                  relation.getABranch(), relationType.getTypeName());
         } catch (Exception ex) {
            SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
         }
      }
   }

   public static void ensureRelationCanBeAdded(RelationType relationType, Artifact artifactA, Artifact artifactB) {
      // For now, relations can not be cross branch.  Ensure that both artifacts are on same branch
      // TODO Fix this when fix cross branching (not writing or reading from db correctly)
      if (!artifactA.getBranch().equals(artifactB.getBranch())) {
         throw new IllegalArgumentException("Cross branch linking is not yet supported.");
      }
      ensureSideWillSupport(artifactA, relationType, RelationSide.SIDE_A, artifactA.getArtifactType(), 1);
      ensureSideWillSupport(artifactB, relationType, RelationSide.SIDE_B, artifactB.getArtifactType(), 1);
   }

   /**
    * Check whether artifactCount number of additional artifacts of type artifactType can be related to the artifact on
    * side relationSide for relations of type relationType
    * 
    * @param relationType
    * @param relationSide
    * @param artifact
    * @param artifactCount
    */
   public static void ensureSideWillSupport(Artifact artifact, RelationType relationType, RelationSide relationSide, ArtifactType artifactType, int artifactCount) {
      int maxCount = RelationTypeManager.getRelationSideMax(relationType, artifactType, relationSide);
      int usedCount = getRelatedArtifactsCount(artifact, relationType, relationSide.oppositeSide());

      if (maxCount == 0) {
         throw new IllegalArgumentException(String.format(
               "Artifact \"%s\" of type \"%s\" does not belong on side \"%s\" of relation \"%s\"",
               artifact.getDescriptiveName(), artifact.getArtifactTypeName(), relationType.getSideName(relationSide),
               relationType.getTypeName()));
      } else if (maxCount == 1 && usedCount + artifactCount > maxCount) {
         throw new IllegalArgumentException(
               String.format(
                     "Artifact \"%s\" of type \"%s\" can not be added to \"%s\" of relation \"%s\" because doing so would exceed the side maximum of %d for this artifact type",
                     artifact.getDescriptiveName(), artifact.getArtifactTypeName(), relationSide.toString(),
                     relationType.getTypeName(), maxCount));
      }
   }

   public static void deleteRelation(RelationType relationType, Artifact artifactA, Artifact artifactB) throws ArtifactDoesNotExist {
      RelationLink relation = getLoadedRelation(artifactA, artifactA.getArtId(), artifactB.getArtId(), relationType);
      relation.delete(true);
   }

   public static void deleteRelationsAll(Artifact artifact) throws ArtifactDoesNotExist {
      List<RelationLink> selectedRelations = artifactToRelations.get(artifact);
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            relation.delete(false);
         }
      }
   }

   public static void deleteRelations(Artifact artifact, RelationType relationType, RelationSide relationSide) throws ArtifactDoesNotExist {
      List<RelationLink> selectedRelations = relationsByType.get(artifact, relationType);
      if (selectedRelations != null) {
         for (RelationLink relation : selectedRelations) {
            if (relationSide == null) {
               relation.delete(true);
            } else {
               if (relation.getSide(artifact) != relationSide) {
                  relation.delete(true);
               }
            }
         }
      }
   }

   /**
    * Remove all relations stored in the list awaiting to be deleted.
    * 
    * @throws OseeDataStoreException
    */
   public static void purgeRelationsFor(Artifact artifact) throws OseeDataStoreException {
      Collection<RelationLink> links = artifactToRelations.get(artifact);
      if (!links.isEmpty()) {
         List<Object[]> batchArgs = new ArrayList<Object[]>(links.size());
         String PURGE_RELATION = "delete from osee_relation_link WHERE rel_link_id = ?";
         for (RelationLink link : links) {
            batchArgs.add(new Object[] {link.getRelationId()});
            link.markAsPurged();
         }
         ConnectionHandler.runPreparedUpdateBatch(PURGE_RELATION, batchArgs);
      }
   }

   /**
    * @param sideA
    * @param targetArtifact
    * @param insertAfterTarget
    * @param relationType
    * @param artifactA
    * @param artifactB
    * @param rationale
    * @throws OseeCoreException
    */
   public static void addRelation(Artifact artifactATarget, boolean insertAfterATarget, Artifact artifactBTarget, boolean insertAfterBTarget, RelationType relationType, Artifact artifactA, Artifact artifactB, String rationale) throws OseeCoreException {
      ensureRelationCanBeAdded(relationType, artifactA, artifactB);
      RelationLink relation = getLoadedRelation(artifactA, artifactA.getArtId(), artifactB.getArtId(), relationType);

      if (relation == null) {
         relation = new RelationLink(artifactA, artifactB, relationType, rationale);
         relation.setDirty();

         setDefaultRelationOrder(relation, artifactA, artifactB);

         RelationManager.manageRelation(relation, RelationSide.SIDE_A);
         RelationManager.manageRelation(relation, RelationSide.SIDE_B);

         setRelationOrdering(RelationSide.SIDE_B, relation, artifactBTarget, insertAfterBTarget, artifactA, artifactA,
               artifactBTarget);
         setRelationOrdering(RelationSide.SIDE_A, relation, artifactATarget, insertAfterATarget, artifactB,
               artifactATarget, artifactB);
      }

      OseeEventManager.kickRelationModifiedEvent(RelationManager.class, RelationModType.Added, relation,
            relation.getBranch(), relation.getRelationType().getTypeName());

   }

   private static void setDefaultRelationOrder(RelationLink relation, Artifact artifactA, Artifact artifactB) {
      if (relation.getRelationType().isOrdered()) {
         List<RelationLink> selectedRelations =
               getRelations(artifactA, relation.getRelationType(), RelationSide.SIDE_B);
         if (selectedRelations != null && selectedRelations.size() > 0) {
            relation.setOrder(RelationSide.SIDE_B, selectedRelations.get(selectedRelations.size() - 1).getArtifactId(
                  RelationSide.SIDE_B));
         } else {
            relation.setOrder(RelationSide.SIDE_B, -1);
         }
         selectedRelations = getRelations(artifactB, relation.getRelationType(), RelationSide.SIDE_A);
         if (selectedRelations != null && selectedRelations.size() > 0) {
            relation.setOrder(RelationSide.SIDE_A, selectedRelations.get(selectedRelations.size() - 1).getArtifactId(
                  RelationSide.SIDE_A));
         } else {
            relation.setOrder(RelationSide.SIDE_A, -1);
         }
      }
   }

   private static void setRelationOrdering(RelationSide side, RelationLink relation, Artifact targetArtifact, boolean insertAfterTarget, Artifact sourceArtifact, Artifact artA, Artifact artB) throws OseeCoreException {
      if (relation.getRelationType().isOrdered() && targetArtifact != null) {

         RelationLink targetRelation =
               getLoadedRelation(sourceArtifact, artA.getArtId(), artB.getArtId(), relation.getRelationType());
         if (targetRelation == null) {
            throw new OseeCoreException(String.format(
                  "No Relation exists on [%s] of type [%s] between aArtId[%d] and bArtId[%d].", artA.toString(),
                  relation.getRelationType().toString(), artA.getArtId(), artB.getArtId()));
         }
         List<RelationLink> selectedRelations = relationsByType.get(sourceArtifact, relation.getRelationType());
         if (selectedRelations.remove(relation)) {
            int targetIndex = selectedRelations.indexOf(targetRelation);
            if (targetIndex == -1) {
               selectedRelations.add(relation);
            } else {
               int index = insertAfterTarget ? targetIndex + 1 : targetIndex;
               selectedRelations.add(index, relation);
            }
            int lastArtId = LINKED_LIST_KEY;
            for (RelationLink link : selectedRelations) {
               if (!link.isDeleted() && link.getSide(sourceArtifact) == side.oppositeSide()) {
                  if (link.getOrder(side) != lastArtId) {
                     link.setOrder(side, lastArtId);
                  }
                  lastArtId = link.getArtifactId(side);
               }
            }
         }
      }
   }

   /**
    * @param targetArtifact
    * @param insertAfterTarget
    * @param relationType
    * @param artifactA
    * @param artifactB
    * @throws OseeCoreException
    */
   public static void setRelationOrder(Artifact artifactATarget, boolean insertAfterATarget, Artifact artifactBTarget, boolean insertAfterBTarget, RelationType relationType, Artifact artifactA, Artifact artifactB) throws OseeCoreException {

      RelationLink relation = getLoadedRelation(artifactA, artifactA.getArtId(), artifactB.getArtId(), relationType);

      setRelationOrdering(RelationSide.SIDE_B, relation, artifactBTarget, insertAfterBTarget, artifactA, artifactA,
            artifactBTarget);
      setRelationOrdering(RelationSide.SIDE_A, relation, artifactATarget, insertAfterATarget, artifactB,
            artifactATarget, artifactB);

      try {
         OseeEventManager.kickRelationModifiedEvent(RelationManager.class, RelationModType.ReOrdered, relation,
               relation.getABranch(), relationType.getTypeName());
      } catch (Exception ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   /**
    * @param artifact
    */
   public static void sortRelations(Artifact artifact, Map<Integer, RelationLink> sideA, Map<Integer, RelationLink> sideB) {
      List<RelationType> types = RelationTypeManager.getValidTypes(artifact.getArtifactType(), artifact.getBranch());
      for (RelationType type : types) {
         sortRelations(artifact, type, sideA, sideB);
      }
   }

   public static void sortRelations(Artifact artifact, RelationType type, Map<Integer, RelationLink> sideA, Map<Integer, RelationLink> sideB) {
      if (type.isOrdered()) {
         List<RelationLink> relations = relationsByType.get(artifact, type);
         if (relations != null) {
            sideA.clear();
            sideB.clear();
            boolean badValues = false;
            for (RelationLink relation : relations) {
               if (!relation.isDeleted()) {
                  if (relation.getSide(artifact) == RelationSide.SIDE_A) {
                     if (sideB.put(relation.getOrder(RelationSide.SIDE_B), relation) != null) {
                        badValues = true;
                     }
                  } else {
                     if (sideA.put(relation.getOrder(RelationSide.SIDE_A), relation) != null) {
                        badValues = true;
                     }
                  }
               }
            }
            if (!badValues) {
               relations.clear();

               //do side b first
               RelationLink relation = sideB.remove(LINKED_LIST_KEY);
               while (relation != null) {
                  relations.add(relation);
                  RelationLink newRelation = sideB.get(relation.getArtifactId(RelationSide.SIDE_B));
                  sideB.remove(relation.getArtifactId(RelationSide.SIDE_B));
                  relation = newRelation;
               }
               if (sideB.values().size() > 0) {
                  OseeLog.log(
                        RelationManager.class,
                        Level.FINE,
                        String.format(
                              "Artifact artId[%d] - %s - is unsorted for relations type - relTypeId[%d] - %s - . (missing a relation)",
                              artifact.getArtId(), artifact.toString(), type.getRelationTypeId(), type.toString()));
               }
               relations.addAll(sideB.values());
               //now side a
               relation = sideA.remove(LINKED_LIST_KEY);
               while (relation != null) {
                  relations.add(relation);
                  relation = sideA.remove(relation.getArtifactId(RelationSide.SIDE_A));
               }
               if (sideA.values().size() > 0) {
                  OseeLog.log(
                        RelationManager.class,
                        Level.FINE,
                        String.format(
                              "Artifact artId[%d] - %s - is unsorted for relations type  - relTypeId[%d] - %s - . (missing a relation)",
                              artifact.getArtId(), artifact.toString(), type.getRelationTypeId(), type.toString()));
               }
               relations.addAll(sideA.values());
            } else {
               OseeLog.log(
                     RelationManager.class,
                     Level.FINE,
                     String.format(
                           "Artifact - artId[%d] - %s - is unsorted for relations type  - relTypeId[%d] - %s - # of relations in mem - %d - . (duplicate relation)",
                           artifact.getArtId(), artifact.toString(), type.getRelationTypeId(), type.toString(),
                           relations.size()));
            }
         }
      }
   }

   /**
    * @param relationLink
    * @param b
    * @throws ArtifactDoesNotExist
    */
   static void setOrderValuesBasedOnCurrentMemoryOrder(RelationLink relationLink, boolean markAsNotDirty) throws ArtifactDoesNotExist {
      Artifact aArt =
            ArtifactCache.getActive(relationLink.getArtifactId(RelationSide.SIDE_A),
                  relationLink.getBranch(RelationSide.SIDE_A));
      if (aArt != null) {
         setOrderValues(aArt, relationLink.getRelationType(), RelationSide.SIDE_B, markAsNotDirty);
      }
      Artifact bArt =
            ArtifactCache.getActive(relationLink.getArtifactId(RelationSide.SIDE_B),
                  relationLink.getBranch(RelationSide.SIDE_B));
      if (bArt != null) {
         setOrderValues(bArt, relationLink.getRelationType(), RelationSide.SIDE_A, markAsNotDirty);
      }
   }

   private static void setOrderValues(Artifact sourceArtifact, RelationType relationType, RelationSide side, boolean markAsNotDirty) {
      if (relationType.isOrdered()) {
         List<RelationLink> selectedRelations = relationsByType.get(sourceArtifact, relationType);
         if (selectedRelations == null) {
            return;
         }
         int lastArtId = LINKED_LIST_KEY;
         for (RelationLink link : selectedRelations) {
            if (!link.isDeleted() && link.getSide(sourceArtifact) == side.oppositeSide()) {
               if (link.getOrder(side) != lastArtId) {
                  link.setOrder(side, lastArtId);
                  if (markAsNotDirty) {
                     link.setNotDirty();
                  }
               }
               lastArtId = link.getArtifactId(side);
            }
         }
      }
   }
}