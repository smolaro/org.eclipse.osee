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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.framework.db.connection.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event.IRelationModifiedEventListener;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationModType;
import org.eclipse.osee.framework.skynet.core.relation.RelationSide;
import org.eclipse.osee.framework.skynet.core.relation.RelationType;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeSide;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.relation.explorer.RelationExplorerWindow;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

/**
 * @author Ryan D. Brooks
 */
public class RelationsComposite extends Composite implements IRelationModifiedEventListener, IFrameworkTransactionEventListener {
   private TreeViewer treeViewer;
   private Tree tree;
   private NeedSelectedArtifactListener needSelectedArtifactListener;
   private NeedArtifactMenuListener needArtifactListener;
   private final IDirtiableEditor editor;
   public static final String VIEW_ID = "osee.define.relation.RelationExplorer";
   public static final String[] columnNames = new String[] {" ", "Rationale"};
   // the index of column order
   private static int COLUMN_ORDER = 1;

   private MenuItem openMenuItem;
   private MenuItem editMenuItem;
   private MenuItem viewRelationTreeItem;
   private MenuItem deleteRelationMenuItem;
   private MenuItem deleteArtifactMenuItem;
   private MenuItem massEditMenuItem;
   private final Artifact artifact;
   private final boolean readOnly;
   private final RelationLabelProvider relationLabelProvider;
   private final ToolBar toolBar;

   private final Map<Integer, RelationLink> artifactToLinkMap;

   public RelationsComposite(IDirtiableEditor editor, Composite parent, int style, Artifact artifact) {
      this(editor, parent, style, artifact, false, null);
   }

   public RelationsComposite(IDirtiableEditor editor, Composite parent, int style, Artifact artifact, ToolBar toolBar) {
      this(editor, parent, style, artifact, false, toolBar);
   }

   public RelationsComposite(IDirtiableEditor editor, Composite parent, int style, Artifact artifact, boolean readOnly, ToolBar toolBar) {
      super(parent, style);
      this.readOnly = readOnly;

      if (artifact == null) throw new IllegalArgumentException("Can not edit a null artifact");

      this.artifact = artifact;
      this.editor = editor;
      this.relationLabelProvider = new RelationLabelProvider(artifact);
      this.artifactToLinkMap = new HashMap<Integer, RelationLink>();

      createPartControl();
      OseeEventManager.addListener(this);
      this.toolBar = toolBar;
   }

   public TreeViewer getTreeViewer() {
      return treeViewer;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
    */
   public void createPartControl() {
      this.setLayout(new GridLayout());
      this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      createTreeArea(this);
      createColumns();
      packColumnData();

      needSelectedArtifactListener = new NeedSelectedArtifactListener();
      needArtifactListener = new NeedArtifactMenuListener();
      tree.setMenu(getPopupMenu());

      setHelpContexts();
   }

   private void createTreeArea(Composite parent) {
      treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
      tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      tree.setHeaderVisible(true);

      CellEditor[] editors = new CellEditor[columnNames.length];
      editors[1] = new TextCellEditor(tree);
      treeViewer.setCellEditors(editors);
      treeViewer.setCellModifier(new RelationCellModifier(treeViewer));
      treeViewer.setColumnProperties(columnNames);
      treeViewer.setContentProvider(new RelationContentProvider(this));
      treeViewer.setLabelProvider(relationLabelProvider);
      treeViewer.setSorter(new LabelSorter() {
         @Override
         public int compare(Viewer viewer, Object e1, Object e2) {
            if (e1 instanceof RelationLink && e2 instanceof RelationLink) {
               return 0;
            }
            return super.compare(viewer, e1, e2);
         }
      });
      treeViewer.setUseHashlookup(true);
      treeViewer.setInput(artifact);

      treeViewer.addDoubleClickListener(new DoubleClickListener());
      tree.addKeyListener(new keySelectedListener());
      treeViewer.addTreeListener(new ITreeViewerListener() {

         public void treeCollapsed(TreeExpansionEvent event) {
            Display.getCurrent().asyncExec(new Runnable() {
               public void run() {
                  packColumnData();
               }
            });

         }

         public void treeExpanded(TreeExpansionEvent event) {
            Display.getCurrent().asyncExec(new Runnable() {
               public void run() {
                  packColumnData();
               }
            });
         }

      });

      tree.addMouseMoveListener(new MouseMoveListener() {

         public void mouseMove(MouseEvent e) {
            // System.out.println("MouseEvent at " + e.x + "," + e.y);
            // TreeItem item = tree.getItem(new Point(e.x, e.y));
            // if (item != null)
            // System.out.println("WOOT");
            // tree.setInsertMark(item, true);

         }
      });

      //expand items that have children
      Object[] types = ((ITreeContentProvider) treeViewer.getContentProvider()).getChildren(treeViewer.getInput());
      for (Object obj : types) {
         if (obj instanceof RelationType) {
            RelationType type = (RelationType) obj;
            if (RelationManager.getRelatedArtifactsCount(artifact, type, null) > 0) {
               treeViewer.expandToLevel(obj, 1);
            }
         }
      }

      new RelationSkynetDragAndDrop(tree, VIEW_ID);
   }

   private void createColumns() {
      for (int index = 0; index < columnNames.length; index++) {
         TreeColumn column = new TreeColumn(tree, SWT.LEFT, index);
         column.setText(columnNames[index]);
      }
   }

   private void packColumnData() {
      TreeColumn[] columns = treeViewer.getTree().getColumns();
      for (TreeColumn column : columns) {
         column.pack();
      }
   }

   public Menu getPopupMenu() {
      Menu popupMenu = new Menu(this);
      popupMenu.addMenuListener(needSelectedArtifactListener);
      popupMenu.addMenuListener(needArtifactListener);

      createOpenMenuItem(popupMenu);
      if (!readOnly) {
         createEditMenuItem(popupMenu);
         createMassEditMenuItem(popupMenu);
      }
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createViewRelationTreeMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);
      createDeleteRelationMenuItem(popupMenu);
      createDeleteArtifactMenuItem(popupMenu);
      new MenuItem(popupMenu, SWT.SEPARATOR);

      createExpandAllMenuItem(popupMenu);
      createSelectAllMenuItem(popupMenu);

      popupMenu.addMenuListener(new RelationMenuListener());
      return popupMenu;
   }

