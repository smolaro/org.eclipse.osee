/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.HasActions;
import org.eclipse.osee.ats.api.workflow.HasAssignees;
import org.eclipse.osee.ats.core.internal.column.ev.AtsColumnService;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * Return current list of assignees sorted if in Working state or string of implementors surrounded by ()
 *
 * @author Donald G. Dunne
 */
public class AssigneeColumn implements IAtsColumn {

   public static AssigneeColumn instance = new AssigneeColumn(ImplementersColumn.instance);
   private final ImplementersStringProvider implementStrProvider;

   public AssigneeColumn(ImplementersStringProvider implementStrProvider) {
      this.implementStrProvider = implementStrProvider;
   }

   @Override
   public String getColumnText(IAtsObject atsObject) {
      String result = "";
      try {
         result = getAssigneeStr(atsObject);
      } catch (OseeCoreException ex) {
         return AtsColumnService.CELL_ERROR_PREFIX + " - " + ex.getLocalizedMessage();
      }
      return result;
   }

   public String getAssigneeStr(IAtsObject atsObject) throws OseeCoreException {
      if (atsObject instanceof HasActions) {
         HasActions hasActions = (HasActions) atsObject;
         // ensure consistent order by using lists
         List<IAtsUser> pocs = new ArrayList<>();
         List<IAtsUser> implementers = new ArrayList<>();
         for (IAtsWorkItem action : hasActions.getActions()) {
            StateType stateType = action.getStateMgr().getStateType();
            if (stateType != null) {
               if (stateType.isCompletedOrCancelled()) {
                  for (IAtsUser user : action.getImplementers()) {
                     if (!implementers.contains(user)) {
                        implementers.add(user);
                     }
                  }
               } else {
                  for (IAtsUser user : action.getAssignees()) {
                     if (!pocs.contains(user)) {
                        pocs.add(user);
                     }
                  }
               }
            }
         }
         Collections.sort(pocs);
         Collections.sort(implementers);
         return AtsObjects.toString("; ",
            pocs) + (implementers.isEmpty() ? "" : "(" + AtsObjects.toString("; ", implementers) + ")");
      } else if (atsObject instanceof IAtsWorkItem) {
         IAtsWorkItem workItem = (IAtsWorkItem) atsObject;
         StateType stateType = workItem.getStateMgr().getStateType();
         if (stateType != null) {
            if (stateType.isCompletedOrCancelled()) {
               String implementers = implementStrProvider.getImplementersStr(workItem);
               if (Strings.isValid(implementers)) {
                  return "(" + implementers + ")";
               }
            }
         }
         if (atsObject instanceof HasAssignees) {
            return AtsObjects.toString("; ", ((HasAssignees) atsObject).getAssignees());
         }
      }
      return "";
   }
}
