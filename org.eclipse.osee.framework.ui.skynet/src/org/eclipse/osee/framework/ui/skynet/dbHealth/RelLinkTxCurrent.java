/*
 * Created on Aug 13, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.dbHealth;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap;
import org.eclipse.osee.framework.ui.skynet.widgets.xresults.XResultData;
import org.eclipse.osee.framework.ui.skynet.widgets.xresults.XResultPage.Manipulations;

/**
 * @author Theron Virgin
 */
public class RelLinkTxCurrent extends DatabaseHealthTask {
   private static final String NO_TX_CURRENT_SET =
         "SELECT distinct rel.rel_link_id, det.branch_id FROM osee_tx_details det, osee_txs txs, osee_relation_link rel WHERE det.transaction_id = txs.transaction_id AND txs.gamma_id = rel.gamma_id AND txs.tx_current = 0 MINUS SELECT distinct rel.rel_link_id, det.branch_id FROM osee_tx_details det, osee_txs txs, osee_relation_link rel WHERE det.transaction_id = txs.transaction_id AND txs.gamma_id = rel.gamma_id AND txs.tx_current in (1,2,3)";

   private static final String QUERY_TX_CURRENT_SET =
         "SELECT txs.transaction_id, txs.gamma_id from osee_txs txs, osee_tx_details det, osee_relation_link rel WHERE det.branch_id = ? And det.transaction_id = txs.transaction_id And txs.tx_current in (1,2) And txs.gamma_id = rel.gamma_id and rel.rel_link_id = ?";

   private static final String NO_TX_CURRENT_CLEANUP =
         "UPDATE osee_txs SET tx_current = CASE WHEN mod_type = 3 THEN 2 WHEN mod_type = 5 THEN 3 ELSE 1  END WHERE (transaction_id, gamma_id) = (SELECT txs2.transaction_id, txs2.gamma_id FROM osee_txs txs2, osee_relation_link rel2 WHERE rel2.rel_link_id = ? And rel2.gamma_id = txs2.gamma_id  And txs2.transaction_id = (SELECT MAX(txs.transaction_id) from osee_txs txs, osee_tx_details det, osee_relation_link rel WHERE det.branch_id = ? And det.transaction_id = txs.transaction_id And txs.gamma_id = rel.gamma_id and rel.rel_link_id = ?))";

   private static final String MULTIPLE_TX_CURRENT_SET =
         "SELECT resulttable.branch_id, resulttable.rel_link_id, COUNT(resulttable.branch_id) AS numoccurrences FROM (SELECT txd1.branch_id, rel1.rel_link_id FROM osee_tx_details txd1, osee_txs txs1, osee_relation_link rel1 WHERE txd1.transaction_id = txs1.transaction_id AND txs1.gamma_id = rel1.gamma_id AND txs1.tx_current in (1,2)) resulttable GROUP BY resulttable.branch_id, resulttable.rel_link_id HAVING(COUNT(resulttable.branch_id) > 1) order by branch_id";

   private static final String DUPLICATE_RELATIONS_TX_CURRENT =
         "Select rel1.rel_link_id, rel1.gamma_id as gamma_id_1, rel2.gamma_id as gamma_id_2, txs1.tx_current as tx_current_1, txs2.tx_current as tx_current_2, txs1.transaction_id as tran_id_1, txs2.transaction_id as tran_id_2, det1.branch_id From osee_relation_link rel1, osee_relation_link rel2, osee_txs txs1, osee_txs txs2, osee_tx_details det1, osee_tx_details det2 WHERE rel1.rel_link_id = rel2.rel_link_id AND rel1.gamma_id < rel2.gamma_id AND rel1.gamma_id = txs1.gamma_id AND rel2.gamma_id = txs2.gamma_id AND txs1.tx_current in (1,2) and txs2.tx_current in (1,2) AND txs1.transaction_id = det1.transaction_id AND txs2.transaction_id = det2.transaction_id AND det1.branch_id = det2.branch_id";

