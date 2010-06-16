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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Donald G. Dunne
 */
public class StaticIdManager {
   /**
    * Will add the single static id value if it does not already exist. Will also cleanup if more than one exists with
    * same staticId.
    */
   public static void setSingletonAttributeValue(Artifact artifact, String staticId) throws OseeCoreException {
      List<Attribute<String>> attributes = artifact.getAttributes(CoreAttributeTypes.STATIC_ID, staticId);
      if (attributes.isEmpty()) {
         artifact.addAttribute(CoreAttributeTypes.STATIC_ID, staticId);
      } else if (attributes.size() > 1) {
         // keep one of the attributes
         for (int x = 1; x < attributes.size(); x++) {
            Attribute<String> attr = attributes.get(x);
            attr.delete();
         }
      }
      ArtifactCache.cacheByStaticId(staticId, artifact);
   }

   public static void deletedStaticIdAttribute(Artifact artifact, String staticId) throws OseeCoreException {
      List<Attribute<String>> attributes = artifact.getAttributes(CoreAttributeTypes.STATIC_ID, staticId);
      for (Attribute<String> attr : attributes) {
         attr.delete();
      }
      ArtifactCache.cacheByStaticId(staticId, artifact);
   }

   public static boolean hasValue(Artifact artifact, String staticId) throws OseeCoreException {
      return artifact.getAttributesToStringList(CoreAttributeTypes.STATIC_ID).contains(staticId);
   }

   /**
    * Return non-deleted artifacts with staticId. This does a new ArtifactQuery each time it's called.
    */
   public static Set<Artifact> getArtifactsFromArtifactQuery(IArtifactType artifactType, String staticId, IOseeBranch branch) throws OseeCoreException {
      Set<Artifact> artifacts = new HashSet<Artifact>();
      // Retrieve database artifacts if cache has none
      artifacts.addAll(ArtifactQuery.getArtifactListFromTypeAndAttribute(artifactType, CoreAttributeTypes.STATIC_ID,
            staticId,
            branch));
      return artifacts;
   }

   /**
    * Return non-deleted artifacts stored in ArtifactCache.
    * 
    * @param queryIfNotFound if true and artifacts with staticId art not in ArtifactCache, query to find
    */
   public static Set<Artifact> getArtifactsFromArtifactCache(IArtifactType artifactType, String staticId, IOseeBranch branch, boolean queryIfNotFound) throws OseeCoreException {
      Set<Artifact> artifacts = new HashSet<Artifact>();
      // Retrieve cached artifacts first
      for (Artifact artifact : ArtifactCache.getArtifactsByStaticId(staticId, branch)) {
         if (artifact.isOfType(artifactType) && !artifact.isDeleted()) {
            artifacts.add(artifact);
         }
      }
      if (artifacts.size() > 0) {
         OseeLog.log(Activator.class, Level.FINE, "StaticId Load: [" + staticId + "][" + artifactType + "]");
      }
      if (queryIfNotFound && artifacts.isEmpty()) {
         artifacts = getArtifactsFromArtifactQuery(artifactType, staticId, branch);
      }
      return artifacts;
   }

   public static Artifact getSingletonArtifactOrException(IArtifactType artifactType, String staticId, Branch branch) throws OseeCoreException {
      Set<Artifact> artifacts = getArtifactsFromArtifactCache(artifactType, staticId, branch, true);
      // Exception on problems
      if (artifacts.isEmpty()) {
         throw new ArtifactDoesNotExist("Can't find requested artifact \"" + staticId + "\"");
      } else if (artifacts.size() > 1) {
         throw new MultipleArtifactsExist("Expected 1 \"" + staticId + "\" artifact, retrieved " + artifacts.size());
      }
      return artifacts.iterator().next();
   }

   /**
    * Return singleton artifact from ArtifactCache.
    * 
    * @param queryIfNotFound true will perform ArtifactQuery if not found
    */
   public static Artifact getSingletonArtifact(IArtifactType artifactType, String staticId, IOseeBranch branch, boolean queryIfNotFound) throws OseeCoreException {
      if (queryIfNotFound) {
         return getOrCreateSingletonArtifactHelper(artifactType, staticId, branch, false);
      } else {
         Set<Artifact> artifacts = getArtifactsFromArtifactCache(artifactType, staticId, branch, false);
         if (artifacts.size() > 0) {
            return artifacts.iterator().next();
         }
         return null;
      }
   }

   /**
    * Return first artifact with staticId (multiples may exist) or create one if non exist
    */
   public static Artifact getOrCreateSingletonArtifact(IArtifactType artifactType, String staticId, IOseeBranch branch) throws OseeCoreException {
      return getOrCreateSingletonArtifactHelper(artifactType, staticId, branch, true);
   }

   /**
    * Searches for singleton artifact in cache, if not found, queries for existence.
    * 
    * @param create will create artifact and add static if not found
    */
   private static Artifact getOrCreateSingletonArtifactHelper(IArtifactType artifactType, String staticId, IOseeBranch branch, boolean create) throws OseeCoreException {
      Set<Artifact> artifacts = getArtifactsFromArtifactCache(artifactType, staticId, branch, true);
      if (artifacts.isEmpty() && create) {
         Artifact artifact = ArtifactTypeManager.addArtifact(artifactType, branch);
         setSingletonAttributeValue(artifact, staticId);
         return artifact;
      }
      if (artifacts.size() > 0) {
         return artifacts.iterator().next();
      }
      return null;
   }
}