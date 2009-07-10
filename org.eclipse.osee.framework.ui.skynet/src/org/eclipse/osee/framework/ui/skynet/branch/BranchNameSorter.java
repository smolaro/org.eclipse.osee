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
package org.eclipse.osee.framework.ui.skynet.branch;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;

/**
 * Default sorter for branch. Sorts on descriptive name
 */
public class BranchNameSorter extends ViewerSorter {

   /**
    * Default sorter for artifacts. Sorts on name
    */
   public BranchNameSorter() {
      super();
   }

   /*
    * (non-Javadoc) Method declared on ViewerSorter.
    */
   @Override
   @SuppressWarnings("unchecked")
   public int compare(Viewer viewer, Object o1, Object o2) {

      return getComparator().compare(((Branch) o1).getBranchName(), ((Branch) o2).getBranchName());
   }

}