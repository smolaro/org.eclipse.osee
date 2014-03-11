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
package org.eclipse.osee.framework.core.model.event;

/**
 * @author Donald G. Dunne
 */
public interface IBasicGuidRelation {

   DefaultBasicGuidArtifact getArtA();

   DefaultBasicGuidArtifact getArtB();

   Long getRelTypeGuid();

   Long getBranchUuid();

   int getGammaId();

}
