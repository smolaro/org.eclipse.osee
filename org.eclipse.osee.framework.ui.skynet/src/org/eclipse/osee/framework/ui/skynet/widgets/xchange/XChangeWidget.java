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

package org.eclipse.osee.framework.ui.skynet.widgets.xchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.revision.ChangeManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.ats.OseeAts;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.status.SwtStatusMonitor;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

/**
 * @author Donald G. Dunne
 * @author Jeff C. Phillips
 */
public class XChangeWidget extends XWidget implements IActionable {

   private ChangeXViewer xChangeViewer;
   private XChangeContentProvider contentProvider;
   public final static String normalColor = "#EEEEEE";
   private static final String LOADING = "Loading ...";
   private static final String NOT_CHANGES = "No changes were found";
   protected Label extraInfoLabel;
   private Branch branch;
   private TransactionId transactionId;

   /**
    * @param label
    */
   public XChangeWidget() {
      super("Change Report");
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.skynet.gui.widgets.XWidget#createWidgets(org.eclipse.swt.widgets.Composite, int)
    */
   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      // Create Text Widgets
      if (isDisplayLabel() && !getLabel().equals("")) {
         labelWidget = new Label(parent, SWT.NONE);
         labelWidget.setText(getLabel() + ":");
         if (getToolTip() != null) {
            labelWidget.setToolTipText(getToolTip());
         }
      }

      Composite mainComp = new Composite(parent, SWT.BORDER);
      mainComp.setLayoutData(new GridData(GridData.FILL_BOTH));
      mainComp.setLayout(ALayout.getZeroMarginLayout());
      if (toolkit != null) toolkit.paintBordersFor(mainComp);

      try {
         createTaskActionBar(mainComp);
      } catch (OseeCoreException ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
      }

      xChangeViewer = new ChangeXViewer(mainComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, this);
      xChangeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

      contentProvider = new XChangeContentProvider(xChangeViewer);
      xChangeViewer.setContentProvider(contentProvider);
      xChangeViewer.setLabelProvider(new XChangeLabelProvider(xChangeViewer));

      if (toolkit != null) toolkit.adapt(xChangeViewer.getStatusLabel(), false, false);

      Tree tree = xChangeViewer.getTree();
      GridData gridData = new GridData(GridData.FILL_BOTH);
      gridData.heightHint = 100;
      tree.setLayout(ALayout.getZeroMarginLayout());
      tree.setLayoutData(gridData);
      tree.setHeaderVisible(true);
      tree.setLinesVisible(true);

      new ChangeDragAndDrop(tree, ChangeXViewerFactory.NAMESPACE);
   }

