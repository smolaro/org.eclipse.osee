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
package org.eclipse.osee.framework.ui.skynet;

import java.sql.SQLException;
import java.util.ArrayList;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeType;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.cellEditor.UniversalCellEditor;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

public class AttributesComposite extends Composite {
   private TableViewer tableViewer;
   private Table table;
   private Text helpText;
   private static final String[] columnNames = new String[] {"name", "value"};
   private static final Integer[] columnWidths = new Integer[] {200, 600};
   private Artifact artifact;
   private IDirtiableEditor editor;
   private Label warningLabel;
   private boolean displayNameAttribute = true;
   private ArrayList<ModifyAttributesListener> modifyAttrListeners = new ArrayList<ModifyAttributesListener>();
   private MenuItem deleteItem;
   private ToolBar toolBar;

   public static final int NAME_COLUMN_INDEX = 0;
   public static final int VALUE_COLUMN_INDEX = 1;

   public AttributesComposite(IDirtiableEditor editor, Composite parent, int style, Artifact artifact, ToolBar toolBar) {
      super(parent, style);
      this.artifact = artifact;
      this.editor = editor;

      create(this);
      Menu popupMenu = new Menu(parent);
      createAddMenuItem(popupMenu);
      createDeleteMenuItem(popupMenu);
      popupMenu.addMenuListener(new AttributeMenuListener());
      tableViewer.getTable().setMenu(popupMenu);

      this.toolBar = toolBar;
   }

   public void updateLabel(String msg) {
      warningLabel.setText(msg);
      layout();
   }

   private void create(Composite parent) {
      this.setLayout(new GridLayout());
      this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      SashForm mainSash = new SashForm(this, SWT.NONE);
      mainSash.setLayout(new GridLayout());
      mainSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      mainSash.setOrientation(SWT.VERTICAL);

      createTableArea(mainSash);

      SashForm sashForm = new SashForm(mainSash, SWT.NONE);
      sashForm.setLayout(new GridLayout());
      sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      sashForm.setOrientation(SWT.HORIZONTAL);

      createWarningArea(sashForm);
      createHelpArea(sashForm);

      mainSash.setWeights(new int[] {8, 2});
      sashForm.setWeights(new int[] {5, 5});

      setHelpContexts();
   }

   private void createTableArea(Composite parent) {
      Group composite = new Group(parent, SWT.NONE);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
      composite.setText("Attributes");

      createTable(composite);
      createColumns();
      createTableViewer(composite);

      tableViewer.refresh();
      attachTableListeners();
   }

