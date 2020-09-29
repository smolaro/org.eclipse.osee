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

package org.eclipse.osee.framework.ui.skynet.artifact;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.skynet.core.access.AccessControlArtifactUtil;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.access.internal.OseeApiService;
import org.eclipse.osee.framework.ui.skynet.artifact.prompt.IHandlePromptChange;
import org.eclipse.osee.framework.ui.skynet.artifact.prompt.IPromptFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.XResultDataDialog;

/**
 * @author Jeff C. Phillips
 * @author Donald G. Dunne
 */
public final class ArtifactPrompt {

   private final IPromptFactory promptFactory;

   public ArtifactPrompt(IPromptFactory promptFactory) {
      this.promptFactory = promptFactory;
   }

   public boolean promptChangeAttribute(AttributeTypeToken attributeType, final Collection<Artifact> artifacts, boolean persist, boolean multiLine) {
      boolean toReturn = false;
      XResultData rd =
         OseeApiService.get().getAccessControlService().hasAttributeTypePermission(artifacts, attributeType,
            PermissionEnum.WRITE, AccessControlArtifactUtil.getXResultAccessHeader("Change Attribute", artifacts, attributeType));

      if (rd.isErrors()) {
         XResultDataDialog.open(rd, "Change Attribute", "Permission Denied Changing Attribute %s",
            attributeType.toStringWithId());
         return false;
      }

      IHandlePromptChange promptChange =
         promptFactory.createPrompt(attributeType, attributeType.getUnqualifiedName(), artifacts, persist, multiLine);
      if (promptChange.promptOk()) {
         toReturn = promptChange.store();
      }
      return toReturn;
   }
}
