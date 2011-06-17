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
package org.eclipse.osee.framework.skynet.core.rule;

import java.util.Collection;
import junit.framework.Assert;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * <p>
 * Not related to {@link Rule}.
 * </p>
 * <p>
 * Checks Artifact Cache for dirty artifacts. Executes after a passing test. Fails tests that have passed but left dirty
 * artifacts in the cache.
 * </p>
 * <p>
 * In the future the behavior of this class could be modified to clean up after a test, regardless of test's context.
 * </p>
 */
public class OseeHousekeepingRule implements MethodRule {

   @Override
   public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
      return new Statement() {
         @Override
         public void evaluate() throws Throwable {
            base.evaluate();
            verify(method, target);
         }
      };
   }

   private void verify(FrameworkMethod method, Object target) throws Throwable {
      final Collection<Artifact> dirtyArtifacts = ArtifactCache.getDirtyArtifacts();

      if (!dirtyArtifacts.isEmpty()) {
         StringBuilder entireMessage = new StringBuilder();
         entireMessage.append("Dirty artifacts in Artifact Cache:");
         for (Artifact artifact : dirtyArtifacts) {
            entireMessage.append(String.format("\n[%s] of type [%s] found while executing: %s.%s()",
               artifact.getName(), artifact.getArtifactType(), target.getClass().getSimpleName(), method.getName()));
         }
         Assert.fail(entireMessage.toString());
      }
   }
}
