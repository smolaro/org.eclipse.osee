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
package org.eclipse.osee.framework.core.data;

import org.codehaus.jackson.annotate.JsonCreator;
import org.eclipse.osee.framework.jdk.core.type.BaseId;
import org.eclipse.osee.framework.jdk.core.type.Id;

/**
 * @author Ryan D. Brooks
 */
public interface TupleFamilyId extends Id {

   @JsonCreator
   public static TupleFamilyId valueOf(long tupleFamilyTypeId) {
      final class TupleFailyTypeImpl extends BaseId implements TupleFamilyId {
         public TupleFailyTypeImpl(Long tupleFamilyTypeId) {
            super(tupleFamilyTypeId);
         }
      }
      return new TupleFailyTypeImpl(tupleFamilyTypeId);
   }
}