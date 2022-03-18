/*********************************************************************
 * Copyright (c) 2012, 2022 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *     Boeing - add SynchronizationEndpoint
 **********************************************************************/

package org.eclipse.osee.framework.core.client;

import org.eclipse.osee.activity.api.ActivityLogEndpoint;
import org.eclipse.osee.define.api.DataRightsEndpoint;
import org.eclipse.osee.define.api.DefineBranchEndpointApi;
import org.eclipse.osee.define.api.GitEndpoint;
import org.eclipse.osee.define.api.ImportEndpoint;
import org.eclipse.osee.define.api.RenderEndpoint;
import org.eclipse.osee.framework.core.OseeApi;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.AttributeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.server.ide.api.SessionEndpoint;
import org.eclipse.osee.framework.server.ide.api.client.ClientEndpoint;
import org.eclipse.osee.orcs.rest.model.ApplicabilityEndpoint;
import org.eclipse.osee.orcs.rest.model.ApplicabilityUiEndpoint;
import org.eclipse.osee.orcs.rest.model.ArtifactEndpoint;
import org.eclipse.osee.orcs.rest.model.BranchEndpoint;
import org.eclipse.osee.orcs.rest.model.DatastoreEndpoint;
import org.eclipse.osee.orcs.rest.model.IndexerEndpoint;
import org.eclipse.osee.orcs.rest.model.OrcsWriterEndpoint;
import org.eclipse.osee.orcs.rest.model.RelationEndpoint;
import org.eclipse.osee.orcs.rest.model.ResourcesEndpoint;
import org.eclipse.osee.orcs.rest.model.TransactionEndpoint;
import org.eclipse.osee.orcs.rest.model.TypesEndpoint;
import org.eclipse.osee.synchronization.api.SynchronizationEndpoint;

/**
 * @author John Misinco
 */
public interface OseeClient extends OseeApi {

   String OSEE_APPLICATION_SERVER = org.eclipse.osee.framework.core.data.OseeClient.OSEE_APPLICATION_SERVER;

   QueryBuilder createQueryBuilder(BranchId branch);

   BranchEndpoint getBranchEndpoint();

   RelationEndpoint getRelationEndpoint(BranchId branch);

   TransactionEndpoint getTransactionEndpoint();

   TypesEndpoint getTypesEndpoint();

   IndexerEndpoint getIndexerEndpoint();

   ClientEndpoint getClientEndpoint();

   ResourcesEndpoint getResourcesEndpoint();

   DatastoreEndpoint getDatastoreEndpoint();

   RenderEndpoint getRenderEndpoint();

   DataRightsEndpoint getDataRightsEndpoint();

   OrcsWriterEndpoint getOrcsWriterEndpoint();

   ApplicabilityEndpoint getApplicabilityEndpoint(BranchId branch);

   ActivityLogEndpoint getActivityLogEndpoint();

   ArtifactEndpoint getArtifactEndpoint(BranchId branch);

   ApplicabilityUiEndpoint getApplicabilityUiEndpoint();

   DefineBranchEndpointApi getDefineBranchEndpoint();

   SessionEndpoint getSessionEndpoint();

   ImportEndpoint getImportEndpoint();

   GitEndpoint getGitEndpoint();

   /**
    * Obtains an object that implements the {@link SynchronizationEndpoint} to process REST API requests for
    * synchronization artifacts.
    *
    * @return an object implementing the interface {@link SynchronizationEndpoint}.
    */

   SynchronizationEndpoint getSynchronizationEndpoint();

   @Deprecated
   String loadAttributeValue(AttributeId attrId, TransactionId transactionId, ArtifactToken artifact);
}
