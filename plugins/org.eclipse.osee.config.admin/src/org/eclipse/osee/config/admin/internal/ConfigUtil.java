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
package org.eclipse.osee.config.admin.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import org.osgi.service.cm.Configuration;

/**
 * @author Roberto E. Escobar
 */
public final class ConfigUtil {

   private ConfigUtil() {
      // Utility class
   }

   public static String getDefaultConfig() {
      return System.getProperty(ConfigManagerConstants.CONFIGURATION_URI, "");
   }

   public static void writeConfig(Configuration config, StringBuilder buffer) {
      buffer.append("PID = ").append(config.getPid()).append("\n");
      String factoryPid = config.getFactoryPid();
      if (factoryPid != null) {
         buffer.append("FactoryPID = ").append(factoryPid).append("\n");
      }
      String location = config.getBundleLocation();
      location = location != null ? location : " < unbound > ";

      buffer.append("Bundle-Location : ").append(location).append("\n");

      buffer.append("Contents :\n");
      Dictionary<String, Object> dict = config.getProperties();
      if (dict != null) {
         Enumeration<String> keys = dict.keys();
         while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = dict.get(key);
            buffer.append("\t").append(key).append("=").append(value).append("\n");
         }
      }
   }
}
