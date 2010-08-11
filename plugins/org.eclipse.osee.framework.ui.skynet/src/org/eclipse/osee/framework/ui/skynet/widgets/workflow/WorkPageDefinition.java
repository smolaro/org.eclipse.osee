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
package org.eclipse.osee.framework.ui.skynet.widgets.workflow;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class WorkPageDefinition extends WorkItemWithChildrenDefinition {

   private String pageName;

   public WorkPageDefinition(String pageName, String pageId, String parentId) {
      this(pageId, pageName, pageId, parentId);
   }

   public WorkPageDefinition(String itemName, String pageName, String pageId, String parentId) {
      super(itemName, pageId, parentId);
      this.pageName = pageName;
   }

   public WorkPageDefinition(Artifact artifact) throws OseeCoreException {
      super(artifact, artifact.getName(), //
         artifact.getSoleAttributeValue(CoreAttributeTypes.WorkPageName, (String) null),//
         artifact.getSoleAttributeValue(CoreAttributeTypes.WorkId, (String) null), //
         artifact.getSoleAttributeValue(CoreAttributeTypes.WorkParentId, (String) null)//
      );
      setType(artifact.getSoleAttributeValue(CoreAttributeTypes.WorkType, (String) null));
      loadWorkDataKeyValueMap(artifact);
      setPageName(artifact.getSoleAttributeValue(CoreAttributeTypes.WorkPageName, (String) null));

   }

   public boolean hasWorkRule(String ruleId) throws OseeCoreException {
      return getWorkItemDefinition(ruleId) != null;
   }

   /**
    * Returns work flow definition with the assumption that WorkFlowDefinition workId = pageWorkId minus pageName
    */
   public WorkFlowDefinition getWorkFlowDefinitionById() throws OseeCoreException {
      String id = getId().replace("." + pageName, "");
      WorkItemDefinition workItemDefinition = WorkItemDefinitionFactory.getWorkItemDefinition(id);
      if (workItemDefinition instanceof WorkFlowDefinition) {
         return (WorkFlowDefinition) WorkItemDefinitionFactory.getWorkItemDefinition(id);
      }
      return null;
   }

   @Override
   public Artifact toArtifact(WriteType writeType) throws OseeCoreException {
      Artifact art = super.toArtifact(writeType);
      // Only store start page if it's part of this definition
      if (pageName != null) {
         art.setSoleAttributeFromString(CoreAttributeTypes.WorkPageName, pageName);
      }
      return art;
   }

   public List<WorkItemDefinition> getWorkItemDefinitionsByType(String workType) throws OseeCoreException {
      List<WorkItemDefinition> wids = new ArrayList<WorkItemDefinition>();
      for (WorkItemDefinition workItemDefinition : getWorkItems(true)) {
         if (workItemDefinition.getType() != null && workItemDefinition.getType().equals(workType)) {
            wids.add(workItemDefinition);
         }
      }
      return wids;
   }

   public boolean isCompletePage() {
      return getPageName().equals("Completed");
   }

   public boolean isCancelledPage() {
      return getPageName().equals("Cancelled");
   }

   public boolean isInstanceof(String workPageDefinitionId) throws OseeCoreException {
      return isInstanceofRecurse(this, workPageDefinitionId);
   }

   private boolean isInstanceofRecurse(WorkPageDefinition workPageDefinition, String workPageDefinitionId) throws OseeCoreException {
      if (workPageDefinition.getId().equals(workPageDefinitionId)) {
         return true;
      }
      if (workPageDefinition.getParent() != null) {
         return isInstanceofRecurse((WorkPageDefinition) workPageDefinition.getParent(), workPageDefinitionId);
      }
      return false;
   }

   @Override
   public IArtifactType getArtifactType() {
      return CoreArtifactTypes.WorkPageDefinition;
   }

   public String getPageName() {
      return pageName;
   }

   public void setPageName(String pageName) {
      this.pageName = pageName;
   }

}
