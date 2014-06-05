/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.ds;

import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.orcs.data.HasLocalId;

/**
 * @author Roberto E. Escobar
 */
public interface BranchData extends HasLocalId<Long> {

   void setUuid(Long uuid);

   Long getUuid();

   String getName();

   void setName(String name);

   int getAssociatedArtifactId();

   void setAssociatedArtifactId(int artId);

   int getBaseTransaction();

   void setBaseTransaction(int baseTx);

   int getSourceTransaction();

   void setSourceTransaction(int sourceTx);

   long getParentBranch();

   void setParentBranch(long parent);

   boolean hasParentBranch();

   BranchArchivedState getArchiveState();

   void setArchiveState(BranchArchivedState state);

   BranchState getBranchState();

   void setBranchState(BranchState state);

   BranchType getBranchType();

   void setBranchType(BranchType type);

   boolean isInheritAccessControl();

   void setInheritAccessControl(boolean inheritAccessControl);

}