   public void createTaskActionBar(Composite parent) throws OseeCoreException {

      // Button composite for state transitions, etc
      Composite composite = new Composite(parent, SWT.NONE);
      // bComp.setBackground(mainSComp.getDisplay().getSystemColor(SWT.COLOR_CYAN));
      composite.setLayout(new GridLayout(2, false));
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      Composite leftComp = new Composite(composite, SWT.NONE);
      leftComp.setLayout(new GridLayout());
      leftComp.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL));

      extraInfoLabel = new Label(leftComp, SWT.NONE);
      extraInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      extraInfoLabel.setText("\n");

      Composite rightComp = new Composite(composite, SWT.NONE);
      rightComp.setLayout(new GridLayout());
      rightComp.setLayoutData(new GridData(GridData.END));

      ToolBar toolBar = new ToolBar(rightComp, SWT.FLAT | SWT.RIGHT);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      toolBar.setLayoutData(gd);
      ToolItem item = null;

      item = new ToolItem(toolBar, SWT.PUSH);
      item.setImage(SkynetGuiPlugin.getInstance().getImage("refresh.gif"));
      item.setToolTipText("Refresh");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            setInputData(branch, transactionId, true);
         }
      });

      item = new ToolItem(toolBar, SWT.PUSH);
      item.setImage(SkynetGuiPlugin.getInstance().getImage("customize.gif"));
      item.setToolTipText("Customize Table");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            xChangeViewer.getCustomizeMgr().handleTableCustomization();
         }
      });

      associatedArtifactToolItem = new ToolItem(toolBar, SWT.PUSH);
      associatedArtifactToolItem.setImage(SkynetGuiPlugin.getInstance().getImage("edit.gif"));
      associatedArtifactToolItem.setToolTipText("Open Associated Artifact");
      associatedArtifactToolItem.setEnabled(false);
      associatedArtifactToolItem.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            try {
               Artifact associatedArtifact = null;
               if (branch != null) {
                  associatedArtifact = branch.getAssociatedArtifact();
               } else if (transactionId != null) {
                  associatedArtifact =
                        ArtifactQuery.getArtifactFromId(transactionId.getCommitArtId(), BranchManager.getCommonBranch());
               }
               if (associatedArtifact == null) {
                  AWorkbench.popup("ERROR", "Can not access associated artifact.");
               } else {
                  RendererManager.openInJob(associatedArtifact, PresentationType.GENERALIZED_EDIT);
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }
      });

      OseeAts.addButtonToEditorToolBar(this, SkynetGuiPlugin.getInstance(), toolBar, ChangeView.VIEW_ID,
            "Change Report");
   }

   private ToolItem associatedArtifactToolItem;

   private void refreshAssociatedArtifact() throws OseeCoreException {
      try {
         Artifact associatedArtifact = null;
         if (branch != null) {
            associatedArtifact = branch.getAssociatedArtifact();
         } else if (transactionId != null && transactionId.getCommitArtId() != 0) {
            associatedArtifact =
                  ArtifactQuery.getArtifactFromId(transactionId.getCommitArtId(), BranchManager.getCommonBranch());
         }
         if (associatedArtifact != null && !(associatedArtifact instanceof User)) {
            associatedArtifactToolItem.setImage(associatedArtifact.getImage());
            associatedArtifactToolItem.setEnabled(true);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
      }
   }

   public void loadTable() {
      refresh();
   }

   @SuppressWarnings("unchecked")
   public ArrayList<Branch> getSelectedBranches() {
      ArrayList<Branch> items = new ArrayList<Branch>();
      if (xChangeViewer == null) return items;
      if (xChangeViewer.getSelection().isEmpty()) return items;
      Iterator i = ((IStructuredSelection) xChangeViewer.getSelection()).iterator();
      while (i.hasNext()) {
         Object obj = i.next();
         items.add((Branch) obj);
      }
      return items;
   }

   @Override
   public Control getControl() {
      return xChangeViewer.getTree();
   }

   @Override
   public void dispose() {
      xChangeViewer.dispose();
   }

   @Override
   public void setFocus() {
      xChangeViewer.getTree().setFocus();
   }

   @Override
   public void refresh() {
      contentProvider.refeshDocOrder();
      xChangeViewer.refresh();
      validate();
   }

   @Override
   public IStatus isValid() {
      return Status.OK_STATUS;
   }

   @Override
   public String toHTML(String labelFont) {
      return AHTML.simplePage("Unhandled");
   }

   /**
    * @return Returns the xViewer.
    */
   public ChangeXViewer getXViewer() {
      return xChangeViewer;
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.skynet.gui.widgets.XWidget#getData()
    */
   @Override
   public Object getData() {
      return xChangeViewer.getInput();
   }

   public void setInputData(final Branch branch, final TransactionId transactionId, final boolean loadChangeReport) {
      this.branch = branch;
      this.transactionId = transactionId;

      extraInfoLabel.setText(LOADING);

      Job job = new Job("Open Change View") {

         @Override
         protected IStatus run(IProgressMonitor monitor) {
            final boolean hasBranch = branch != null;
            final Collection<Change> changes = new ArrayList<Change>();
            SwtStatusMonitor swtMonitor = new SwtStatusMonitor(monitor);

            try {
               if (loadChangeReport) {
                  changes.addAll((hasBranch ? ChangeManager.getChangesPerBranch(branch, swtMonitor) : ChangeManager.getChangesPerTransaction(
                        transactionId, swtMonitor)));
               }

               Displays.ensureInDisplayThread(new Runnable() {
                  public void run() {
                     if (loadChangeReport) {
                        if (changes.size() == 0) {
                           extraInfoLabel.setText(NOT_CHANGES);
                           xChangeViewer.setInput(changes);
                        } else {
                           String infoLabel =
                                 String.format(
                                       "Changes %s to branch: %s\n%s",
                                       hasBranch || transactionId.getComment() == null ? "made" : "committed",
                                       hasBranch ? branch : "(" + transactionId.getTransactionNumber() + ") " + transactionId.getBranch(),
                                       hasBranch || transactionId.getComment() == null ? "" : "Comment: " + transactionId.getComment());
                           extraInfoLabel.setText(infoLabel);
                           xChangeViewer.setInput(changes);
                        }
                        try {
                           refreshAssociatedArtifact();
                        } catch (OseeCoreException ex) {
                           OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
                        }
                     } else {
                        extraInfoLabel.setText("Cleared on shut down - press refresh to reload");
                     }
                  }
               });
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
            return Status.OK_STATUS;
         }
      };
      Jobs.startJob(job);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.XWidget#getReportData()
    */
   @Override
   public String getReportData() {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.XWidget#getXmlData()
    */
   @Override
   public String getXmlData() {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.XWidget#setXmlData(java.lang.String)
    */
   @Override
   public void setXmlData(String str) {
   }
   public class ChangeDragAndDrop extends SkynetDragAndDrop {

      public ChangeDragAndDrop(Tree tree, String viewId) {
         super(tree, viewId);
      }

      @Override
      public void performDragOver(DropTargetEvent event) {
         event.detail = DND.DROP_NONE;
      }

      @Override
      public Artifact[] getArtifacts() {
         IStructuredSelection selection = (IStructuredSelection) xChangeViewer.getSelection();
         ArrayList<Artifact> artifacts = new ArrayList<Artifact>();

         if (selection != null && !selection.isEmpty()) {
            for (Object object : selection.toArray()) {

               if (object instanceof IAdaptable) {
                  Artifact artifact = (Artifact) ((IAdaptable) object).getAdapter(Artifact.class);

                  if (artifact != null) {
                     artifacts.add(artifact);
                  }
               }
            }
         }
         return artifacts.toArray(new Artifact[artifacts.size()]);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.ats.IActionable#getActionDescription()
    */
   @Override
   public String getActionDescription() {
      StringBuffer sb = new StringBuffer();
      if (branch != null) sb.append("\nBranch: " + branch);
      if (transactionId != null) sb.append("\nTransaction Id: " + transactionId.getTransactionNumber());
      return sb.toString();
   }

   public TransactionId getTransactionId() throws OseeCoreException {
      return transactionId;
   }

   /**
    * @return the branch
    */
   public Branch getBranch() {
      return branch;
   }

   /**
    * @param showDocOrder
    */
   public void setShowDocumentOrder(boolean showDocOrder) {
      if (contentProvider != null) {
         contentProvider.setShowDocOrder(showDocOrder);
         refresh();
      }

   }

}
