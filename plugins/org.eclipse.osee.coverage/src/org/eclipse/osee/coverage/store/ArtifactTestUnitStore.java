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
package org.eclipse.osee.coverage.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author John Misinco
 */
public class ArtifactTestUnitStore implements ITestUnitStore {

   public static final String COVERAGE_GUID = "AhQuIIQa0Vkw4mswxhwA";

   private final IOseeBranch branch;

   public ArtifactTestUnitStore(IOseeBranch branch) {
      this.branch = branch;
   }

   public ArtifactTestUnitStore() {
      this(CoreBranches.COMMON);
   }

   @Override
   public void load(TestUnitCache cache) throws OseeCoreException {
      String data = getAttributeData();
      parse(data, cache);
   }

   private String getAttributeData() throws OseeCoreException {
      Artifact artifact = getCoverageTestUnitArtifact();
      return artifact.getSoleAttributeValueAsString(CoreAttributeTypes.GeneralStringData, "");
   }

   @Override
   public void store(TestUnitCache cache) throws OseeCoreException {
      Artifact artifact = getCoverageTestUnitArtifact();

      Set<Entry<Integer, String>> entries = cache.getAllCachedTestUnitEntries();
      List<Entry<Integer, String>> entriesList = new ArrayList<Entry<Integer, String>>(entries);
      Collections.sort(entriesList, new Comparator<Entry<Integer, String>>() {

         @Override
         public int compare(Entry<Integer, String> o1, Entry<Integer, String> o2) {
            return o1.getKey().compareTo(o2.getKey());
         }
      });

      String storage = asStorage(entriesList);
      artifact.setSoleAttributeFromString(CoreAttributeTypes.GeneralStringData, storage);
      artifact.persist(getClass().getSimpleName());
   }

   protected String asStorage(List<Entry<Integer, String>> entries) {
      StringBuilder sb = new StringBuilder();
      boolean firstTime = true;
      for (Entry<Integer, String> entry : entries) {
         if (!firstTime) {
            sb.append("\n");
         }
         sb.append(entry.getKey());
         sb.append("|");
         sb.append(entry.getValue());
         firstTime = false;
      }
      return sb.toString();
   }

   protected void parse(String data, TestUnitCache cache) throws OseeCoreException {
      StringTokenizer entries = new StringTokenizer(data, "\n");
      while (entries.hasMoreElements()) {
         StringTokenizer idName = new StringTokenizer(entries.nextToken(), "|");
         if (idName.countTokens() == 2) {
            String id = idName.nextToken();
            String testUnitName = idName.nextToken().trim();
            if (Strings.isValid(id, testUnitName)) {
               int key = Integer.parseInt(id);
               cache.put(key, testUnitName);
            } else {
               throw new OseeArgumentException("Invalid Test Unit Name");
            }
         } else {
            throw new OseeArgumentException("Invalid Test Unit Name");
         }
      }
   }

   private Artifact getCoverageTestUnitArtifact() throws OseeCoreException {
      return ArtifactQuery.getOrCreate(COVERAGE_GUID, null, CoreArtifactTypes.GeneralData, branch);
   }
}
