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

package org.eclipse.osee.define.operations.publishing.templatemanager;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.osee.define.api.publishing.templatemanager.InvalidWordMlTemplateException;
import org.eclipse.osee.define.api.publishing.templatemanager.PublishingTemplateKeyType;
import org.eclipse.osee.define.api.publishing.templatemanager.PublishingTemplateScalarKey;
import org.eclipse.osee.define.api.publishing.templatemanager.PublishingTemplateVectorKey;
import org.eclipse.osee.framework.core.data.ArtifactReadable;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.publishing.InvalidRendererOptionsException;
import org.eclipse.osee.framework.core.publishing.PublishingTemplate;
import org.eclipse.osee.framework.core.publishing.RendererOptions;
import org.eclipse.osee.framework.core.publishing.TemplateContent;
import org.eclipse.osee.framework.core.publishing.WordCoreUtil;
import org.eclipse.osee.framework.jdk.core.util.Message;
import org.xml.sax.SAXParseException;

/**
 * An implementation of the {@link PublishingTemplateInternal} interface for OSEE Artifact Publishing Templates.
 *
 * @author Loren K. Ashley
 */

class ArtifactPublishingTemplate implements PublishingTemplateInternal {

   //@formatter:off
   private final EnumMap<PublishingTemplateKeyType,Iterable<PublishingTemplateScalarKey>> keyExtractors;
   //@formatter:on

   /**
    * Saves the {@link ArtifactReadable} containing the Publishing Template.
    */

   private final ArtifactReadable artifactReadable;

   /**
    * A {@link String} representation of the OSEE Artifact Identifier.
    */

   private final PublishingTemplateScalarKey identifier;

   /**
    * The OSEE Artifact name.
    */

   private final PublishingTemplateScalarKey name;

   /**
    * Saves the parsed JSON Renderer Options from the OSEE Artifact.
    */

   private final RendererOptions rendererOptions;

   /**
    * Saves the parsed Word ML XML from the OSEE Artifact.
    */

   private final TemplateContent templateContent;

   /**
    * Saves an unmodifiable list of the Publishing Template Artifact's TemplateMatchCritera attribute values.
    */

   private final PublishingTemplateVectorKey matchCriteria;

   /**
    * Saves the safe name of the Publishing Template's Artifact
    */

   private final PublishingTemplateScalarKey safeName;

   /**
    * Parses an {@link ArtifactReadable} into an {@link ArtifactPublishingTemplate}.
    *
    * @param artifactReadable the OSEE Artifact containing the Publishing Template.
    * @throws NullPointerException when the <code>artifactReadable</code> is <code>null</code>.
    * @throws InvalidRendererOptionsException when the Renderer Options in the OSEE Artifact do not parse.
    * @throws INvalidWordMlTemplateException when the Word ML XML does not parse.
    */

   ArtifactPublishingTemplate(ArtifactReadable artifactReadable) {

      /*
       * Validate the template artifact
       */

      this.artifactReadable = Objects.requireNonNull(artifactReadable,
         "ArtifactPublishingTemplate::new, parameter \"artifactReadable\" cannot be null.");

      /*
       * Check for alternate styles
       */

      String styles = null;

      try {
         //@formatter:off
         var templateRelatedArtifacts =
            this.artifactReadable.getRelated(CoreRelationTypes.SupportingInfo_SupportingInfo).getList();

         styles =
            ( templateRelatedArtifacts.size() == 1 )
               ? templateRelatedArtifacts.get(0).getAttributeValuesAsString( CoreAttributeTypes.WholeWordContent )
               : null;
         //@formatter:on
      } catch (Exception e) {

         styles = null;

      }

      //@formatter:off
      this.keyExtractors = new EnumMap<>( PublishingTemplateKeyType.class );
      this.keyExtractors.put( PublishingTemplateKeyType.IDENTIFIER,     this.getIdentifierKeyExtractor()    );
      this.keyExtractors.put( PublishingTemplateKeyType.MATCH_CRITERIA, this.getMatchCriteriaKeyExtractor() );
      this.keyExtractors.put( PublishingTemplateKeyType.NAME,           this.getNameKeyExtractor()          );
      this.keyExtractors.put( PublishingTemplateKeyType.SAFE_NAME,      this.getSafeNameKeyExtractor()      );
      //@formatter:on

      this.identifier = new PublishingTemplateScalarKey("AT-" + this.artifactReadable.getIdString(),
         PublishingTemplateKeyType.IDENTIFIER);

      this.matchCriteria = this.buildTemplateMatchCriteriaList();

      this.name = new PublishingTemplateScalarKey(this.artifactReadable.getName(), PublishingTemplateKeyType.NAME);

      this.safeName =
         new PublishingTemplateScalarKey(this.artifactReadable.getSafeName(), PublishingTemplateKeyType.SAFE_NAME);

      try {
         this.rendererOptions = RendererOptions.create(
            this.artifactReadable.getAttributeValuesAsString(CoreAttributeTypes.RendererOptions));
      } catch (InvalidRendererOptionsException e) {
         e.setPublishingTemplateInformation(this.identifier.getKey(), this.name.getKey());
         throw e;
      }

      CharSequence templateXml = this.artifactReadable.getAttributeValuesAsString(CoreAttributeTypes.WholeWordContent);

      /*
       * If alternate styles were found with a supporting info link, replace the template styles
       */

      if (Objects.nonNull(styles)) {
         templateXml = WordCoreUtil.replaceStyles(templateXml, styles);
      }

      this.templateContent = new TemplateContent(templateXml);

      var documentOptional = this.templateContent.getTemplateXml();

      if (documentOptional.isEmpty()) {

         //@formatter:off
         var message =
            this.templateContent.getTemplateXmlParseError()
               .map
                  (
                     ( exception ) ->
                        ( exception instanceof SAXParseException )
                           ? new Message()
                                    .title( "Template Word ML XML parsing failed." )
                                    .indentInc()
                                    .title( exception.getMessage() )
                                    .indentInc()
                                    .segment( "LineNumber",    ((SAXParseException) exception).getLineNumber()   )
                                    .segment( "Column Number", ((SAXParseException) exception).getColumnNumber() )
                                    .toString()
                            : exception.getMessage()
                  )
               .orElse( "Template Word ML XML parsing failed." );
         //@formatter:on

         throw new InvalidWordMlTemplateException(this.identifier.getKey(), this.name.getKey(), message,
            templateXml.toString());
      }

   }