   private static final String DUPLICATE_TX_CURRENT_CLEANUP =
         "UPDATE osee_txs SET tx_current = 0 WHERE gamma_id = ? AND transaction_id = ?";

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthTask#getFixTaskName()
    */
   @Override
   public String getFixTaskName() {
      return "Fix TX_Current Relation Link Errors";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthTask#getVerifyTaskName()
    */
   @Override
   public String getVerifyTaskName() {
      return "Check for TX_Current Relation Link Errors";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthTask#run(org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthTask.Operation, java.lang.StringBuilder)
    */
   @Override
   public void run(BlamVariableMap variableMap, IProgressMonitor monitor, Operation operation, StringBuilder builder, boolean showDetails) throws Exception {
      monitor.beginTask("Verify TX_Current Relation Link Errors", 100);
      StringBuffer sbFull = new StringBuffer(AHTML.beginMultiColumnTable(100, 1));
      ConnectionHandlerStatement chStmt1 = null;
      ConnectionHandlerStatement chStmt2 = null;
      ResultSet resultSet;
      int count = 0;
      if (operation.equals(Operation.Verify)) {
         try {
            String[] columnHeaders = new String[] {"Count", "Rel Link Id", "Branch id"};
            try {
               if (showDetails) {
                  sbFull.append(AHTML.beginMultiColumnTable(100, 1));
                  sbFull.append(AHTML.addHeaderRowMultiColumnTable(columnHeaders));
                  sbFull.append(AHTML.addRowSpanMultiColumnTable("Relation Links with no tx_current set",
                        columnHeaders.length));
               }
               chStmt1 = ConnectionHandler.runPreparedQuery(NO_TX_CURRENT_SET);
               monitor.worked(35);
               if (monitor.isCanceled()) return;
               resultSet = chStmt1.getRset();
               while (resultSet.next()) {
                  count++;
                  if (showDetails) {
                     String str =
                           AHTML.addRowMultiColumnTable(new String[] {String.valueOf(count),
                                 resultSet.getString("rel_link_id"), resultSet.getString("branch_id")});
                     sbFull.append(str);
                  }
               }
            } finally {
               ConnectionHandler.close(chStmt1);
            }
            monitor.worked(15);
            if (monitor.isCanceled()) return;
            builder.append(count > 0 ? "Failed: " : "Passed: ");
            builder.append("Found ");
            builder.append(count);
            builder.append(" Relation Links that have no tx_current value set\n");
            try {
               if (showDetails) {
                  columnHeaders = new String[] {"Count", "Relation Link id", "Branch id", "Num TX_Currents"};
                  sbFull.append(AHTML.addHeaderRowMultiColumnTable(columnHeaders));
                  sbFull.append(AHTML.addRowSpanMultiColumnTable("Relation Links with multiple tx_currents set",
                        columnHeaders.length));
               }
               count = 0;
               monitor.worked(5);
               chStmt1 = ConnectionHandler.runPreparedQuery(MULTIPLE_TX_CURRENT_SET);
               monitor.worked(30);
               if (monitor.isCanceled()) return;
               resultSet = chStmt1.getRset();
               while (resultSet.next()) {
                  count++;
                  if (showDetails) {
                     String str =
                           AHTML.addRowMultiColumnTable(new String[] {String.valueOf(count),
                                 resultSet.getString("rel_link_id"), resultSet.getString("branch_id"),
                                 resultSet.getString("numoccurrences")});
                     sbFull.append(str);
                  }
               }
            } finally {
               ConnectionHandler.close(chStmt1);
            }
            monitor.worked(15);
            builder.append(count > 0 ? "Failed: " : "Passed: ");
            builder.append("Found ");
            builder.append(count);
            builder.append(" Relation Links that have multiple tx_current values set\n");
         } finally {
            if (showDetails) {
               sbFull.append(AHTML.endMultiColumnTable());
               XResultData rd = new XResultData();
               rd.addRaw(sbFull.toString());
               rd.report("Relation TX_Current Check", Manipulations.RAW_HTML);
            }

         }

      } else {
         /** Duplicate TX_current Cleanup **/
         String[] columnHeaders =
               new String[] {"Rel Link ID", "Gamma Id 1", "Gamma Id 2", "TX_Current 1", "TX_Current 2", "Trans Id 1",
                     "Trans Id 2", "Trans ID Fixed", "Branch id"};
         if (showDetails) {
            sbFull.append(AHTML.beginMultiColumnTable(100, 1));
            sbFull.append(AHTML.addHeaderRowMultiColumnTable(columnHeaders));
            sbFull.append(AHTML.addRowSpanMultiColumnTable("Fixed Relation Links with multiple tx_currents set",
                  columnHeaders.length));
         }
         monitor.worked(1);
         monitor.subTask("Querying for multiple Tx_currents");
         try {
            try {
               chStmt1 = ConnectionHandler.runPreparedQuery(DUPLICATE_RELATIONS_TX_CURRENT);
               resultSet = chStmt1.getRset();
               monitor.worked(9);
               monitor.subTask("Processing Results");
               if (monitor.isCanceled()) return;

               int total = 0;
               while (resultSet.next()) {
                  total++;
                  int transaction_id =
                        resultSet.getInt("tran_id_1") < resultSet.getInt("tran_id_2") ? resultSet.getInt("tran_id_1") : resultSet.getInt("tran_id_2");
                  int gamma_id =
                        resultSet.getInt("tran_id_1") < resultSet.getInt("tran_id_2") ? resultSet.getInt("gamma_id_1") : resultSet.getInt("gamma_id_2");
                  ConnectionHandler.runPreparedUpdateReturnCount(DUPLICATE_TX_CURRENT_CLEANUP, gamma_id, transaction_id);
                  if (showDetails) {
                     showTxCurrentText(resultSet, total, sbFull, transaction_id);
                  }
                  if (monitor.isCanceled()) {
                     builder.append("Cleaned up " + total + " Tx_Current duplication errors\n");
                     return;
                  }
               }
               builder.append("Cleaned up " + total + " Tx_Current duplication errors\n");

            } finally {
               ConnectionHandler.close(chStmt1);
            }
            try {
               chStmt1 = ConnectionHandler.runPreparedQuery(NO_TX_CURRENT_SET);
               monitor.worked(35);
               if (monitor.isCanceled()) return;
               resultSet = chStmt1.getRset();
               if (showDetails) {
                  columnHeaders = new String[] {"Rel Link ID", "Gamma Id", "Transaction Id", "Branch id"};
                  sbFull.append(AHTML.beginMultiColumnTable(100, 1));
                  sbFull.append(AHTML.addHeaderRowMultiColumnTable(columnHeaders));
                  sbFull.append(AHTML.addRowSpanMultiColumnTable("Fixed Relation Links with no tx_currents set",
                        columnHeaders.length));
               }
               while (resultSet.next()) {
                  count++;
                  ConnectionHandler.runPreparedUpdate(NO_TX_CURRENT_CLEANUP, resultSet.getInt("rel_link_id"),
                        resultSet.getInt("branch_id"), resultSet.getInt("rel_link_id"));
                  if (showDetails) {
                     chStmt2 =
                           ConnectionHandler.runPreparedQuery(QUERY_TX_CURRENT_SET, resultSet.getInt("branch_id"),
                                 resultSet.getInt("rel_link_id"));
                     ResultSet resultSet2 = chStmt2.getRset();
                     String trans_id = "Not Found", gamma_id = "Not Found";
                     if (resultSet2.next()) {
                        trans_id = resultSet2.getString("transaction_id");
                        gamma_id = resultSet2.getString("gamma_id");
                     }
                     sbFull.append(AHTML.addRowMultiColumnTable(new String[] {resultSet.getString("rel_link_id"),
                           gamma_id, trans_id, resultSet.getString("branch_id")}));
                     ConnectionHandler.close(chStmt2);
                  }
                  if (monitor.isCanceled()) {
                     builder.append("Canceled: Cleaned up " + count + " no Tx_Current set errors\n");
                     return;
                  }
               }
               builder.append("Cleaned up " + count + " no Tx_Current set errors\n");
            } finally {
               ConnectionHandler.close(chStmt2);
               ConnectionHandler.close(chStmt1);
            }
         } finally {
            if (showDetails) {
               sbFull.append(AHTML.endMultiColumnTable());
               XResultData rd = new XResultData();
               rd.addRaw(sbFull.toString());
               rd.report("Relation Link TX_Current Fix", Manipulations.RAW_HTML);
            }
         }
      }
   }

   protected void showTxCurrentText(ResultSet resultSet, int x, StringBuffer builder, int transaction_id) throws SQLException {
      builder.append(AHTML.addRowMultiColumnTable(new String[] {resultSet.getString("rel_link_id"),
            resultSet.getString("gamma_id_1"), resultSet.getString("gamma_id_2"), resultSet.getString("tx_current_1"),
            resultSet.getString("tx_current_2"), resultSet.getString("tran_id_1"), resultSet.getString("tran_id_2"),
            String.valueOf(transaction_id), resultSet.getString("branch_id")}));
   }
}