   public class DoubleClickListener implements IDoubleClickListener {
      public void doubleClick(DoubleClickEvent event) {
         openViewer((IStructuredSelection) event.getSelection());
      }
   }

   private void createDeleteRelationMenuItem(final Menu parentMenu) {
      deleteRelationMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      deleteRelationMenuItem.setText("&Delete Relation");
      deleteRelationMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

            try {
               performDeleteRelation(selection);
            } catch (ArtifactDoesNotExist ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }
      });

      deleteRelationMenuItem.setEnabled(true);
   }

   private void createDeleteArtifactMenuItem(final Menu parentMenu) {
      deleteArtifactMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      deleteArtifactMenuItem.setText("&Delete Artifact");
      deleteArtifactMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

            performDeleteArtifact(selection);
         }
      });

      deleteArtifactMenuItem.setEnabled(true);
   }

   private void createMassEditMenuItem(final Menu parentMenu) {
      massEditMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      massEditMenuItem.setText("&Mass Edit");
      massEditMenuItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

            performMassEdit(selection);
         }
      });

      massEditMenuItem.setEnabled(true);
   }

   private void createViewRelationTreeMenuItem(Menu menu) {
      viewRelationTreeItem = new MenuItem(menu, SWT.PUSH);
      viewRelationTreeItem.setText("&View Relation Table Report");
      viewRelationTreeItem.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            TreeViewerReport report =
                  new TreeViewerReport("Relation View Report for " + artifact.getDescriptiveName(), treeViewer);
            ArrayList<Integer> ignoreCols = new ArrayList<Integer>();
            ignoreCols.add(COLUMN_ORDER);
            report.setIgnoreColumns(ignoreCols);
            report.open();
         }
      });
   }

   private void createOpenMenuItem(Menu parentMenu) {
      openMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      openMenuItem.setText("Open");

      needArtifactListener.add(openMenuItem);
      needSelectedArtifactListener.add(openMenuItem);
      openMenuItem.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            openViewer((IStructuredSelection) treeViewer.getSelection());
         }
      });
   }

   private void openViewer(IStructuredSelection selection) {
      // TODO: check permission
      Object object = selection.getFirstElement();
      Artifact selectedArtifact = null;

      if (object instanceof RelationLink) {
         RelationLink link = (RelationLink) object;
         try {
            selectedArtifact = link.getArtifactOnOtherSide(artifact);
         } catch (OseeCoreException ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, true);
         }
         ArtifactEditor.editArtifact(selectedArtifact);
      }
   }

   private void performMassEdit(IStructuredSelection selection) {
      // TODO: check permission
      Set<Artifact> selectedArtifacts = new HashSet<Artifact>();
      Iterator<?> iter = selection.iterator();
      while (iter.hasNext()) {
         Object object = iter.next();
         if (object instanceof RelationLink) {
            RelationLink link = (RelationLink) object;
            try {
               selectedArtifacts.add(link.getArtifactB());
            } catch (OseeCoreException ex) {
               OSEELog.logException(SkynetGuiPlugin.class, ex, true);
            }
         }
      }
      MassArtifactEditor.editArtifacts("Mass Edit", selectedArtifacts);
   }

   private void createEditMenuItem(Menu parentMenu) {
      editMenuItem = new MenuItem(parentMenu, SWT.PUSH);
      editMenuItem.setText("&Edit");

      needArtifactListener.add(editMenuItem);
      editMenuItem.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Object object = selection.getFirstElement();

            if (object instanceof Artifact) {
               RendererManager.getInstance().editInJob((Artifact) object);
            }
         }
      });
   }

   private void createExpandAllMenuItem(Menu parentMenu) {
      MenuItem menuItem = new MenuItem(parentMenu, SWT.PUSH);
      menuItem.setText("Expand All\tCtrl+X");
      menuItem.addSelectionListener(new ExpandListener());
   }

   public class ExpandListener extends SelectionAdapter {
      @Override
      public void widgetSelected(SelectionEvent event) {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         Iterator<?> iter = selection.iterator();
         while (iter.hasNext()) {
            treeViewer.expandToLevel(iter.next(), TreeViewer.ALL_LEVELS);
         }
         packColumnData();
      }
   }

   public class NeedSelectedArtifactListener implements MenuListener {
      Collection<MenuItem> items;

      public NeedSelectedArtifactListener() {
         this.items = new LinkedList<MenuItem>();
      }

      public void add(MenuItem item) {
         items.add(item);
      }

      public void menuHidden(MenuEvent e) {
      }

      public void menuShown(MenuEvent e) {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         boolean valid = selection.getFirstElement() instanceof Artifact;
         for (MenuItem item : items)
            item.setEnabled(valid);
      }
   }

   public class NeedArtifactMenuListener implements MenuListener {
      Collection<MenuItem> items;

      public NeedArtifactMenuListener() {
         this.items = new LinkedList<MenuItem>();
      }

      public void add(MenuItem item) {
         items.add(item);
      }

      public void menuHidden(MenuEvent e) {
      }

      public void menuShown(MenuEvent e) {
         boolean valid = treeViewer.getInput() instanceof Artifact;
         for (MenuItem item : items)
            item.setEnabled(valid);
      }
   }

   public class RelationMenuListener implements MenuListener {
      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.swt.events.ArmListener#widgetArmed(org.eclipse.swt.events.ArmEvent)
       */
      public void menuHidden(MenuEvent e) {
      }

      public void menuShown(MenuEvent e) {
         // check permission
      }
   }

   private void createSelectAllMenuItem(Menu parentMenu) {
      MenuItem menuItem = new MenuItem(parentMenu, SWT.PUSH);
      menuItem.setText("&Select All\tCtrl+A");
      menuItem.addListener(SWT.Selection, new Listener() {
         public void handleEvent(org.eclipse.swt.widgets.Event event) {
            treeViewer.getTree().selectAll();
         }
      });
   }

   public void disposeRelationsComposite() {
      OseeEventManager.removeListener(this);
   }

   private void expandAll(IStructuredSelection selection) {
      Iterator<?> iter = selection.iterator();
      while (iter.hasNext()) {
         treeViewer.expandToLevel(iter.next(), TreeViewer.ALL_LEVELS);
      }
      this.packColumnData();
   }

   /**
    * Performs the deletion functionality
    * 
    * @param selection
    */
   private void performDeleteArtifact(IStructuredSelection selection) {
      Object object = selection.getFirstElement();
      try {
         if (object instanceof RelationLink) {
            RelationLink relLink = (RelationLink) object;
            Artifact artToDelete = null;
            if (relLink.getArtifactA() == artifact)
               artToDelete = relLink.getArtifactB();
            else
               artToDelete = relLink.getArtifactA();
            if (MessageDialog.openConfirm(
                  Display.getCurrent().getActiveShell(),
                  "Delete Artifact",
                  "Delete Artifact?\n\n\"" + artToDelete + "\"\n\nNOTE: This will delete the artifact from the system.  Use \"Delete Relation\" to remove this artifact from the relation.")) {
               artToDelete.delete();
            }
         }
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
      refresh();
   }

   /**
    * Performs the deletion functionality
    * 
    * @param selection
    * @throws ArtifactDoesNotExist
    */
   private void performDeleteRelation(IStructuredSelection selection) throws ArtifactDoesNotExist {
      Object[] objects = selection.toArray();
      for (Object object : objects) {
         if (object instanceof RelationLink) {
            ((RelationLink) object).delete(true);

            RelationType relationType = ((RelationLink) object).getRelationType();
            int sideAMax =
                  RelationTypeManager.getRelationSideMax(relationType, artifact.getArtifactType(), RelationSide.SIDE_A);
            int sideBMax =
                  RelationTypeManager.getRelationSideMax(relationType, artifact.getArtifactType(), RelationSide.SIDE_B);
            RelationTypeSide sideA = new RelationTypeSide(relationType, RelationSide.SIDE_A, artifact);
            RelationTypeSide sideB = new RelationTypeSide(relationType, RelationSide.SIDE_B, artifact);
            boolean onSideA = sideBMax > 0;
            boolean onSideB = sideAMax > 0;
            if (onSideA && onSideB) {
               treeViewer.refresh(sideA);
            } else if (onSideA) {
               treeViewer.refresh(sideA);
               treeViewer.refresh(sideB);
            } else if (onSideB) {
               treeViewer.refresh(sideB);
            }
         } else if (object instanceof RelationType) {
            RelationType relationType = (RelationType) object;
            RelationManager.deleteRelations(artifact, relationType, null);
            treeViewer.refresh(relationType);
         } else if (object instanceof RelationTypeSide) {
            RelationTypeSide group = (RelationTypeSide) object;
            RelationManager.deleteRelations(artifact, group.getRelationType(), group.getSide());
            treeViewer.refresh(group);
         }
      }
   }

   public void refresh() {
      if (!treeViewer.getTree().isDisposed()) {
         treeViewer.refresh();
         packColumnData();
      }
   }

   private class keySelectedListener implements KeyListener {
      public void keyPressed(KeyEvent e) {
      }

      public void keyReleased(KeyEvent e) {
         if (e.keyCode == SWT.DEL) {
            try {
               performDeleteRelation((IStructuredSelection) treeViewer.getSelection());
            } catch (ArtifactDoesNotExist ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }
         if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL) {
            treeViewer.getTree().selectAll();
         }
         if (e.keyCode == 'x' && e.stateMask == SWT.CONTROL) {
            expandAll((IStructuredSelection) treeViewer.getSelection());
         }
      }
   }

   /**
    * @return Returns the artifact.
    */
   public Artifact getArtifact() {
      return artifact;
   }

   public void refreshArtifact(Artifact newArtifact) {
      relationLabelProvider.setArtifact(newArtifact);
      treeViewer.setInput(newArtifact);

      refresh();
   }

   private class RelationSkynetDragAndDrop extends SkynetDragAndDrop {
      boolean isFeedbackAfter = false;

      public RelationSkynetDragAndDrop(Tree tree, String viewId) {
         super(tree, viewId);
      }

      @Override
      public Artifact[] getArtifacts() {
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         Object[] objects = selection.toArray();
         Artifact[] artifacts = null;

         if (objects.length > 0 && objects[0] instanceof RelationLink) {
            artifacts = new Artifact[objects.length];

            for (int index = 0; index < objects.length; index++) {
               RelationLink link = (RelationLink) objects[index];
               Artifact selectedArtifact = null;
               try {
                  selectedArtifact = link.getArtifactOnOtherSide(artifact);
               } catch (OseeCoreException ex) {
                  OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
               }
               artifacts[index] = selectedArtifact;
               artifactToLinkMap.put(selectedArtifact.getArtId(), link);
            }
         }
         return artifacts;
      }

      @Override
      public void performDragOver(DropTargetEvent event) {
         Tree tree = treeViewer.getTree();
         TreeItem selected = tree.getItem(treeViewer.getTree().toControl(event.x, event.y));

         event.feedback = DND.FEEDBACK_EXPAND;
         event.detail = DND.DROP_NONE;

         if (selected != null && selected.getData() instanceof RelationTypeSide) {
            event.detail = DND.DROP_COPY;
            tree.setInsertMark(null, false);
         } else if (selected != null && selected.getData() instanceof RelationLink) {
            RelationLink targetLink = (RelationLink) selected.getData();
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            Object obj = selection.getFirstElement();
            if (obj instanceof RelationLink) {
               RelationLink dropTarget = (RelationLink) obj;

               // the links must be in the same group

               if (targetLink.getSide(artifact).equals(dropTarget.getSide(artifact)) && targetLink.getRelationType().equals(
                     dropTarget.getRelationType())) {
                  if (isFeedbackAfter) {
                     event.feedback = DND.FEEDBACK_INSERT_AFTER;
                  } else {
                     event.feedback = DND.FEEDBACK_INSERT_BEFORE;
                  }
                  event.detail = DND.DROP_MOVE;
               }
            }
         } else {
            tree.setInsertMark(null, false);
         }
      }

      @Override
      public void operationChanged(DropTargetEvent event) {
         if (!isCtrlPressed(event)) {
            isFeedbackAfter = false;
         }
      }

      private boolean isCtrlPressed(DropTargetEvent event) {
         boolean ctrPressed = (event.detail == 1);

         if (ctrPressed) {
            isFeedbackAfter = true;
         }
         return ctrPressed;
      }

      @Override
      public void performDrop(DropTargetEvent event) {
         TreeItem selected = treeViewer.getTree().getItem(treeViewer.getTree().toControl(event.x, event.y));
         Object object = selected.getData();
         try {

            System.out.println(event.getSource());

            if (object instanceof RelationLink) {//used for ordering
               RelationLink targetLink = (RelationLink) object;
               //               RelationManager.addRelationAndModifyOrder(artifact, targetLink.getArtifactOnOtherSide(artifact),
               //                     ((ArtifactData) event.data).getArtifacts(), targetLink.getRelationType(), true);

               Artifact target = targetLink.getArtifactOnOtherSide(artifact);
               for (Artifact art : ((ArtifactData) event.data).getArtifacts()) {
                  artifact.setRelationOrder(target, isFeedbackAfter, new RelationTypeSide(targetLink.getRelationType(),
                        targetLink.getSide(artifact).oppositeSide(), artifact), art);
                  target = art;
               }
               treeViewer.refresh();
               editor.onDirtied();
            } else if (object instanceof RelationTypeSide) {
               RelationTypeSide group = (RelationTypeSide) object;

               RelationExplorerWindow window = new RelationExplorerWindow(treeViewer, group);

               ArtifactDragDropSupport.performDragDrop(event, window,
                     PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
               window.createArtifactInformationBox(null);
            }
         } catch (SQLException ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, true);
         } catch (ArtifactDoesNotExist ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, true);
         } catch (OseeCoreException ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, true);
         }

         isFeedbackAfter = false;
      }
   }

   private void setHelpContexts() {
      SkynetGuiPlugin.getInstance().setHelp(treeViewer.getControl(), "relation_page_tree_viewer");
   }

   /**
    * @return the toolBar
    */
   public ToolBar getToolBar() {
      return toolBar;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.eventx.IRelationModifiedEventListener#handleRelationModifiedEvent(org.eclipse.osee.framework.ui.plugin.event.Sender, org.eclipse.osee.framework.skynet.core.relation.RelationModifiedEvent.RelationModType, org.eclipse.osee.framework.skynet.core.relation.RelationLink, org.eclipse.osee.framework.skynet.core.artifact.Branch, java.lang.String, java.lang.String)
    */
   @Override
   public void handleRelationModifiedEvent(Sender sender, RelationModType relationModType, RelationLink link, Branch branch, String relationType) {
      try {
         if (link.getArtifactA().equals(this.artifact) || link.getArtifactB().equals(this.artifact)) {
            Displays.ensureInDisplayThread(new Runnable() {
               /* (non-Javadoc)
                * @see java.lang.Runnable#run()
                */
               @Override
               public void run() {
                  treeViewer.refresh();
               }
            });
         }
      } catch (Exception ex) {
         // do nothing
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.eventx.IFrameworkTransactionEventListener#handleFrameworkTransactionEvent(org.eclipse.osee.framework.ui.plugin.event.Sender.Source, org.eclipse.osee.framework.skynet.core.eventx.FrameworkTransactionData)
    */
   @Override
   public void handleFrameworkTransactionEvent(Sender sender, FrameworkTransactionData transData) {
      if (transData.isRelAddedChangedDeleted(this.artifact)) {
         Displays.ensureInDisplayThread(new Runnable() {
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
               treeViewer.refresh();
            }
         });
      }
   }
}