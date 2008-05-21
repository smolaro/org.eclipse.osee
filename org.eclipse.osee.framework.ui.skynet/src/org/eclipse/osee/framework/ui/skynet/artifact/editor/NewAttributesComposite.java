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
package org.eclipse.osee.framework.ui.skynet.artifact.editor;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeType;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.AttributeXWidgetFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DefaultXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayoutData;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkPage;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class NewAttributesComposite extends Composite {
   private Artifact artifact;
   private Label warningLabel;
   private ToolBar toolBar;
   private FormToolkit toolkit;
   private ScrolledForm scrolledForm;

   public static final int NAME_COLUMN_INDEX = 0;
   public static final int VALUE_COLUMN_INDEX = 1;

   public NewAttributesComposite(IDirtiableEditor editor, Composite parent, int style, Artifact artifact, ToolBar toolBar) {
      super(parent, style);
      this.artifact = artifact;
      this.toolBar = toolBar;
      setLayout(ALayout.getZeroMarginLayout(1, true));
      setLayoutData(new GridData(GridData.FILL_BOTH));

      toolkit = new FormToolkit(getDisplay());
      scrolledForm = toolkit.createScrolledForm(this);
      scrolledForm.setLayout(new GridLayout(1, false));
      scrolledForm.setLayoutData(new GridData(GridData.FILL_BOTH));

      Composite mainComp = scrolledForm.getBody();
      mainComp.setLayout(new GridLayout(1, false));
      mainComp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));
      //      mainComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW));
      mainComp.layout();

      try {
         List<DynamicXWidgetLayoutData> widgets = new ArrayList<DynamicXWidgetLayoutData>();
         for (AttributeType attrType : getOrderedArtifactTypes()) {
            widgets.add(AttributeXWidgetFactory.getAttributeXWidgetProvider(attrType).getDynamicXWidgetLayoutData(
                  attrType));
         }

         WorkPage page = new WorkPage("Attributes", "attributes", widgets, new DefaultXWidgetOptionResolver());
         page.createBody(toolkit, mainComp, artifact, null, true);
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }

   private List<AttributeType> getOrderedArtifactTypes() throws Exception {
      List<AttributeType> allTypes = new ArrayList<AttributeType>(artifact.getAttributeTypes());
      List<AttributeType> types = new ArrayList<AttributeType>();

      // Name attribute first
      for (AttributeType type : artifact.getAttributeTypes()) {
         if (type.getName().equals("Name")) {
            types.add(type);
            allTypes.remove(type);
            break;
         }
      }

      // All all attributes that have a value
      for (AttributeType attributeType : artifact.getAttributeTypes()) {
         if (artifact.getAttributes(attributeType.getName()).size() > 0 && !attributeType.getName().equals("Name")) {
            types.add(attributeType);
            allTypes.remove(attributeType);
         }
      }

      // Add all remaining
      types.addAll(allTypes);

      return types;
   }

   public void updateLabel(String msg) {
      warningLabel.setText(msg);
      layout();
   }

   public Artifact getArtifact() {
      return artifact;
   }

   /**
    * @return the toolBar
    */
   public ToolBar getToolBar() {
      return toolBar;
   }
}
