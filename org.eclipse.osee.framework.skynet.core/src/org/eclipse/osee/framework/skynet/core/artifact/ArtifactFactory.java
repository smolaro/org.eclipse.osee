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

import java.sql.SQLException;
import org.eclipse.osee.framework.skynet.core.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.attribute.ArtifactSubtypeDescriptor;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeToTransactionOperation;
import org.eclipse.osee.framework.skynet.core.change.ModificationType;

public abstract class ArtifactFactory {
   private final int factoryId;

   protected ArtifactFactory(int factoryId) {
      super();

      if (factoryId < 1) {
         throw new IllegalStateException(this + " has not (yet) been registered");
      }
      this.factoryId = factoryId;
   }

   protected boolean compatibleWith(ArtifactSubtypeDescriptor descriptor) {
      return descriptor.getFactory().getFactoryId() == this.factoryId;
   }

   /**
    * Used to create a new artifact (one that has never been saved into the datastore)
    * 
    * @param branch
    * @param artifactType
    * @param guid
    * @param humandReadableId
    * @return
    * @throws SQLException
    * @throws OseeCoreException
    * @deprecated Use {@link #makeNewArtifact(Branch,ArtifactSubtypeDescriptor,String,String,ArtifactProcessor)} instead
    */
   public Artifact makeNewArtifact(Branch branch, ArtifactSubtypeDescriptor artifactType, String guid, String humandReadableId) throws OseeCoreException {
      return makeNewArtifact(branch, artifactType, guid, humandReadableId, null);
   }

   /**
    * Used to create a new artifact (one that has never been saved into the datastore)
    * 
    * @param branch
    * @param artifactType
    * @param guid
    * @param humandReadableId
    * @param earlyArtifactInitialization TODO
    * @return
    * @throws SQLException
    */
   public Artifact makeNewArtifact(Branch branch, ArtifactSubtypeDescriptor artifactType, String guid, String humandReadableId, ArtifactProcessor earlyArtifactInitialization) throws OseeCoreException {
      if (!compatibleWith(artifactType)) {
         throw new IllegalArgumentException("The supplied descriptor is not appropriate for this factory");
      }

      Artifact artifact =
            getArtifactInstance(guid, humandReadableId, artifactType.getFactoryKey(), branch, artifactType);
      if (earlyArtifactInitialization != null) {
         earlyArtifactInitialization.run(artifact);
      }
      AttributeToTransactionOperation.meetMinimumAttributeCounts(artifact);
      artifact.setLinksLoaded();
      artifact.onBirth();
      artifact.onInitializationComplete();

      return artifact;
   }

   public synchronized Artifact loadExisitingArtifact(int artId, int gammaId, String guid, String humandReadableId, String factoryKey, Branch branch, ArtifactSubtypeDescriptor artifactType, int transactionId, ModificationType modType, boolean active) {
      Artifact artifact = getArtifactInstance(guid, humandReadableId, factoryKey, branch, artifactType);

      if (modType == ModificationType.DELETED) {
         artifact.setDeleted(transactionId);
      }

      if (active) {
         artifact.setIds(artId, gammaId);
      } else {
         artifact.setIds(artId, gammaId, transactionId);
      }

      ArtifactCache.cache(artifact);
      return artifact;
   }

   /**
    * Request the factory to create a new instance of the type. The implementation of this method should not result in a
    * call to the persistence manager to acquire the <code>Artifact</code> or else an infinite loop will occur since
    * this method is used by the persistence manager when it needs a new instance of the class to work with and can not
    * come up with it on its own.
    * 
    * @param branch branch on which this instance of this artifact will be associated
    * @return Return artifact reference
    */
   protected abstract Artifact getArtifactInstance(String guid, String humandReadableId, String factoryKey, Branch branch, ArtifactSubtypeDescriptor artifactType);

   public int getFactoryId() {
      return factoryId;
   }

   public String toString() {
      return getClass().getName();
   }
}
