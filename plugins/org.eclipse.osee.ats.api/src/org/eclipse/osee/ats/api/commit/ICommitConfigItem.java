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
package org.eclipse.osee.ats.api.commit;

import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface ICommitConfigItem {

   public String getName() throws OseeCoreException;

   public long getBaselineBranchUuid() throws OseeCoreException;

   public Result isAllowCommitBranchInherited() throws OseeCoreException;

   public Result isAllowCreateBranchInherited() throws OseeCoreException;

   public String getCommitFullDisplayName() throws OseeCoreException;

   public String getTypeName();

}
