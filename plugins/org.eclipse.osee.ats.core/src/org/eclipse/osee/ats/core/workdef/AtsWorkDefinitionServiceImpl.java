/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.ats.core.workdef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.agile.IAgileBacklog;
import org.eclipse.osee.ats.api.agile.IAgileSprint;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.review.IAtsAbstractReview;
import org.eclipse.osee.ats.api.review.IAtsDecisionReview;
import org.eclipse.osee.ats.api.review.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.team.ITeamWorkflowProvider;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workdef.AtsWorkDefinitionToken;
import org.eclipse.osee.ats.api.workdef.AtsWorkDefinitionTokens;
import org.eclipse.osee.ats.api.workdef.IAtsCompositeLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionBuilder;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;
import org.eclipse.osee.ats.api.workdef.model.RuleDefinitionOption;
import org.eclipse.osee.ats.api.workflow.IAtsGoal;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.INewActionListener;
import org.eclipse.osee.ats.api.workflow.ITeamWorkflowProvidersLazy;
import org.eclipse.osee.ats.core.agile.AgileItem;
import org.eclipse.osee.ats.core.workdef.operations.ValidateWorkDefinitionsOperation;
import org.eclipse.osee.ats.core.workflow.TeamWorkflowProviders;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.NamedIdBase;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class AtsWorkDefinitionServiceImpl implements IAtsWorkDefinitionService {

   private final AtsApi atsApi;
   private final ITeamWorkflowProvidersLazy teamWorkflowProvidersLazy;
   private final Map<String, IAtsWorkDefinition> workDefNameToWorkDef = new HashMap<>();
   private final Map<IAtsWorkItem, IAtsWorkDefinition> bootstrappingWorkItemToWorkDefCache = new HashMap<>();
   private final Set<IAtsWorkItem> logOnce = new HashSet<>();

   public AtsWorkDefinitionServiceImpl(AtsApi atsApi, ITeamWorkflowProvidersLazy teamWorkflowProvidersLazy) {
      this.atsApi = atsApi;
      this.teamWorkflowProvidersLazy = teamWorkflowProvidersLazy;
   }

   @Override
   public IAtsWorkDefinition getWorkDefinitionFromAsObject(IAtsObject atsObject, AttributeTypeToken workDefAttrTypeId) {
      IAtsWorkDefinition workDefinition = null;
      String workDefIdStr =
         atsApi.getAttributeResolver().getSoleAttributeValueAsString(atsObject, workDefAttrTypeId, "");
      if (Strings.isNumeric(workDefIdStr)) {
         workDefinition = getWorkDefinition(Long.valueOf(workDefIdStr));
      }
      return workDefinition;
   }

   private IAtsWorkDefinition getWorkDefinitionFromAsObject(IAtsObject atsObject) {
      return getWorkDefinitionFromAsObject(atsObject, AtsAttributeTypes.WorkflowDefinitionReference);
   }

   @Override
   public IAtsWorkDefinition getWorkDefinition(IAtsWorkItem workItem) {
      // check cache used for initial creation of work item
      IAtsWorkDefinition workDefinition = bootstrappingWorkItemToWorkDefCache.get(workItem);
      if (workDefinition != null) {
         return workDefinition;
      }
      try {
         workDefinition = getWorkDefinitionFromAsObject(workItem);
      } catch (Exception ex) {
         throw new OseeWrappedException(ex, "Error getting work definition for work item %s",
            workItem.toStringWithId());
      }
      if (workDefinition == null && atsApi.isWorkDefAsName()) {
         try {
            return computeWorkDefinition(workItem);
         } catch (Exception ex) {
            throw new OseeWrappedException(ex, "Error getting work definition for work item %s",
               workItem.toStringWithId());
         }
      }
      return workDefinition;
   }

   @Override
   public IAtsWorkDefinition getWorkDefinitionByName(String name) {
      for (IAtsWorkDefinition workDef : atsApi.getWorkDefinitionProviderService().getAll()) {
         if (workDef.getName().equals(name)) {
            return workDef;
         }
      }
      if (Strings.isNumeric(name)) {
         throw new OseeArgumentException("Can't get work def, but is numeric [%s], probably wrong method", name);
      }
      return null;
   }

   private IAtsWorkDefinition getWorkDefinitionFromArtifactsAttributeValue(IAtsWorkItem workItem) {
      IAtsWorkDefinition workDefinition = getWorkDefinitionFromAsObject(workItem);
      if (workDefinition == null && atsApi.isWorkDefAsName()) {
         // If this artifact specifies it's own workflow definition, use it
         String workFlowDefName = null;
         Collection<Object> attributeValues =
            atsApi.getAttributeResolver().getAttributeValues(workItem, AtsAttributeTypes.WorkflowDefinition);
         if (!attributeValues.isEmpty()) {
            workFlowDefName = (String) attributeValues.iterator().next();
         }
         if (Strings.isValid(workFlowDefName)) {
            workDefinition = getWorkDefinitionByName(workFlowDefName);
         }
      }
      return workDefinition;
   }

   private IAtsWorkDefinition getWorkDefinitionFromArtifactsAttributeValue(IAtsTeamDefinition teamDef) {
      IAtsWorkDefinition workDefinition = getWorkDefinitionFromAsObject(teamDef);
      if (workDefinition == null && atsApi.isWorkDefAsName()) {
         String workFlowDefName =
            atsApi.getAttributeResolver().getSoleAttributeValue(teamDef, AtsAttributeTypes.WorkflowDefinition, "");
         if (Strings.isValid(workFlowDefName)) {
            workDefinition = getWorkDefinitionByName(workFlowDefName);
         }
      }
      return workDefinition;
   }

   private IAtsWorkDefinition getTaskWorkDefinitionFromArtifactsAttributeValue(IAtsTeamDefinition teamDef) {
      IAtsWorkDefinition workDefinition =
         getWorkDefinitionFromAsObject(teamDef, AtsAttributeTypes.RelatedTaskWorkflowDefinitionReference);
      if (workDefinition == null && atsApi.isWorkDefAsName()) {
         // If this artifact specifies it's own workflow definition, use it
         String workFlowDefName = atsApi.getAttributeResolver().getSoleAttributeValueAsString(teamDef,
            AtsAttributeTypes.RelatedTaskWorkflowDefinition, "");
         if (Strings.isValid(workFlowDefName)) {
            workDefinition = getWorkDefinitionByName(workFlowDefName);
         }
      }
      return workDefinition;
   }

   /**
    * Look at team def's attribute for Work Definition setting, otherwise, walk up team tree for setting
    */
   private IAtsWorkDefinition getWorkDefinitionFromTeamDefinitionAttributeInherited(IAtsTeamDefinition teamDef) {
      IAtsWorkDefinition workDef = getWorkDefinitionFromArtifactsAttributeValue(teamDef);
      if (workDef != null) {
         return workDef;
      }
      IAtsTeamDefinition parentArt = atsApi.getTeamDefinitionService().getParentTeamDef(teamDef);
      if (parentArt != null) {
         workDef = getWorkDefinitionFromTeamDefinitionAttributeInherited(parentArt);
      }
      return workDef;
   }

   /**
    * Return the WorkDefinition that would be assigned to a new Task. This is not necessarily the actual WorkDefinition
    * used because it can be overridden once the Task artifact is created.
    */
   @Override
   public IAtsWorkDefinition computedWorkDefinitionForTaskNotYetCreated(IAtsTeamWorkflow teamWf) {
      Conditions.assertNotNull(teamWf, "Team Workflow can not be null");
      IAtsWorkDefinition workDefinition = null;
      for (ITeamWorkflowProvider provider : TeamWorkflowProviders.getTeamWorkflowProviders()) {
         AtsWorkDefinitionToken workDefTok = provider.getRelatedTaskWorkflowDefinitionId(teamWf);
         if (workDefTok != null && workDefTok.isValid()) {
            workDefinition = getWorkDefinition(workDefTok);
            break;
         }
         if (atsApi.isWorkDefAsName()) {
            AtsWorkDefinitionToken workDefT = provider.getRelatedTaskWorkflowDefinitionId(teamWf);
            if (workDefT != null && workDefT.isValid()) {
               workDefinition = getWorkDefinition(workDefT);
               break;
            }
         }
      }
      if (workDefinition == null) {
         // Else If parent TeamWorkflow's IAtsTeamDefinition has a related task definition workflow id, use it
         workDefinition = getTaskWorkDefinitionFromArtifactsAttributeValue(teamWf.getTeamDefinition());
      }
      if (workDefinition == null) {
         workDefinition =
            atsApi.getWorkDefinitionService().getWorkDefinition(AtsWorkDefinitionTokens.WorkDef_Task_Default);
      }
      return workDefinition;
   }

   @Override
   public IAtsWorkDefinition computeWorkDefinition(IAtsWorkItem workItem) {
      return computeWorkDefinition(workItem, true);
   }

   @Override
   public IAtsWorkDefinition computeWorkDefinition(IAtsWorkItem workItem, boolean useAttr) {
      // If this artifact specifies it's own workflow definition, use it
      IAtsWorkDefinition workDef = null;
      if (useAttr) {
         workDef = getWorkDefinitionFromArtifactsAttributeValue(workItem);
      }
      if (workDef == null) {
         if (!logOnce.contains(workItem)) {
            OseeLog.log(AtsWorkDefinitionServiceImpl.class, Level.INFO,
               "No WorkDef attr for " + workItem.toStringWithId());
            logOnce.add(workItem);
         }
         // Tasks should never be needed once a database is converted to store work def as attribute
         if (workItem instanceof IAtsTask && ((IAtsTask) workItem).getParentTeamWorkflow() != null) {
            workDef = computedWorkDefinitionForTaskNotYetCreated(((IAtsTask) workItem).getParentTeamWorkflow());
         }
         if (workDef == null) {
            // Check extensions for definition handling
            for (ITeamWorkflowProvider provider : teamWorkflowProvidersLazy.getProviders()) {
               AtsWorkDefinitionToken workFlowDefId = provider.getWorkflowDefinitionId(workItem);
               if (workFlowDefId != null && workFlowDefId.isValid()) {
                  workDef = getWorkDefinition(workFlowDefId);
               }
            }
            if (workDef == null) {
               if (workItem instanceof AgileItem) {
                  workItem = atsApi.getWorkItemService().getWorkItem(((AgileItem) workItem).getId());
               }

               // Otherwise, use workflow defined by attribute of WorkflowDefinition
               // Note: This is new.  Old TeamDefs got workflow off relation
               if (workItem instanceof IAtsTeamWorkflow) {
                  IAtsTeamDefinition teamDef = ((IAtsTeamWorkflow) workItem).getTeamDefinition();
                  Conditions.assertNotNull(teamDef, "Team Def can not be null for %s.  Re-convert?",
                     workItem.toStringWithId());
                  workDef = getWorkDefinitionFromTeamDefinitionAttributeInherited(teamDef);
               } else if (workItem instanceof IAtsGoal) {
                  workDef = atsApi.getWorkDefinitionService().getWorkDefinition(AtsWorkDefinitionTokens.WorkDef_Goal);
               } else if (workItem instanceof IAgileBacklog) {
                  workDef = atsApi.getWorkDefinitionService().getWorkDefinition(AtsWorkDefinitionTokens.WorkDef_Goal);
               } else if (workItem instanceof IAgileSprint) {
                  workDef = atsApi.getWorkDefinitionService().getWorkDefinition(AtsWorkDefinitionTokens.WorkDef_Sprint);
               } else if (workItem instanceof IAtsPeerToPeerReview) {
                  workDef = atsApi.getWorkDefinitionService().getWorkDefinition(
                     AtsWorkDefinitionTokens.WorkDef_Review_PeerToPeer);
               } else if (workItem instanceof IAtsDecisionReview) {
                  workDef = atsApi.getWorkDefinitionService().getWorkDefinition(
                     AtsWorkDefinitionTokens.WorkDef_Review_Decision);
               }
            }
         }
      }
      return workDef;
   }

   /**
    * @return WorkDefinitionMatch for Peer Review either from attribute value or default
    */
   @Override
   public IAtsWorkDefinition getWorkDefinitionForPeerToPeerReview(IAtsPeerToPeerReview review) {
      Conditions.notNull(review, AtsWorkDefinitionServiceImpl.class.getSimpleName());
      IAtsWorkDefinition workDef = getWorkDefinitionFromArtifactsAttributeValue(review);
      if (workDef == null) {
         workDef = getDefaultPeerToPeerWorkflowDefinition();
      }
      return workDef;
   }

   @Override
   public IAtsWorkDefinition getDefaultPeerToPeerWorkflowDefinition() {
      return getWorkDefinition(AtsWorkDefinitionTokens.WorkDef_Review_PeerToPeer);
   }

   /**
    * @return WorkDefinitionMatch for peer review off created teamWf. Will use configured value off team definitions
    * with recurse or return default review work definition
    */
   @Override
   public IAtsWorkDefinition getWorkDefinitionForPeerToPeerReviewNotYetCreated(IAtsTeamWorkflow teamWf) {
      Conditions.notNull(teamWf, AtsWorkDefinitionServiceImpl.class.getSimpleName());
      IAtsTeamDefinition teamDefinition = teamWf.getTeamDefinition();
      IAtsWorkDefinition workDef = getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(teamDefinition);
      if (workDef == null) {
         workDef = getDefaultPeerToPeerWorkflowDefinition();
      }

      return workDef;
   }

   /**
    * @return WorkDefinitionMatch of peer review from team definition related to actionableItem or return default review
    * work definition
    */
   @Override
   public IAtsWorkDefinition getWorkDefinitionForPeerToPeerReviewNotYetCreatedAndStandalone(IAtsActionableItem actionableItem) {
      Conditions.notNull(actionableItem, AtsWorkDefinitionServiceImpl.class.getSimpleName());
      IAtsWorkDefinition workDef = getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(
         actionableItem.getAtsApi().getActionableItemService().getTeamDefinitionInherited(actionableItem));
      if (workDef == null) {
         workDef = getDefaultPeerToPeerWorkflowDefinition();
      }
      return workDef;
   }

   /**
    * @return WorkDefinitionMatch of teamDefinition configured with RelatedPeerWorkflowDefinition attribute with recurse
    * up to top teamDefinition or will return no match
    */
   public IAtsWorkDefinition getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(IAtsTeamDefinition teamDef) {
      Conditions.notNull(teamDef, AtsWorkDefinitionServiceImpl.class.getSimpleName());
      IAtsWorkDefinition workDefinition =
         getWorkDefinitionFromAsObject(teamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinitionReference);
      if (workDefinition == null || workDefinition.isInvalid()) {
         IAtsTeamDefinition parentTeamDef = atsApi.getTeamDefinitionService().getParentTeamDef(teamDef);
         if (parentTeamDef != null) {
            workDefinition = getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(parentTeamDef);
         }
      }
      if (workDefinition == null && atsApi.isWorkDefAsName()) {
         String workDefId = atsApi.getAttributeResolver().getSoleAttributeValue(teamDef,
            AtsAttributeTypes.RelatedPeerWorkflowDefinition, "");
         if (Strings.isNumeric(workDefId)) {
            workDefinition = getWorkDefinition(Long.valueOf(workDefId));
         } else {
            IAtsTeamDefinition parentTeamDef = atsApi.getTeamDefinitionService().getParentTeamDef(teamDef);
            if (parentTeamDef != null) {
               workDefinition = getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(parentTeamDef);
            }
         }
      }
      return workDefinition;
   }

   @Override
   public List<IAtsStateDefinition> getStatesOrderedByOrdinal(IAtsWorkDefinition workDef) {
      List<IAtsStateDefinition> orderedPages = new ArrayList<>();
      List<IAtsStateDefinition> unOrderedPages = new ArrayList<>();
      for (int x = 1; x < workDef.getStates().size() + 1; x++) {
         for (IAtsStateDefinition state : workDef.getStates()) {
            if (state.getOrdinal() == x) {
               orderedPages.add(state);
            } else if (state.getOrdinal() == 0 && !unOrderedPages.contains(state)) {
               unOrderedPages.add(state);
            }
         }
      }
      orderedPages.addAll(unOrderedPages);
      return orderedPages;
   }

   /**
    * Recursively decend StateItems and grab all widgetDefs.<br>
    * <br>
    * Note: Modifing this list will not affect the state widgets. Use addStateItem().
    */
   @Override
   public List<IAtsWidgetDefinition> getWidgetsFromLayoutItems(IAtsStateDefinition stateDef) {
      List<IAtsWidgetDefinition> widgets = new ArrayList<>();
      getWidgets(stateDef, widgets, stateDef.getLayoutItems());
      return widgets;
   }

   @Override
   public List<IAtsWidgetDefinition> getWidgetsFromLayoutItems(IAtsStateDefinition stateDef, List<IAtsLayoutItem> layoutItems) {
      List<IAtsWidgetDefinition> widgets = new ArrayList<>();
      getWidgets(stateDef, widgets, layoutItems);
      return widgets;
   }

   private static void getWidgets(IAtsStateDefinition stateDef, List<IAtsWidgetDefinition> widgets, List<IAtsLayoutItem> stateItems) {
      for (IAtsLayoutItem stateItem : stateItems) {
         if (stateItem instanceof IAtsCompositeLayoutItem) {
            getWidgets(stateDef, widgets, ((IAtsCompositeLayoutItem) stateItem).getaLayoutItems());
         } else if (stateItem instanceof IAtsWidgetDefinition) {
            widgets.add((IAtsWidgetDefinition) stateItem);
         }
      }
   }

   @Override
   public boolean hasWidgetNamed(IAtsStateDefinition stateDef, String name) {
      for (IAtsWidgetDefinition widgetDef : getWidgetsFromLayoutItems(stateDef)) {
         if (widgetDef.getName().equals(name)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public Collection<String> getStateNames(IAtsWorkDefinition workDef) {
      List<String> names = new ArrayList<>();
      for (IAtsStateDefinition state : workDef.getStates()) {
         names.add(state.getName());
      }
      return names;
   }

   @Override
   public IAtsWorkDefinition getWorkDefinition(Long id) {
      Conditions.assertTrue(id > 0, "Id must be > 0, not %s", id);
      return atsApi.getWorkDefinitionProviderService().getWorkDefinition(id);
   }

   @Override
   public boolean isStateWeightingEnabled(IAtsWorkDefinition workDef) {
      for (IAtsStateDefinition stateDef : workDef.getStates()) {
         if (stateDef.getStateWeight() != 0) {
            return true;
         }
      }
      return false;
   }

   @Override
   public IAtsStateDefinition getStateDefinitionByName(IAtsWorkItem workItem, String stateName) {
      return getWorkDefinition(workItem).getStateByName(stateName);
   }

   @Override
   public Collection<String> getAllValidStateNames(XResultData resultData) throws Exception {
      Set<String> allValidStateNames = new HashSet<>();
      for (IAtsWorkDefinition workDef : getAllWorkDefinitions()) {
         for (String stateName : getStateNames(workDef)) {
            if (!allValidStateNames.contains(stateName)) {
               allValidStateNames.add(stateName);
            }
         }
      }
      return allValidStateNames;
   }

   @Override
   public boolean teamDefHasRule(IAtsWorkItem workItem, RuleDefinitionOption option) {
      boolean hasRule = false;
      IAtsTeamWorkflow teamWf = null;
      try {
         if (workItem instanceof IAtsTeamWorkflow) {
            teamWf = (IAtsTeamWorkflow) workItem;
         } else if (workItem instanceof IAtsAbstractReview) {
            teamWf = ((IAtsAbstractReview) workItem).getParentTeamWorkflow();
         }
         if (teamWf != null) {
            hasRule = atsApi.getTeamDefinitionService().hasRule(teamWf.getTeamDefinition(), option.name());
         }
      } catch (Exception ex) {
         atsApi.getLogger().error(ex, "Error reading rule [%s] for workItem %s", option, workItem.toStringWithId());
      }
      return hasRule;
   }

   @Override
   public boolean isInState(IAtsWorkItem workItem, IAtsStateDefinition stateDef) {
      return workItem.getStateMgr().getCurrentStateName().equals(stateDef.getName());
   }

   @Override
   public Collection<IAtsWorkDefinition> getAllWorkDefinitions() {
      return atsApi.getWorkDefinitionProviderService().getAll();
   }

   @Override
   public ArtifactToken getWorkDefArt(String workDefName) {
      return atsApi.getQueryService().getArtifactByName(AtsArtifactTypes.WorkDefinition, workDefName);
   }

   @Override
   public IAtsWorkDefinition computeWorkDefinitionForTeamWfNotYetCreated(IAtsTeamDefinition teamDef, Collection<INewActionListener> newActionListeners) {
      Conditions.assertNotNull(teamDef, "Team Definition can not be null");

      // If work def id is specified by listener, set as attribute
      IAtsWorkDefinition workDefinition = null;
      if (newActionListeners != null) {
         for (INewActionListener listener : newActionListeners) {
            AtsWorkDefinitionToken workDefTok = listener.getOverrideWorkDefinitionId(teamDef);
            if (workDefTok != null) {
               workDefinition = atsApi.getWorkDefinitionService().getWorkDefinition(workDefTok);
            }
         }
      }
      // else if work def is specified by provider, set as attribute
      if (workDefinition == null) {
         for (ITeamWorkflowProvider provider : atsApi.getWorkItemService().getTeamWorkflowProviders().getProviders()) {
            AtsWorkDefinitionToken workDefTok = provider.getOverrideWorkflowDefinitionId(teamDef);
            if (workDefTok != null) {
               workDefinition = atsApi.getWorkDefinitionService().getWorkDefinition(workDefTok);
            }
         }
      }
      // else if work def is specified by teamDef
      if (workDefinition == null) {
         workDefinition = getWorkDefinitionForTeamWfFromTeamDef(teamDef);
      }
      if (workDefinition == null) {
         throw new OseeStateException("Work Definition not computed for %s", teamDef.toStringWithId());
      }
      return workDefinition;
   }

   private IAtsWorkDefinition getWorkDefinitionForTeamWfFromTeamDef(IAtsTeamDefinition teamDef) {
      IAtsWorkDefinition workDefinition =
         getWorkDefinitionFromAsObject(teamDef, AtsAttributeTypes.WorkflowDefinitionReference);
      if (workDefinition != null && workDefinition.isValid()) {
         return workDefinition;
      }
      String workDefName = atsApi.getAttributeResolver().getSoleAttributeValueAsString(teamDef,
         AtsAttributeTypes.WorkflowDefinition, null);
      if (Strings.isValid(workDefName)) {
         workDefinition = workDefNameToWorkDef.get(workDefName);
         if (workDefinition == null) {
            workDefinition = getWorkDefinitionByName(workDefName);
            workDefNameToWorkDef.put(workDefName, workDefinition);
         }
         return workDefinition;
      }

      IAtsTeamDefinition parentTeamDef = atsApi.getTeamDefinitionService().getParentTeamDef(teamDef);
      if (parentTeamDef == null) {
         return atsApi.getWorkDefinitionService().getWorkDefinition(AtsWorkDefinitionTokens.WorkDef_Team_Default);
      }
      return getWorkDefinitionForTeamWfFromTeamDef(parentTeamDef);
   }

   @Override
   public void setWorkDefinitionAttrs(IAtsTeamDefinition teamDef, IAtsWorkDefinition workDefinition, IAtsChangeSet changes) {
      setWorkDefinitionAttrs((IAtsObject) teamDef, workDefinition, changes);
   }

   @Override
   public void setWorkDefinitionAttrs(IAtsWorkItem workItem, IAtsWorkDefinition workDefinition, IAtsChangeSet changes) {
      setWorkDefinitionAttrs((IAtsObject) workItem, workDefinition, changes);
   }

   private void setWorkDefinitionAttrs(IAtsObject atsObject, IAtsWorkDefinition workDef, IAtsChangeSet changes) {
      Conditions.assertNotNull(workDef, "workDefArt");
      Conditions.assertNotSentinel(workDef, "workDefArt");
      changes.setSoleAttributeValue(atsObject, AtsAttributeTypes.WorkflowDefinition, workDef.getName());
      changes.setSoleAttributeValue(atsObject, AtsAttributeTypes.WorkflowDefinitionReference,
         Id.valueOf(workDef.getId()));
   }

   @Override
   public void setWorkDefinitionAttrs(IAtsTeamDefinition topTeam, NamedIdBase id, IAtsChangeSet changes) {
      Conditions.assertNotNull(topTeam, "topTeam");
      Conditions.assertNotSentinel(topTeam, "topTeam");
      Conditions.assertNotNull(id, "id");
      Conditions.assertNotSentinel(id, "id");
      changes.setSoleAttributeValue(topTeam, AtsAttributeTypes.WorkflowDefinition, id.getName());
      changes.setSoleAttributeValue(topTeam, AtsAttributeTypes.WorkflowDefinitionReference, id);
   }

   @Override
   public void setWorkDefinitionAttrs(IAtsTeamWorkflow teamWf, NamedIdBase id, IAtsChangeSet changes) {
      Conditions.assertNotNull(teamWf, "teamWf");
      Conditions.assertNotSentinel(teamWf, "teamWf");
      Conditions.assertNotNull(id, "id");
      Conditions.assertNotSentinel(id, "id");
      changes.setSoleAttributeValue(teamWf, AtsAttributeTypes.WorkflowDefinition, id.getName());
      changes.setSoleAttributeValue(teamWf, AtsAttributeTypes.WorkflowDefinitionReference, id);
   }

   @Override
   public void internalSetWorkDefinition(IAtsWorkItem workItem, IAtsWorkDefinition workDef) {
      bootstrappingWorkItemToWorkDefCache.put(workItem, workDef);
   }

   @Override
   public void internalClearWorkDefinition(IAtsWorkItem workItem) {
      bootstrappingWorkItemToWorkDefCache.remove(workItem);
   }

   @Override
   public void addWorkDefinition(IAtsWorkDefinitionBuilder workDefBuilder) {
      atsApi.getWorkDefinitionProviderService().addWorkDefinition(workDefBuilder.build());
   }

   @Override
   public IAtsWorkDefinition getWorkDefinition(Id id) {
      return getWorkDefinition(id.getId());
   }

   @Override
   public XResultData validateWorkDefinitions() {
      ValidateWorkDefinitionsOperation op = new ValidateWorkDefinitionsOperation(atsApi);
      return op.run();
   }

}
