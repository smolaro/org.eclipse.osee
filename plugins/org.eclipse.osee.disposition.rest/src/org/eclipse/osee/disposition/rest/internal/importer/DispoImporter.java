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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.eclipse.osee.disposition.model.Discrepancy;
import org.eclipse.osee.disposition.model.DispoAnnotationData;
import org.eclipse.osee.disposition.model.DispoItem;
import org.eclipse.osee.disposition.model.DispoItemData;
import org.eclipse.osee.disposition.model.DispoStrings;
import org.eclipse.osee.disposition.rest.internal.DispoDataFactory;
import org.eclipse.osee.disposition.rest.internal.LocationRangesCompressor;
import org.eclipse.osee.disposition.rest.util.DispoUtil;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Angel Avila
 */
public class DispoImporter {

   public DispoImporter() {

   }

   public static List<DispoItem> importDirectory(HashMap<String, DispoItem> exisitingItems, String path, final DispoDataFactory dataFactory, ExecutorAdmin executor) throws Exception {
      List<DispoItem> toReturn = new LinkedList<DispoItem>();
      File tmoDirectory = new File(path);
      if (tmoDirectory.isDirectory()) {
         TmoFileFilter filter = new TmoFileFilter();
         File[] files = tmoDirectory.listFiles(filter);
         List<File> listOfFiles = Arrays.asList(files);
         int numThreads = 8;
         int partitionSize = listOfFiles.size() / numThreads;

         int remainder = listOfFiles.size() % numThreads;
         int startIndex = 0;
         int endIndex = 0;
         List<Future<List<DispoItem>>> futures = new LinkedList<Future<List<DispoItem>>>();
         for (int i = 0; i < numThreads; i++) {
            startIndex = endIndex;
            endIndex = startIndex + partitionSize;
            if (i == 0) {
               endIndex += remainder;
            }
            List<File> sublist = listOfFiles.subList(startIndex, endIndex);
            Worker worker = new Worker(sublist, dataFactory, exisitingItems);
            Future<List<DispoItem>> future = executor.schedule(worker);
            futures.add(future);
         }
         for (Future<List<DispoItem>> future : futures) {
            toReturn.addAll(future.get());
         }
      }
      return toReturn;
   }

   private static final class Worker implements Callable<List<DispoItem>> {

      private final List<File> sublist;
      private final DispoDataFactory dataFactory;
      HashMap<String, DispoItem> exisitingItems;

      public Worker(List<File> sublist, DispoDataFactory dataFactory, HashMap<String, DispoItem> exisitingItems) {
         super();
         this.sublist = sublist;
         this.dataFactory = dataFactory;
         this.exisitingItems = exisitingItems;
      }

      @Override
      public List<DispoItem> call() throws Exception {
         List<DispoItem> fromThread = new LinkedList<DispoItem>();
         for (File file : sublist) {
            InputStream inputStream = null;
            try {
               inputStream = new FileInputStream(file);

               String scriptName = file.getName().replaceAll("\\..*", "");

               DispoItemData itemToBuild = new DispoItemData();
               // We already have an item with this name so we now have to check the dates
               if (exisitingItems.containsKey(scriptName)) {
                  DispoItem oldItem = exisitingItems.get(scriptName);
                  Date lastUpdate = oldItem.getLastUpdate();
                  boolean wasSameFile =
                     DiscrepancyParser.buildItemFromFile(itemToBuild, file.getName(), inputStream, false, lastUpdate);
                  if (!wasSameFile) {
                     itemToBuild.setCreationDate(oldItem.getCreationDate());
                     JSONObject newItemDiscrepancies = itemToBuild.getDiscrepanciesList();
                     JSONArray oldAnnotations = oldItem.getAnnotationsList();
                     HashMap<String, Integer> idsToUpdate =
                        matchupOldDiscrepancies(oldItem.getDiscrepanciesList(), newItemDiscrepancies, oldAnnotations);
                     updateTestPointNumbersForAnntations(idsToUpdate, oldAnnotations, newItemDiscrepancies);
                     itemToBuild.setGuid(oldItem.getGuid());
                     itemToBuild.setAnnotationsList(oldAnnotations);
                     fromThread.add(itemToBuild);
                  }
               } else {
                  DiscrepancyParser.buildItemFromFile(itemToBuild, file.getName(), inputStream, true, new Date());
                  dataFactory.initDispoItem(itemToBuild);
                  fromThread.add(itemToBuild);
               }
            } finally {
               Lib.close(inputStream);
            }
         }
         return fromThread;
      }
   };

   private static void updateTestPointNumbersForAnntations(HashMap<String, Integer> idsToUpdate, JSONArray annotations, JSONObject discrepancies) throws JSONException {
      for (int j = 0; j < annotations.length(); j++) {
         JSONObject annotationAsJson = annotations.getJSONObject(j);
         DispoAnnotationData annotation = DispoUtil.jsonObjToDispoAnnotationData(annotationAsJson);
         JSONArray idsOfCoveredDiscrepancies = annotation.getIdsOfCoveredDiscrepancies();

         int length = idsOfCoveredDiscrepancies.length();
         for (int i = 0; i < length; i++) {
            String coveredId = idsOfCoveredDiscrepancies.getString(i);
            if (idsToUpdate.containsKey(coveredId)) {
               String newLocRef = rebuildLocRef(idsOfCoveredDiscrepancies, discrepancies, idsToUpdate);
               annotation.setLocationRefs(newLocRef);
               JSONObject updatedAnnotationAsJson = DispoUtil.annotationToJsonObj(annotation);
               annotations.put(annotation.getIndex(), updatedAnnotationAsJson);
               break; // We can break here because we're gonna reconstruct the whole loc refs for the annotation, no need to keep checking
            }
         }
      }
   }

