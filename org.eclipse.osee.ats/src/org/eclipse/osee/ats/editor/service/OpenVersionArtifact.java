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
package org.eclipse.osee.ats.editor.service;

import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.editor.SMAManager;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * @author Donald G. Dunne
 */
public class OpenVersionArtifact extends WorkPageService {

   Action action;

   public OpenVersionArtifact(SMAManager smaMgr) {
      super(smaMgr);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.service.WorkPageService#getName()
    */
   @Override
   public String getName() {
      return "Open Targeted for Version";
   }

   private void performOpen() {
      try {
         if (((TeamWorkFlowArtifact) smaMgr.getSma()).getTargetedForVersion() != null) ArtifactEditor.editArtifact(((TeamWorkFlowArtifact) smaMgr.getSma()).getTargetedForVersion());
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.service.WorkPageService#createToolbarService()
    */
   @Override
   public Action createToolbarService() {
      if (!(smaMgr.getSma() instanceof TeamWorkFlowArtifact)) return null;
      action = new Action(getName(), Action.AS_PUSH_BUTTON) {
         public void run() {
            performOpen();
         }
      };
      action.setToolTipText(getName());
      action.setImageDescriptor(AtsPlugin.getInstance().getImageDescriptor("version.gif"));
      return action;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.service.WorkPageService#refresh()
    */
   @Override
   public void refresh() {
      if (action == null) return;
      boolean enabled = false;
      try {
         enabled = ((TeamWorkFlowArtifact) smaMgr.getSma()).getTargetedForVersion() != null;
      } catch (Exception ex) {
         OSEELog.logException(AtsPlugin.class, ex, true);
      }
      action.setEnabled(enabled);
   }

}