   private void createTable(Composite parent) {
      table =
            new Table(parent,
                  SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
      table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      table.setLinesVisible(true);
      table.setHeaderVisible(true);
   }

   private void attachTableListeners() {
      tableViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {

         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Object selected = selection.getFirstElement();

            if (selected instanceof Attribute) {
               Attribute<?> attribute = (Attribute<?>) selected;
               AttributeType attributeType = attribute.getAttributeType();
               if (attributeType.getTipText() != null && !attributeType.getTipText().equals("null"))
                  helpText.setText(attributeType.getTipText());
               else
                  helpText.setText("");
            }
         }
      });
   }

   private void createTableViewer(Composite parent) {
      tableViewer = new TableViewer(table);

      TableViewerEditor.create(
            tableViewer,
            new ColumnViewerEditorActivationStrategy(tableViewer),
            ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
      tableViewer.setUseHashlookup(true);
      tableViewer.setColumnProperties(columnNames);

      if (!artifact.isReadOnly()) {
         CellEditor[] editors = new CellEditor[columnNames.length];
         editors[VALUE_COLUMN_INDEX] = new UniversalCellEditor(table, SWT.NONE);

         tableViewer.setCellEditors(editors);
         tableViewer.setCellModifier(new AttributeCellModifier(editor, tableViewer, this));
      }
      tableViewer.setContentProvider(new AttributeContentProvider());
      tableViewer.setLabelProvider(new AttributeLabelProvider());
      tableViewer.setInput(artifact);
   }

   private void createColumns() {
      for (int index = 0; index < columnNames.length; index++) {
         TableColumn column = new TableColumn(table, SWT.LEFT, index);
         column.setText(columnNames[index]);
         column.setWidth(columnWidths[index]);
      }
   }

   private void createHelpArea(Composite parent) {
      Group composite = new Group(parent, SWT.NONE);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      composite.setText("Tips");

      helpText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
      helpText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
   }

   private void createWarningArea(Composite parent) {
      Group composite = new Group(parent, SWT.NONE);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      composite.setText("Warnings");

      warningLabel = new Label(composite, SWT.NONE);
      warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      warningLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
      updateLabel("");
   }

   private void createAddMenuItem(Menu parentMenu) {
      MenuItem addItem = new MenuItem(parentMenu, SWT.CASCADE);
      addItem.setText("Add");
      addItem.setEnabled(true && !artifact.isReadOnly());

      // Update the enabled values for the popup menu each time it comes up
      addItem.addArmListener(new ArmListener() {

         public void widgetArmed(ArmEvent e) {
            MenuItem addItem = (MenuItem) e.getSource();
            for (MenuItem attrItem : addItem.getMenu().getItems()) {
               try {
                  attrItem.setEnabled(artifact.getRemainingAttributeCount((AttributeType) attrItem.getData()) > 0);
               } catch (SQLException ex) {

               }
            }
         }
      });

      Menu attributesMenu = new Menu(parentMenu);

      try {
         SelectionAdapter listener = new AttributeMenuSelectionListener(this, tableViewer, editor);
         for (AttributeType attributeType : artifact.getAttributeTypes()) {
            MenuItem item = new MenuItem(attributesMenu, SWT.CASCADE);
            item.setText(attributeType.getName() + " Attribute");
            item.setData(attributeType);
            item.addSelectionListener(listener);
         }
      } catch (SQLException ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
      addItem.setMenu(attributesMenu);
   }

   private void createDeleteMenuItem(Menu parentMenu) {
      deleteItem = new MenuItem(parentMenu, SWT.PUSH);
      deleteItem.setImage(null);
      deleteItem.setText("Delete");
      deleteItem.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            Attribute<?> attribute = getSelectedAttribute();
            attribute.delete();
            tableViewer.refresh();
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }
      });

      deleteItem.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            editor.onDirtied();
            notifyModifyAttribuesListeners();
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }

      });
   }

   private Attribute<?> getSelectedAttribute() {
      TableItem[] items = tableViewer.getTable().getSelection();
      if (items.length > 0)
         return (Attribute<?>) (tableViewer.getTable().getSelection()[0]).getData();
      else
         return null;
   }

   public class AttributeMenuListener implements MenuListener {
      public void menuHidden(MenuEvent e) {
      }

      public void menuShown(MenuEvent e) {
         Attribute<?> attribute = getSelectedAttribute();

         if (attribute == null) {
            deleteItem.setText("Delete - No Attribute Selected");
            deleteItem.setEnabled(false);
         } else if (!attribute.canDelete()) {
            deleteItem.setText("Delete - Lower Limit Met");
            deleteItem.setEnabled(false);
         } else {
            deleteItem.setText("Delete");
            deleteItem.setEnabled(!artifact.isReadOnly());
         }
      }
   }

   public Artifact getArtifact() {
      return artifact;
   }

   public void refreshArtifact(Artifact artifact) {
      this.artifact = artifact;

      if (tableViewer.getContentProvider() != null) {
         tableViewer.setInput(artifact);
         tableViewer.refresh();
      }
   }

   public boolean isDisplayNameAttribute() {
      return displayNameAttribute;
   }

   public void addModifyAttributesListener(ModifyAttributesListener listener) {
      if (!modifyAttrListeners.contains(listener)) modifyAttrListeners.add(listener);
   }

   public void removeModifyAttributesListener(ModifyAttributesListener listener) {
      modifyAttrListeners.remove(listener);
   }

   public void notifyModifyAttribuesListeners() {
      for (ModifyAttributesListener listener : modifyAttrListeners)
         listener.handleEvent();
   }

   private void setHelpContexts() {
      SkynetGuiPlugin.getInstance().setHelp(tableViewer.getControl(), "artifact_editor");
   }

   /**
    * @return the toolBar
    */
   public ToolBar getToolBar() {
      return toolBar;
   }
}
