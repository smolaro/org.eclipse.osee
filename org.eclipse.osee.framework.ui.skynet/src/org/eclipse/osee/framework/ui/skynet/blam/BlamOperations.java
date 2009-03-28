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

package org.eclipse.osee.framework.ui.skynet.blam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionPoints;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation;
import org.eclipse.osee.framework.ui.skynet.widgets.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.skynet.widgets.xnavigate.XNavigateItemBlam;
import org.osgi.framework.Bundle;

/**
 * @author Donald G. Dunne
 */
public class BlamOperations {

   public static Collection<BlamOperation> getBlamOperationsNameSort() {
      ArrayList<BlamOperation> blamsSortedByName = new ArrayList<BlamOperation>();
      Map<String, BlamOperation> blamMap = new HashMap<String, BlamOperation>();
      for (BlamOperation blam : getBlamOperations()) {
         blamMap.put(blam.getName(), blam);
      }
      String names[] = blamMap.keySet().toArray(new String[blamMap.keySet().size()]);
      Arrays.sort(names);
      for (String name : names)
         blamsSortedByName.add(blamMap.get(name));
      return blamsSortedByName;
   }

   public static Collection<BlamOperation> getBlamOperations() {
      List<BlamOperation> blamOperations = new ArrayList<BlamOperation>();
      for (IConfigurationElement iConfigurationElement : ExtensionPoints.getExtensionElements(
            "org.eclipse.osee.framework.ui.skynet.BlamOperation", "Operation")) {

         String classname = iConfigurationElement.getAttribute("className");
         String bundleName = iConfigurationElement.getContributor().getName();
         if (classname != null && bundleName != null) {
            Bundle bundle = Platform.getBundle(bundleName);
            try {
               Class<?> taskClass = bundle.loadClass(classname);
               Object obj = taskClass.newInstance();
               BlamOperation task = (BlamOperation) obj;
               blamOperations.add(task);
            } catch (Exception ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, "Error loading BlamOperation extension", ex);
            }
         }
      }
      return blamOperations;
   }

   public static void addBlamOperationsToNavigator(List<XNavigateItem> items) throws OseeCoreException {
      Map<String, XNavigateItem> nameToParent = new HashMap<String, XNavigateItem>();
      XNavigateItem blamOperationItems = new XNavigateItem(null, "Blam Operations");
      for (BlamOperation blamOperation : BlamOperations.getBlamOperationsNameSort()) {
         // If categories not specified, add to top level
         if (blamOperation.getCategories().size() == 0) {
            new XNavigateItemBlam(blamOperationItems, blamOperation);
         }
         // Create categories
         for (String category : blamOperation.getCategories()) {
            if (AccessControlManager.isOseeAdmin() || !category.contains("Admin") || (category.contains("Admin") && AccessControlManager.isOseeAdmin())) {
               createCategories(category.split("\\."), 0, blamOperationItems, nameToParent);
            }
         }
         // Add this navigate item to categories
         for (String category : blamOperation.getCategories()) {
            if (AccessControlManager.isOseeAdmin() || !category.contains("Admin") || (category.contains("Admin") && AccessControlManager.isOseeAdmin())) {
               new XNavigateItemBlam(nameToParent.get(category), blamOperation);
            }
         }
      }
      items.add(blamOperationItems);
   }

   private static void createCategories(String[] categoryElements, int index, XNavigateItem parentItem, Map<String, XNavigateItem> nameToParent) throws OseeCoreException {
      String firstElement = categoryElements[index];
      XNavigateItem thisCategoryItem = null;
      for (XNavigateItem childItem : parentItem.getChildren()) {
         if (childItem.getName().equals(firstElement)) {
            thisCategoryItem = childItem;
            break;
         }
      }
      // Create new folder category
      if (thisCategoryItem == null) {
         // Add to parentItem
         thisCategoryItem = new XNavigateItem(parentItem, firstElement);
         String catName = "";
         for (int x = 0; x <= index; x++) {
            if (!catName.equals("")) {
               catName += ".";
            }
            catName += categoryElements[x];
         }
         // Add to lookup map
         nameToParent.put(catName, thisCategoryItem);
      }
      // Process children categories
      if (categoryElements.length > index + 1) {
         createCategories(categoryElements, index + 1, thisCategoryItem, nameToParent);
      }
   }
}
