/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.ide.actions;

import java.util.Arrays;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osee.ats.api.util.AtsImage;
import org.eclipse.osee.ats.ide.actions.wizard.NewActionWizard;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class NewAction extends AbstractAtsAction {

   private final String actionableItem;

   public NewAction() {
      this(null);
   }

   public NewAction(String actionableItem) {
      super("Create New Action");
      this.actionableItem = actionableItem;
      setImageDescriptor(ImageManager.getImageDescriptor(AtsImage.NEW_ACTION));
      setToolTipText("Create New Action");
   }

   @Override
   public void runWithException() {
      NewActionWizard wizard = new NewActionWizard();
      if (actionableItem != null) {
         wizard.setInitialAias(AtsApiService.get().getActionableItemService().getActionableItems(
            Arrays.asList(actionableItem)));
      }
      WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
      dialog.create();
      dialog.open();
   }

}