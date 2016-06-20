/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.artifact.editor.pages;

import java.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.branch.ViewApplicabilityUtil;
import org.eclipse.osee.framework.ui.skynet.internal.ServiceUtil;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.orcs.rest.model.Applicabilities;
import org.eclipse.osee.orcs.rest.model.ApplicabilityEndpoint;
import org.eclipse.osee.orcs.rest.model.ArtifactIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Donald G. Dunne
 */
public class ArtifactFormPageViewApplicability {

   private FormText text;
   private static Image LOCK_IMAGE;
   private Button button;
   private final FormToolkit toolkit;
   private final ScrolledForm form;
   private final Artifact artifact;

   public ArtifactFormPageViewApplicability(Artifact artifact, FormToolkit toolkit, ScrolledForm form) {
      this.artifact = artifact;
      this.toolkit = toolkit;
      this.form = form;
   }

   private void refreshButton() {
      if (ViewApplicabilityUtil.isChangeApplicabilityValid(Collections.singleton(artifact))) {
         button.setText("Change");
         button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
               super.widgetSelected(e);
               boolean changed = ViewApplicabilityUtil.changeApplicability(Collections.singleton(artifact));
               if (changed) {
                  refresh();
               }
            }

         });
      } else {
         button.setImage(getLockImage());
         button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
               AWorkbench.popup("Permission Denied", ViewApplicabilityUtil.CHANGE_APPLICABILITY_INVAILD);
            }

         });

      }
   }

   private Image getLockImage() {
      if (LOCK_IMAGE == null) {
         LOCK_IMAGE = ImageManager.getImage(FrameworkImage.LOCK_OVERLAY);
      }
      return LOCK_IMAGE;
   }

   public void create() {
      Composite applicabilityComp = toolkit.createComposite(form.getForm().getBody(), SWT.WRAP);
      applicabilityComp.setLayout(ALayout.getZeroMarginLayout(2, false));
      applicabilityComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));

      text = toolkit.createFormText(applicabilityComp, false);
      text.setText(getArtifactViewApplicabiltyText(), true, false);
      text.setForeground(Displays.getSystemColor(SWT.COLOR_DARK_GRAY));

      button = toolkit.createButton(applicabilityComp, "", SWT.PUSH);
      refreshButton();
   }

   public void refresh() {
      text.setText(getArtifactViewApplicabiltyText(), true, false);
      refreshButton();
   }

   private String getArtifactViewApplicabiltyText() {
      String result = "";
      ApplicabilityEndpoint applEndpoint = ServiceUtil.getOseeClient().getApplicabilityEndpoint();
      if (applEndpoint == null) {
         result = "Error: Applicabilty Service not found";
      } else {
         ArtifactIds artifactIds = new ArtifactIds();
         artifactIds.getArtifactIds().add(artifact.getUuid());
         Applicabilities applicabilities = applEndpoint.getApplicabilities(artifactIds);
         if (!applicabilities.getApplicabilities().isEmpty() && applicabilities.getApplicabilities().iterator().next().getApplicability() != null) {
            result = applicabilities.getApplicabilities().iterator().next().getApplicability().getName();
         }
      }
      return String.format("<form><p><b>View Applicability:</b> %s</p></form>", result);
   }

}
