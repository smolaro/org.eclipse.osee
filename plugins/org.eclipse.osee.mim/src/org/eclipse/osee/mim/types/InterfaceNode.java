/*********************************************************************
 * Copyright (c) 2021 Boeing
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
package org.eclipse.osee.mim.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.eclipse.osee.accessor.types.ArtifactAccessorResult;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactReadable;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.rest.model.transaction.Attribute;
import org.eclipse.osee.orcs.rest.model.transaction.CreateArtifact;

/**
 * @author Luciano T. Vaglienti
 */
public class InterfaceNode extends ArtifactAccessorResult {

   public static final InterfaceNode SENTINEL = new InterfaceNode();

   private String description;
   private String interfaceNodeNumber;
   private String interfaceNodeGroupId;
   private ApplicabilityToken applicability;
   private String interfaceNodeBackgroundColor = generateColor() ? "#81d4fa" : "#c5e1a5";
   private String interfaceNodeAddress;
   private String nameAbbrev;
   private String interfaceNodeCodeGenName;
   private String InterfaceNodeType;
   private String notes;
   private boolean interfaceNodeCodeGen;
   private boolean interfaceNodeBuildCodeGen;
   private boolean interfaceNodeToolUse;

   public InterfaceNode(ArtifactToken art) {
      this((ArtifactReadable) art);
   }

