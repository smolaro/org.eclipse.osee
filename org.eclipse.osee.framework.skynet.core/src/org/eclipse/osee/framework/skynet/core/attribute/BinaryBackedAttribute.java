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
package org.eclipse.osee.framework.skynet.core.attribute;

import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.providers.IBinaryAttributeDataProvider;

/**
 * @author Roberto E. Escobar
 */
public abstract class BinaryBackedAttribute<T> extends Attribute<T> {

   protected BinaryBackedAttribute(AttributeType attributeType, Artifact artifact, ModificationType modificationType) throws OseeCoreException {
      super(attributeType, artifact, modificationType);
   }

   public IBinaryAttributeDataProvider getAttributeDataProvider() {
      // this cast is always safe since the the data provider passed in the constructor to 
      // the super class is of type  IBinaryAttributeDataProvider
      return (IBinaryAttributeDataProvider) super.getAttributeDataProvider();
   }
}
