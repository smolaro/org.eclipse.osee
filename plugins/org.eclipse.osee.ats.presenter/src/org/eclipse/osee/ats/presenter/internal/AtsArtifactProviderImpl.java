/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.presenter.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.ui.api.search.AtsArtifactProvider;
import org.eclipse.osee.display.presenter.ArtifactProviderImpl;
import org.eclipse.osee.display.presenter.Utility;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsTypes;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.search.QueryFactory;

/**
 * @author John R. Misinco
 */
public class AtsArtifactProviderImpl extends ArtifactProviderImpl implements AtsArtifactProvider {

   private static final String FILTER_KEYWORD = "Apply_Filter";

   public AtsArtifactProviderImpl(Log logger, ExecutorAdmin executorAdmin, QueryFactory queryFactory, OrcsTypes orcsTypes) {
      super(logger, executorAdmin, queryFactory, orcsTypes);
   }

   @Override
   public Iterable<ArtifactReadable> getPrograms() throws OseeCoreException {
      ResultSet<ArtifactReadable> programs = null;
      ArtifactReadable webProgramsArtifact =
         getArtifactByArtifactToken(AtsUtilCore.getAtsBranch(), AtsArtifactToken.WebPrograms);

      if (webProgramsArtifact != null) {
         programs = getRelatedArtifacts(webProgramsArtifact, CoreRelationTypes.Universal_Grouping__Members);
      }

      setFilterAllTypesAllowed(areProgramsFilterClean(webProgramsArtifact));
      return Utility.sort(programs);
   }

   private boolean areProgramsFilterClean(ArtifactReadable webProgram) throws OseeCoreException {
      List<String> filterData = webProgram.getAttributeValues(CoreAttributeTypes.Description);
      for (String att : filterData) {
         if (att.equals(FILTER_KEYWORD)) {
            return false;
         }
      }
      return true;
   }

   @Override
   public Iterable<ArtifactReadable> getBuilds(String programGuid) throws OseeCoreException {
      ArtifactReadable teamDef = null;
      ArtifactReadable programArtifact = getArtifactByGuid(AtsUtilCore.getAtsBranch(), programGuid);
      if (programArtifact != null) {
         teamDef = getRelatedArtifact(programArtifact, CoreRelationTypes.SupportingInfo_SupportingInfo);
      }

      Iterable<ArtifactReadable> toReturn;
      if (teamDef != null) {
         ResultSet<ArtifactReadable> relatedArtifacts =
            getRelatedArtifacts(teamDef, AtsRelationTypes.TeamDefinitionToVersion_Version);
         Iterator<ArtifactReadable> iterator = relatedArtifacts.iterator();
         while (iterator.hasNext()) {
            ArtifactReadable art = iterator.next();
            String baselineBranchUuid = art.getSoleAttributeValue(AtsAttributeTypes.BaselineBranchUuid, "");
            if (!Strings.isValid(baselineBranchUuid)) {
               iterator.remove();
            }
         }
         toReturn = Utility.sort(relatedArtifacts);
      } else {
         toReturn = Collections.emptyList();
      }
      return toReturn;
   }

   @Override
   public long getBaselineBranchUuid(String buildArtGuid) throws OseeCoreException {
      long uuid = 0;
      ArtifactReadable buildArtifact = getArtifactByGuid(AtsUtilCore.getAtsBranch(), buildArtGuid);
      if (buildArtifact != null) {
         uuid = buildArtifact.getSoleAttributeValue(AtsAttributeTypes.BaselineBranchUuid, 0L);
      }
      return uuid;
   }

}