   /**
    * Extracts the Publishing Template Artifact's {@link CoreAttributeTypes#TemplateMatchCriteria} attribute values and
    * builds an unmodifiable {@link List}.
    *
    * @return on success a {@link List} of the Publishing Template's match criteria; otherwise, an empty {@link List}.
    */

   private PublishingTemplateVectorKey buildTemplateMatchCriteriaList() {
      try {
         var matchCriteriaObjectList =
            this.artifactReadable.getAttributeValues(CoreAttributeTypes.TemplateMatchCriteria);
         //@formatter:off
         var matchCriteriaList =
            matchCriteriaObjectList
               .stream()
               .map( ( matchCriteria ) -> new PublishingTemplateScalarKey( matchCriteria.toString(), PublishingTemplateKeyType.MATCH_CRITERIA ) )
               .collect( Collectors.toUnmodifiableList() );
         return new PublishingTemplateVectorKey( matchCriteriaList );
      } catch (Exception e) {
         //TODO: non-selectable template
         return new PublishingTemplateVectorKey( List.of() );
      }
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public PublishingTemplate getBean() {
      //@formatter:off
      return
         new PublishingTemplate
                (
                   this.identifier.getKey(),
                   this.name.getKey(),
                   this.rendererOptions,
                   this.templateContent
                );
      //@formatter:on
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public PublishingTemplateScalarKey getIdentifier() {
      return this.identifier;
   }

   @Override
   public Iterable<PublishingTemplateScalarKey> getKeyIterable(PublishingTemplateKeyType keyType) {
      return this.keyExtractors.get(keyType);
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public PublishingTemplateScalarKey getName() {
      return this.name;
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public RendererOptions getRendererOptions() {
      return this.rendererOptions;
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public PublishingTemplateScalarKey getSafeName() {
      return this.safeName;
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public TemplateContent getTemplateContent() {
      return this.templateContent;
   }

   /**
    * {@inheritDoc}
    * <p>
    * The match criteria strings for Artifact Publishing Templates are extracted from the OSEE Artifact's
    * TemplateMatchCriteria attribute values.
    */

   @Override
   public PublishingTemplateVectorKey getMatchCriteria() {
      return this.matchCriteria;
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public Message toMessage(int indent, Message message) {

      var outMessage = Objects.nonNull(message) ? message : new Message();

      //@formatter:off
      outMessage
         .indent( indent )
         .title( "ArtifactPublishingTemplate" )
         .indentInc()
         .segment( "Class",                   this.getClass().getSimpleName() )
         .segment( "Artifact Readable",       this.artifactReadable           )
         .segment( "Identifier",              this.identifier                 )
         .segment( "Name",                    this.name                       )
         .segment( "Template Match Criteria", this.matchCriteria      )
         .toMessage( this.rendererOptions )
         .toMessage( this.templateContent )
         ;
      //@formatter:off

     return outMessage;
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public String toString() {
      return this.toMessage(0, null).toString();
   }
}

/* EOF */