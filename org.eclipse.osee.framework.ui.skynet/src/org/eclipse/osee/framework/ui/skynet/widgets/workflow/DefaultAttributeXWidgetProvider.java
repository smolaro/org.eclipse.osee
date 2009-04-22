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
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeType;
import org.eclipse.osee.framework.skynet.core.attribute.BooleanAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.DateAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.EnumeratedAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.StringAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.ui.skynet.widgets.XOption;

/**
 * @author Donald G. Dunne
 */
public class DefaultAttributeXWidgetProvider implements IAttributeXWidgetProvider {

   private DynamicXWidgetLayoutData createDynamicXWidgetLayout(AttributeType attributeType) {
      DynamicXWidgetLayoutData defaultData = new DynamicXWidgetLayoutData(null);
      defaultData.setName(attributeType.getName());
      defaultData.setStorageName(attributeType.getName());
      defaultData.setToolTip(attributeType.getTipText());
      if (attributeType.getMinOccurrences() > 0) {
         defaultData.getXOptionHandler().add(XOption.REQUIRED);
      }
      defaultData.getXOptionHandler().add(XOption.HORIZONTAL_LABEL);
      return defaultData;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.workflow.IAttributeXWidgetProvider#getDynamicXWidgetLayoutData(org.eclipse.osee.framework.skynet.core.attribute.Attribute)
    */
   @Override
   public List<DynamicXWidgetLayoutData> getDynamicXWidgetLayoutData(AttributeType attributeType) {
      int minOccurrence = attributeType.getMinOccurrences();
      int maxOccurrence = attributeType.getMaxOccurrences();

      List<DynamicXWidgetLayoutData> xWidgetLayoutData = new ArrayList<DynamicXWidgetLayoutData>();

      DynamicXWidgetLayoutData defaultData = createDynamicXWidgetLayout(attributeType);
      xWidgetLayoutData.add(defaultData);

      if (attributeType.getBaseAttributeClass().equals(EnumeratedAttribute.class)) {
         if (maxOccurrence == 1) {
            defaultData.setXWidgetName("XComboDam(" + Collections.toString(",",
                  EnumeratedAttribute.getChoices(attributeType)) + ")");
         } else {
            defaultData.setXWidgetName("XListDam(" + Collections.toString(",",
                  EnumeratedAttribute.getChoices(attributeType)) + ")");
            defaultData.getXOptionHandler().add(XOption.VERTICAL_LABEL);
         }
      } else if (attributeType.getBaseAttributeClass().equals(StringAttribute.class)) {
         if (maxOccurrence == 1) {
            defaultData.setXWidgetName("XTextDam");
         } else {
            defaultData.setXWidgetName("XMultiXWidgetTextDam");
         }
      } else if (attributeType.getBaseAttributeClass().equals(BooleanAttribute.class)) {
         if (minOccurrence == 1) {
            defaultData.setXWidgetName("XCheckBoxDam");
         } else {
            defaultData.setXWidgetName("XComboBooleanDam");
         }
      } else if (attributeType.getBaseAttributeClass().equals(DateAttribute.class)) {
         if (maxOccurrence <= 1) {
            defaultData.setXWidgetName("XDateDam");
         } else {
            defaultData.setXWidgetName("XLabelDam");
         }
      } else if (attributeType.getBaseAttributeClass().equals(WordAttribute.class)) {
         defaultData.setXWidgetName("XTextDam");
         defaultData.getXOptionHandler().add(XOption.FILL_VERTICALLY);
         defaultData.getXOptionHandler().add(XOption.NOT_EDITABLE);
      } else {
         defaultData.setXWidgetName("XLabelDam");
      }
      //      defaultData.getXOptionHandler().add(XOption.FILL_HORIZONTALLY);
      return xWidgetLayoutData;
   }
}
