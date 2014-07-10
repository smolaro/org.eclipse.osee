/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.cpa;

import org.eclipse.osee.ats.api.cpa.IAtsCpaProgram;
import org.eclipse.osee.framework.jdk.core.type.UuidNamedIdentity;

/**
 * @author Donald G. Dunne
 */
public class CpaProgram extends UuidNamedIdentity<Long> implements IAtsCpaProgram {

   public CpaProgram(Long uuid, String name) {
      super(uuid, name);
   }
}
