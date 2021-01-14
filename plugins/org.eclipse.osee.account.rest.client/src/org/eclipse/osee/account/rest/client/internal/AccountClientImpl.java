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

package org.eclipse.osee.account.rest.client.internal;

import static org.eclipse.osee.account.rest.model.AccountContexts.ACCOUNTS;
import static org.eclipse.osee.account.rest.model.AccountContexts.ACCOUNT_ACTIVE;
import static org.eclipse.osee.account.rest.model.AccountContexts.ACCOUNT_ID_TEMPLATE;
import static org.eclipse.osee.account.rest.model.AccountContexts.ACCOUNT_LOGIN;
import static org.eclipse.osee.account.rest.model.AccountContexts.ACCOUNT_LOGOUT;
import static org.eclipse.osee.account.rest.model.AccountContexts.ACCOUNT_PREFERENCES;
import static org.eclipse.osee.account.rest.model.AccountContexts.ACCOUNT_SESSSIONS;
import static org.eclipse.osee.account.rest.model.AccountContexts.ACCOUNT_USERNAME_TEMPLATE;
import static org.eclipse.osee.framework.core.data.OseeClient.OSEE_APPLICATION_SERVER;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.osee.account.rest.client.AccountClient;
import org.eclipse.osee.account.rest.model.AccountActiveData;
import org.eclipse.osee.account.rest.model.AccountDetailsData;
import org.eclipse.osee.account.rest.model.AccountInfoData;
import org.eclipse.osee.account.rest.model.AccountInput;
import org.eclipse.osee.account.rest.model.AccountLoginData;
import org.eclipse.osee.account.rest.model.AccountPreferencesData;
import org.eclipse.osee.account.rest.model.AccountPreferencesInput;
import org.eclipse.osee.account.rest.model.AccountSessionData;
import org.eclipse.osee.account.rest.model.AccountSessionDetailsData;
import org.eclipse.osee.account.rest.model.AccountWebPreferences;
import org.eclipse.osee.account.rest.model.SubscriptionData;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.ResultSets;
import org.eclipse.osee.jaxrs.client.JaxRsClient;
import org.eclipse.osee.jaxrs.client.JaxRsWebTarget;

/**
 * @author Roberto E. Escobar
 */
public class AccountClientImpl implements AccountClient {
   private volatile JaxRsClient client;
   private volatile URI baseUri;

   public void start(Map<String, Object> properties) {
      update(properties);
   }

   public void stop() {
      client = null;
      baseUri = null;
   }

   public void update(Map<String, Object> properties) {
      client = JaxRsClient.newBuilder().properties(properties).build();
      String address = properties != null ? (String) properties.get(OSEE_APPLICATION_SERVER) : null;
      if (address == null) {
         address = System.getProperty(OSEE_APPLICATION_SERVER, "");
      }
      baseUri = UriBuilder.fromUri(address).build();
   }

   private JaxRsWebTarget newTarget(URI uri) {
      return client.target(uri);
   }

