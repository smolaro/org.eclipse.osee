/*
 * Created on Jan 11, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.utility;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ExcelSaxHandler;
import org.eclipse.osee.framework.jdk.core.util.io.xml.RowProcessor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCacheQuery;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class ExcelXmlArtifact implements RowProcessor {
   private final boolean firstRowIsHeader;
   private final XMLReader xmlReader;
   private final String staticId;
   private final IOseeBranch branch;
   private final ExcelSaxHandler excelHandler;
   private final List<Worksheet> workbook = new ArrayList<Worksheet>();
   private Worksheet currentWorksheet = null;
   private boolean initialized = false;

   public ExcelXmlArtifact(boolean firstRowIsHeader, String staticId, IOseeBranch branch) throws SAXException, OseeCoreException, IOException {
      this.firstRowIsHeader = firstRowIsHeader;

      excelHandler = new ExcelSaxHandler(this, firstRowIsHeader, true);
      xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setContentHandler(excelHandler);
      this.staticId = staticId;
      this.branch = branch;
   }

   private void startParsing() throws OseeCoreException, IOException, SAXException {
      Artifact artifact =
         ArtifactCacheQuery.getSingletonArtifactByText(CoreArtifactTypes.GeneralDocument, CoreAttributeTypes.StaticId,
            staticId, branch, true);
      if (artifact != null) {
         String xmlData = artifact.getSoleAttributeValueAsString(CoreAttributeTypes.NativeContent, null);
         Reader reader = new StringReader(xmlData);
         xmlReader.parse(new InputSource(reader));
      }
      initialized = true;
   }

   public List<Worksheet> getWorkbook() throws OseeCoreException, IOException, SAXException {
      if (!initialized) {
         startParsing();
      }
      return workbook;
   }

   @Override
   public void processRow(String[] row) throws Exception {
      if (currentWorksheet != null) {
         currentWorksheet.addRow(row);
      }
   }

   @Override
   public void processHeaderRow(String[] row) {
      if (currentWorksheet != null) {
         currentWorksheet.setHeaderRow(row);
      }
   }

   @Override
   public void processEmptyRow() {
      if (currentWorksheet != null) {
         currentWorksheet.addBlankRow();
      }
   }

   @Override
   public void processCommentRow(String[] row) {
      if (currentWorksheet != null) {
         currentWorksheet.addBlankRow();
      }
   }

   @Override
   public void reachedEndOfWorksheet() {
      currentWorksheet = null;
   }

   @Override
   public void foundStartOfWorksheet(String sheetName) throws Exception {
      Worksheet ws = new Worksheet(sheetName);
      workbook.add(ws);
      currentWorksheet = ws;
   }

   @Override
   public void detectedRowAndColumnCounts(int rowCount, int columnCount) {
      if (currentWorksheet != null) {
         currentWorksheet.setRowCount(rowCount);
         currentWorksheet.setColCount(columnCount);
      }
   }

   public class Row {
      private final List<String> row = new ArrayList<String>();

      public Row() {
         //
      }

      public Row(String row[]) {
         for (String cell : row) {
            this.row.add(cell);
         }
      }

      public List<String> getRow() {
         return row;
      }

      public List<String> getColRange(int fromIndex, int toIndex) {
         return row.subList(fromIndex, toIndex);
      }

      public String getCell(int colIndex) {
         if (colIndex < row.size()) {
            return row.get(colIndex);
         } else {
            return null;
         }
      }
   }

   public class Worksheet {
      private final List<Row> rows = new ArrayList<Row>();
      private Row headerRow = null;
      private final String name;
      private int rowCount = 0;
      private int colCount = 0;

      public Worksheet(String name) {
         this.name = name;
      }

      public List<Row> getRows() {
         return rows;
      }

      public void addBlankRow() {
         Row row2 = new Row();
         rows.add(row2);
      }

      public void addRow(String row[]) {
         Row row2 = new Row(row);
         rows.add(row2);
      }

      public void setHeaderRow(String row[]) {
         headerRow = new Row(row);
      }

      public Row getHeaderRow() {
         return headerRow;
      }

      public String getName() {
         return name;
      }

      public void setRowCount(int rowCount) {
         this.rowCount = rowCount;
      }

      public int getRowCount() {
         return rowCount;
      }

      public void setColCount(int colCount) {
         this.colCount = colCount;
      }

      public int getColCount() {
         return colCount;
      }
   }
}
