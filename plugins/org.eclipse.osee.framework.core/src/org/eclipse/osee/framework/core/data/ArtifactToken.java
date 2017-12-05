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
package org.eclipse.osee.framework.core.data;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.NamedId;
import org.eclipse.osee.framework.jdk.core.type.NamedIdBase;
import org.eclipse.osee.framework.jdk.core.type.NamedIdSerializer;
import org.eclipse.osee.framework.jdk.core.util.GUID;

/**
 * @author Ryan D. Brooks
 * @author Donald G. Dunne
 */
@JsonSerialize(using = NamedIdSerializer.class)
public interface ArtifactToken extends ArtifactId, HasBranch, NamedId {

   default ArtifactTypeId getArtifactTypeId() {
      return ArtifactTypeId.SENTINEL;
   }

   default boolean isTypeEqual(ArtifactTypeId artifactType) {
      return artifactType.equals(getArtifactTypeId());
   }

   public static ArtifactToken valueOf(Id id, BranchId branch) {
      return valueOf(id.getId(), GUID.create(), null, branch, ArtifactTypeId.SENTINEL);
   }

   public static ArtifactToken valueOf(ArtifactId id, String name) {
      return valueOf(id.getId(), GUID.create(), name, BranchId.SENTINEL, ArtifactTypeId.SENTINEL);
   }

   public static ArtifactToken valueOf(ArtifactId id, BranchId branch) {
      if (id instanceof ArtifactToken) {
         return valueOf((ArtifactToken) id, branch);
      }
      return valueOf(id.getId(), GUID.create(), "", branch, ArtifactTypeId.SENTINEL);
   }

   public static ArtifactToken valueOf(ArtifactToken token, BranchId branch) {
      return valueOf(token.getId(), token.getGuid(), token.getName(), branch, token.getArtifactTypeId());
   }

   public static ArtifactToken valueOf(long id, BranchId branch) {
      return valueOf(id, GUID.create(), "", branch, ArtifactTypeId.SENTINEL);
   }

   public static ArtifactToken valueOf(long id, String name, BranchId branch) {
      return valueOf(id, GUID.create(), name, branch, ArtifactTypeId.SENTINEL);
   }

   public static ArtifactToken valueOf(long id, String name) {
      return valueOf(id, GUID.create(), name, BranchId.SENTINEL, ArtifactTypeId.SENTINEL);
   }

   public static ArtifactToken valueOf(long id, String name, ArtifactTypeId artifactType) {
      return valueOf(id, GUID.create(), name, BranchId.SENTINEL, artifactType);
   }

   public static ArtifactToken valueOf(long id, String name, BranchId branch, ArtifactTypeId artifactType) {
      return valueOf(id, GUID.create(), name, branch, artifactType);
   }

   public static ArtifactToken valueOf(long id, String guid, String name, BranchId branch, ArtifactTypeId artifactType) {
      final class ArtifactTokenImpl extends NamedIdBase implements ArtifactToken {
         private final BranchId branch;
         private final ArtifactTypeId artifactType;
         private final String guid;

         public ArtifactTokenImpl(Long id, String guid, String name, BranchId branch, ArtifactTypeId artifactType) {
            super(id, name);
            this.branch = branch;
            this.artifactType = artifactType;
            this.guid = guid;
         }

         @Override
         public ArtifactTypeId getArtifactTypeId() {
            return artifactType;
         }

         @Override
         public BranchId getBranch() {
            return branch;
         }

         @Override
         public String getGuid() {
            return guid;
         }

         @Override
         public Long getUuid() {
            return getId();
         }

         @Override
         public boolean equals(Object obj) {
            boolean equal = super.equals(obj);
            if (equal && obj instanceof HasBranch) {
               return isOnSameBranch((HasBranch) obj);
            }
            return equal;
         }
      }
      return new ArtifactTokenImpl(id, guid, name, branch, artifactType);
   }
}