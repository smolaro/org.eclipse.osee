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
package org.eclipse.osee.ats.rest.test.db;

import org.eclipse.osee.ats.rest.test.db.internal.AtsTestDatabase;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Donald G. Dunne
 */
public class AtsMethodDatabase implements TestRule {

   @Override
   public Statement apply(final Statement base, final Description description) {
      return new Statement() {
         @Override
         public void evaluate() throws Throwable {
            Assert.assertNotNull("Description cannot be null", description);
            AtsTestDatabase db = new AtsTestDatabase(description.getClassName(), description.getMethodName(), false);
            try {
               db.initialize();
               base.evaluate();
            } finally {
               db.cleanup();
            }
         }
      };
   }
}
