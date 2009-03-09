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
package org.eclipse.osee.framework.ui.skynet.widgets.xBranch;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Jeff C. Phillips
 */
public class BranchXViewer extends XViewer {
   private final XBranchWidget xBranchViewer;

   public BranchXViewer(Composite parent, int style, XBranchWidget xRoleViewer) {
      super(parent, style, new BranchXViewerFactory());
      this.xBranchViewer = xRoleViewer;
   }

   @Override
   public void handleDoubleClick() {
//      try {
//         if (getSelectedChanges().size() == 0) return;
//
//         Artifact artifact = getSelectedChanges().iterator().next();
//
//         if (artifact != null) {
//            ArrayList<Artifact> artifacts = new ArrayList<Artifact>(1);
//            artifacts.add(artifact);
//
//            if (StaticIdManager.hasValue(UserManager.getUser(),
//                  EditorsPreferencePage.PreviewOnDoubleClickForWordArtifacts)) {
//               RendererManager.previewInJob(artifacts);
//            } else {
//               RendererManager.openInJob(artifacts, PresentationType.GENERALIZED_EDIT);
//            }
//         }
//      } catch (OseeCoreException ex) {
//         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
//      }
   }

//   public ArrayList<Artifact> getSelectedChanges() {
//      ArrayList<Artifact> arts = new ArrayList<Artifact>();
//      TreeItem items[] = getTree().getSelection();
//
//      if (items.length > 0) {
//         for (TreeItem item : items) {
//            Artifact artifact = null;
//            if(item.getData() instanceof IAdaptable){
//               artifact = (Artifact)((IAdaptable)item.getData()).getAdapter(Artifact.class);
//               
//               if(artifact != null){
//                  arts.add(artifact);
//               }
//            }
//         }
//      }
//      return arts;
//   }

   @Override
   protected void createSupportWidgets(Composite parent) {
      super.createSupportWidgets(parent);
      createMenuActions();
   }

   public void createMenuActions() {
      MenuManager mm = getMenuManager();
      mm.createContextMenu(getControl());
      mm.addMenuListener(new IMenuListener() {
         public void menuAboutToShow(IMenuManager manager) {
            updateMenuActions();
         }
      });
   }

   public void updateMenuActions() {
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

   /**
    * @return the xHistoryViewer
    */
   public XBranchWidget getXBrznchViewer() {
      return xBranchViewer;
   }

}
