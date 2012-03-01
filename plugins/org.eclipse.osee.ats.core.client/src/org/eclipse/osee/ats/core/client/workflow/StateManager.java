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

package org.eclipse.osee.ats.core.client.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.notify.AtsNotificationManager;
import org.eclipse.osee.ats.core.client.notify.AtsNotifyType;
import org.eclipse.osee.ats.core.client.team.SimpleTeamState;
import org.eclipse.osee.ats.core.client.team.TeamState;
import org.eclipse.osee.ats.core.client.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.client.util.AtsUsers;
import org.eclipse.osee.ats.core.client.util.AtsUtilCore;
import org.eclipse.osee.ats.core.client.workdef.WorkDefinitionFactory;
import org.eclipse.osee.ats.core.client.workflow.log.LogItem;
import org.eclipse.osee.ats.core.client.workflow.log.LogType;
import org.eclipse.osee.ats.core.model.IAtsUser;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workdef.WorkDefinition;
import org.eclipse.osee.ats.core.workflow.IWorkPage;
import org.eclipse.osee.ats.core.workflow.WorkPageType;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Donald G. Dunne
 */
public class StateManager {

   private final XCurrentStateDam currentStateDam;
   private final XStateDam stateDam;
   private final AbstractWorkflowArtifact sma;
   private static final Set<String> notValidAttributeType = new HashSet<String>();
   private static List<String> stateNames = null;

   public StateManager(AbstractWorkflowArtifact sma) {
      this.sma = sma;
      currentStateDam = new XCurrentStateDam(sma);
      stateDam = new XStateDam(sma);
   }

   /**
    * Get state and create if not there.
    */
   private SMAState getSMAState(IWorkPage state, boolean create) throws OseeCoreException {
      if (currentStateDam.getState().getName().equals(state.getPageName())) {
         return currentStateDam.getState();
      } else {
         return stateDam.getState(state, create);
      }
   }

   public boolean isAnyStateHavePercentEntered() throws OseeCoreException {
      if (currentStateDam.getState().getPercentComplete() > 0) {
         return true;
      }
      for (SMAState state : stateDam.getStates()) {
         if (state.getPercentComplete() > 0) {
            return true;
         }
      }
      return false;
   }

   public boolean isInState(IWorkPage state) {
      return (getCurrentStateName().equals(state.getPageName()));
   }

   /**
    * Discouraged Access. This method should not normally be called except in cases were state data is being manually
    * created.
    */
   public void internalCreateIfNotExists(IWorkPage state) throws OseeCoreException {
      if (isStateVisited(state)) {
         return;
      }
      SMAState smaState = getSMAState(state, true);
      putState(smaState);
   }

   /**
    * @return true if UnAssigned user is currently an assignee
    */
   public boolean isUnAssigned() throws OseeCoreException {
      return getAssignees().contains(AtsUsers.getUnAssigned());
   }

   public boolean isUnAssignedSolely() throws OseeCoreException {
      return getAssignees().size() == 1 && isUnAssigned();
   }

   /**
    * Return Hours Spent for State
    *
    * @return hours spent or 0 if none
    */
   public double getHoursSpent(IWorkPage state) throws OseeCoreException {
      SMAState smaState = getSMAState(state, false);
      if (smaState == null) {
         return 0.0;
      }
      return smaState.getHoursSpent();
   }

   public double getHoursSpent() throws OseeCoreException {
      return getHoursSpent(getCurrentState());
   }

   /**
    * Return Percent Complete for State
    *
    * @return percent complete or 0 if none
    */
   public int getPercentComplete(IWorkPage teamState) throws OseeCoreException {
      if (teamState.getWorkPageType().isCompletedOrCancelledPage()) {
         return 100;
      }
      SMAState state = getSMAState(teamState, false);
      if (state == null) {
         return 0;
      }
      return state.getPercentComplete();

   }

   public int getPercentComplete() throws OseeCoreException {
      return getPercentComplete(getCurrentState());
   }

