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
package org.eclipse.osee.framework.ui.skynet.panels;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.framework.skynet.core.importing.ArtifactSourceParserContributionManager;
import org.eclipse.osee.framework.skynet.core.importing.parsers.IArtifactSourceParser;
import org.eclipse.osee.framework.skynet.core.importing.parsers.IArtifactSourceParserDelegate;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.HidingComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Roberto E. Escobar
 * @author Ryan C. Schmitt
 */
public class ArtifactSourceParserSelectPanel {

   private Combo parserCombo;
   private Combo parserComboDelegate;
   private final ArtifactSourceParserContributionManager importContributionManager;
   private final Set<Listener> listeners;
   private IArtifactSourceParser selectedParser;

   public ArtifactSourceParserSelectPanel(ArtifactSourceParserContributionManager importContributionManager) {
      this.listeners = new HashSet<Listener>();
      this.importContributionManager = importContributionManager;
   }

   public void addListener(Listener listener) {
      synchronized (listeners) {
         listeners.add(listener);
      }
   }

   public void removeListener(Listener listener) {
      synchronized (listeners) {
         listeners.remove(listener);
      }
   }

   private void fireSelectionEvent(Event event) {
      for (Listener listener : listeners) {
         listener.handleEvent(event);
      }
   }

   public void createControl(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(ALayout.getZeroMarginLayout(2, false));
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      parserCombo = new Combo(composite, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN);
      parserCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      Composite delegateGroup =
            createHidingGroup(parent.getParent(), 1, "Select additional parse option", "Select additional parse option");

      parserComboDelegate = new Combo(delegateGroup, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN);
      parserComboDelegate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      SelectionAdapter listener = new SelectionListener();
      parserCombo.addSelectionListener(listener);
      parserComboDelegate.addSelectionListener(listener);

      populateData();
   }

   private void populateData() {
      for (IArtifactSourceParser sourceParser : importContributionManager.getArtifactSourceParser()) {
         String extractorName = sourceParser.getName();
         parserCombo.add(extractorName);
         parserCombo.setData(extractorName, sourceParser);
      }
      parserCombo.select(parserCombo.getItemCount() - 1);
   }

   private Composite createHidingGroup(Composite parent, int numberOfColumns, String text, String toolTip) {
      Composite composite = new HidingComposite(parent, SWT.NONE);
      composite.setLayout(ALayout.getZeroMarginLayout(1, false));
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      Group delegateGroup = new Group(composite, SWT.NONE);
      delegateGroup.setText(text);
      delegateGroup.setToolTipText(toolTip);
      delegateGroup.setLayout(new GridLayout(numberOfColumns, false));
      delegateGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      return delegateGroup;
   }

   public IArtifactSourceParser getArtifactParser() {
      return selectedParser;
   }

   private void setArtifactParser(IArtifactSourceParser selectedParser) {
      this.selectedParser = selectedParser;
   }

   public IArtifactSourceParserDelegate getArtifactParserDelegate() {
      IArtifactSourceParserDelegate parserDelegate = null;
      if (parserComboDelegate.isVisible()) {
         int index = parserComboDelegate.getSelectionIndex();
         if (index >= 0) {
            String key = parserComboDelegate.getItem(index);
            Object object = parserComboDelegate.getData(key);
            if (object instanceof IArtifactSourceParserDelegate) {
               parserDelegate = (IArtifactSourceParserDelegate) object;
            }
         }
      }
      return parserDelegate;
   }

   private final class SelectionListener extends SelectionAdapter {

      @Override
      public void widgetSelected(SelectionEvent e) {
         IArtifactSourceParser sourceParser = null;
         int index = parserCombo.getSelectionIndex();
         if (index >= 0) {
            String key = parserCombo.getItem(index);
            Object object = parserCombo.getData(key);
            if (object instanceof IArtifactSourceParser) {
               sourceParser = (IArtifactSourceParser) object;
            }
         }

         if (sourceParser != null) {
            // TODO add a floating tip text similar to Java doc/Content Assist
            parserCombo.setToolTipText(sourceParser.getDescription());
            setArtifactParser(sourceParser);
         } else {
            parserCombo.setToolTipText("Select a source parser");
         }

         Collection<IArtifactSourceParserDelegate> delegates =
               importContributionManager.getArtifactSourceParserDelegate(sourceParser);
         if (!delegates.isEmpty()) {
            parserComboDelegate.removeAll();
            for (IArtifactSourceParserDelegate handler : delegates) {
               parserComboDelegate.add(handler.getName());
               parserComboDelegate.setData(handler.getName(), handler);
            }
            parserComboDelegate.select(0);
         }
         parserComboDelegate.getParent().getParent().setVisible(!delegates.isEmpty());
         parserCombo.getParent().getParent().getParent().layout();
         Event event = new Event();
         event.widget = e.widget;
         fireSelectionEvent(event);
      }
   }
}