   @Override
   public AccountSessionData login(String scheme, String username, String password) {
      AccountLoginData data = new AccountLoginData();
      data.setUsername(username);
      data.setPassword(password);
      data.setScheme(scheme);

      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_LOGIN).build();
      return newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(data), AccountSessionData.class);
   }

   @Override
   public boolean logout(AccountSessionData session) {
      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_LOGOUT).build();
      Response response = newTarget(uri).request().post(Entity.json(session));
      return Status.OK.getStatusCode() == response.getStatus();
   }

   @Override
   public AccountInfoData createAccount(String userName, AccountInput input) {
      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_USERNAME_TEMPLATE).build(userName);
      return newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(input), AccountInfoData.class);
   }

   @Override
   public boolean deleteAccount(Long accountId) {
      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_ID_TEMPLATE).build(accountId);
      Response response = newTarget(uri).request().delete();
      return Status.OK.getStatusCode() == response.getStatus();
   }

   @Override
   public ResultSet<AccountSessionDetailsData> getAccountSessionDataByUniqueField(String accountId) {
      URI uri =
         UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_ID_TEMPLATE).path(ACCOUNT_SESSSIONS).build(accountId);
      AccountSessionDetailsData[] data =
         newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).get(AccountSessionDetailsData[].class);
      return ResultSets.newResultSet(data);
   }

   @Override
   public ResultSet<AccountInfoData> getAllAccounts() {
      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).build();
      AccountInfoData[] accounts = newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).get(AccountInfoData[].class);
      return ResultSets.newResultSet(accounts);
   }

   @Override
   public AccountDetailsData getAccountDetailsById(Long accountId) {
      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_ID_TEMPLATE).build(accountId);
      return newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).get(AccountDetailsData.class);
   }

   @Override
   public AccountPreferencesData getAccountPreferencesById(Long accountId) {
      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_ID_TEMPLATE).path(ACCOUNT_PREFERENCES).build(
         accountId);
      return newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).get(AccountPreferencesData.class);
   }

   @Override
   public AccountWebPreferences getAccountWebPreferencesByUniqueField(ArtifactId accountId) {
      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_PREFERENCES).path(ACCOUNT_ID_TEMPLATE).build(
         accountId.getId());
      return newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).get(AccountWebPreferences.class);
   }

   @Override
   public boolean setAccountActive(Long accountId, boolean active) {
      URI uri =
         UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_ID_TEMPLATE).path(ACCOUNT_ACTIVE).build(accountId);

      boolean result;
      if (active) {
         result = setAccountActive(uri);
      } else {
         result = setAccountInActive(uri);
      }
      return result;
   }

   @Override
   public boolean isAccountActive(Long accountId) {
      URI uri =
         UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_ID_TEMPLATE).path(ACCOUNT_ACTIVE).build(accountId);

      AccountActiveData data = newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).get(AccountActiveData.class);
      return data.isActive();
   }

   private boolean setAccountActive(URI uri) {
      Response response = newTarget(uri).request().put(null);
      return Status.OK.getStatusCode() == response.getStatus();
   }

   private boolean setAccountInActive(URI uri) {
      Response response = newTarget(uri).request().delete();
      return Status.OK.getStatusCode() == response.getStatus();
   }

   @Override
   public boolean setAccountPreferences(Long accountId, Map<String, String> preferences) {
      URI uri = UriBuilder.fromUri(baseUri).path(ACCOUNTS).path(ACCOUNT_ID_TEMPLATE).path(ACCOUNT_PREFERENCES).build(
         accountId);

      AccountPreferencesInput input = new AccountPreferencesInput();
      input.setMap(preferences);
      Response response = newTarget(uri).request().put(Entity.json(input));
      return Status.OK.getStatusCode() == response.getStatus();
   }

   private ResultSet<SubscriptionData> getSubscriptionsForAccount(Long accountId) {
      URI uri =
         UriBuilder.fromUri(baseUri).path("subscriptions").path("for-account").path("{account-id}").build(accountId);
      SubscriptionData[] data = newTarget(uri).request(MediaType.APPLICATION_JSON_TYPE).get(SubscriptionData[].class);
      return ResultSets.newResultSet(data);
   }

   @Override
   public ResultSet<UnsubscribeInfo> getUnsubscribeUris(Long accountId, Collection<String> groupNames) {
      ResultSet<UnsubscribeInfo> toReturn = ResultSets.emptyResultSet();
      ResultSet<SubscriptionData> results = getSubscriptionsForAccount(accountId);
      if (!results.isEmpty()) {
         List<UnsubscribeInfo> infos = new ArrayList<>();

         UriBuilder builder = UriBuilder.fromUri(baseUri).path("unsubscribe").path("ui").path("{subscription-uuid}");
         for (SubscriptionData subscription : results) {
            if (subscription.isActive() && groupNames.contains(subscription.getName())) {
               String name = subscription.getName();
               URI unsubscribeUri = builder.build(subscription.getGuid());
               infos.add(newUnsubscribeInfo(name, unsubscribeUri));
            }
         }
         toReturn = ResultSets.newResultSet(infos);
      }
      return ResultSets.newResultSet(toReturn);
   }

   private UnsubscribeInfo newUnsubscribeInfo(final String subscriptionName, final URI unsubscribeUri) {
      return new UnsubscribeInfo() {

         @Override
         public String getName() {
            return subscriptionName;
         }

         @Override
         public URI getUnsubscribeUri() {
            return unsubscribeUri;
         }
      };
   }

   @Override
   public boolean isLocalHost() {
      return baseUri.toString().contains("localhost");
   }

   @Override
   public String getBaseUri() {
      return baseUri.toString();
   }
}
