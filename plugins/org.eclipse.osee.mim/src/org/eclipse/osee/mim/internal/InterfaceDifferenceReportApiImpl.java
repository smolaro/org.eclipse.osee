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
package org.eclipse.osee.mim.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.core.model.change.ChangeType;
import org.eclipse.osee.mim.InterfaceConnectionViewApi;
import org.eclipse.osee.mim.InterfaceDifferenceReportApi;
import org.eclipse.osee.mim.InterfaceElementApi;
import org.eclipse.osee.mim.InterfaceEnumerationApi;
import org.eclipse.osee.mim.InterfaceEnumerationSetApi;
import org.eclipse.osee.mim.InterfaceMessageApi;
import org.eclipse.osee.mim.InterfaceNodeViewApi;
import org.eclipse.osee.mim.InterfacePlatformTypeApi;
import org.eclipse.osee.mim.InterfaceStructureApi;
import org.eclipse.osee.mim.InterfaceSubMessageApi;
import org.eclipse.osee.mim.types.InterfaceConnection;
import org.eclipse.osee.mim.types.InterfaceEnumeration;
import org.eclipse.osee.mim.types.InterfaceEnumerationSet;
import org.eclipse.osee.mim.types.InterfaceMessageToken;
import org.eclipse.osee.mim.types.InterfaceNode;
import org.eclipse.osee.mim.types.InterfaceStructureElementToken;
import org.eclipse.osee.mim.types.InterfaceStructureToken;
import org.eclipse.osee.mim.types.InterfaceSubMessageToken;
import org.eclipse.osee.mim.types.MimDifferenceReport;
import org.eclipse.osee.mim.types.PlatformTypeToken;
import org.eclipse.osee.orcs.OrcsApi;

/**
 * @author Ryan T. Baldwin
 */
public class InterfaceDifferenceReportApiImpl implements InterfaceDifferenceReportApi {

   private BranchId branch1;
   private BranchId branch2;

   private final OrcsApi orcsApi;
   private final InterfaceNodeViewApi interfaceNodeApi;
   private final InterfaceConnectionViewApi interfaceConnectionApi;
   private final InterfaceMessageApi interfaceMessageApi;
   private final InterfaceSubMessageApi interfaceSubMessageApi;
   private final InterfaceStructureApi interfaceStructureApi;
   private final InterfaceElementApi interfaceElementApi;
   private final InterfacePlatformTypeApi interfacePlatformApi;
   private final InterfaceEnumerationSetApi interfaceEnumerationSetApi;
   private final InterfaceEnumerationApi interfaceEnumerationApi;

   private Map<ArtifactId, List<ChangeItem>> changeMap;
   private Map<ArtifactId, List<ChangeItem>> nodeMap;
   private Map<ArtifactId, List<ChangeItem>> connectionMap;
   private Map<ArtifactId, List<ChangeItem>> messageMap;
   private Map<ArtifactId, List<ChangeItem>> submessageMap;
   private Map<ArtifactId, List<ChangeItem>> structureMap;
   private Map<ArtifactId, List<ChangeItem>> elementMap;
   private Map<ArtifactId, List<ChangeItem>> typeMap;
   private Map<ArtifactId, List<ChangeItem>> enumMap;

   private MimDifferenceReport diffReport;

   InterfaceDifferenceReportApiImpl(OrcsApi orcsApi, InterfaceNodeViewApi interfaceNodeApi, InterfaceConnectionViewApi interfaceConnectionViewApi, InterfaceMessageApi interfaceMessageApi, InterfaceSubMessageApi interfaceSubMessageApi, InterfaceStructureApi interfaceStructureApi, InterfaceElementApi interfaceElementApi, InterfacePlatformTypeApi interfacePlatformApi, InterfaceEnumerationSetApi interfaceEnumerationSetApi, InterfaceEnumerationApi interfaceEnumerationApi) {
      this.orcsApi = orcsApi;
      this.interfaceNodeApi = interfaceNodeApi;
      this.interfaceConnectionApi = interfaceConnectionViewApi;
      this.interfaceMessageApi = interfaceMessageApi;
      this.interfaceSubMessageApi = interfaceSubMessageApi;
      this.interfaceStructureApi = interfaceStructureApi;
      this.interfaceElementApi = interfaceElementApi;
      this.interfacePlatformApi = interfacePlatformApi;
      this.interfaceEnumerationSetApi = interfaceEnumerationSetApi;
      this.interfaceEnumerationApi = interfaceEnumerationApi;
   }

