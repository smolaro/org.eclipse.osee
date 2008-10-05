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
package org.eclipse.osee.framework.ui.skynet.widgets.xviewer.test;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerStyledTextLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class XViewerTestStyledStringLabelProvider extends XViewerStyledTextLabelProvider {
   Font font = null;
   private final XViewerStyledStringLableProviderTest xViewerTest;

   public XViewerTestStyledStringLabelProvider(XViewerStyledStringLableProviderTest xViewerTest) {
      super(xViewerTest);
      this.xViewerTest = xViewerTest;
   }

  

   public void dispose() {
      if (font != null) font.dispose();
      font = null;
   }

   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   public void addListener(ILabelProviderListener listener) {
   }

   public void removeListener(ILabelProviderListener listener) {
   }

   /**
    * Allows test to be run as standalone without workbench kickoff.<br>
    * TODO Add ability to display images when XViewerTest kicked off as Java Application
    * 
    * @param imageName
    * @return
    */
   private Image getSkynetImages(String imageName) {
      if (SkynetGuiPlugin.getInstance() != null) return SkynetGuiPlugin.getInstance().getImage(imageName);
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerLabelProvider#getColumnImage(java.lang.Object, org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn)
    */
   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
      if (xCol.equals(XViewerTestFactory.Run_Col)) {
         return xViewerTest.isRun((IXViewerTestTask) element) ? getSkynetImages("chkbox_enabled.gif") : getSkynetImages("chkbox_disabled.gif");
      }
      if (xCol.equals(XViewerTestFactory.Name_Col) && xViewerTest.isScheduled((IXViewerTestTask) element)) {
         return getSkynetImages("clock.gif");
      }
      return null;
   }

/* (non-Javadoc)
 * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerStyledTextLabelProvider#getBackground(java.lang.Object, org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn, int)
 */
@Override
public Color getBackground(Object element, XViewerColumn viewerColumn,
		int columnIndex) throws OseeCoreException {
	return null;
}

/* (non-Javadoc)
 * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerStyledTextLabelProvider#getFont(java.lang.Object, org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn, int)
 */
@Override
public Font getFont(Object element, XViewerColumn viewerColumn, int columnIndex)
		throws OseeCoreException {
	return null;
}

/* (non-Javadoc)
 * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerStyledTextLabelProvider#getForeground(java.lang.Object, org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn, int)
 */
@Override
public Color getForeground(Object element, XViewerColumn viewerColumn,
		int columnIndex) throws OseeCoreException {
	return null;
}

/* (non-Javadoc)
 * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerStyledTextLabelProvider#getStyledText(java.lang.Object, org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn, int)
 */
@Override
public StyledString getStyledText(Object element, XViewerColumn xCol, int columnIndex)
		throws OseeCoreException {
	   if (element instanceof String) {
	         if (columnIndex == 1)
	            return new StyledString((String) element);
	         else
	            return new StyledString("");
	      }
	      IXViewerTestTask task = ((IXViewerTestTask) element);
	      if (task == null) return new StyledString("");
	      if (xCol.equals(XViewerTestFactory.Run_Col)) return new StyledString(String.valueOf(xViewerTest.isRun(task)),StyledString.COUNTER_STYLER);
	      if (xCol.equals(XViewerTestFactory.Name_Col)) return new StyledString(task.getId(),StyledString.DECORATIONS_STYLER);
	      if (xCol.equals(XViewerTestFactory.Schedule_Time)) return new StyledString(task.getStartTime(),StyledString.QUALIFIER_STYLER);
	      if (xCol.equals(XViewerTestFactory.Run_Db)) return new StyledString(task.getRunDb().name(),StyledString.COUNTER_STYLER);
	      if (xCol.equals(XViewerTestFactory.Task_Type)) return new StyledString(task.getTaskType().name(),StyledString.DECORATIONS_STYLER);
	      if (xCol.equals(XViewerTestFactory.Description)) return new StyledString(task.getDescription(),StyledString.COUNTER_STYLER);
	      if (xCol.equals(XViewerTestFactory.Category)) return new StyledString(task.getCategory(),StyledString.DECORATIONS_STYLER);
	      if (xCol.equals(XViewerTestFactory.Notification)) return new StyledString(task.getEmailAddress(),StyledString.QUALIFIER_STYLER);
	      return new StyledString("unhandled column");
}



}
