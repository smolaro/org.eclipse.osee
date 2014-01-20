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
package org.eclipse.osee.ats.rest.internal;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.eclipse.osee.ats.impl.resource.AtsResourceTokens;
import org.eclipse.osee.ats.rest.internal.resources.AtsUiResource;
import org.eclipse.osee.ats.rest.internal.util.JaxRsExceptionMapper;
import org.eclipse.osee.framework.jdk.core.type.IResourceRegistry;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.template.engine.OseeTemplateTokens;

/**
 * @author Donald G. Dunne
 */
public class AtsUiApplication extends Application {

   private final Set<Object> singletons = new HashSet<Object>();

   private OrcsApi orcsApi;

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   public void start() {
      IResourceRegistry registry = orcsApi.getResourceRegistry();

      AtsResourceTokens.register(registry);
      OseeTemplateTokens.register(registry);

      singletons.add(new JaxRsExceptionMapper(registry));
      singletons.add(new AtsUiResource(orcsApi));
      System.out.println("ATS - AtsUiApplication started");
   }

   public void stop() {
      singletons.clear();
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }

}