   @Override
   public MimDifferenceReport getDifferenceReport(BranchId branch1, BranchId branch2) {
      this.branch1 = branch1;
      this.branch2 = branch2;

      // Initialize maps
      this.changeMap = new HashMap<>();
      this.nodeMap = new HashMap<>();
      this.connectionMap = new HashMap<>();
      this.messageMap = new HashMap<>();
      this.submessageMap = new HashMap<>();
      this.structureMap = new HashMap<>();
      this.elementMap = new HashMap<>();
      this.typeMap = new HashMap<>();
      this.enumMap = new HashMap<>();
      this.diffReport = new MimDifferenceReport();

      TransactionToken sourceTx =
         orcsApi.getQueryFactory().transactionQuery().andIsHead(branch1).getResults().getExactlyOne();
      TransactionToken destinationTx =
         orcsApi.getQueryFactory().transactionQuery().andIsHead(branch2).getResults().getExactlyOne();
      List<ChangeItem> changes = orcsApi.getBranchOps().compareBranch(sourceTx, destinationTx);

      // First, add all of the artifact changes to the correct maps. Every diff should have an artifact change.
      changes.stream().filter(c -> c.getChangeType().getId() == ChangeType.Artifact.getId()).forEach(
         c -> addChangeToMap(c));

      // Add all change items to the changeMap. These are the lists that will be put into the diff report.
      for (ChangeItem change : changes) {
         List<ChangeItem> list = getListOrDefault(changeMap, change.getArtId().getId());
         list.add(change);
         changeMap.put(change.getArtId(), list);
      }

      // NODES
      for (ArtifactId artId : nodeMap.keySet()) {
         processNode(artId);
      }

      // CONNECTIONS
      for (ArtifactId artId : connectionMap.keySet()) {
         processConnection(artId);
      }

      // MESSAGES
      for (ArtifactId artId : messageMap.keySet()) {
         processMessage(artId);
      }

      // SUBMESSAGES
      for (ArtifactId artId : submessageMap.keySet()) {
         processSubMessage(artId);
      }

      // STRUCTURES
      for (ArtifactId artId : structureMap.keySet()) {
         processStructure(artId);
      }

      // ELEMENTS
      for (ArtifactId artId : elementMap.keySet()) {
         processElement(artId, ArtifactId.SENTINEL);
      }

      // PLATFORM TYPES
      for (ArtifactId artId : typeMap.keySet()) {
         List<InterfaceStructureElementToken> elements =
            interfaceElementApi.getElementsByType(getBranchId(artId), artId);
         for (InterfaceStructureElementToken element : elements) {
            processElement(ArtifactId.valueOf(element.getId()), artId, element);
         }
      }

      // ENUMS
      for (ArtifactId artId : enumMap.keySet()) {
         processEnumeration(artId);
      }

      return diffReport;
   }

   private void processNode(ArtifactId nodeId) {
      List<ChangeItem> changeItems = changeMap.get(nodeId);
      InterfaceNode node = interfaceNodeApi.get(getBranchId(nodeId), nodeId);
      if (node.isValid()) {
         diffReport.addItem(node, changeItems);
         diffReport.getNodes().add(nodeId);
      }
   }

   private void processConnection(ArtifactId connectionId) {
      InterfaceConnection connection = interfaceConnectionApi.get(getBranchId(connectionId), connectionId);
      if (connection.isValid()) {
         diffReport.addItem(connection, getListOrDefault(changeMap, connectionId.getId()));
         diffReport.getConnections().add(connectionId);
      }
   }

   private void processMessage(ArtifactId messageId) {
      InterfaceMessageToken message = interfaceMessageApi.getWithAllParentRelations(getBranchId(messageId), messageId);

      if (message.isValid()) {
         diffReport.addItem(message, getListOrDefault(changeMap, messageId.getId()));

         addMessageParent(message);

         diffReport.getMessages().add(messageId);
      }
   }

