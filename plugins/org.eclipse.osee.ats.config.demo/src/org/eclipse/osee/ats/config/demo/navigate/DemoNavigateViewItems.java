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
package org.eclipse.osee.ats.config.demo.navigate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.config.demo.PopulateDemoActions;
import org.eclipse.osee.ats.config.demo.internal.OseeAtsConfigDemoActivator;
import org.eclipse.osee.ats.health.ValidateAtsDatabase;
import org.eclipse.osee.ats.navigate.IAtsNavigateItem;
import org.eclipse.osee.ats.navigate.SearchNavigateItem;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.version.CreateNewVersionItem;
import org.eclipse.osee.ats.version.ReleaseVersionItem;
import org.eclipse.osee.ats.world.search.ArtifactTypeSearchItem;
import org.eclipse.osee.ats.world.search.ArtifactTypeWithInheritenceSearchItem;
import org.eclipse.osee.ats.world.search.NextVersionSearchItem;
import org.eclipse.osee.ats.world.search.TeamWorldSearchItem;
import org.eclipse.osee.ats.world.search.TeamWorldSearchItem.ReleasedOption;
import org.eclipse.osee.ats.world.search.VersionTargetedForTeamSearchItem;
import org.eclipse.osee.ats.world.search.WorldSearchItem.LoadView;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.OseeUiActivator;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemFolder;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateUrlItem;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.support.test.util.DemoTeam;

/**
 * Provides the ATS Navigator items for the sample XYZ company's teams
 * 
 * @author Donald G. Dunne
 */
public class DemoNavigateViewItems implements IAtsNavigateItem {

   public DemoNavigateViewItems() {
      super();
   }

   private static TeamDefinitionArtifact getTeamDef(DemoTeam team) throws OseeCoreException {
      // Add check to keep exception from occurring for OSEE developers running against production
      if (ClientSessionManager.isProductionDataStore()) {
         return null;
      }
      try {
         return (TeamDefinitionArtifact) ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.TeamDefinition,
            team.name().replaceAll("_", " "), AtsUtil.getAtsBranch());
      } catch (Exception ex) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.SEVERE, ex);
      }
      return null;
   }

   @Override
   public List<XNavigateItem> getNavigateItems(XNavigateItem parentItem) throws OseeCoreException {
      List<XNavigateItem> items = new ArrayList<XNavigateItem>();

      if (OseeUiActivator.areOSEEServicesAvailable().isFalse()) {
         return items;
      }

      // If Demo Teams not configured, ignore these navigate items
      try {
         if (getTeamDef(DemoTeam.Process_Team) == null) {
            return items;
         }
      } catch (Exception ex) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.WARNING, "Demo Teams Not Cofigured", ex);
         return items;
      }
      // If Demo Teams not configured, ignore these navigate items
      try {
         if (getTeamDef(DemoTeam.Process_Team) == null) {
            return items;
         }
      } catch (Exception ex) {
         OseeLog.log(OseeAtsConfigDemoActivator.class, Level.WARNING, "Demo Teams Not Cofigured", ex);
         return items;
      }
      XNavigateItem jhuItem = new XNavigateItemFolder(parentItem, "John Hopkins Univ (JHU)");
      new XNavigateUrlItem(jhuItem, "Open JHU Website - Externally", "http://www.jhu.edu/", true);
      new XNavigateUrlItem(jhuItem, "Open JHU Website - Internally", "http://www.jhu.edu/", false);

      items.add(jhuItem);

      for (DemoTeam team : DemoTeam.values()) {
         try {
            TeamDefinitionArtifact teamDef = getTeamDef(team);
            XNavigateItem teamItems = new XNavigateItemFolder(jhuItem, "JHU " + team.name().replaceAll("_", " "));
            new SearchNavigateItem(teamItems, new TeamWorldSearchItem("Show Open " + teamDef + " Actions",
               Arrays.asList(teamDef), false, false, true, true, null, null, ReleasedOption.Both, null));
            new SearchNavigateItem(teamItems, new TeamWorldSearchItem("Show Open " + teamDef + " Workflows",
               Arrays.asList(teamDef), false, false, false, true, null, null, ReleasedOption.Both, null));
            // Handle all children teams
            for (TeamDefinitionArtifact childTeamDef : Artifacts.getChildrenOfTypeSet(teamDef,
               TeamDefinitionArtifact.class, true)) {
               new SearchNavigateItem(teamItems, new TeamWorldSearchItem("Show Open " + childTeamDef + " Workflows",
                  Arrays.asList(childTeamDef), false, false, false, false, null, null, ReleasedOption.Both, null));
            }
            if (teamDef.isTeamUsesVersions()) {
               if (team.name().contains("SAW")) {
                  new XNavigateUrlItem(teamItems, "Open SAW Website", "http://www.cisst.org/cisst/saw/", false);
               } else if (team.name().contains("CIS")) {
                  new XNavigateUrlItem(teamItems, "Open CIS Website", "http://www.cisst.org/cisst/cis/", false);
               }

               new SearchNavigateItem(teamItems, new NextVersionSearchItem(teamDef, LoadView.WorldEditor));
               new SearchNavigateItem(teamItems, new VersionTargetedForTeamSearchItem(teamDef, null, false,
                  LoadView.WorldEditor));
               new SearchNavigateItem(teamItems, new TeamWorldSearchItem("Show Un-Released Team Workflows",
                  Arrays.asList(teamDef), true, true, false, true, null, null, ReleasedOption.UnReleased, null));
               new ReleaseVersionItem(teamItems, teamDef);
               new CreateNewVersionItem(teamItems, teamDef);
            }
         } catch (Exception ex) {
            OseeLog.log(OseeAtsConfigDemoActivator.class, Level.SEVERE, ex);
         }
      }

      XNavigateItem adminItems = new XNavigateItem(jhuItem, "JHU Admin", FrameworkImage.LASER);

      new SearchNavigateItem(adminItems, new ArtifactTypeSearchItem("Show all Actions", AtsArtifactTypes.Action));
      new SearchNavigateItem(adminItems, new ArtifactTypeSearchItem("Show all Decision Review",
         AtsArtifactTypes.DecisionReview));
      new SearchNavigateItem(adminItems, new ArtifactTypeSearchItem("Show all PeerToPeer Review",
         AtsArtifactTypes.PeerToPeerReview));
      new SearchNavigateItem(adminItems, new ArtifactTypeWithInheritenceSearchItem("Show all Team Workflows",
         AtsArtifactTypes.TeamWorkflow));
      new SearchNavigateItem(adminItems, new ArtifactTypeSearchItem("Show all Tasks", AtsArtifactTypes.Task));

      XNavigateItem healthItems = new XNavigateItem(adminItems, "Health", FrameworkImage.LASER);
      new ValidateAtsDatabase(healthItems);

      XNavigateItem demoItems = new XNavigateItem(adminItems, "Demo Data", PluginUiImage.ADMIN);
      new PopulateDemoActions(demoItems);

      return items;
   }
}
