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
package org.eclipse.osee.ats.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.editor.service.WorkPageService;
import org.eclipse.osee.ats.workflow.AtsWorkPage;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Donald G. Dunne
 */
/**
 * @author Donald G. Dunne
 */
public abstract class AtsStateItem implements IAtsStateItem {

   public static String ALL_STATE_IDS = "ALL";

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#committing(org.eclipse.osee.ats.editor.SMAManager)
    */
   public Result committing(SMAManager smaMgr) {
      return Result.TrueResult;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#getBranchShortName(org.eclipse.osee.ats.editor.SMAManager)
    */
   public String getBranchShortName(SMAManager smaMgr) {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#getId()
    */
   public String getId() {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#getOverrideTransitionToAssignees(org.eclipse.osee.ats.editor.SMAWorkFlowSection)
    */
   public Collection<User> getOverrideTransitionToAssignees(SMAWorkFlowSection section) {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#getOverrideTransitionToStateName(org.eclipse.osee.ats.editor.SMAWorkFlowSection)
    */
   public String getOverrideTransitionToStateName(SMAWorkFlowSection section) {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#getServices(org.eclipse.osee.ats.editor.SMAManager, org.eclipse.osee.ats.workflow.AtsWorkPage, org.eclipse.osee.framework.ui.skynet.XFormToolkit, org.eclipse.osee.ats.editor.SMAWorkFlowSection)
    */
   public List<WorkPageService> getServices(SMAManager smaMgr, AtsWorkPage page, XFormToolkit toolkit, SMAWorkFlowSection session) {
      return new ArrayList<WorkPageService>();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#pageCreated(org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.osee.ats.workflow.AtsWorkPage, org.eclipse.osee.ats.editor.SMAManager, org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener, boolean)
    */
   public Result pageCreated(FormToolkit toolkit, AtsWorkPage page, SMAManager smaMgr, XModifiedListener xModListener, boolean isEditable) {
      return Result.TrueResult;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#transitioned(org.eclipse.osee.ats.editor.SMAManager, java.lang.String, java.lang.String, java.util.Collection)
    */
   public void transitioned(SMAManager smaMgr, String fromState, String toState, Collection<User> toAssignees) {
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#transitioning(org.eclipse.osee.ats.editor.SMAManager, java.lang.String, java.lang.String, java.util.Collection)
    */
   public Result transitioning(SMAManager smaMgr, String fromState, String toState, Collection<User> toAssignees) throws Exception {
      return Result.TrueResult;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#widgetModified(org.eclipse.osee.ats.editor.SMAWorkFlowSection, org.eclipse.osee.framework.ui.skynet.widgets.XWidget)
    */
   public void widgetModified(SMAWorkFlowSection section, XWidget xWidget) {
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#xWidgetCreated(org.eclipse.osee.framework.ui.skynet.widgets.XWidget, org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.osee.ats.workflow.AtsWorkPage, org.eclipse.osee.framework.skynet.core.artifact.Artifact, org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener, boolean)
    */
   public void xWidgetCreated(XWidget xWidget, FormToolkit toolkit, AtsWorkPage page, Artifact art, XModifiedListener xModListener, boolean isEditable) {
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#xWidgetCreating(org.eclipse.osee.framework.ui.skynet.widgets.XWidget, org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.osee.ats.workflow.AtsWorkPage, org.eclipse.osee.framework.skynet.core.artifact.Artifact, org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener, boolean)
    */
   public Result xWidgetCreating(XWidget xWidget, FormToolkit toolkit, AtsWorkPage page, Artifact art, XModifiedListener xModListener, boolean isEditable) {
      return Result.TrueResult;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.IAtsStateItem#isAccessControlViaAssigneesEnabledForBranching()
    */
   public boolean isAccessControlViaAssigneesEnabledForBranching() {
      return false;
   }

}