   private void processSubMessage(ArtifactId submessageId) {
      InterfaceSubMessageToken subMessage =
         interfaceSubMessageApi.getWithAllParentRelations(getBranchId(submessageId), submessageId);

      if (subMessage.isValid()) {
         diffReport.addItem(subMessage, getListOrDefault(changeMap, submessageId.getId()));

         addSubMessageParents(subMessage);

         diffReport.getSubMessages().add(submessageId);
      }
   }

   private void processStructure(ArtifactId structureId) {
      InterfaceStructureToken structure =
         interfaceStructureApi.getWithAllParentRelations(getBranchId(structureId), structureId);

      if (structure.isValid()) {
         diffReport.addItem(structure, getListOrDefault(changeMap, structureId.getId()));

         addStructureParents(structure);

         diffReport.getStructures().add(structureId);
      }
   }

   private void processElement(ArtifactId elementId, ArtifactId typeId) {
      InterfaceStructureElementToken element = interfaceElementApi.get(getBranchId(elementId), elementId);
      processElement(elementId, typeId, element);
   }

   private void processElement(ArtifactId elementId, ArtifactId typeId, InterfaceStructureElementToken element) {
      if (element.isValid()) {
         diffReport.addItem(element, getListOrDefault(changeMap, element.getId()));

         if (typeId.isValid()) {
            diffReport.addItem(element, getListOrDefault(changeMap, typeId.getId()));
         }

         if (!diffReport.hasParents(elementId)) {
            InterfaceStructureElementToken elementWithParentRelations =
               interfaceElementApi.getWithAllParentRelations(branch1, elementId);
            addElementParents(elementWithParentRelations);
         }

         diffReport.getElements().add(elementId);
      }
   }

   private void processEnumeration(ArtifactId enumId) {
      InterfaceEnumeration enumeration = interfaceEnumerationApi.get(getBranchId(enumId), enumId,
         Arrays.asList(CoreRelationTypes.InterfaceEnumeration_EnumerationSet,
            CoreRelationTypes.InterfacePlatformTypeEnumeration_Element,
            CoreRelationTypes.InterfaceElementPlatformType_Element));

      List<InterfaceEnumerationSet> enumSets = enumeration.getArtifactReadable().getRelatedList(
         CoreRelationTypes.InterfaceEnumeration_EnumerationSet).stream().filter(
            a -> !a.getExistingAttributeTypes().isEmpty()).map(a -> new InterfaceEnumerationSet(a)).collect(
               Collectors.toList());

      for (InterfaceEnumerationSet enumSet : enumSets) {
         // Get enum set with populated enum list
         ArtifactId enumSetId = ArtifactId.valueOf(enumSet.getId());
         InterfaceEnumerationSet populatedSet = interfaceEnumerationSetApi.get(getBranchId(enumId), enumSetId);
         diffReport.addItem(populatedSet, getListOrDefault(changeMap, enumId.getId()));
         diffReport.getEnumSets().add(enumSetId);
         addEnumSetParents(enumSet); // Use the non-populated enum set that has the parent relations here
      }
   }

   private void addMessageParent(InterfaceMessageToken message) {
      ArtifactId messageId = ArtifactId.valueOf(message.getId());
      if (!diffReport.hasParents(messageId)) {
         InterfaceConnection connection = interfaceConnectionApi.getRelatedFromMessage(message);
         ArtifactId connectionId = ArtifactId.valueOf(connection.getId());
         diffReport.addParent(messageId, connectionId);
         diffReport.addItem(connection);
      }
   }

   private void addSubMessageParents(InterfaceSubMessageToken subMessage) {
      ArtifactId subMessageId = ArtifactId.valueOf(subMessage.getId());
      if (!diffReport.hasParents(subMessageId)) {
         List<InterfaceMessageToken> messages = interfaceMessageApi.getAllRelatedFromSubMessage(subMessage);
         for (InterfaceMessageToken message : messages) {
            ArtifactId messageId = ArtifactId.valueOf(message.getId());
            diffReport.addParent(subMessageId, messageId);
            diffReport.addItem(message);
            addMessageParent(message);
         }
      }
   }