   public String getCurrentStateName() {
      try {
         return currentStateDam.getState().getName();
      } catch (OseeCoreException ex) {
         return ex.getLocalizedMessage();
      }
   }

   public IWorkPage getCurrentState() {
      return new SimpleTeamState(getCurrentStateName(), getCurrentWorkPageType());
   }

   public WorkPageType getCurrentWorkPageType() {
      try {
         if (sma.isAttributeTypeValid(AtsAttributeTypes.CurrentStateType)) {
            // backward compatibility
            if (sma.getSoleAttributeValueAsString(AtsAttributeTypes.CurrentStateType, null) == null) {
               if (getCurrentStateName().equals(TeamState.Completed.getPageName())) {
                  return WorkPageType.Completed;
               } else if (getCurrentStateName().equals(TeamState.Cancelled.getPageName())) {
                  return WorkPageType.Cancelled;
               } else {
                  return WorkPageType.Working;
               }
            } else {
               return WorkPageType.valueOf(sma.getSoleAttributeValueAsString(AtsAttributeTypes.CurrentStateType, null));
            }
         } else {
            // display console error, but only once
            if (!notValidAttributeType.contains(sma.getArtifactTypeName())) {
               notValidAttributeType.add(sma.getArtifactTypeName());
               System.err.println("CurrentStateType not valid for " + sma.getArtifactTypeName());
            }
            // TODO get rid of this once database configured for new types (or leave for backward compatibility?
            if (getCurrentStateName().equals(TeamState.Completed.getPageName())) {
               return WorkPageType.Completed;
            } else if (getCurrentStateName().equals(TeamState.Cancelled.getPageName())) {
               return WorkPageType.Cancelled;
            } else {
               return WorkPageType.Working;
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   public String getAssigneesStr() throws OseeCoreException {
      return AtsObjects.toString("; ", sma.getStateMgr().getAssignees());
   }

   public String getAssigneesStr(IWorkPage state, int length) throws OseeCoreException {
      String str = getAssigneesStr(state);
      if (str.length() > length) {
         return str.substring(0, length - 1) + "...";
      }
      return str;
   }

   public String getAssigneesStr(IWorkPage state) throws OseeCoreException {
      return AtsObjects.toString("; ", sma.getStateMgr().getAssignees(state));
   }

   public String getAssigneesStr(int length) throws OseeCoreException {
      String str = getAssigneesStr();
      if (str.length() > length) {
         return str.substring(0, length - 1) + "...";
      }
      return str;
   }

   public Collection<? extends IAtsUser> getAssignees() throws OseeCoreException {
      return getAssignees(getCurrentState());
   }

   public Collection<? extends IAtsUser> getAssignees(IWorkPage state) throws OseeCoreException {
      SMAState smaState = getSMAState(state, false);
      if (smaState == null) {
         return Collections.emptyList();
      } else {
         return smaState.getAssignees();
      }
   }

   public void updateMetrics(IWorkPage state, double additionalHours, int percentComplete, boolean logMetrics) throws OseeCoreException {
      if (sma.isInState(state)) {
         if (sma.getWorkDefinition().isStateWeightingEnabled()) {
            currentStateDam.updateMetrics(additionalHours, percentComplete, logMetrics);
         } else {
            currentStateDam.updateMetrics(additionalHours, logMetrics);
            sma.setSoleAttributeValue(AtsAttributeTypes.PercentComplete, percentComplete);
         }
      } else {
         if (sma.getWorkDefinition().isStateWeightingEnabled()) {
            stateDam.updateMetrics(state, additionalHours, percentComplete, logMetrics);
         } else {
            stateDam.updateMetrics(state, additionalHours, logMetrics);
         }
      }
   }

   public void setMetrics(IWorkPage state, double hours, int percentComplete, boolean logMetrics, IAtsUser user, Date date) throws OseeCoreException {
      if (state.getPageName().equals(getCurrentStateName())) {
         if (sma.getWorkDefinition().isStateWeightingEnabled()) {
            currentStateDam.setMetrics(hours, percentComplete, logMetrics, user, date);
         } else {
            currentStateDam.setMetrics(hours, logMetrics, user, date);
            sma.setSoleAttributeValue(AtsAttributeTypes.PercentComplete, percentComplete);
         }
      } else {
         if (sma.getWorkDefinition().isStateWeightingEnabled()) {
            stateDam.setMetrics(state, hours, percentComplete, logMetrics, user, date);
         } else {
            stateDam.setMetrics(state, hours, logMetrics, user, date);
         }
      }
   }

   /**
    * Adds the assignee AND writes to artifact. Does not persist. Will remove UnAssigned user if another assignee
    * exists.
    */
   public void addAssignee(IAtsUser assignee) throws OseeCoreException {
      addAssignee(getSMAState(getCurrentState(), false), assignee);
   }

   public void addAssignee(SMAState smaState, IAtsUser assignee) throws OseeCoreException {
      addAssignees(smaState, Arrays.asList(assignee));
   }

   public void addAssignees(Collection<IAtsUser> assignees) throws OseeCoreException {
      addAssignees(getSMAState(getCurrentState(), false), assignees);
   }

   public void addAssignees(SMAState smaState, Collection<IAtsUser> assignees) throws OseeCoreException {
      List<IAtsUser> notifyAssignees = new ArrayList<IAtsUser>();
      for (IAtsUser assignee : assignees) {
         if (!smaState.getAssignees().contains(assignee)) {
            notifyAssignees.add(assignee);
            smaState.addAssignee(assignee);
         }
      }
      AtsNotificationManager.notify(sma, notifyAssignees, AtsNotifyType.Assigned);
      if (smaState.getAssignees().size() > 1 && smaState.getAssignees().contains(AtsUsers.getUnAssigned())) {
         smaState.removeAssignee(AtsUsers.getUnAssigned());
      }
      putState(smaState);
   }

   public void setAssignee(IAtsUser assignee) throws OseeCoreException {
      setAssignees(Arrays.asList(assignee));
   }

   public void setAssignees(Collection<IAtsUser> newAssignees) throws OseeCoreException {
      setAssignees(getSMAState(getCurrentState(), false), newAssignees);
   }

   /**
    * Sets the assignees as attributes and relations AND writes to artifact. Does not persist.
    */
   public void setAssignees(SMAState smaState, Collection<IAtsUser> newAssignees) throws OseeCoreException {
      Collection<? extends IAtsUser> currentAssignees = smaState.getAssignees();
      List<IAtsUser> notifyAssignees = new ArrayList<IAtsUser>();
      for (IAtsUser user : newAssignees) {
         if (!currentAssignees.contains(user)) {
            notifyAssignees.add(user);
         }
      }
      AtsNotificationManager.notify(sma, notifyAssignees, AtsNotifyType.Assigned);
      if (smaState.getAssignees().size() > 1 && smaState.getAssignees().contains(AtsUsers.getUnAssigned())) {
         smaState.removeAssignee(AtsUsers.getUnAssigned());
      }
      smaState.setAssignees(newAssignees);
      putState(smaState);
   }

   /**
    * Sets the assignee AND writes to artifact. Does not persist.
    */
   public void setAssignee(IWorkPage state, IAtsUser assignee) throws OseeCoreException {
      SMAState smaState = getSMAState(state, false);
      if (!isStateVisited(state)) {
         throw new OseeArgumentException("State [%s] does not exist.", state);
      }
      setAssignees(smaState, Arrays.asList(assignee));
   }

   /**
    * Removes the assignee from stateName state AND writes to SMA. Does not persist.
    */
   public void removeAssignee(IWorkPage state, IAtsUser assignee) throws OseeCoreException {
      if (!isStateVisited(state)) {
         return;
      }
      SMAState smaState = getSMAState(state, false);
      smaState.removeAssignee(assignee);
      putState(smaState);
   }

   /**
    * Removes the assignee AND writes to SMA. Does not persist.
    */
   public void removeAssignee(IAtsUser assignee) throws OseeCoreException {
      SMAState smaState = getSMAState(getCurrentState(), false);
      smaState.removeAssignee(assignee);
      putState(smaState);
   }

   /**
    * Removes ALL assignees AND writes to SMA. Does not persist.
    */
   public void clearAssignees() throws OseeCoreException {
      SMAState smaState = getSMAState(getCurrentState(), false);
      smaState.clearAssignees();
      putState(smaState);
   }

   public boolean isStateVisited(IWorkPage state) {
      return getVisitedStateNames().contains(state.getPageName());
   }

   public void transitionHelper(Collection<? extends IAtsUser> toAssignees, StateDefinition fromState, StateDefinition toState, String cancelReason) throws OseeCoreException {
      // Set XCurrentState info to XState
      stateDam.setState(currentStateDam.getState());

      // Set XCurrentState; If been to this state, copy state info from prev state; else create new
      SMAState previousState = stateDam.getState(toState, false);
      if (previousState == null) {
         currentStateDam.setState(new SMAState(toState, toAssignees));
      } else {
         List<IAtsUser> previousAssignees = new ArrayList<IAtsUser>();
         previousAssignees.addAll(previousState.getAssignees());
         List<IAtsUser> nextAssignees = new ArrayList<IAtsUser>();
         nextAssignees.addAll(toAssignees);
         if (!org.eclipse.osee.framework.jdk.core.util.Collections.isEqual(previousAssignees, nextAssignees)) {
            previousState.setAssignees(nextAssignees);
         }
         for (IAtsUser user : previousAssignees) {
            if (!previousAssignees.contains(user)) {
               AtsNotificationManager.notify(sma, Arrays.asList(user), AtsNotifyType.Assigned);
            }
         }

         currentStateDam.setState(previousState);
      }
      sma.setSoleAttributeValue(AtsAttributeTypes.CurrentStateType, toState.getWorkPageType().name());
   }

   /**
    * Initializes state machine and sets the current state to stateName
    */
   public void initializeStateMachine(IWorkPage state, Collection<IAtsUser> assignees) throws OseeCoreException {
      SMAState smaState = null;
      if (getVisitedStateNames().contains(state.getPageName())) {
         smaState = getSMAState(state, false);
      } else {
         if (assignees == null) {
            List<IAtsUser> assigned = new ArrayList<IAtsUser>();
            if (state.isWorkingPage()) {
               assigned.add(AtsUsers.getUser());
            }
            smaState = new SMAState(state, assigned);
         } else {
            smaState = new SMAState(state, assignees);
         }
      }
      currentStateDam.setState(smaState);
      if (sma.isAttributeTypeValid(AtsAttributeTypes.CurrentStateType)) {
         sma.setSoleAttributeValue(AtsAttributeTypes.CurrentStateType, state.getWorkPageType().name());
      }
      Collection<IAtsUser> notifyUsers = new ArrayList<IAtsUser>();
      notifyUsers.addAll(smaState.getAssignees());
      AtsNotificationManager.notify(sma, notifyUsers, AtsNotifyType.Assigned);
   }

   private void putState(SMAState state) throws OseeCoreException {
      if (getCurrentStateName().equals(state.getName())) {
         currentStateDam.setState(state);
      } else {
         stateDam.setState(state);
      }
   }

   public Collection<String> getVisitedStateNames() {
      Set<String> names = new HashSet<String>();
      for (SMAState state : stateDam.getStates()) {
         names.add(state.getName());
      }
      names.add(getCurrentStateName());
      return names;
   }

   public long getTimeInState() throws OseeCoreException {
      return getTimeInState(getCurrentState());
   }

   public long getTimeInState(IWorkPage state) throws OseeCoreException {
      if (state == null) {
         return 0;
      }
      LogItem logItem = sma.getStateStartedData(state);
      if (logItem == null) {
         return 0;
      }
      return new Date().getTime() - logItem.getDate().getTime();
   }

   /**
    * return currently assigned state machine artifacts
    */
   public static Set<Artifact> getAssigned(IAtsUser user) throws OseeCoreException {
      return getAssigned(user, null);
   }

   /**
    * return currently assigned state machine artifacts that match clazz
    *
    * @param clazz to match or all if null
    */
   public static Set<Artifact> getAssigned(IAtsUser user, Class<?> clazz) throws OseeCoreException {
      return getAssigned(user.getUserId(), clazz);
   }

   /**
    * return currently assigned state machine artifacts that match clazz
    *
    * @param clazz to match or all if null
    */
   public static Set<Artifact> getAssigned(String userId, Class<?> clazz) throws OseeCoreException {
      Set<Artifact> assigned = new HashSet<Artifact>();
      for (Artifact artifact : ArtifactQuery.getArtifactListFromAttribute(AtsAttributeTypes.CurrentState,
         "%<" + userId + ">%", AtsUtilCore.getAtsBranch())) {
         if (clazz == null || clazz.isInstance(artifact)) {
            assigned.add(artifact);
         }
      }
      return assigned;

   }

   public static List<IAtsUser> getImplementersByState(AbstractWorkflowArtifact workflow, IWorkPage state) throws OseeCoreException {
      List<IAtsUser> users = new ArrayList<IAtsUser>();
      if (workflow.isCancelled()) {
         users.add(workflow.getCancelledBy());
      } else {
         for (IAtsUser user : workflow.getStateMgr().getAssignees(state)) {
            if (!users.contains(user)) {
               users.add(user);
            }
         }
         if (workflow.isCompleted()) {
            IAtsUser user = workflow.getCompletedBy();
            if (user != null && !users.contains(user)) {
               users.add(user);
            }
         }
      }
      return users;
   }

   public void internalSetCurrentStateName(String stateName) throws OseeCoreException {
      SMAState state = currentStateDam.getState();
      if (state != null && !state.getName().equals(stateName)) {
         state.setName(stateName);
      }
      currentStateDam.setState(state);
   }

   public static Collection<? extends IAtsUser> getAssigneesByState(AbstractWorkflowArtifact workflow, StateDefinition state) throws OseeCoreException {
      Set<IAtsUser> users = new HashSet<IAtsUser>();
      SMAState smaState = workflow.getStateMgr().getSMAState(state, false);
      if (smaState != null) {
         users.addAll(smaState.getAssignees());
      }
      users.remove(AtsUsers.getUnAssigned());
      return users;
   }

   public synchronized static Collection<? extends String> getStateNames() {
      if (stateNames == null) {
         stateNames = new ArrayList<String>();
         try {
            for (WorkDefinition workDef : WorkDefinitionFactory.loadAllDefinitions()) {
               for (StateDefinition state : workDef.getStates()) {
                  if (!stateNames.contains(state.getName())) {
                     stateNames.add(state.getName());
                  }
               }
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
         Collections.sort(stateNames);
      }
      return stateNames;
   }

   public static String getCompletedDateByState(AbstractWorkflowArtifact awa, StateDefinition state) throws OseeCoreException {
      LogItem stateEvent = awa.getLog().getStateEvent(LogType.StateComplete, state.getPageName());
      if (stateEvent != null && stateEvent.getDate() != null) {
         return DateUtil.getMMDDYYHHMM(stateEvent.getDate());
      }
      return "";
   }

   public List<IAtsUser> getAssignees(String stateName) throws OseeCoreException {
      List<IAtsUser> assignees = new ArrayList<IAtsUser>();
      if (currentStateDam.getState().getName().equals(stateName)) {
         assignees.addAll(currentStateDam.getState().getAssignees());
      } else {
         for (SMAState state : stateDam.getStates()) {
            if (state.getName().equals(stateName)) {
               assignees.addAll(state.getAssignees());
            }
         }
      }
      return assignees;
   }

   public void validateNoBootstrapUser() throws OseeCoreException {
      for (IAtsUser user : getAssignees()) {
         if (user.getUserId().equals(SystemUser.BootStrap.getUserId())) {
            throw new OseeStateException("Assignee can't be bootstrap user");
         }
      }

   }

}
