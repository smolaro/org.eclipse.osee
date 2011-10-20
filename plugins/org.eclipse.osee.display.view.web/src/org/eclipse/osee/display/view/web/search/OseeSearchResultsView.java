/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.display.view.web.search;

import org.eclipse.osee.display.view.web.components.OseeSearchHeaderComponent;
import org.eclipse.osee.display.view.web.components.OseeSearchResultsListComponent;
import org.eclipse.osee.vaadin.widgets.Navigator;
import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Shawn F. Cook
 */
@SuppressWarnings("serial")
public class OseeSearchResultsView extends CustomComponent implements Navigator.View {

   protected OseeSearchHeaderComponent searchHeader;
   protected OseeSearchResultsListComponent searchResultsListComponent = new OseeSearchResultsListComponent();
   private final int LEFTMARGIN_WIDTH = 5;

   protected void createLayout() {
      setSizeFull();

      Label spacer = new Label();
      spacer.setHeight(5, UNITS_PIXELS);

      HorizontalLayout leftMarginAndBody = new HorizontalLayout();
      leftMarginAndBody.setSizeFull();
      Label leftMarginSpace = new Label("");
      leftMarginSpace.setWidth(LEFTMARGIN_WIDTH, UNITS_PIXELS);

      searchResultsListComponent.setSizeFull();

      final VerticalLayout vertLayout = new VerticalLayout();
      vertLayout.setSizeFull();

      leftMarginAndBody.addComponent(leftMarginSpace);
      leftMarginAndBody.addComponent(searchResultsListComponent);

      if (searchHeader != null) {
         searchHeader.setShowOseeTitleAbove(false);
         vertLayout.addComponent(searchHeader);
         vertLayout.setComponentAlignment(searchHeader, Alignment.TOP_LEFT);
         searchHeader.setWidth(100, UNITS_PERCENTAGE);
         searchHeader.setHeight(null);
      }

      vertLayout.addComponent(spacer);
      vertLayout.addComponent(leftMarginAndBody);

      leftMarginAndBody.setExpandRatio(searchResultsListComponent, 1.0f);
      vertLayout.setExpandRatio(leftMarginAndBody, 1.0f);

      setCompositionRoot(vertLayout);
   }

   @Override
   public void init(Navigator navigator, Application application) {
      //Do nothing.
   }

   protected OseeSearchHeaderComponent getOseeSearchHeader() {
      return new OseeSearchHeaderComponent();
   }

   @Override
   public void navigateTo(String requestedDataId) {
      if (searchHeader != null) {
         searchHeader.createLayout();
      }
      createLayout();
   }

   @Override
   public String getWarningForNavigatingFrom() {
      return null;
   }

}
