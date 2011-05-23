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

package org.eclipse.osee.framework.database.init;

import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.GlobalXViewerSettings;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.SystemGroup;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * This class creates the common branch and imports the appropriate skynet types. Class should be extended for plugins
 * that require extra skynet types to be added to common.
 * 
 * @author Donald G. Dunne
 */
public abstract class AddCommonBranch implements IDbInitializationTask {
   private final boolean initializeRootArtifacts;

   public AddCommonBranch() {
      this(true);
   }

   public AddCommonBranch(boolean initializeRootArtifacts) {
      this.initializeRootArtifacts = initializeRootArtifacts;
   }

   @Override
   public void run() throws OseeCoreException {
      if (initializeRootArtifacts) {
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.RootArtifact, CoreBranches.SYSTEM_ROOT,
            OseeSystemArtifacts.DEFAULT_HIERARCHY_ROOT_NAME).persist();
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.UniversalGroup, CoreBranches.SYSTEM_ROOT,
            OseeSystemArtifacts.ROOT_ARTIFACT_TYPE_NAME).persist();

         BranchManager.createTopLevelBranch(CoreBranches.COMMON);

         SkynetTransaction transaction = new SkynetTransaction(BranchManager.getCommonBranch(), "Add Common Branch");

         //create everyone group
         Artifact everyonGroup = SystemGroup.Everyone.getArtifact();
         everyonGroup.setSoleAttributeValue(CoreAttributeTypes.DefaultGroup, true);
         everyonGroup.persist(transaction);

         // Create Default Users
         for (IUserToken userToken : SystemUser.values()) {
            UserManager.createUser(userToken, transaction);
         }
         // Create Global Preferences artifact that lives on common branch
         OseeSystemArtifacts.createGlobalPreferenceArtifact().persist(transaction);

         // Create XViewer Customization artifact that lives on common branch
         GlobalXViewerSettings.createCustomArtifact().persist(transaction);

         // Create OseeAdmin group
         SystemGroup.OseeAdmin.getArtifact().persist(transaction);

         transaction.execute();
      }
   }
}
