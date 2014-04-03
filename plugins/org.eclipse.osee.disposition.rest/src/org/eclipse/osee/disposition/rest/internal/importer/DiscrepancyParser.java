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
package org.eclipse.osee.disposition.rest.internal.importer;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.eclipse.osee.disposition.model.Discrepancy;
import org.eclipse.osee.disposition.model.DispoItemData;
import org.eclipse.osee.disposition.rest.util.DispoUtil;
import org.eclipse.osee.framework.jdk.core.type.MutableBoolean;
import org.eclipse.osee.framework.jdk.core.type.MutableInteger;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Angel Avila
 */
public class DiscrepancyParser {

   public DiscrepancyParser() {

   }

   public static final class MutableDate {

      private Date value;

      public Date getValue() {
         return value;
      }

      public void setValue(Date value) {
         this.value = value;
      }

   }

   public static final class MutableString {

      private String value;

      public String getValue() {
         return value;
      }

      public void setValue(String value) {
         this.value = value;
      }
   }

   public static boolean buildItemFromFile(DispoItemData dispoItem, String resourceName, InputStream inputStream, final boolean isNewImport, final Date lastUpdate) throws Exception {
      final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy H:mm:ss aa", Locale.US);
      final MutableBoolean isWithinTestPointElement = new MutableBoolean(false);
      final MutableBoolean isCheckGroup = new MutableBoolean(false);
      final MutableInteger idOfTestPoint = new MutableInteger(0);
      final StringBuilder textAppendable = new StringBuilder();
      final MutableBoolean isFailure = new MutableBoolean(false);
      final JSONObject discrepancies = new JSONObject();
      final MutableDate lastModifiedDate = new MutableDate();
      final MutableBoolean stoppedParsing = new MutableBoolean(false);
      final MutableString version = new MutableString();

      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      DispoSaxHandler handler = new DispoSaxHandler();
      xmlReader.setContentHandler(handler);
      xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

      handler.getHandler("ScriptVersion").addListener(new IBaseSaxElementListener() {
         @Override
         public void onEndElement(Object obj) {
            //
         }

         @Override
         public void onStartElement(Object obj) throws Exception {
            ScriptVersionData data = (ScriptVersionData) obj;
            try {
               lastModifiedDate.setValue(DATE_FORMAT.parse(data.getLastModified()));
               if (!isNewImport && !lastModifiedDate.getValue().after(lastUpdate)) {
                  throw new BreakSaxException("Stopped Parsing");
               }

               version.setValue(data.getRevision());
            } catch (ParseException ex) {
               throw ex;
            }
         }
      });

      handler.getHandler("TestPoint").addListener(new IBaseSaxElementListener() {

         @Override
         public void onEndElement(Object obj) throws JSONException {
            if (isWithinTestPointElement.getValue() && isFailure.getValue()) {
               Discrepancy discrepancy = new Discrepancy();
               discrepancy.setText(textAppendable.toString());
               discrepancy.setLocation(idOfTestPoint.getValue());
               String id = GUID.create();
               discrepancy.setId(id);
               JSONObject discrepancyAsJson = DispoUtil.discrepancyToJsonObj(discrepancy);
               discrepancies.put(id, discrepancyAsJson);

               isFailure.setValue(false);
            }
            textAppendable.delete(0, textAppendable.length());
            isWithinTestPointElement.setValue(false);
         }

         @Override
         public void onStartElement(Object obj) {
            isWithinTestPointElement.setValue(true);
         }
      });

      handler.getHandler("Number").addListener(new IBaseSaxElementListener() {
         @Override
         public void onEndElement(Object obj) {
            if (isWithinTestPointElement.getValue()) {
               idOfTestPoint.setValue(Integer.valueOf(obj.toString()));

               textAppendable.append("Failure at Test Point ");
               textAppendable.append(idOfTestPoint);
               textAppendable.append(". ");
            }
         }

         @Override
         public void onStartElement(Object obj) {
            //
         }
      });

      handler.getHandler("TestPointName").addListener(new IBaseSaxElementListener() {
         @Override
         public void onEndElement(Object obj) {
            if (isFailure.getValue()) {
               textAppendable.append("Check Point: ");
               textAppendable.append(obj);
               textAppendable.append(". ");
            }
         }

         @Override
         public void onStartElement(Object obj) {
            //
         }
      });

      handler.getHandler("Result").addListener(new IBaseSaxElementListener() {

         @Override
         public void onEndElement(Object obj) {
            if (isWithinTestPointElement.getValue()) {
               if (!isCheckGroup.getValue() && "PASSED".equals(obj)) {
                  isFailure.setValue(false);
               } else if ("FAILED".equals(obj)) {
                  isFailure.setValue(true);
               }
            }
         }

         @Override
         public void onStartElement(Object obj) {
            //
         }
      });

      handler.getHandler("CheckGroup").addListener(new IBaseSaxElementListener() {
         @Override
         public void onEndElement(Object obj) {
            //
         }

         @Override
         public void onStartElement(Object obj) {
            isCheckGroup.setValue(true);
            if (isFailure.getValue() && isWithinTestPointElement.getValue()) {
               textAppendable.append("Check Group with Checkpoint Failures: ");
            }
         }
      });

      handler.getHandler("Expected").addListener(new IBaseSaxElementListener() {
         @Override
         public void onEndElement(Object obj) {
            if (isWithinTestPointElement.getValue() && isFailure.getValue()) {
               textAppendable.append("Expected: ");
               textAppendable.append(obj);
               textAppendable.append(". ");
            }
         }

         @Override
         public void onStartElement(Object obj) {
            //
         }
      });
      handler.getHandler("Actual").addListener(new IBaseSaxElementListener() {
         @Override
         public void onEndElement(Object obj) {
            if (isWithinTestPointElement.getValue() && isFailure.getValue()) {
               textAppendable.append("Actual: ");
               textAppendable.append(obj);
               textAppendable.append(". ");
            }
         }

         @Override
         public void onStartElement(Object obj) {
            //
         }
      });

      try {
         xmlReader.parse(new InputSource(inputStream));
      } catch (Exception ex) {
         if (ex.getMessage().equals("Stopped Parsing")) {
            stoppedParsing.setValue(true);
         } else {
            throw ex;
         }
      }

      if (!stoppedParsing.getValue()) {
         String normalizedName = resourceName.replaceAll("\\..*", "");
         dispoItem.setName(normalizedName);
         dispoItem.setDiscrepanciesList(discrepancies);
         dispoItem.setVersion(version.getValue());
         if (isNewImport) {
            dispoItem.setCreationDate(lastModifiedDate.getValue());
         }
         dispoItem.setLastUpdate(lastModifiedDate.getValue());
      }
      return stoppedParsing.getValue();
   }

}
