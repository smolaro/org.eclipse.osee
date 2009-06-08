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
package org.eclipse.osee.framework.ui.skynet.test.cases;

import static org.junit.Assert.assertTrue;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.skynet.core.test2.util.FrameworkTestUtil;
import org.eclipse.osee.framework.skynet.core.utility.Requirements;
import org.eclipse.osee.framework.ui.skynet.render.FileRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.render.WordTemplateRenderer;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;

/**
 * @author Megumi Telles
 */
public class WordTrackedChangesTest {
   private static final String TEST_PATH_NAME =
         "../org.eclipse.osee.framework.ui.skynet.test/src/org/eclipse/osee/framework/ui/skynet/test/cases/support/";
   private static final String TEST_WORD_EDIT_FILE_NAME = TEST_PATH_NAME + "WordTrackedChangesTest.xml";
   private static final String TEST_GEN_WORD_EDIT_FILE_NAME = TEST_PATH_NAME + "GeneralWordTrackedChangesTest.doc";
   private static boolean isWordRunning = false;

   /**
    * This test Word Edit's are being saved.
    */
   @Before
   public void setUp() throws Exception {
      isWordRunning = false;
      FrameworkTestUtil.cleanupSimpleTest(BranchManager.getKeyedBranch(DemoSawBuilds.SAW_Bld_2.name()),
            WordTrackedChangesTest.class.getSimpleName());
      WordAttribute.setDisplayTrackedChangesErrorMessage("");
      isWordRunning = FrameworkTestUtil.areWinWordsRunning();
      assertTrue(
            "This test kills all Word Documents. Cannot continue due to existing open Word Documents." + " Please save and close existing Word Documents before running this test.",
            isWordRunning == false);
   }

   /*
    * Verifies that the document does not save when it has tracked changes on  
    */

   @org.junit.Test
   public void testWordSaveWithTrackChanges() throws Exception {

      List<Artifact> artifacts = new ArrayList<Artifact>();
      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();
      FileRenderer.setWorkbenchSavePopUpDisabled(true);
      Branch branch = BranchManager.getKeyedBranch(DemoSawBuilds.SAW_Bld_2.name());
      // create a new requirement artifact
      Artifact newArt =
            ArtifactTypeManager.addArtifact(Requirements.SOFTWARE_REQUIREMENT, branch, getClass().getSimpleName());
      newArt.persistAttributesAndRelations();
      artifacts = Arrays.asList(newArt);
      WordTemplateRenderer renderer = new WordTemplateRenderer();
      renderer = WordEditTest.openArtifacts(artifacts);
      makeChangesToArtifact(renderer, TEST_WORD_EDIT_FILE_NAME, artifacts);
      Thread.sleep(5000);
      assertTrue("Detected Tracked Changes Succcessfully", WordAttribute.getDisplayTrackedChangesErrorMessage().equals(
            "Detected tracked changes on for this artifact.") == true);
      TestUtil.severeLoggingEnd(monitorLog);
   }

   /*
    * Verifies that on a general document the save was success with tracked changes
    */
   @org.junit.Test
   public void testGeneralWordSaveWithTrackChanges() throws Exception {
      List<Artifact> artifacts = new ArrayList<Artifact>();
      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();
      FileRenderer.setWorkbenchSavePopUpDisabled(true);
      Branch branch = BranchManager.getCommonBranch();
      // create a new general document artifact
      Artifact newArt = ArtifactTypeManager.addArtifact("General Document", branch, getClass().getSimpleName());
      newArt.persistAttributesAndRelations();
      artifacts = Arrays.asList(newArt);
      RendererManager.openInJob(artifacts, PresentationType.SPECIALIZED_EDIT);
      FileRenderer renderer = RendererManager.getBestFileRenderer(PresentationType.SPECIALIZED_EDIT, newArt);
      makeChangesToArtifact(renderer, TEST_GEN_WORD_EDIT_FILE_NAME, artifacts);
      Thread.sleep(5000);
      assertTrue("Did not Detect Tracked Changes",
            WordAttribute.getDisplayTrackedChangesErrorMessage().equals("") == true);
      TestUtil.severeLoggingEnd(monitorLog);
   }

   /*
    * Verifies that a whole word document cannot save with tracked changes on
    */
   @org.junit.Test
   public void testWholeWordSaveWithTrackChanges() throws Exception {

      List<Artifact> artifacts = new ArrayList<Artifact>();
      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();
      FileRenderer.setWorkbenchSavePopUpDisabled(true);
      Branch branch = BranchManager.getKeyedBranch(DemoSawBuilds.SAW_Bld_2.name());
      // create a new requirement artifact
      Artifact newArt = ArtifactTypeManager.addArtifact("Test Procedure WML", branch, getClass().getSimpleName());
      newArt.persistAttributesAndRelations();
      artifacts = Arrays.asList(newArt);
      RendererManager.openInJob(artifacts, PresentationType.SPECIALIZED_EDIT);
      FileRenderer renderer = RendererManager.getBestFileRenderer(PresentationType.SPECIALIZED_EDIT, newArt);
      makeChangesToArtifact(renderer, TEST_WORD_EDIT_FILE_NAME, artifacts);
      Thread.sleep(5000);
      assertTrue("Detected Tracked Changes Succcessfully", WordAttribute.getDisplayTrackedChangesErrorMessage().equals(
            "Detected tracked changes on for this artifact.") == true);
      TestUtil.severeLoggingEnd(monitorLog);
   }

   @After
   public void tearDown() throws Exception {
      if (!isWordRunning) {
         FrameworkTestUtil.cleanupSimpleTest(BranchManager.getKeyedBranch(DemoSawBuilds.SAW_Bld_2.name()),
               WordTrackedChangesTest.class.getSimpleName());
         FrameworkTestUtil.cleanupSimpleTest(BranchManager.getCommonBranch(),
               WordTrackedChangesTest.class.getSimpleName());
         Thread.sleep(7000);
         FrameworkTestUtil.killAllOpenWinword();
      }
   }

   public static IFile makeChangesToArtifact(FileRenderer renderer, String file, List<Artifact> artifacts) throws IOException, InterruptedException {
      IFile renderedFile = null;
      try {
         WordAttribute.setNoPopUps(true);
         renderedFile = renderer.getRenderedFile(artifacts, PresentationType.SPECIALIZED_EDIT);
         InputStream inputStream = new FileInputStream(file);
         final IFile rFile = renderedFile;
         rFile.setContents(inputStream, IResource.FORCE, new NullProgressMonitor());
         inputStream.close();
      } catch (Exception ex) {
         System.out.println(ex.toString());
      } finally {
         FrameworkTestUtil.killAllOpenWinword();
      }
      return renderedFile;
   }

}
