/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.artifact;

import java.util.Collection;
import java.util.logging.Level;
import junit.framework.Assert;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * This test should be run as the last test of a suite to make sure that the ArtifactCache has no dirty artifacts.
 * 
 * @author Donald G. Dunne
 */
public class DirtyArtifactCacheTest {

   @org.junit.Test
   public void testArtifactCacheNotDirty() {
      final Collection<Artifact> dirtyArtifacts = ArtifactCache.getDirtyArtifacts();
      Assert.assertTrue(String.format(
         "After all tests are run, there should be no dirty artifacts in Artifact Cache; Found [%s]",
         Artifacts.getNames(dirtyArtifacts)), dirtyArtifacts.isEmpty());
      for (Artifact artifact : dirtyArtifacts) {
         OseeLog.format(getClass(), Level.WARNING, "Name: %s Type: %s ", artifact.getName(),
            artifact.getArtifactTypeName());
      }
   }
}
