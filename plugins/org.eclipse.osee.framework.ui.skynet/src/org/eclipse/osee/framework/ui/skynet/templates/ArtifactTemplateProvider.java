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

package org.eclipse.osee.framework.ui.skynet.templates;

import static org.eclipse.osee.framework.skynet.core.artifact.DeletionFlag.EXCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;

/**
 * This provider gets all of its templates from the common branch based on a name created from concatenating the
 * getTemplate parameters together in the various possible combinations
 * 
 * @author Ryan D. Brooks
 */
public class ArtifactTemplateProvider implements ITemplateProvider {
   private HashMap<String, Artifact> templateMap;

   private List<Artifact> templates;

   private synchronized void ensureTemplateCachePopulated() throws OseeCoreException {
      if (templateMap == null) {
         templateMap = new HashMap<String, Artifact>();
         templates =
            ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.RendererTemplate, BranchManager.getCommonBranch());
         for (Artifact art : templates) {
            Collection<Attribute<String>> attrs = art.getAttributes(CoreAttributeTypes.TemplateMatchCriteria);
            for (Attribute<String> attr : attrs) {
               String matchCriteria = attr.getValue();
               Artifact cachedArt = templateMap.get(matchCriteria);
               if (cachedArt == null) {
                  templateMap.put(matchCriteria, art);
               } else { //use the artifact with the higher name value and warn the user that there are duplicate match criteria
                  int value = cachedArt.getName().compareTo(art.getName());
                  if (value < 0) {
                     templateMap.put(matchCriteria, art);
                  }
                  OseeLog.log(
                     SkynetGuiPlugin.class,
                     Level.SEVERE,
                     String.format(
                        "ArtifactTemplateProvider has detected a conflict with 'Template Match Criteria' [%s].  Artifact [%s] will supply the template for all requests with this match criteria.",
                        matchCriteria, templateMap.get(matchCriteria).getName()));

               }
            }
         }
      }
   }

   @Override
   public Artifact getTemplate(IRenderer renderer, Artifact artifact, String presentationType, String option) throws OseeCoreException {
      ensureTemplateCachePopulated();

      Artifact template = getArtifactFromOptionName(option);

      if (template == null) {
         List<String> possibleTemplateNames =
            getPossibleTemplateNamesOrderedBySpecialization(renderer, artifact, presentationType, option);

         for (String name : possibleTemplateNames) {
            template = templateMap.get(name);
            if (template != null) {
               return template;
            }
         }
      } else {
         return template;
      }
      throw new OseeArgumentException(String.format("Unable to find a valid template match for [%s, %s, %s, %s].",
         renderer.toString(), artifact.toString(), presentationType, option));
   }

   private Artifact getArtifactFromOptionName(String name) throws OseeCoreException {
      Artifact toReturn = null;

      if (name == null) {
         return toReturn;
      }
      List<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromName(name, BranchManager.getCommonBranch(), EXCLUDE_DELETED);

      if (!artifacts.isEmpty()) {
         toReturn = artifacts.iterator().next();
      }
      return toReturn;
   }

   private List<String> getPossibleTemplateNamesOrderedBySpecialization(IRenderer renderer, Artifact artifact, String presentationType, String option) throws OseeArgumentException {
      if (renderer == null || presentationType == null) {
         throw new OseeArgumentException(String.format("Invalid renderer[%s] or presentationType[%s]",
            renderer.toString(), presentationType.toString()));
      }
      List<String> list = new ArrayList<String>();

      String rendererId = renderer.getClass().getCanonicalName();
      if (artifact != null && option != null) {
         list.add(rendererId + " " + artifact.getArtifactTypeName() + " " + presentationType + " " + option);
      }
      if (artifact != null) {
         list.add(rendererId + " " + artifact.getArtifactTypeName() + " " + presentationType);
      }
      if (option != null) {
         list.add(rendererId + " " + presentationType + " " + option);
      }

      list.add(rendererId + " " + presentationType);
      return list;
   }

   @Override
   public int getApplicabilityRating(IRenderer rendererId, Artifact artifact, String presentationType, String option) {
      return ITemplateProvider.DEFAULT_MATCH;
   }

   @Override
   public List<Artifact> getAllTemplates() throws OseeCoreException {
      if (templates == null) {
         templates =
            ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.RendererTemplate, BranchManager.getCommonBranch());
      }
      return templates;
   }

}
