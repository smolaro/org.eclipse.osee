/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.workdef.model;

import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;

/**
 * @author Donald G. Dunne
 */
public class LayoutItem extends AbstractWorkDefItem implements IAtsLayoutItem {

   public LayoutItem(String name) {
      super(Long.valueOf(name.hashCode()), name);
   }

}