   private void addStructureParents(InterfaceStructureToken structure) {
      ArtifactId structureId = ArtifactId.valueOf(structure.getId());
      if (!diffReport.hasParents(structureId)) {
         List<InterfaceSubMessageToken> subMessages = interfaceSubMessageApi.getAllRelatedFromStructure(structure);
         for (InterfaceSubMessageToken subMessage : subMessages) {
            ArtifactId subMessageId = ArtifactId.valueOf(subMessage.getId());
            diffReport.addParent(structureId, subMessageId);
            diffReport.addItem(subMessage);
            addSubMessageParents(subMessage);
         }
      }
   }

   private void addElementParents(InterfaceStructureElementToken element) {
      ArtifactId elementId = ArtifactId.valueOf(element.getId());
      if (!diffReport.hasParents(elementId)) {
         List<InterfaceStructureToken> structures = interfaceStructureApi.getAllRelatedFromElement(element);
         for (InterfaceStructureToken structure : structures) {
            ArtifactId structureId = ArtifactId.valueOf(structure.getId());
            diffReport.addParent(elementId, structureId);
            diffReport.addItem(structure);
            addStructureParents(structure);
         }
      }
   }

   private void addEnumSetParents(InterfaceEnumerationSet enumSet) {
      ArtifactId enumSetId = ArtifactId.valueOf(enumSet.getId());
      if (!diffReport.hasParents(enumSetId)) {
         for (PlatformTypeToken pType : interfacePlatformApi.getAllFromEnumerationSet(enumSet)) {
            ArtifactId pTypeId = ArtifactId.valueOf(pType.getId());
            diffReport.addParent(enumSetId, pTypeId);
            for (InterfaceStructureElementToken element : interfaceElementApi.getAllFromPlatformType(pType)) {
               processElement(ArtifactId.valueOf(element.getId()), ArtifactId.SENTINEL);
            }
         }
      }
   }

   private BranchId getBranchId(ArtifactId artId) {
      List<ChangeItem> changes = changeMap.get(artId);
      if (changes == null || changes.isEmpty()) {
         return branch1;
      }
      ChangeItem change =
         changes.stream().filter(c -> c.getChangeType().getId() == ChangeType.Artifact.getId()).findFirst().orElse(
            null);
      return change != null && change.isDeleted() ? branch2 : branch1;
   }

   private List<ChangeItem> getListOrDefault(Map<ArtifactId, List<ChangeItem>> changeMap, Long artId) {
      return changeMap.getOrDefault(ArtifactId.valueOf(artId), new LinkedList<>());
   }

   private void addChangeToMap(ChangeItem changeItem) {
      Map<ArtifactId, List<ChangeItem>> map = null;
      long itemTypeId = changeItem.getItemTypeId().getId();
      if (itemTypeId == CoreArtifactTypes.InterfaceNode.getId()) {
         map = nodeMap;
      } else if (itemTypeId == CoreArtifactTypes.InterfaceConnection.getId()) {
         map = connectionMap;
      } else if (itemTypeId == CoreArtifactTypes.InterfaceMessage.getId()) {
         map = messageMap;
      } else if (itemTypeId == CoreArtifactTypes.InterfaceSubMessage.getId()) {
         map = submessageMap;
      } else if (itemTypeId == CoreArtifactTypes.InterfaceStructure.getId()) {
         map = structureMap;
      } else if (itemTypeId == CoreArtifactTypes.InterfaceDataElementArray.getId() || itemTypeId == CoreArtifactTypes.InterfaceDataElement.getId()) {
         map = elementMap;
      } else if (itemTypeId == CoreArtifactTypes.InterfacePlatformType.getId()) {
         map = typeMap;
      } else if (itemTypeId == CoreArtifactTypes.InterfaceEnum.getId()) {
         map = enumMap;
      }

      if (map != null) {
         List<ChangeItem> changes = getListOrDefault(map, changeItem.getArtId().getId());
         changes.add(changeItem);
         map.put(changeItem.getArtId(), changes);
      }
   }

}