   public InterfaceNode(ArtifactReadable art) {
      super(art);
      this.setDescription(art.getSoleAttributeValue(CoreAttributeTypes.Description, ""));
      this.setInterfaceNodeBackgroundColor(
         art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeBackgroundColor, ""));
      this.setInterfaceNodeAddress(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeAddress, ""));
      this.setInterfaceNodeNumber(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeNumber, ""));
      this.setInterfaceNodeGroupId(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeGroupId, ""));
      this.setApplicability(
         !art.getApplicabilityToken().getId().equals(-1L) ? art.getApplicabilityToken() : ApplicabilityToken.SENTINEL);
      this.setNameAbbrev(art.getSoleAttributeValue(CoreAttributeTypes.NameAbbrev, ""));
      this.setInterfaceNodeCodeGenName(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeCodeGenName, ""));
      this.setInterfaceNodeType(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeType, ""));
      this.setNotes(art.getSoleAttributeValue(CoreAttributeTypes.Notes, ""));
      this.setInterfaceNodeCodeGen(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeCodeGen, false));
      this.setInterfaceNodeBuildCodeGen(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeBuildCodeGen, false));
      this.setInterfaceNodeToolUse(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceNodeToolUse, false));
   }

   public InterfaceNode(Long id, String name) {
      super(id, name);
   }

   public InterfaceNode() {
   }

   /**
    * @return the description
    */
   public String getDescription() {
      return description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description) {
      this.description = description;
   }

   /**
    * @return the applicability
    */
   public ApplicabilityToken getApplicability() {
      return applicability;
   }

   /**
    * @param applicability the applicability to set
    */
   public void setApplicability(ApplicabilityToken applicability) {
      this.applicability = applicability;
   }

   /**
    * @return the color
    */
   public String getInterfaceNodeBackgroundColor() {
      return interfaceNodeBackgroundColor;
   }

   /**
    * @param interfaceNodeBackgroundColor the color to set
    */
   public void setInterfaceNodeBackgroundColor(String interfaceNodeBackgroundColor) {
      this.interfaceNodeBackgroundColor = interfaceNodeBackgroundColor;
   }

   /**
    * @return the address
    */
   public String getInterfaceNodeAddress() {
      return interfaceNodeAddress;
   }

   /**
    * @param interfaceNodeAddress the address to set
    */
   public void setInterfaceNodeAddress(String interfaceNodeAddress) {
      this.interfaceNodeAddress = interfaceNodeAddress;
   }

   public String getInterfaceNodeNumber() {
      return interfaceNodeNumber;
   }

   public void setInterfaceNodeNumber(String interfaceNodeNumber) {
      this.interfaceNodeNumber = interfaceNodeNumber;
   }

   public String getInterfaceNodeGroupId() {
      return interfaceNodeGroupId;
   }

   public void setInterfaceNodeGroupId(String interfaceNodeGroupId) {
      this.interfaceNodeGroupId = interfaceNodeGroupId;
   }

   public String getNameAbbrev() {
      return nameAbbrev;
   }

   public void setNameAbbrev(String nameAbbrev) {
      this.nameAbbrev = nameAbbrev;
   }

   public String getInterfaceNodeCodeGenName() {
      return interfaceNodeCodeGenName;
   }

   public void setInterfaceNodeCodeGenName(String interfaceNodeCodeGenName) {
      this.interfaceNodeCodeGenName = interfaceNodeCodeGenName;
   }

   public String getInterfaceNodeType() {
      return InterfaceNodeType;
   }

   public void setInterfaceNodeType(String interfaceNodeType) {
      InterfaceNodeType = interfaceNodeType;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }

   public boolean isInterfaceNodeCodeGen() {
      return interfaceNodeCodeGen;
   }

   public void setInterfaceNodeCodeGen(boolean interfaceNodeCodeGen) {
      this.interfaceNodeCodeGen = interfaceNodeCodeGen;
   }

   public boolean isInterfaceNodeBuildCodeGen() {
      return interfaceNodeBuildCodeGen;
   }

   public void setInterfaceNodeBuildCodeGen(boolean interfaceNodeBuildCodeGen) {
      this.interfaceNodeBuildCodeGen = interfaceNodeBuildCodeGen;
   }

   public boolean isInterfaceNodeToolUse() {
      return interfaceNodeToolUse;
   }

   public void setInterfaceNodeToolUse(boolean interfaceNodeToolUse) {
      this.interfaceNodeToolUse = interfaceNodeToolUse;
   }

   public String getColor() {
      return interfaceNodeBackgroundColor;
   }

   @JsonIgnore
   private boolean generateColor() {
      return ThreadLocalRandom.current().nextInt(1, 3) > 1 ? true : false;
   }

   public CreateArtifact createArtifact(String key, ApplicabilityId applicId) {
      // @formatter:off
      Map<AttributeTypeToken, String> values = new HashMap<>();
      values.put(CoreAttributeTypes.InterfaceNodeAddress, this.getInterfaceNodeAddress());
      values.put(CoreAttributeTypes.InterfaceNodeNumber, this.getInterfaceNodeNumber());
      values.put(CoreAttributeTypes.InterfaceNodeGroupId, this.getInterfaceNodeGroupId());
      values.put(CoreAttributeTypes.NameAbbrev, this.getNameAbbrev());
      values.put(CoreAttributeTypes.InterfaceNodeCodeGenName, this.getInterfaceNodeCodeGenName());
      values.put(CoreAttributeTypes.InterfaceNodeType, this.getInterfaceNodeType());
      values.put(CoreAttributeTypes.InterfaceNodeBackgroundColor, this.getInterfaceNodeBackgroundColor());
      values.put(CoreAttributeTypes.InterfaceNodeCodeGen, Boolean.toString(this.isInterfaceNodeCodeGen()));
      values.put(CoreAttributeTypes.InterfaceNodeBuildCodeGen, Boolean.toString(this.isInterfaceNodeBuildCodeGen()));
      values.put(CoreAttributeTypes.InterfaceNodeToolUse, Boolean.toString(this.isInterfaceNodeToolUse()));
      values.put(CoreAttributeTypes.Notes, this.getNotes());
      // @formatter:on

      CreateArtifact art = new CreateArtifact();
      art.setName(this.getName());
      art.setTypeId(CoreArtifactTypes.InterfaceNode.getIdString());

      List<Attribute> attrs = new LinkedList<>();

      for (AttributeTypeToken type : CoreArtifactTypes.InterfaceNode.getValidAttributeTypes()) {
         String value = values.get(type);
         if (Strings.isInValid(value)) {
            continue;
         }
         Attribute attr = new Attribute(type.getIdString());
         attr.setValue(Arrays.asList(value));
         attrs.add(attr);
      }

      art.setAttributes(attrs);
      art.setApplicabilityId(applicId.getIdString());
      art.setkey(key);
      return art;
   }

}
