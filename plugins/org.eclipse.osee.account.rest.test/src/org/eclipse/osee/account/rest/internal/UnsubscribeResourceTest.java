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
package org.eclipse.osee.account.rest.internal;

import static org.eclipse.osee.account.rest.internal.UnsubscribeResource.ACCOUNT_DISPLAY_NAME_TAG;
import static org.eclipse.osee.account.rest.internal.UnsubscribeResource.SUBSCRIPTION_NAME_TAG;
import static org.eclipse.osee.account.rest.internal.UnsubscribeResource.UNSUBSCRIBE_NO_SUBSCRIPTION_TEMPLATE;
import static org.eclipse.osee.account.rest.internal.UnsubscribeResource.UNSUBSCRIBE_TEMPLATE;
import static org.eclipse.osee.account.rest.internal.UnsubscribeResource.UNSUBSCRIBE_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.osee.account.admin.Subscription;
import org.eclipse.osee.account.admin.SubscriptionAdmin;
import org.eclipse.osee.framework.jdk.core.type.ViewModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Test Case for {@link UnsubscribeResource}
 * 
 * @author Roberto E. Escobar
 */
public class UnsubscribeResourceTest {

   private static final String SUBSCRIPTION_UUID = "D_UhOLi6D7q_MbOiUYny75bUWxYdlHI9yCLyosilpDMYxRhasnqYvwCOlNEPgvrk";

   private static final String GROUP_UUID = "sadjha322";
   private static final long GROUP_ID = 37219891L;
   private static final String GROUP_NAME = "group-1";
   private static final long ACCOUNT_ID = 3129303L;
   private static final String ACCOUNT_NAME = "account-1";

   //@formatter:off
   @Mock private SubscriptionAdmin manager;
   @Mock private Subscription subscription;
   //@formatter:on

   private UnsubscribeResource resource;

   @Before
   public void setUp() {
      initMocks(this);

      resource = new UnsubscribeResource(manager);

      when(subscription.getGuid()).thenReturn(GROUP_UUID);
      when(subscription.getGroupId()).thenReturn(GROUP_ID);
      when(subscription.getName()).thenReturn(GROUP_NAME);
      when(subscription.getAccountId()).thenReturn(ACCOUNT_ID);
      when(subscription.getAccountName()).thenReturn(ACCOUNT_NAME);
   }

   @Test
   public void testGetUnsubscribePageActiveSubscription() {
      when(manager.getSubscription(SUBSCRIPTION_UUID)).thenReturn(subscription);
      when(subscription.isActive()).thenReturn(true);
      when(subscription.getGuid()).thenReturn(SUBSCRIPTION_UUID);

      URI expected = UriBuilder.fromPath(SUBSCRIPTION_UUID).path("confirm").build();

      ViewModel actual = resource.getUnsubscribePage(SUBSCRIPTION_UUID);

      verify(manager).getSubscription(SUBSCRIPTION_UUID);
      verify(subscription).isActive();
      verify(subscription).getGuid();

      assertEquals(UNSUBSCRIBE_TEMPLATE, actual.getViewId());
      assertEquals(GROUP_NAME, actual.asMap().get(SUBSCRIPTION_NAME_TAG));
      assertEquals(ACCOUNT_NAME, actual.asMap().get(ACCOUNT_DISPLAY_NAME_TAG));
      assertEquals(expected, actual.asMap().get(UNSUBSCRIBE_URL));
   }

   @Test
   public void testGetUnsubscribePageInActiveSubscription() {
      when(manager.getSubscription(SUBSCRIPTION_UUID)).thenReturn(subscription);
      when(subscription.isActive()).thenReturn(false);

      ViewModel actual = resource.getUnsubscribePage(SUBSCRIPTION_UUID);

      verify(manager).getSubscription(SUBSCRIPTION_UUID);
      verify(subscription).isActive();
      verify(subscription, times(0)).getGuid();

      assertEquals(UNSUBSCRIBE_NO_SUBSCRIPTION_TEMPLATE, actual.getViewId());
      assertEquals(GROUP_NAME, actual.asMap().get(SUBSCRIPTION_NAME_TAG));
      assertEquals(ACCOUNT_NAME, actual.asMap().get(ACCOUNT_DISPLAY_NAME_TAG));
   }

   @Test
   public void testProcessUnsubscribePageSubscriptionRemoved() {
      when(manager.getSubscription(SUBSCRIPTION_UUID)).thenReturn(subscription);
      when(manager.setSubscriptionActive(subscription, false)).thenReturn(true);

      ViewModel actual = resource.processUnsubscribePage(SUBSCRIPTION_UUID);

      verify(manager).getSubscription(SUBSCRIPTION_UUID);
      verify(manager).setSubscriptionActive(subscription, false);

      assertEquals(UnsubscribeResource.UNSUBSCRIBE_SUCCESS_TEMPLATE, actual.getViewId());
      assertEquals(GROUP_NAME, actual.asMap().get(SUBSCRIPTION_NAME_TAG));
      assertEquals(ACCOUNT_NAME, actual.asMap().get(ACCOUNT_DISPLAY_NAME_TAG));
   }

   @Test
   public void testProcessUnsubscribePageSubscriptionNoChange() {
      when(manager.getSubscription(SUBSCRIPTION_UUID)).thenReturn(subscription);
      when(manager.setSubscriptionActive(subscription, false)).thenReturn(false);

      ViewModel actual = resource.processUnsubscribePage(SUBSCRIPTION_UUID);

      verify(manager).getSubscription(SUBSCRIPTION_UUID);
      verify(manager).setSubscriptionActive(subscription, false);

      assertEquals(UNSUBSCRIBE_NO_SUBSCRIPTION_TEMPLATE, actual.getViewId());
      assertEquals(GROUP_NAME, actual.asMap().get(SUBSCRIPTION_NAME_TAG));
      assertEquals(ACCOUNT_NAME, actual.asMap().get(ACCOUNT_DISPLAY_NAME_TAG));
   }

}
