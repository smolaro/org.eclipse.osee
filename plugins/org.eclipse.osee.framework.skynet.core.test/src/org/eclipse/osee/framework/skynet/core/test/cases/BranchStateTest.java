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
package org.eclipse.osee.framework.skynet.core.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import junit.framework.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.data.SystemUser;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.operation.FinishUpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.update.ConflictResolverOperation;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.junit.Before;

/**
 * @author Roberto E. Escobar
 */
public class BranchStateTest {

   @Before
   public void setUp() throws Exception {
      // This test should only be run on test db
      assertFalse(ClientSessionManager.isProductionDataStore());
      BranchManager.refreshBranches();
   }

   @org.junit.Test
   public void testCreateState() throws OseeCoreException {
      String originalBranchName = "Create State Branch";
      Branch workingBranch = null;
      try {
         User user = UserManager.getUser(SystemUser.OseeSystem);
         workingBranch = BranchManager.createWorkingBranch(DemoSawBuilds.SAW_Bld_1, originalBranchName, user);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(workingBranch.isEditable());
      } finally {
         if (workingBranch != null) {
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @org.junit.Test
   public void testModifiedState() throws OseeCoreException {
      String originalBranchName = "Modified State Branch";
      Branch workingBranch = null;
      try {
         User user = UserManager.getUser(SystemUser.OseeSystem);
         workingBranch = BranchManager.createWorkingBranch(DemoSawBuilds.SAW_Bld_1, originalBranchName, user);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(workingBranch.isEditable());

         Artifact change =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch,
               "Test Object on Working Branch");
         change.persist();

         assertEquals(BranchState.MODIFIED, workingBranch.getBranchState());
         assertTrue(workingBranch.isEditable());
      } finally {
         if (workingBranch != null) {
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @org.junit.Test
   public void testDeleteState() throws OseeCoreException, InterruptedException {
      String originalBranchName = "Deleted State Branch";
      Branch workingBranch = null;
      try {
         User user = UserManager.getUser(SystemUser.OseeSystem);
         workingBranch = BranchManager.createWorkingBranch(DemoSawBuilds.SAW_Bld_1, originalBranchName, user);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(workingBranch.isEditable());

         Job job = BranchManager.deleteBranch(workingBranch);
         job.join();
         assertEquals(BranchState.DELETED, workingBranch.getBranchState());
         assertTrue(workingBranch.getArchiveState().isArchived());
         assertTrue(!workingBranch.isEditable());
         assertTrue(workingBranch.getBranchState().isDeleted());
      } finally {
         if (workingBranch != null) {
            // needed to allow for archiving to occur
            Thread.sleep(5000);
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @org.junit.Test
   public void testCommitState() throws OseeCoreException, InterruptedException {
      String originalBranchName = "Commit State Branch";
      Branch workingBranch = null;
      Artifact change = null;
      try {
         User user = UserManager.getUser(SystemUser.OseeSystem);
         workingBranch = BranchManager.createWorkingBranch(DemoSawBuilds.SAW_Bld_1, originalBranchName, user);
         assertEquals(BranchState.CREATED, workingBranch.getBranchState());
         assertTrue(workingBranch.isEditable());

         change =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch, "A commit change");
         change.persist();

         assertEquals(BranchState.MODIFIED, workingBranch.getBranchState());
         assertTrue(workingBranch.isEditable());

         ConflictManagerExternal conflictManager =
            new ConflictManagerExternal(BranchManager.getBranch(DemoSawBuilds.SAW_Bld_1), workingBranch);
         BranchManager.commitBranch(null, conflictManager, true, false);

         assertEquals(BranchState.COMMITTED, workingBranch.getBranchState());
         assertTrue(workingBranch.getArchiveState().isArchived());
         assertTrue(!workingBranch.isEditable());
      } finally {
         if (workingBranch != null) {
            // needed to allow for archiving to occur
            Thread.sleep(5000);
            BranchManager.purgeBranch(workingBranch);
         }
      }
   }

   @org.junit.Test
   public void testRebaselineBranchNoChanges() throws Exception {
      String originalBranchName = "UpdateBranch No Changes Test";
      Branch workingBranch = null;
      try {
         User user = UserManager.getUser(SystemUser.OseeSystem);
         workingBranch = BranchManager.createWorkingBranch(DemoSawBuilds.SAW_Bld_1, originalBranchName, user);

         // Update the branch
         ConflictResolverOperation resolverOperation =
            new ConflictResolverOperation("Test 1 Resolver", BranchStateTest.class.getCanonicalName()) {

               @Override
               protected void doWork(IProgressMonitor monitor) throws Exception {
                  assertFalse("This code should not be executed since there shouldn't be any conflicts.", wasExecuted());
               }
            };

         Job job = BranchManager.updateBranch(workingBranch, resolverOperation);
         job.join();

         Assert.assertEquals(BranchState.DELETED, workingBranch.getBranchState());
         Assert.assertEquals(user.getArtId(), BranchManager.getAssociatedArtifact(workingBranch).getArtId());

         Collection<Branch> branches = BranchManager.getBranchesByName(originalBranchName);
         assertEquals("Check only 1 original branch", 1, branches.size());

         Branch newWorkingBranch = branches.iterator().next();
         assertTrue(workingBranch.getId() != newWorkingBranch.getId());
         assertEquals(originalBranchName, newWorkingBranch.getName());
         assertTrue("New Working branch was not editable", newWorkingBranch.isEditable());
         assertFalse("New Working branch was editable", workingBranch.isEditable());
      } finally {
         cleanup(originalBranchName, workingBranch, null);
      }
   }

   @org.junit.Test
   public void testRebaselineWithoutConflicts() throws Exception {
      String originalBranchName = "UpdateBranch Test 1";
      Artifact baseArtifact = null;
      Branch workingBranch = null;
      Artifact change = null;
      try {
         baseArtifact =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, DemoSawBuilds.SAW_Bld_1,
               "Test Object");
         baseArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation, "This is the base annotation");
         baseArtifact.persist();

         User user = UserManager.getUser(SystemUser.OseeSystem);
         workingBranch = BranchManager.createWorkingBranch(DemoSawBuilds.SAW_Bld_1, originalBranchName, user);

         // Add a new artifact on the working branch
         change =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement, workingBranch,
               "Test Object on Working Branch");
         change.persist();

         // Make a change on the parent
         baseArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation, "This is the updated annotation");
         baseArtifact.persist();

         // Update the branch
         ConflictResolverOperation resolverOperation =
            new ConflictResolverOperation("Test 1 Resolver", BranchStateTest.class.getCanonicalName()) {

               @Override
               protected void doWork(IProgressMonitor monitor) throws Exception {
                  assertFalse("This code should not be executed since there shouldn't be any conflicts.", wasExecuted());
               }
            };

         Job job = BranchManager.updateBranch(workingBranch, resolverOperation);
         job.join();
         assertTrue("UpdateBranch was not successful", job.getResult().isOK());
         assertTrue("Resolver was executed", !resolverOperation.wasExecuted());

         checkBranchWasRebaselined(originalBranchName, workingBranch);
         // Check that the associated artifact remained unchanged
         assertEquals(workingBranch.getAssociatedArtifactId().intValue(), user.getArtId());

         Collection<Branch> branches = BranchManager.getBranchesByName(originalBranchName);
         assertEquals("Check only 1 original branch", 1, branches.size());

         Branch newWorkingBranch = branches.iterator().next();
         assertTrue(workingBranch.getId() != newWorkingBranch.getId());
         assertEquals(originalBranchName, newWorkingBranch.getName());
         assertTrue("New Working branch is editable", newWorkingBranch.isEditable());
      } finally {
         cleanup(originalBranchName, workingBranch, null, change, baseArtifact);
      }
   }

   @org.junit.Test
   public void testRebaselineWithConflicts() throws Exception {
      String originalBranchName = "UpdateBranch Test 2";
      Artifact baseArtifact = null;
      Branch workingBranch = null;
      Branch mergeBranch = null;
      Artifact sameArtifact = null;
      try {
         baseArtifact =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirement,
               BranchManager.getBranch(DemoSawBuilds.SAW_Bld_1), "Test Object");
         baseArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation, "This is the base annotation");
         baseArtifact.persist();

         User user = UserManager.getUser(SystemUser.OseeSystem);
         workingBranch = BranchManager.createWorkingBranch(DemoSawBuilds.SAW_Bld_1, originalBranchName, user);

         // Modify same artifact on working branch
         sameArtifact = ArtifactQuery.getArtifactFromId(baseArtifact.getGuid(), workingBranch);
         sameArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation,
            "This is the working branch update annotation");
         sameArtifact.persist();

         // Make a change on the parent
         baseArtifact.setSoleAttributeFromString(CoreAttributeTypes.Annotation, "This is the updated annotation");
         baseArtifact.persist();

         ConflictResolverOperation resolverOperation =
            new ConflictResolverOperation("Test 2 Resolver", BranchStateTest.class.getCanonicalName()) {

               @Override
               protected void doWork(IProgressMonitor monitor) throws Exception {
                  assertTrue("This code should have been executed since there should be conflicts.", wasExecuted());
               }
            };

         Job job = BranchManager.updateBranch(workingBranch, resolverOperation);
         job.join();

         IStatus status = getCauseStatus(job.getResult());
         String message =
            String.format("UpdateBranch was not successful\n %s", status.getMessage(), status.getException());
         assertTrue(message, job.getResult().isOK());
         assertTrue("Resolver not executed", resolverOperation.wasExecuted());

         assertTrue("Branch was archived", !workingBranch.getArchiveState().isArchived());
         assertTrue("Branch was not marked as rebaseline in progress",
            workingBranch.getBranchState().isRebaselineInProgress());
         assertTrue("Branch was not editable", workingBranch.isEditable());
         assertTrue("Branch state was set to rebaselined before complete",
            !workingBranch.getBranchState().isRebaselined());

         assertEquals("Branch name was changed before update was complete", originalBranchName, workingBranch.getName());

         // Check that a new destination branch exists
         Branch destinationBranch = resolverOperation.getConflictManager().getDestinationBranch();
         assertTrue("Branch name not set correctly",
            destinationBranch.getName().startsWith(String.format("%s - for update -", originalBranchName)));
         assertTrue("Branch was not editable", destinationBranch.isEditable());

         // Check that we have a merge branch
         mergeBranch = BranchManager.getMergeBranch(workingBranch, destinationBranch);
         assertTrue("MergeBranch was not editable", mergeBranch.isEditable());
         assertEquals("Merge Branch should be in Created State", BranchState.CREATED, mergeBranch.getBranchState());

         // Run FinishBranchUpdate and check
         FinishUpdateBranchOperation finishUpdateOperation =
            new FinishUpdateBranchOperation("Update Branch Test 2", resolverOperation.getConflictManager(), true, true);
         Operations.executeWork(finishUpdateOperation, new NullProgressMonitor(), -1);

         IStatus status1 = getCauseStatus(finishUpdateOperation.getStatus());
         message =
            String.format("FinishUpdateBranch was not successful\n %s", status1.getMessage(), status1.getException());
         assertTrue(message, status1.isOK());

         checkBranchWasRebaselined(originalBranchName, workingBranch);

         Collection<Branch> branches = BranchManager.getBranchesByName(originalBranchName);
         assertEquals("Check only 1 original branch", 1, branches.size());

         Branch newWorkingBranch = branches.iterator().next();
         assertTrue(workingBranch.getId() != newWorkingBranch.getId());
         assertEquals(originalBranchName, newWorkingBranch.getName());
         assertTrue("New Working branch is editable", newWorkingBranch.isEditable());

         // Swapped successfully
         assertEquals(destinationBranch.getId(), newWorkingBranch.getId());
      } catch (Exception ex) {
         throw ex;
      } finally {
         cleanup(originalBranchName, workingBranch, mergeBranch, sameArtifact, baseArtifact);

      }
   }

