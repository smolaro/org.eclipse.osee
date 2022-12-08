/*********************************************************************
 * Copyright (c) 2022 Boeing
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
package org.eclipse.osee.orcs.health;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Donald G. Dunne
 */
public class HealthLinks {

   List<HealthLink> links = new ArrayList<>();

   public List<HealthLink> getLinks() {
      return links;
   }

   public void setLinks(List<HealthLink> links) {
      this.links = links;
   }

   public void add(HealthLink link) {
      links.add(link);
   }

}
