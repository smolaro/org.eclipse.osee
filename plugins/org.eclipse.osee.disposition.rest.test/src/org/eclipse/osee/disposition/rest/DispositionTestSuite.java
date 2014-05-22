/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.disposition.rest;

import org.eclipse.osee.disposition.rest.importer.ImporterTestSuite;
import org.eclipse.osee.disposition.rest.internal.InternalTestSuite;
import org.eclipse.osee.disposition.rest.report.ReportTestSuite;
import org.eclipse.osee.disposition.rest.resources.ResourcesTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Angel Avila
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ResourcesTestSuite.class, InternalTestSuite.class, ImporterTestSuite.class, ReportTestSuite.class})
public class DispositionTestSuite {
   // Test Suite
}
