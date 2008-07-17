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
package org.eclipse.osee.framework.search.engine;

/**
 * @author Roberto E. Escobar
 */
public interface ITaggerStatistics {

   public long getAverageWaitTime();

   public long getAverageProcessingTime();

   public int getTotalProcessed();

   public long getLongestProcessingTime();

   public long getLongestWaitTime();

   public long getTotalTags();

   public ITaskStatistics getLongestTask();

   public ITaskStatistics getMostTagsTask();

   public long getTagsInSystem();

}