   private IStatus getCauseStatus(IStatus status) {
      IStatus toReturn = status;
      if (!status.isOK() && status.isMultiStatus()) {
         for (IStatus child : status.getChildren()) {
            Throwable error = child.getException();
            if (error != null) {
               toReturn = child;
               break;
            }
         }
      }
      return toReturn;
   }

   private void cleanup(String originalBranchName, Branch workingBranch, Branch mergeBranch, Artifact... toDelete) {
      try {
         if (mergeBranch != null) {
            BranchManager.purgeBranch(mergeBranch);
         }
         if (workingBranch != null) {
            purgeBranchAndChildren(workingBranch);
         }
         for (Branch branch : BranchManager.getBranchesByName(originalBranchName)) {
            purgeBranchAndChildren(branch);
         }
         if (toDelete != null && toDelete.length > 0) {
            new PurgeArtifacts(Arrays.asList(toDelete)).execute();
         }
      } catch (Exception ex) {
         // Do Nothing;
      }
   }

   private void purgeBranchAndChildren(Branch branch) throws OseeCoreException {
      for (Branch child : branch.getChildBranches(true)) {
         BranchManager.purgeBranch(child);
      }
      BranchManager.purgeBranch(branch);
   }

   private void checkBranchWasRebaselined(String originalBranchName, Branch branchToCheck) {
      assertTrue("Branch was not archived", branchToCheck.getArchiveState().isArchived());
      assertTrue("Branch was still editable", !branchToCheck.isEditable());
      assertTrue("Branch state was not set as rebaselined", branchToCheck.getBranchState().isRebaselined());
      assertTrue("Branch name not set correctly",
         branchToCheck.getName().startsWith(String.format("%s - moved by update on -", originalBranchName)));
   }

}
