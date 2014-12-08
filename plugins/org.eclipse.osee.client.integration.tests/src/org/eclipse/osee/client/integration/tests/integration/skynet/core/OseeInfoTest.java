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
package org.eclipse.osee.client.integration.tests.integration.skynet.core;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.framework.skynet.core.utility.ConnectionHandler;
import org.eclipse.osee.framework.skynet.core.utility.OseeInfo;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class OseeInfoTest {

   private static final String INSERT_KEY_VALUE_SQL = "INSERT INTO osee_info (OSEE_KEY, OSEE_VALUE) VALUES (?, ?)";
   private static final String DELETE_KEY_SQL = "DELETE FROM osee_info WHERE OSEE_KEY = ?";

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   @Test
   public void testMaxStaleness() throws InterruptedException {
      ConnectionHandler.runPreparedUpdate(INSERT_KEY_VALUE_SQL, "testKey", "testValue");
      String value = OseeInfo.getValue("testKey");
      Assert.assertEquals("testValue", value);

      ConnectionHandler.runPreparedUpdate(DELETE_KEY_SQL, "testKey");
      ConnectionHandler.runPreparedUpdate(INSERT_KEY_VALUE_SQL, "testKey", "testValue2");
      // Max Staleness is 3 weeks cached value shouldn't change
      value = OseeInfo.getValue("testKey");
      Assert.assertEquals("testValue", value);

      // Max Staleness is 1 second cached value shouldn't change even if we sleep for .8 seconds
      Thread.sleep(800);
      value = OseeInfo.getValue("testKey", (long) 1000);
      Assert.assertEquals("testValue", value);

      // Wait another .2 seconds and max staleness of 1 second will be exceeded and fresh value retrieved from DB
      Thread.sleep(800);
      value = OseeInfo.getValue("testKey", (long) 1000);
      Assert.assertEquals("testValue2", value);
   }
}