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
package org.eclipse.osee.disposition.rest.resources;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.eclipse.osee.disposition.rest.DispoApi;
import org.eclipse.osee.disposition.rest.messages.DispoAnnotationMessageReader;
import org.eclipse.osee.disposition.rest.messages.DispoAnnotationMessageWriter;
import org.eclipse.osee.disposition.rest.messages.DispoItemMessageReader;
import org.eclipse.osee.disposition.rest.messages.DispoItemMessageWriter;
import org.eclipse.osee.disposition.rest.messages.DispoSetMessageReader;
import org.eclipse.osee.disposition.rest.messages.DispoSetMessageWriter;
import org.eclipse.osee.disposition.rest.util.DispoHtmlWriter;
import org.eclipse.osee.disposition.rest.util.TemplateRegistry;

/**
 * @author Angel Avila
 */
@ApplicationPath("dispor")
public final class DispoApplication extends Application {

   private DispoApi dispoApi;

   private final Set<Object> singletons = new HashSet<Object>();

   public void setDispoApi(DispoApi dispoApi) {
      this.dispoApi = dispoApi;
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }

   public void start() {
      singletons.add(new DispoSetMessageReader());
      singletons.add(new DispoSetMessageWriter());
      singletons.add(new DispoItemMessageReader());
      singletons.add(new DispoItemMessageWriter());
      singletons.add(new DispoAnnotationMessageReader());
      singletons.add(new DispoAnnotationMessageWriter());

      DispoHtmlWriter writer = new DispoHtmlWriter(TemplateRegistry.newRegistry());
      singletons.add(new DispoProgramResource(dispoApi, dispoApi.getDispoFactory()));
      singletons.add(new DispoInitResource(writer));
      singletons.add(new DispoAdminResource(writer));
   }

   public void stop() {
      singletons.clear();
   }
}