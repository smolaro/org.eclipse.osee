/*********************************************************************
 * Copyright (c) 2023 Boeing
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
package org.eclipse.osee.orcs.rest.model.transaction;

/**
 * @author David W. Miller
 * @author autogenerated by jsonschema2pojo
 */
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
   "branch",
   "txComment",
   "createArtifacts",
   "modifyArtifacts",
   "deleteArtifacts",
   "deleteRelations",
   "addRelations"})
public class TransactionBuilderData {

   @JsonProperty("branch")
   private String branch;
   @JsonProperty("txComment")
   private String txComment;
   @JsonProperty("txCommitArtId")
   private Long txCommitArtId;
   @JsonProperty("createArtifacts")
   private List<CreateArtifact> createArtifacts;
   @JsonProperty("modifyArtifacts")
   private List<ModifyArtifact> modifyArtifacts;
   @JsonProperty("deleteArtifacts")
   private List<Long> deleteArtifacts;
   @JsonProperty("deleteRelations")
   private List<DeleteRelation> deleteRelations;
   @JsonProperty("addRelations")
   private List<AddRelation> addRelations;
   @JsonIgnore
   private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

   @JsonProperty("branch")
   public String getBranch() {
      return branch;
   }

   @JsonProperty("branch")
   public void setBranch(String branch) {
      this.branch = branch;
   }

   @JsonProperty("txComment")
   public String getTxComment() {
      return txComment;
   }

   @JsonProperty("txComment")
   public void setTxComment(String txComment) {
      this.txComment = txComment;
   }

   @JsonProperty("txCommitArtId")
   public Long getTxCommitArtId() {
      return txCommitArtId;
   }

   @JsonProperty("txCommitArtId")
   public void setTxCommitArtId(Long txCommitArtId) {
      this.txCommitArtId = txCommitArtId;
   }

   @JsonProperty("createArtifacts")
   public List<CreateArtifact> getCreateArtifacts() {
      return createArtifacts;
   }

   @JsonProperty("createArtifacts")
   public void setCreateArtifacts(List<CreateArtifact> createArtifacts) {
      this.createArtifacts = createArtifacts;
   }

   @JsonProperty("modifyArtifacts")
   public List<ModifyArtifact> getModifyArtifacts() {
      return modifyArtifacts;
   }

   @JsonProperty("modifyArtifacts")
   public void setModifyArtifacts(List<ModifyArtifact> modifyArtifacts) {
      this.modifyArtifacts = modifyArtifacts;
   }

   @JsonProperty("deleteArtifacts")
   public List<Long> getDeleteArtifacts() {
      return deleteArtifacts;
   }

   @JsonProperty("deleteArtifacts")
   public void setDeleteArtifacts(List<Long> deleteArtifacts) {
      this.deleteArtifacts = deleteArtifacts;
   }

   @JsonProperty("deleteRelations")
   public List<DeleteRelation> getDeleteRelations() {
      return deleteRelations;
   }

   @JsonProperty("deleteRelations")
   public void setDeleteRelations(List<DeleteRelation> deleteRelations) {
      this.deleteRelations = deleteRelations;
   }

   @JsonProperty("addRelations")
   public List<AddRelation> getAddRelations() {
      return addRelations;
   }

   @JsonProperty("addRelations")
   public void setAddRelations(List<AddRelation> addRelations) {
      this.addRelations = addRelations;
   }

   @JsonAnyGetter
   public Map<String, Object> getAdditionalProperties() {
      return this.additionalProperties;
   }

   @JsonAnySetter
   public void setAdditionalProperty(String name, Object value) {
      this.additionalProperties.put(name, value);
   }
}
