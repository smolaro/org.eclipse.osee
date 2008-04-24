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

package org.eclipse.osee.framework.skynet.core.change;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.attribute.ConfigurationPersistenceManager;
import org.eclipse.osee.framework.skynet.core.attribute.DynamicAttributeDescriptor;
import org.eclipse.osee.framework.skynet.core.revision.ArtifactChange;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionType;
import org.eclipse.swt.graphics.Image;

/**
 * @author Jeff C. Phillips
 */
public class AttributeChanged extends Change {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(AttributeChanged.class);
   private String sourceValue;
   private InputStream sourceContent;
   private int attrId;
   private int attrTypeId;
   private DynamicAttributeDescriptor dynamicAttributeDescriptor;
   private ArtifactChange artifactChange;

   /**
    * @param sourceGamma
    * @param artId
    * @param toTransactionId
    * @param fromTransactionId
    * @param transactionType
    * @param changeType
    * @param sourceValue
    * @param sourceContent
    * @param attrId
    * @param attrTypeId
    */
   public AttributeChanged(int artTypeId, String artName, int sourceGamma, int artId, TransactionId toTransactionId, TransactionId fromTransactionId, TransactionType transactionType, ChangeType changeType, String sourceValue, InputStream sourceContent, int attrId, int attrTypeId) {
      super(artTypeId, artName, sourceGamma, artId, toTransactionId, fromTransactionId, transactionType, changeType);
      this.sourceValue = sourceValue;
      this.sourceContent = sourceContent;
      this.attrId = attrId;
      this.attrTypeId = attrTypeId;
   }

   /**
    * @return the sourceContent
    */
   public InputStream getSourceContent() {
      return sourceContent;
   }

   /**
    * @return the attrId
    */
   public int getAttrId() {
      return attrId;
   }

   /**
    * @return the attrTypeId
    */
   public int getAttrTypeId() {
      return attrTypeId;
   }

   /**
    * @return the dynamicAttributeDescriptor
    */
   public DynamicAttributeDescriptor getDynamicAttributeDescriptor() throws SQLException {
      if (dynamicAttributeDescriptor == null) {
         dynamicAttributeDescriptor = ConfigurationPersistenceManager.getInstance().getDynamicAttributeType(attrTypeId);
      }
      return dynamicAttributeDescriptor;
   }

   public Image getItemTypeImage() {
      return AttributeChangeIcons.getImage(getChangeType(),
            TransactionType.convertTransactionTypeToModificationType(getTransactionType()));
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.change.Change#getName()
    */
   @Override
   public String getName() {
      return getArtifactName();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.change.Change#getTypeName()
    */
   @Override
   public String getItemTypeName() throws SQLException {
      return getDynamicAttributeDescriptor().getName();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.change.Change#getItemKind()
    */
   @Override
   public String getItemKind() {
      return "Attribute";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.change.Change#getValue()
    */
   @Override
   public String getValue() {
      return sourceValue != null ? sourceValue : "Stream data";
   }

   private ArtifactChange getArtifactChange() throws SQLException {
      if (artifactChange == null) {
         artifactChange =
               new ArtifactChange(getChangeType(), getArtId(),
                     TransactionType.convertTransactionTypeToModificationType(getTransactionType()).getValue(),
                     getGamma(), getToTransactionId(), getFromTransactionId(),
                     ConfigurationPersistenceManager.getInstance().getArtifactSubtypeDescriptor(getArtTypeId()));
      }
      return artifactChange;
   }

   /* (non-Javadoc)
    * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   public Object getAdapter(Class adapter) {
      if (adapter == null) throw new IllegalArgumentException("adapter can not be null");

      try {
         // this is a temporary fix until the old change report goes away.
         if (adapter.isInstance(getArtifactChange())) {
            return getArtifactChange();
         }
         if (adapter.isInstance(getArtifact())) {
            return getArtifact();
         }
      } catch (IllegalArgumentException ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      } catch (SQLException ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.change.Change#getObjectImage()
    */
   @Override
   public Image getItemKindImage() throws IllegalArgumentException, SQLException {
      return ConfigurationPersistenceManager.getInstance().getArtifactSubtypeDescriptor(artTypeId).getImage(
            getChangeType(), TransactionType.convertTransactionTypeToModificationType(getTransactionType()));
   }
}
