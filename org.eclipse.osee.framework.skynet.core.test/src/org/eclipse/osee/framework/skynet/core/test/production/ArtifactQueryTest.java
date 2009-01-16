package org.eclipse.osee.framework.skynet.core.test.production;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactQueryTest extends TestCase {

   public void testGetArtifactFromId() throws OseeCoreException {
      Branch common = BranchManager.getCommonBranch();
      Artifact root = ArtifactPersistenceManager.getDefaultHierarchyRootArtifact(common);
      Artifact artifact = ArtifactQuery.getArtifactFromId(root.getHumanReadableId(), common);
      assertEquals(root.getHumanReadableId(), artifact.getHumanReadableId());
   }

   public void testGetArtifactsFromBranch() throws OseeCoreException {
      Branch common = BranchManager.getCommonBranch();
      List<Artifact> artifacts = ArtifactQuery.getArtifactsFromBranch(common, true);

      assertTrue(artifacts.size() > 0);
      for (Artifact artifact : artifacts) {
         assertTrue(artifact.getDescriptiveName().length() > 0);
         artifact.isOrphan(); // this is good exercise - like doing push-ups
      }
   }

   public void testQuickSearch() throws Exception {
      Branch branch = BranchManager.getKeyedBranch("V13 FTB0");

      String query = "[.PRE_RETRIES]";
      String expected =
            "PRESET_DATABASE Local Data Definition, {MODEM_PARAMETER_SELECTION} Procedure, {RETRIES_0} Display Logic, {RETRIES_1} Display Logic, {RETRIES_2} Display Logic, {RETRIES} Display Logic";
      checkSearch(branch, query, expected, true, false);

      query = "[.PRE_RETRIES]";
      expected =
            "PRESET_DATABASE Local Data Definition, {MODEM_PARAMETER_SELECTION} Procedure, {RETRIES_0} Display Logic, {RETRIES_1} Display Logic, {RETRIES_2} Display Logic, {RETRIES} Display Logic, {UPDATE_LONGBOW_NET_PARAMETERS} Procedure";
      checkSearch(branch, query, expected, true, true);

      query = "edit_ale_net_sel";
      expected =
            "{EDIT_ALE_NET_SEL} Display Logic, {EDIT_ALE_NET} Display Logic, {EDIT_CALL_ADDRESS} Display Logic, {EDIT_CALL_SIGN} Display Logic, {EDIT_CIPHER_MODE} Display Logic, {EDIT_FREQ_SEL} Display Logic, {EDIT_MODE} Display Logic, {EDIT_UNIT_ID} Display Logic";
      checkSearch(branch, query, expected, true, false);

      query = "[.MODE_SUPPORTED]";
      expected =
            "Radio_Configuration Local Data Definition, {NON_TAC_SLOTS} Display Logic, {SC_FH_TUNE} Display Logic, {TAC_SLOTS} Display Logic";
      checkSearch(branch, query, expected, true, false);

      query = "[<.MODE>](B)";
      expected =
            "(PLT|CPG) {CURSOR} Display Logic, ARC-164 Tactical Tuning., ARC-186 Tactical Tune., Auto Reply to FARM Query, Basic Weight and Moment., Chaff Dispensing Selections., EDIT_PRESET_DATABASE NVM Local Data Definition, Engine Performance., Guard Mode Processing Requirements., IDM Message Receive Summary Buffer., MICD Changes, MISSION DTC FORMATS (A THRU K), MISSION DTC FORMATS (L THRU Z), Manual Fuel Boost OFF Selection., PN Tuning, Symbology Select/Flight Page Access Switch, Tones, View Uncorrelated Overlay , {ARC 220 STANDARD INFO RESPONSE}PROCEDURE.  (4097), {ARC-201 RADIOS TACTICAL TUNE}., {COORD_SEARCH} Display Logic, {CURRENT_OVERLAY} Display Logic, {EDIT_FREQ} Display Logic, {FH Moding for Load}PROCEDURE., {FM1_OFFSET} Display Logic, {FM2_OFFSET} Display Logic, {HF_SHOT_AT_SYMBOL} Display Logic, {HF_XMIT_MAN} Display Logic, {IDM_AFAPD_OTA_RECV}., {IDM_API_PROCESSING}PROCEDURE., {IDM_JVMF_OTA_RECV}., {IDM_MSG_REC_BUFF_REQ}., {IDM_MSG_REC_BUFF_STORAGE}., {IFF MODE 4 CAUTION CONDITION}., {IFF MODE 5 CAUTION CONDITION}., {LIST_MSG_REC_BUFFER}, {NON_TAC_SLOTS} Display Logic, {ORDERS} Display Logic, {PROCESS_PRIORITY_FIRE_ZONES_DATA} Procedure, {RPT_SEND} Display Logic, {TAC_SLOTS} Display Logic, {UFD_PRESET_DATA} Procedure, {WEAPONS PROCESSOR TO DISPLAY PROCESSOR}.";
      checkSearch(branch, query, expected, true, false);

      query = "EDIT_CALL_ADDRESS";
      expected =
            "{EDIT_ALE_NET} Display Logic, {EDIT_CALL_ADDRESS_SEL} Display Logic, {EDIT_CALL_ADDRESS} Display Logic, {EDIT_CALL_ADDRESS} Maintained Button, {EDIT_CALL_SIGN} Display Logic, {EDIT_CIPHER_MODE} Display Logic, {EDIT_CNV} Display Logic, {EDIT_FREQ_SEL} Display Logic, {EDIT_FREQ_UIG} Display Logic, {EDIT_MODE} Display Logic, {EDIT_UNIT_ID} Display Logic";
      checkSearch(branch, query, expected, true, false);

      branch = BranchManager.getBranch("Block III - FTB2");
      query = "EOM_CCF_SEND_CHKFIRE";
      expected = "{IDM NETWORK API STATUS}, {VMF_ARTY_CANC_CHK_FIRE_SEND} Procedure";
      checkSearch(branch, query, expected, true, false);
   }

   private void checkSearch(Branch branch, String query, String expected, boolean matchWordOrder, boolean allowDeleted, String... attributeTypes) throws Exception {
      List<Artifact> artifacts =
            ArtifactQuery.getArtifactsFromAttributeWithKeywords(branch, query, matchWordOrder, allowDeleted,
                  attributeTypes);
      Collections.sort(artifacts);
      Artifact[] results = artifacts.toArray(new Artifact[artifacts.size()]);
      assertEquals(String.format("[%s]", expected), Arrays.deepToString(results));
   }
}