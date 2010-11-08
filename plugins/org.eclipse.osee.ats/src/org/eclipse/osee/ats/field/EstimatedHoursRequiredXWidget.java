/*
 * Created on Nov 8, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.field;

import static org.eclipse.osee.framework.ui.skynet.widgets.XOption.REQUIRED;
import org.eclipse.osee.ats.workflow.item.AtsAttributeXWidgetWorkItem;

public class EstimatedHoursRequiredXWidget extends AtsAttributeXWidgetWorkItem {

   public EstimatedHoursRequiredXWidget() {
      super(EstimatedHoursColumn.EstimatedHours, "XFloatDam", REQUIRED);
   }

}
