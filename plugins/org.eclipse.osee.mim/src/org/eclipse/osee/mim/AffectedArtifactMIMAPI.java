/*********************************************************************
 * Copyright (c) 2022 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
package org.eclipse.osee.mim;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.mim.types.ArtifactMatch;
import org.eclipse.osee.mim.types.PLGenericDBObject;

public interface AffectedArtifactMIMAPI<T extends PLGenericDBObject> {

   Collection<ArtifactMatch> getAffectedArtifacts(BranchId branch, ArtifactId relatedId);
}
