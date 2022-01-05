/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.ats.core.column;

import org.eclipse.osee.ats.api.column.IAtsColumnId;

/**
 * NOTE: column ids can NOT be changed without affecting the stored customizations<br/>
 * <br/>
 * Architecture/Design<br/>
 * <br/>
 * - First, declare a column id. This needs to be a unique string that will be used to store/restore customizations<br/>
 * <br/>
 * - Second, declare a column token for each column id. This either specifies an attr column (preferred) or value
 * column.<br/>
 * - If attr column, this is what brings the id together with column data and the attr type token<br/>
 * <br/>
 * - Third, add to AtsColumnService or an AtsColumnProvider to resolve and instantiate columns<br/>
 * <br/>
 * - Fourth, add to Register column in WorldXViewerFactory(ATS bundle) or an IAtsWorldEditorItem (other bundles)<br/>
 *
 * @author Donald G. Dunne
 */
public enum AtsColumnId implements IAtsColumnId {

   ActionableItem("ats.column.actionableItems"),
   ActivityId("ats.column.activityId"),
   AgileTeamPoints("ats.agileTeam.Points"),
   Assignees("ats.column.assignees"),
   AtsId("ats.id"),
   SiblingAtsIds("ats.sibling.id"),
   ChangeType("ats.column.changetype"),
   CreatedDate("ats.column.createdDate"),
   CompletedDate("ats.column.completedDate"),
   CancelledDate("ats.column.cancelledDate"),
   CancelledBy("ats.column.cancelledBy"),
   CompletedBy("ats.column.completedBy"),
   CompletedCancelledBy("ats.column.cmpCnclBy"),
   CompletedCancelledDate("ats.column.cmpCnclDate"),
   CancelledReason("ats.column.cancelledReason"),
   CancelledReasonDetails("ats.column.cancelledReasonDetails"),
   CancelReason("ats.column.cancelReason"),
   CrashOrBlankDisplay("ats.column.crash.or.blank.display"),
   AgileFeatureGroup("ats.column.agileFeatureGroup"),
   Insertion("ats.column.insertion"),
   InsertionActivity("ats.column.insertionActivity"),
   LegacyPcrId("ats.column.legacyPcr"),
   Name("framework.artifact.name"),
   Notes("ats.column.notes"),
   ParentTitle("ats.column.parentTitle"),
   PercentCompleteWorkflow("ats.column.workflowPercentComplete"),
   PercentCompleteTasks("ats.column.taskPercentComplete"),
   Points("ats.column.points"),
   Priority("ats.column.priority"),
   ReleaseDate("ats.column.releaseDate"),
   State("ats.column.state"),
   SprintOrder("ats.column.sprintOrder"),
   TaskToRelatedArtifactType("ats.column.taskToRelArtType"),
   Team("ats.column.team"),
   TargetedVersion("ats.column.versionTarget"),
   FoundInVersion("ats.column.foundInVersion"),
   Title("framework.artifact.name.Title"),
   Type("ats.column.type"),
   Id("framework.id"),
   UnPlannedWork("ats.Unplanned Work"),
   WorkDefinition("ats.column.workDefinition"),
   WorkPackageName("ats.column.workPackageName"),
   WorkPackageId("ats.column.workPackageId"),
   WorkPackageType("ats.column.workPackageType"),
   WorkPackageProgram("ats.column.workPackageProgram"),
   Implementers("ats.column.implementer"),;

   private final String id;

   private AtsColumnId(String id) {
      this.id = id;
   }

   @Override
   public String getId() {
      return id;
   }

}
