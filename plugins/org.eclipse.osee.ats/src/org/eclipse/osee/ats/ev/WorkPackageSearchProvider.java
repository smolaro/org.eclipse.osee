/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ev;

import java.util.Collection;
import java.util.LinkedList;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.ats.column.WorkPackageFilterTreeDialog.IWorkPackageProvider;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * Provides work packages from quick search
 *
 * @author Donald G. Dunne
 */
public class WorkPackageSearchProvider implements IWorkPackageProvider {

   public WorkPackageSearchProvider() {
   }

   @Override
   public Collection<IAtsWorkPackage> getActiveWorkPackages() {
      Collection<IArtifactToken> selectableWorkPackageTokens = ArtifactQuery.getArtifactTokenListFromTypeAndActive(
         AtsArtifactTypes.WorkPackage, AtsAttributeTypes.Active, AtsUtilCore.getAtsBranch());
      Collection<IAtsWorkPackage> items = new LinkedList<>();
      for (Artifact art : ArtifactQuery.getArtifactListFromTokens(selectableWorkPackageTokens,
         AtsUtilCore.getAtsBranch())) {
         items.add(AtsClientService.get().getConfigItemFactory().getWorkPackage(art));
      }
      return items;
   }

   @Override
   public Collection<IAtsWorkPackage> getAllWorkPackages() {
      Collection<IArtifactToken> selectableWorkPackageTokens =
         ArtifactQuery.getArtifactTokenListFromType(AtsArtifactTypes.WorkPackage, AtsUtilCore.getAtsBranch());
      Collection<IAtsWorkPackage> items = new LinkedList<>();
      for (Artifact art : ArtifactQuery.getArtifactListFromTokens(selectableWorkPackageTokens,
         AtsUtilCore.getAtsBranch())) {
         items.add(AtsClientService.get().getConfigItemFactory().getWorkPackage(art));
      }
      return items;
   }

}
