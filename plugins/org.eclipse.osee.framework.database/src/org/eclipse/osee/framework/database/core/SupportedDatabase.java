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
package org.eclipse.osee.framework.database.core;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;

public enum SupportedDatabase {
   h2,
   oracle,
   derby,
   foxpro,
   mysql,
   postgresql;

   public static SupportedDatabase getDatabaseType(DatabaseMetaData metaData) throws OseeCoreException {
      SupportedDatabase toReturn = null;
      try {
         String dbName = metaData.getDatabaseProductName();
         String lowerCaseName = dbName.toLowerCase();
         if (lowerCaseName.contains(SupportedDatabase.h2.toString())) {
            toReturn = SupportedDatabase.h2;
         } else if (lowerCaseName.contains(SupportedDatabase.derby.toString())) {
            toReturn = SupportedDatabase.derby;
         } else if (lowerCaseName.contains(SupportedDatabase.oracle.toString())) {
            toReturn = SupportedDatabase.oracle;
         } else if (lowerCaseName.contains(SupportedDatabase.foxpro.toString())) {
            toReturn = SupportedDatabase.foxpro;
         } else if (lowerCaseName.contains(SupportedDatabase.mysql.toString())) {
            toReturn = SupportedDatabase.mysql;
         } else if (lowerCaseName.contains(SupportedDatabase.postgresql.toString())) {
            toReturn = SupportedDatabase.postgresql;
         } else {
            throw new OseeDataStoreException("Unsupported database type [%s] ", dbName);
         }
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return toReturn;
   }

   public static boolean isDatabaseType(DatabaseMetaData metaData, SupportedDatabase dbType) throws OseeCoreException {
      return getDatabaseType(metaData) == dbType;
   }

   public static boolean areHintsSupported(DatabaseMetaData metaData) throws OseeCoreException {
      try {
         if (SupportedDatabase.isDatabaseType(metaData, oracle)) {
            return metaData.getDatabaseMajorVersion() > 10;
         }
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return false;
   }

   public static String getComplementSql(DatabaseMetaData metaData) throws OseeCoreException {
      return isDatabaseType(metaData, oracle) ? "MINUS" : "EXCEPT";
   }
}