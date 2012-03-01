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
package org.eclipse.osee.ats.util.widgets.commit;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.osee.ats.core.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.branch.CommitStatus;
import org.eclipse.osee.ats.core.commit.ICommitConfigArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class CommitXManager extends XViewer {

   private final XCommitManager xCommitManager;

   public CommitXManager(Composite parent, int style, XCommitManager xRoleViewer) {
      super(parent, style, new CommitXManagerFactory());
      this.xCommitManager = xRoleViewer;
   }

   @Override
   public void updateMenuActionsForTable() {
      MenuManager mm = getMenuManager();

      mm.insertBefore(MENU_GROUP_PRE, new Separator());
   }

   /**
    * Release resources
    */
   @Override
   public void dispose() {
      getLabelProvider().dispose();
   }

   public List<Object> getSelectedArtifacts() {
      List<Object> arts = new ArrayList<Object>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            arts.add(item.getData());
         }
      }
      return arts;
   }

   /**
    * @return the xUserRoleViewer
    */
   public XCommitManager getXCommitViewer() {
      return xCommitManager;
   }

   @Override
   public void handleDoubleClick() {
      try {
         Object firstSelectedArt = getSelectedArtifacts().iterator().next();
         Branch branch = null;
         String displayName = "";
         ICommitConfigArtifact configArt = null;
         if (firstSelectedArt instanceof ICommitConfigArtifact) {
            configArt = (ICommitConfigArtifact) firstSelectedArt;
            branch = configArt.getParentBranch();
            displayName = configArt.toString();
         } else if (firstSelectedArt instanceof TransactionRecord) {
            TransactionRecord txRecord = (TransactionRecord) firstSelectedArt;
            branch = txRecord.getBranch();
            displayName = txRecord.toString();
         } else {
            throw new OseeArgumentException("Unhandled element type [%s]", firstSelectedArt.getClass().toString());
         }

         CommitStatus commitStatus =
            AtsBranchManagerCore.getCommitStatus(xCommitManager.getTeamArt(), branch, configArt);
         if (commitStatus == CommitStatus.Working_Branch_Not_Created) {
            AWorkbench.popup(commitStatus.getDisplayName(), "Need to create a working branch");
         } else if (commitStatus == CommitStatus.No_Commit_Needed) {
            AWorkbench.popup(commitStatus.getDisplayName(),
               "Destination Branch creation date is after commit to Parent Destination Branch; No Action Needed");
         } else if (commitStatus == CommitStatus.Branch_Not_Configured) {
            AWorkbench.popup(commitStatus.getDisplayName(),
               "Talk to project lead to configure branch for version [" + displayName + "]");
         } else if (commitStatus == CommitStatus.Branch_Commit_Disabled) {
            AWorkbench.popup(commitStatus.getDisplayName(),
               "Talk to project lead as to why commit disabled for version [" + displayName + "]");
         } else if (commitStatus == CommitStatus.Commit_Needed) {
            AtsBranchManager.commitWorkingBranch(xCommitManager.getTeamArt(), true, false, branch,
               AtsBranchManagerCore.isBranchesAllCommittedExcept(xCommitManager.getTeamArt(), branch));
         } else if (commitStatus == CommitStatus.Merge_In_Progress) {
            AtsBranchManager.commitWorkingBranch(xCommitManager.getTeamArt(), true, false, branch,
               AtsBranchManagerCore.isBranchesAllCommittedExcept(xCommitManager.getTeamArt(), branch));
         } else if (commitStatus == CommitStatus.Committed) {
            AtsBranchManager.showChangeReportForBranch(xCommitManager.getTeamArt(), branch);
         } else if (commitStatus == CommitStatus.Committed_With_Merge) {
            MessageDialog dialog =
               new MessageDialog(Displays.getActiveShell(), "Select Report", null,
                  "Both Change Report and Merge Manager exist.\n\nSelect to open.", MessageDialog.QUESTION,
                  new String[] {"Show Change Report", "Show Merge Manager", "Cancel"}, 0);
            int result = dialog.open();
            if (result == 2) {
               return;
            }
            // change report
            if (result == 0) {
               AtsBranchManager.showChangeReportForBranch(xCommitManager.getTeamArt(), branch);
            }
            // merge manager
            else {
               AtsBranchManager.showMergeManager(xCommitManager.getTeamArt(), branch);
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

}
