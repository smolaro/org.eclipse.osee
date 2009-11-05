/*
 * Created on Oct 3, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.editor.xmerge;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.coverage.editor.xcover.CoverageXViewer;
import org.eclipse.osee.coverage.editor.xcover.XCoverageViewer.TableType;
import org.eclipse.osee.coverage.merge.MergeItem;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.util.CoveragePackageImportManager;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class CoverageMergeXViewer extends CoverageXViewer {

   //   public Map<ICoverage, Boolean> importChecked = new HashMap<ICoverage, Boolean>();
   public Map<ICoverage, XResultData> importError = new HashMap<ICoverage, XResultData>();
   Action toggleImport;
   private final CoveragePackageImportManager coveragePackageImportManager;
   public static enum ImportType {
      Add, Replace, Folder, Error, None
   };

   public CoverageMergeXViewer(CoveragePackageImportManager coveragePackageImport, Composite parent, int style, IXViewerFactory xViewerFactory, XCoverageMergeViewer xCoverageMergeViewer) {
      super(parent, style, xViewerFactory, xCoverageMergeViewer);
      this.coveragePackageImportManager = coveragePackageImport;
   }

   @Override
   public boolean handleLeftClickInIconArea(TreeColumn treeColumn, TreeItem treeItem) {
      XViewerColumn xCol = (XViewerColumn) treeColumn.getData();
      if (xCol.equals(CoverageMergeXViewerFactoryImport.Import)) {
         if (treeItem.getData() instanceof MergeItem) {
            ((MergeItem) treeItem.getData()).setChecked(!((MergeItem) treeItem.getData()).isChecked());
            xCoverageViewer.getXViewer().update(treeItem.getData());
         }
         return true;
      }
      return super.handleLeftClickInIconArea(treeColumn, treeItem);
   }

   @Override
   public boolean handleLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      //      XViewerColumn xCol = (XViewerColumn) treeColumn.getData();
      return super.handleLeftClick(treeColumn, treeItem);
   }

   @Override
   public void updateEditMenuActions() {
      MenuManager mm = getMenuManager();
      if (xCoverageViewer.isType(TableType.Merge) && xCoverageViewer.isType(TableType.Import)) {
         mm.insertBefore(MENU_GROUP_PRE, toggleImport);
         toggleImport.setEnabled(isToggleImportEnabled());
      }
      super.updateEditMenuActions();
   }

   private boolean isToggleImportEnabled() {
      if (xCoverageViewer.getSelectedCoverageItems().size() == 0) return false;
      for (ICoverage item : xCoverageViewer.getSelectedCoverageItems()) {
         if (item.isEditable().isFalse() || !(item instanceof MergeItem) || !((MergeItem) item).isImportAllowed()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public void createMenuActions() {
      super.createMenuActions();

      toggleImport = new Action("Toggle Import", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            for (ICoverage coverageItem : xCoverageViewer.getSelectedCoverageItems()) {
               if (coverageItem instanceof MergeItem) {
                  ((MergeItem) coverageItem).setChecked(!((MergeItem) coverageItem).isChecked());
                  xCoverageViewer.getXViewer().update(coverageItem);
               }
            }
         }
      };
   }

   public CoveragePackageImportManager getCoveragePackageImportManager() {
      return coveragePackageImportManager;
   }
}
