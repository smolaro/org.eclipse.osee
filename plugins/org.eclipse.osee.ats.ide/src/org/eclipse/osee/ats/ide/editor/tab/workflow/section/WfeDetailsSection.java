/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.ide.editor.tab.workflow.section;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.access.IAtsAccessService;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.workflow.IAtsAction;
import org.eclipse.osee.ats.ide.editor.WorkflowEditor;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.core.data.AccessContextToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Donald G. Dunne
 */
public class WfeDetailsSection extends SectionPart {

   private Browser browser;
   private final WorkflowEditor editor;
   private boolean sectionCreated = false;

   public WfeDetailsSection(WorkflowEditor editor, Composite parent, FormToolkit toolkit, int style) {
      super(parent, toolkit, style | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
      this.editor = editor;
   }

   @Override
   public void initialize(IManagedForm form) {
      super.initialize(form);
      Section section = getSection();
      section.setText("Details");
      section.setLayout(new GridLayout());
      section.setExpanded(false);
      section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      // Only load when users selects section
      section.addListener(SWT.Activate, new Listener() {

         @Override
         public void handleEvent(Event e) {
            createSection();
         }
      });
   }

   private synchronized void createSection() {
      if (!sectionCreated) {
         final FormToolkit toolkit = getManagedForm().getToolkit();
         Composite composite = toolkit.createComposite(getSection(), toolkit.getBorderStyle() | SWT.WRAP);
         composite.setLayout(ALayout.getZeroMarginLayout());
         composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         composite.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
               if (Widgets.isAccessible(browser)) {
                  browser.dispose();
               }
            }
         });

         browser = new Browser(composite, SWT.NONE);
         GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
         gd.widthHint = 200;
         gd.heightHint = 300;
         browser.setLayoutData(gd);

         getSection().setClient(composite);
         toolkit.paintBordersFor(composite);
         sectionCreated = true;
      }

      refresh();
   }

   @Override
   public void refresh() {
      if (Widgets.isAccessible(browser)) {
         IAtsWorkItem workItem = editor.getWorkItem();

         try {
            Map<String, String> smaDetails = Artifacts.getDetailsKeyValues((Artifact) workItem.getStoreObject());
            addSMADetails(workItem, smaDetails);

            FontData systemFont = browser.getDisplay().getSystemFont().getFontData()[0];
            String formattedDetails =
               Artifacts.getDetailsFormText(smaDetails, systemFont.getName(), systemFont.getHeight());
            browser.setText(formattedDetails);
         } catch (Exception ex) {
            browser.setText(Lib.exceptionToString(ex));
         }
         getManagedForm().reflow(true);
      }
   }

   private void addSMADetails(IAtsWorkItem workItem, Map<String, String> details) {
      details.put("Workflow Definition", workItem.getWorkDefinition().getName());
      IAtsAction parentAction = workItem.getParentAction();
      if (parentAction == null) {
         details.put("Action Id", "No Parent Action");
      } else {
         details.put("Action Id", parentAction.getAtsId());
      }
      if (!workItem.isOfType(AtsArtifactTypes.TeamWorkflow) && workItem.getParentTeamWorkflow() != null) {
         details.put("Parent Team Workflow Id", workItem.getParentTeamWorkflow().getAtsId());
      }
      if (workItem.isOfType(AtsArtifactTypes.TeamWorkflow)) {
         details.put("Working Branch Access Context Id", getAccessContextId((TeamWorkFlowArtifact) workItem));
      }
   }

   private String getAccessContextId(TeamWorkFlowArtifact workflow) {
      String message;
      IAtsAccessService accessService = AtsApiService.get().getAtsAccessService();
      BranchId workingBranch = null;
      try {
         workingBranch = workflow.getWorkingBranch();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      Collection<? extends AccessContextToken> ids = null;
      if (workingBranch == null) {
         try {
            // get what would be if branch created
            ids = accessService.getFromWorkflow(workflow);
            message = ids.toString();
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            message = String.format("Error getting context id [%s]", ex.getMessage());
         }
      } else {
         try {
            ids = accessService.getContextIds(workingBranch);
            message = ids.toString();
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            message = String.format("Error getting context id [%s]", ex.getMessage());
         }
      }
      return message;
   }

   public boolean isDisposed() {
      return browser == null || browser.isDisposed();
   }

}
