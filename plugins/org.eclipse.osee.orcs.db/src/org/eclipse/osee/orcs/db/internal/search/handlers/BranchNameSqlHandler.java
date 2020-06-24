/*********************************************************************
 * Copyright (c) 2013 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.db.internal.search.handlers;

import org.eclipse.osee.framework.core.enums.TableEnum;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaBranchName;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;

/**
 * @author Roberto E. Escobar
 */
public class BranchNameSqlHandler extends SqlHandler<CriteriaBranchName> {

   private CriteriaBranchName criteria;

   private String brAlias;

   @Override
   public void setData(CriteriaBranchName criteria) {
      this.criteria = criteria;
   }

   @Override
   public void addTables(AbstractSqlWriter writer) {
      brAlias = writer.getMainTableAlias(TableEnum.BRANCH_TABLE);
   }

   @Override
   public void addPredicates(AbstractSqlWriter writer) {
      String value = criteria.getValue();
      if (criteria.isPattern()) {
         writer.writePatternMatch(brAlias, "branch_name", value);
      } else {
         writer.write(brAlias);
         writer.write(".branch_name = ?");
         writer.addParameter(value);
      }
   }

   @Override
   public int getPriority() {
      return SqlHandlerPriority.BRANCH_NAME.ordinal();
   }
}
