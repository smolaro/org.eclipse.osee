/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.disposition.rest.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.osee.disposition.model.LocationRange;

/**
 * @author Angel Avila
 */
public class LocationRangesCompressor {

   public static String compress(List<Integer> locationPoints) {
      Collections.sort(locationPoints);
      StringBuilder workingLocRefs = new StringBuilder();

      boolean isRange = false;
      boolean endOfRange = false;
      int startOfRange = -1;
      int previous = -1;

      Iterator<Integer> iterator = locationPoints.iterator();

      while (iterator.hasNext()) {
         int currentTestPoint = iterator.next();
         boolean isLastElement = !iterator.hasNext();

         // Starting a Range
         if (previous == (currentTestPoint - 1) && startOfRange == -1) { // if the previous is 1 less than our current we are in a range
            isRange = true;
            endOfRange = false;
            startOfRange = previous;
            int lastIndexOf = workingLocRefs.lastIndexOf(",");
            if (lastIndexOf > 0) {
               workingLocRefs.replace(lastIndexOf, workingLocRefs.length(), "");
            } else {
               workingLocRefs.setLength(0);
            }
         }
         if (isRange && (previous != (currentTestPoint - 1) || isLastElement)) { // End Range
            endOfRange = true;
         }

         StringBuilder toAppend = new StringBuilder();

         if (!isRange) { // If we are not in a range just add the single point
            toAppend.append(currentTestPoint);
         } else if (endOfRange) { // other wise check to see if we ended the range
            if (isLastElement) {
               toAppend.append(new LocationRange(startOfRange, currentTestPoint).toString()); // append the range ending with the previous point and append this current point
            } else {
               toAppend.append(new LocationRange(startOfRange, previous).toString()); // append the range ending with the previous point and append this current point
               toAppend.append(", ");
               toAppend.append(currentTestPoint);
            }

            isRange = false;
            startOfRange = -1;
         }

         if (toAppend.length() != 0) {
            if (workingLocRefs.length() > 0) {
               workingLocRefs.append(", ");
            }
            workingLocRefs.append(toAppend);
         }

         previous = currentTestPoint;
      }
      return workingLocRefs.toString();
   }

}
