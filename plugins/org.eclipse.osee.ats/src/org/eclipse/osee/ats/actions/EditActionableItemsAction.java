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
package org.eclipse.osee.ats.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.action.ActionArtifact;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class EditActionableItemsAction extends Action {

   private final TeamWorkFlowArtifact teamWf;

   public EditActionableItemsAction(TeamWorkFlowArtifact teamWf) {
      super("Add/Update Actionable Items/Workflows");
      this.teamWf = teamWf;
      setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.ACTIONABLE_ITEM));
   }

   @Override
   public void run() {
      try {
         ActionArtifact parentAction = teamWf.getParentActionArtifact();
         if (parentAction == null) {
            AWorkbench.popup("No Parent Action; Aborting");
            return;
         }
         AtsUtil.editActionableItems(parentAction);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

}
