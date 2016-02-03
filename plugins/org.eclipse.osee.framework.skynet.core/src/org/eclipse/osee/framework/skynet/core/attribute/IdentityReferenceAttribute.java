/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.attribute;

import org.eclipse.osee.framework.core.data.HasUuid;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.UuidBaseIdentityJaxRs;
import org.eclipse.osee.framework.skynet.core.attribute.service.AttributeAdapterService;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;

public abstract class IdentityReferenceAttribute<T extends HasUuid> extends CharacterBackedAttribute<T> {

   @Override
   public T getValue() throws OseeCoreException {
      return convertStringToValue(getAttributeDataProvider().getValueAsString());
   }

   @Override
   protected T convertStringToValue(String value) throws OseeCoreException {
      AttributeAdapterService service = getAttributeAdapter();
      T identity = service.adapt(this, new UuidBaseIdentityJaxRs(Long.valueOf(value)));
      return identity;
   }

   private AttributeAdapterService getAttributeAdapter() throws OseeCoreException {
      return ServiceUtil.getAttributeAdapterService();
   }

}