   private static String rebuildLocRef(JSONArray idsOfCoveredDiscrepancies, JSONObject discrepancies, HashMap<String, Integer> idsToUpdate) throws JSONException {
      int length = idsOfCoveredDiscrepancies.length();
      List<Integer> testPointNumber = new ArrayList<Integer>();
      for (int i = 0; i < length; i++) {
         String id = idsOfCoveredDiscrepancies.getString(i);
         if (discrepancies.has(id)) {
            JSONObject discrepancyAsObject = discrepancies.getJSONObject(id);
            Discrepancy discrepancy = DispoUtil.jsonObjToDiscrepancy(discrepancyAsObject);
            testPointNumber.add(discrepancy.getLocation());
         } else {
            String justTestPoint = id.replaceAll(DispoStrings.DeletedDiscrepancy, "");
            testPointNumber.add(Integer.valueOf(justTestPoint));
         }

      }

      Collections.sort(testPointNumber);

      return LocationRangesCompressor.compress(testPointNumber);
   }

   @SuppressWarnings("unchecked")
   private static HashMap<String, Integer> matchupOldDiscrepancies(JSONObject oldDiscrepancies, JSONObject newDiscrepancies, JSONArray annotations) throws JSONException {
      HashMap<String, Discrepancy> textToNewDiscrepancies = createMap(newDiscrepancies);
      HashMap<String, Integer> idsToUpdate = new HashMap<String, Integer>();

      Iterator<String> iterator = oldDiscrepancies.keys();
      while (iterator.hasNext()) {
         String key = iterator.next();
         JSONObject oldDiscrepancyAsJson = oldDiscrepancies.getJSONObject(key);
         Discrepancy oldDiscrepany = DispoUtil.jsonObjToDiscrepancy(oldDiscrepancyAsJson);
         String normalizedText = oldDiscrepany.getText().replaceFirst(".*?\\.", "");

         Discrepancy matchedNewDiscrepancy = textToNewDiscrepancies.get(normalizedText);
         int oldTestPointNumber = oldDiscrepany.getLocation();
         if (matchedNewDiscrepancy != null) {
            // Transfer the id from the old discrepancy to the new one
            String idToReplace = matchedNewDiscrepancy.getId();
            String idOfOldDiscrep = oldDiscrepany.getId();
            matchedNewDiscrepancy.setId(idOfOldDiscrep);

            int newTestPointNumber = matchedNewDiscrepancy.getLocation();

            if (oldTestPointNumber != newTestPointNumber) {
               idsToUpdate.put(idOfOldDiscrep, newTestPointNumber);
            }

            JSONObject matchedNewDiscrepAsJson = DispoUtil.discrepancyToJsonObj(matchedNewDiscrepancy);
            newDiscrepancies.remove(idToReplace);
            newDiscrepancies.put(idOfOldDiscrep, matchedNewDiscrepAsJson);
         } else {
            int outdateNumber = oldTestPointNumber * -1;
            idsToUpdate.put(DispoStrings.DeletedDiscrepancy + outdateNumber, outdateNumber);
            removeDiscrepancyFromAnnotation(oldDiscrepany, annotations);

         }
      }

      return idsToUpdate;

   }

   private static void removeDiscrepancyFromAnnotation(Discrepancy toRemove, JSONArray annotations) throws JSONException {
      for (int i = 0; i < annotations.length(); i++) {
         JSONObject annotationAsJson = annotations.getJSONObject(i);
         DispoAnnotationData annotation = DispoUtil.jsonObjToDispoAnnotationData(annotationAsJson);
         JSONArray idsOfCoveredDiscrepancies = annotation.getIdsOfCoveredDiscrepancies();
         if (idsOfCoveredDiscrepancies.toString().contains(toRemove.getId())) {
            replaceIdInList(toRemove, idsOfCoveredDiscrepancies);
            annotation.setIsConnected(false);
         }

         JSONObject updatedAnnotationAsJson = DispoUtil.annotationToJsonObj(annotation);
         annotations.put(annotation.getIndex(), updatedAnnotationAsJson);
      }
   }

   private static void replaceIdInList(Discrepancy discrepany, JSONArray idsList) throws JSONException {
      int length = idsList.length();
      String id = discrepany.getId();
      for (int i = 0; i < length; i++) {
         if (id.equals(idsList.getString(i))) {
            int testPoint = discrepany.getLocation() * -1;
            String newMockId = DispoStrings.DeletedDiscrepancy + testPoint;
            idsList.put(i, newMockId);
            break;
         }
      }
   }

   @SuppressWarnings("unchecked")
   private static HashMap<String, Discrepancy> createMap(JSONObject discrepancies) throws JSONException {
      HashMap<String, Discrepancy> textToDiscrepancy = new HashMap<String, Discrepancy>();
      Iterator<String> iterator = discrepancies.keys();
      while (iterator.hasNext()) {
         String key = iterator.next();
         JSONObject discrepancyAsObject = discrepancies.getJSONObject(key);
         Discrepancy discrepancy = DispoUtil.jsonObjToDiscrepancy(discrepancyAsObject);
         String normalizedText = discrepancy.getText().replaceFirst(".*?\\.", ""); // Want to exclude Point number from text we match with
         textToDiscrepancy.put(normalizedText, discrepancy);
      }

      return textToDiscrepancy;
   }

   private static final class TmoFileFilter implements FilenameFilter {

      @Override
      public boolean accept(File dir, String name) {
         return name.endsWith(".tmo");
      }
   }

}
