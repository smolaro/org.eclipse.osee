/*********************************************************************
 * Copyright (c) 2022 Boeing
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

package org.eclipse.osee.synchronization.rest;

import java.util.Objects;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.synchronization.rest.IdentifierType.Identifier;

/**
 * A common base class to represent Specifications or Spec Objects in the Synchronization Artifact.
 *
 * @author Loren K. Ashley
 */

public class CommonObjectGroveThing extends AbstractGroveThing {

   /**
    * Saves a reference to the {@link SpecTypeGroveThing} or {@link SpecObjectTypeGroveThing} object that defines the set of attributes that
    * this SpecificationGroveThing or Spec Object may contain.
    */

   private CommonObjectTypeGroveThing commonObjectTypeGroveThing;

   /**
    * A reference to a foreign hierarchy thing. Used for building the Spec Object hierarchy in the foreign DOM.
    */

   private Object foreignHierarchy;

   /**
    * Creates a new {@link CommonObjectGroveThing}. When assertions are enabled an assertion error will be throw when
    * <code>identifier</code> is not of the following types:
    * <ul>
    * <li>{@link IdentifierType#SPECIFICATION}, or</li>
    * <li>{@link IdentifierType#SPEC_OBJECT}.</li>
    * </ul>
    *
    * @param identifier a unique {@link Identifier}.
    */

   CommonObjectGroveThing(Identifier identifier) {
      super(identifier);

      assert ((identifier.getType() == IdentifierType.SPECIFICATION) || (identifier.getType() == IdentifierType.SPEC_OBJECT));

      this.commonObjectTypeGroveThing = null;
      this.foreignHierarchy = null;
   }

   /**
    * Gets the {@link SpecTypeGroveThing} or {@link SpecObjectTypeGroveThing} that defines the set of attributes for this SpecificationGroveThing or
    * Spec Object. When assertions are enabled an assertion error will be thrown if the encapsulate
    * {@link CommonObjectTypeGroveThing} reference is <code>null</code>.
    *
    * @return the {@link SpecTypeGroveThing} or {@link SpecObjectTypeGroveThing} that defines the set of attributes for this SpecificationGroveThing
    * or Spec Object.
    */

   public CommonObjectTypeGroveThing getCommonObjectType() {
      assert (this.commonObjectTypeGroveThing != null);
      return this.commonObjectTypeGroveThing;
   }

   /**
    * Gets the unique {@link Identifier} assigned to this {@link CommonObjectGroveThing}.
    *
    * @return the assigned identifier.
    */

   Identifier getIdentifier() {
      return super.getGroveThingKey();
   }

   /**
    * Gets the foreign hierarchy thing.
    *
    * @return the foreign hierarchy thing.
    */

   public Object getForeignHierarchy() {
      return this.foreignHierarchy;
   }

   /**
    * Sets the {@link SpecTypeGroveThing} or {@link SpecObjectTypeGroveThing} that defines the attributes for this SpecificationGroveThing or Spec
    * Object. When assertions are enabled an assertion error will be thrown when the {@link CommonObjectTypeGroveThing} object has
    * already been set.
    *
    * @param commonObjectTypeGroveThing the {@link CommonObjectTypeGroveThing} object to be saved.
    * @throws NullPointerException when the parameter <code>commonObjectTypeGroveThing</code> is <code>null</code>.
    */

   void setCommonObjectType(CommonObjectTypeGroveThing commonObjectTypeGroveThing) {
      assert Objects.isNull(this.commonObjectTypeGroveThing);
      this.commonObjectTypeGroveThing = Objects.requireNonNull(commonObjectTypeGroveThing);
   }

   /**
    * Saves a reference to a foreign hierarchy thing. Used for building the foreign DOM.
    *
    * @param foreignHierarchy the foreign hierarchy thing.
    */

   public void setForeignHierarchy(Object foreignHierarchy) {
      this.foreignHierarchy = foreignHierarchy;
   }

   /**
    * {@inheritDoc}<br>
    * <br>
    * When assertions are enabled an assertion error will be thrown when the <code>nativeThing</code> is not an instance
    * of {@link ArtifactReadable}.
    */

   @Override
   public GroveThing setNativeThing(Object nativeThing) {
      assert nativeThing instanceof ArtifactReadable;
      return super.setNativeThing(nativeThing);
   }

}

/* EOF */
