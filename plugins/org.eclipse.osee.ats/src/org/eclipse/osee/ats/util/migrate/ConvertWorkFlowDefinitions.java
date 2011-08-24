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
package org.eclipse.osee.ats.util.migrate;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.workdef.WorkDefinition;
import org.eclipse.osee.ats.workdef.WorkDefinitionFactoryLegacy;
import org.eclipse.osee.ats.workdef.provider.AtsWorkDefinitionProvider;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.ArtifactCheckTreeDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkFlowDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinitionFactory;

public class ConvertWorkFlowDefinitions extends XNavigateItemAction {

   public ConvertWorkFlowDefinitions(XNavigateItem parent) {
      super(parent, "Convert Work Flow Definition(s)", AtsImage.WORK_DEFINITION);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws Exception {
      XResultData resultData = new XResultData();

      Map<Artifact, WorkFlowDefinition> artToWorkFlowDefs = new HashMap<Artifact, WorkFlowDefinition>();
      WorkFlowDefinition selectedWorkFlowDef = null;
      for (WorkItemDefinition item : WorkItemDefinitionFactory.getWorkItemDefinitions()) {
         if (item instanceof WorkFlowDefinition) {
            artToWorkFlowDefs.put(((WorkFlowDefinition) item).getArtifact(), (WorkFlowDefinition) item);
            //            if (item.getId().equals(TeamWorkflowDefinition.ID)) {
            //               selectedWorkFlowDef = (WorkFlowDefinition) item;
            //            }
         }
      }

      Map<String, String> idToName = new HashMap<String, String>();
      idToName.put("osee.ats.decisionReview", "WorkDef_Review_Decision.ats");
      idToName.put("osee.ats.goalWorkflow", "WorkDef_Goal.ats");
      idToName.put("osee.ats.peerToPeerReview", "WorkDef_Review_PeerToPeer.ats");
      idToName.put("osee.ats.simpleTeamWorkflow", "WorkDef_Team_Simple.ats");
      idToName.put("osee.ats.taskWorkflow", "WorkDef_Task_Default.ats");
      idToName.put("demo.code", "WorkDef_Team_Demo_Code.ats");
      idToName.put("demo.req", "WorkDef_Team_Demo_Req.ats");
      idToName.put("demo.swdesign", "WorkDef_Team_Demo_SwDesign.ats");
      idToName.put("demo.test", "WorkDef_Team_Demo_Test.ats");

      ArtifactCheckTreeDialog dialog = new ArtifactCheckTreeDialog(artToWorkFlowDefs.keySet());
      if (dialog.open() == 0) {
         for (Object obj : dialog.getResult()) {
            selectedWorkFlowDef = artToWorkFlowDefs.get(obj);
            String filename = idToName.get(selectedWorkFlowDef.getName());
            convert(selectedWorkFlowDef, resultData, filename);
         }
         XResultDataUI.report(resultData, getName());
      }
   }

   private void convert(WorkFlowDefinition workFlowDef, XResultData resultData, String filename) throws OseeCoreException {
      WorkDefinition workDef = WorkDefinitionFactoryLegacy.translateToWorkDefinition(workFlowDef);
      AtsWorkDefinitionProvider.get().convertAndOpenAtsDsl(workDef, resultData, filename);
   }
}
