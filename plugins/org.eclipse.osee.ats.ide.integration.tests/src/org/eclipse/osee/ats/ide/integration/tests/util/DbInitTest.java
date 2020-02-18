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
package org.eclipse.osee.ats.ide.integration.tests.util;

import static org.junit.Assert.assertTrue;
import java.util.List;
import org.eclipse.osee.ats.ide.demo.DemoChoice;
import org.eclipse.osee.ats.ide.integration.tests.AtsClientService;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.database.init.DatabaseInitializationOperation;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.ui.skynet.render.RenderingUtil;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * @author Donald G. Dunne
 */
public class DbInitTest {
   private static boolean wasDbInitSuccessful = false;

   @BeforeClass
   public static void setup() throws Exception {
      OseeProperties.setIsInTest(true);
      assertTrue("Demo Application Server must be running",
         ClientSessionManager.getAuthenticationProtocols().contains("demo"));
      RenderingUtil.setPopupsAllowed(false);
   }

   @org.junit.Test
   public void testDbInit() throws Exception {
      OseeProperties.setIsInTest(true);
      List<String> protocols = ClientSessionManager.getAuthenticationProtocols();
      Assert.assertTrue("Application Server must be running." + protocols, protocols.contains("demo"));

      System.out.println("\nBegin Database Initialization");

      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();
      OseeLog.registerLoggerListener(monitorLog);

      DatabaseInitializationOperation.execute(DemoChoice.ATS_CLIENT_DEMO);

      TestUtil.severeLoggingEnd(monitorLog);
      wasDbInitSuccessful = true;

      if (wasDbInitSuccessful) {
         TestUtil.setDbInitSuccessful(true);

         // Re-authenticate so we can continue and NOT be bootstrap
         ClientSessionManager.releaseSession();
         ClientSessionManager.getSession();
         UserManager.releaseUser();

         AtsClientService.get().getServerEndpoints().getConfigEndpoint().getWithPend();
         AtsClientService.get().clearCaches();
         AtsClientService.get().getConfigService().getConfigurationsWithPend();

         if (UserManager.isBootstrap()) {
            throw new OseeStateException("Should not be bootstrap user here");
         }
         if (AtsClientService.get().getUserService().getCurrentUser().getUserId().equals("bootstrap")) {
            throw new OseeStateException("Should not be bootstrap user here");
         }
      }

      OseeProperties.setIsInTest(false);

      //Ensure that all workDefs loaded without error
      AtsClientService.get().getWorkDefinitionService().getAllWorkDefinitions();

      System.out.println("End Database Initialization\n");

   }
}
