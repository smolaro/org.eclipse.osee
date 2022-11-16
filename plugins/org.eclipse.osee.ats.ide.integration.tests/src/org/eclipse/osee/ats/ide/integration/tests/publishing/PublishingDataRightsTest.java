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

package org.eclipse.osee.ats.ide.integration.tests.publishing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.osee.ats.ide.integration.tests.skynet.core.utils.AttributeSetters;
import org.eclipse.osee.ats.ide.integration.tests.skynet.core.utils.BasicArtifactInfoRecord;
import org.eclipse.osee.ats.ide.integration.tests.skynet.core.utils.BasicAttributeSpecification;
import org.eclipse.osee.ats.ide.integration.tests.skynet.core.utils.BuilderRecord;
import org.eclipse.osee.ats.ide.integration.tests.skynet.core.utils.TestDocumentBuilder;
import org.eclipse.osee.ats.ide.integration.tests.synchronization.TestUserRules;
import org.eclipse.osee.client.demo.DemoChoice;
import org.eclipse.osee.client.test.framework.NotProductionDataStoreRule;
import org.eclipse.osee.define.api.DataRightsEndpoint;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.client.OseeClient;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.model.datarights.DataRightResult;
import org.eclipse.osee.framework.core.util.OseeInf;
import org.eclipse.osee.framework.core.util.OsgiUtil;
import org.eclipse.osee.framework.core.xml.publishing.PublishingXmlUtils;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.orcs.core.util.Artifacts;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;

/**
 * Tests for publishing data rights functions.
 *
 * @author Loren K. Ashley
 */

public class PublishingDataRightsTest {

   /**
    * Set this flag to <code>true</code> to print the received Word ML documents to <code>stdout</code>.
    */

   private static boolean printDocuments = true;

   /**
    * Set this flag to <code>false</code> to prevent the test setup code from altering attribute values in the database.
    * The default (normal for testing) value is <code>true</code>.
    */

   private static boolean setValues = true;

   /**
    * Class level testing rules are applied before the {@link #testSetup} method is invoked. These rules are used for
    * the following:
    * <dl>
    * <dt>Not Production Data Store Rule</dt>
    * <dd>This rule is used to prevent modification of a production database.</dd>
    * <dt>In Publishing Group Test Rule</dt>
    * <dd>This rule is used to ensure the test user has been added to the OSEE publishing group and the server
    * {@Link UserToken} cache has been flushed.</dd></dt>
    */

   //@formatter:off
   @ClassRule
   public static TestRule classRuleChain =
      RuleChain
         .outerRule( TestUserRules.createInPublishingGroupTestRule() )
         .around( new NotProductionDataStoreRule() );
   //@formatter:on

   /**
    * A rule to get the method name of the currently running test.
    */

   @Rule
   public TestName testName = new TestName();

   /**
    * List of {@link BuilderRecord}s describing the test artifacts.
    * <p>
    * Artifacts are created in the list order. Follow the rules:
    * <ul>
    * <li>Ensure identifiers are unique.</li>
    * <li>The identifier 0 is reserved.</li>
    * <li>Ensure hierarchical parents are at lower list indices.</li>
    * <li>Top level test artifact have a hierarchical parent identifier of 0.</li>
    * <li>Ensure children artifact's of a hierarchical parent artifact have unique names.</li>
    * </ul>
    */

   //@formatter:off
   private static List<BuilderRecord> artifactInfoRecords =
      List.of
         (
         new BasicArtifactInfoRecord
            (
               1,                                                                                /* Identifier                             (Integer)                               */
               0,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Data Rights Artifacts Folder",                                                   /* Artifact Name                          (String)                                */
               CoreArtifactTypes.Folder,                                                         /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This folder contains artifacts for data rights tests." ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               2,                                                                                /* Identifier                             (Integer)                               */
               1,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Heading A",                                                                      /* Artifact Name                          (String)                                */
               CoreArtifactTypes.HeadingMsWord,                                                  /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Section A Heading." ),                                   /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>Section A</w:t></w:r></w:p>" ),           /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Default" ),                                              /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Portrait" ),                                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               3,                                                                                /* Identifier                             (Integer)                               */
               2,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Requirement A-1",                                                                /* Artifact Name                          (String)                                */
               CoreArtifactTypes.SoftwareRequirementMsWord,                                      /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This is Requirement A-1." ),                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>This is the word content for Requirement A-1.</w:t></w:r></w:p>" ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Default" ),                                              /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Portrait" ),                                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               4,                                                                                /* Identifier                             (Integer)                               */
               2,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Requirement A-2",                                                                /* Artifact Name                          (String)                                */
               CoreArtifactTypes.SoftwareRequirementMsWord,                                      /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This is Requirement A-2." ),                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>This is the word content for Requirement A-2.</w:t></w:r></w:p>" ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Default" ),                                              /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Portrait" ),                                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               5,                                                                                /* Identifier                             (Integer)                               */
               2,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Requirement A-3",                                                                /* Artifact Name                          (String)                                */
               CoreArtifactTypes.SoftwareRequirementMsWord,                                      /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This is Requirement A-3." ),                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>This is the word content for Requirement A-3.</w:t></w:r></w:p>" ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Default" ),                                              /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Portrait" ),                                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               6,                                                                                /* Identifier                             (Integer)                               */
               2,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Requirement A-4",                                                                /* Artifact Name                          (String)                                */
               CoreArtifactTypes.SoftwareRequirementMsWord,                                      /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This is Requirement A-4." ),                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>This is the word content for Requirement A-4.</w:t></w:r></w:p>" ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Restricted Rights" ),                                    /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Portrait" ),                                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               7,                                                                                /* Identifier                             (Integer)                               */
               2,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Requirement A-5",                                                                /* Artifact Name                          (String)                                */
               CoreArtifactTypes.SoftwareRequirementMsWord,                                      /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This is Requirement A-5." ),                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>This is the word content for Requirement A-5.</w:t></w:r></w:p>" ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Restricted Rights" ),                                    /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Portrait" ),                                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               8,                                                                                /* Identifier                             (Integer)                               */
               2,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Requirement A-6",                                                                /* Artifact Name                          (String)                                */
               CoreArtifactTypes.SoftwareRequirementMsWord,                                      /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This is Requirement A-6." ),                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>This is the word content for Requirement A-6.</w:t></w:r></w:p>" ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Restricted Rights" ),                                    /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Landscape" ),                                            /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               9,                                                                                /* Identifier                             (Integer)                               */
               2,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Requirement A-7",                                                                /* Artifact Name                          (String)                                */
               CoreArtifactTypes.SoftwareRequirementMsWord,                                      /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This is Requirement A-7." ),                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>This is the word content for Requirement A-7.</w:t></w:r></w:p>" ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Restricted Rights" ),                                    /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Landscape" ),                                            /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

         new BasicArtifactInfoRecord
            (
               10,                                                                               /* Identifier                             (Integer)                               */
               2,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
               "Requirement A-8",                                                                /* Artifact Name                          (String)                                */
               CoreArtifactTypes.SoftwareRequirementMsWord,                                      /* Artifact Type                          (ArtifactTypeToken)                     */
               List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                  (
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "This is Requirement A-8." ),                             /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.WordTemplateContent,                            /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "<w:p><w:r><w:t>This is the word content for Requirement A-8.</w:t></w:r></w:p>" ), /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.DataRightsClassification,                       /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Restricted Rights" ),                                    /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            ),
                     new BasicAttributeSpecification
                            (
                              CoreAttributeTypes.PageOrientation,                                /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                              List.of( "Landscape" ),                                            /* Test Attribute Values                  (List<Object>)                          */
                              AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                            )
                  ),
               List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
            ),

            new BasicArtifactInfoRecord
               (
                  11,                                                                               /* Identifier                             (Integer)                               */
                  1,                                                                                /* Hierarchical Parent Identifier         (Integer)                               */
                  "Heading B",                                                                      /* Artifact Name                          (String)                                */
                  CoreArtifactTypes.HeadingHtml,                                                    /* Artifact Type                          (ArtifactTypeToken)                     */
                  List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                     (
                        new BasicAttributeSpecification
                               (
                                 CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                                 List.of( "Section B Heading." ),                                   /* Test Attribute Values                  (List<Object>)                          */
                                 AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                               ),
                        new BasicAttributeSpecification
                               (
                                 CoreAttributeTypes.HtmlContent,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                                 List.of( "<p><b>Section A</b></p>" ),                              /* Test Attribute Values                  (List<Object>)                          */
                                 AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                               )
                     ),
                  List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
               ),

            new BasicArtifactInfoRecord
               (
                  12,                                                                               /* Identifier                             (Integer)                               */
                  11,                                                                               /* Hierarchical Parent Identifier         (Integer)                               */
                  "Requirement B-1",                                                                /* Artifact Name                          (String)                                */
                  CoreArtifactTypes.SoftwareRequirementHtml,                                        /* Artifact Type                          (ArtifactTypeToken)                     */
                  List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                     (
                        new BasicAttributeSpecification
                               (
                                 CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                                 List.of( "This is Requirement B-1." ),                             /* Test Attribute Values                  (List<Object>)                          */
                                 AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                               ),
                        new BasicAttributeSpecification
                               (
                                 CoreAttributeTypes.HtmlContent,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                                 List.of( "<p>This is the HTML content for Requirement B-1.</p>" ), /* Test Attribute Values                  (List<Object>)                          */
                                 AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                               )
                     ),
                  List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
               ),

            new BasicArtifactInfoRecord
               (
                  13,                                                                               /* Identifier                             (Integer)                               */
                  11,                                                                               /* Hierarchical Parent Identifier         (Integer)                               */
                  "Requirement B-2",                                                                /* Artifact Name                          (String)                                */
                  CoreArtifactTypes.SoftwareRequirementHtml,                                        /* Artifact Type                          (ArtifactTypeToken)                     */
                  List.of                                                                           /* Attribute Specifications               (List<AttributeSpecificationRecord>)    */
                     (
                        new BasicAttributeSpecification
                               (
                                 CoreAttributeTypes.Description,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                                 List.of( "This is Requirement B-2." ),                             /* Test Attribute Values                  (List<Object>)                          */
                                 AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                               ),
                        new BasicAttributeSpecification
                               (
                                 CoreAttributeTypes.HtmlContent,                                    /* Test Attribute Type                    (AttributeTypeGeneric<?>)               */
                                 List.of( "<p>This is the HTML content for Requirement B-2.</p>" ), /* Test Attribute Values                  (List<Object>)                          */
                                 AttributeSetters.stringAttributeSetter                             /* AttributeSetter                        (BiConsumer<Attribute<?>,Object>)       */
                               )
                     ),
                  List.of()                                                                         /* BuilderRelationshipRecords             (List<BuilderRelationshipRecords>)      */
               )

         );
   //@formatter:on

   /**
    * Saves a {@link Map} of the builder record identifier associated with each {@link ArtifactId}.
    */

   private static Map<Long, Integer> artifactIdMap;

   /**
    * Saves a {@link Map} of the {@link BuilderRecord} objects by builder record identifier.
    */

   private static Map<Integer, BuilderRecord> builderRecordByIdentifierMap;

   /**
    * Saves a {@link Map} of the artifact identifiers for each artifact associated with each {@link BuilderRecord}.
    */

   private static Map<Integer, Optional<Long>> builderRecordMap;

   /**
    * Saves a handle to the Data Rights REST API endpoint.
    */

   private static DataRightsEndpoint dataRightsEndpoint;

   /**
    * Save a reference to the {@link PublishingXmlUtils} helper object.
    */

   private static PublishingXmlUtils publishingXmlUtils;

   /**
    * Saves the {@link ArtifactId} of the root artifact of the test document.
    */

   private static ArtifactId rootArtifactId;

   /**
    * Saves the {@link BranchId} of the root artifact of the test document.
    */

   private static BranchId rootBranchId;

   /**
    * Name used for the OSEE branch holding the test document.
    */

   private static String testBranchName = "Publishing Server Data Rights Test Branch";

   /**
    * Creation comment used for the OSEE test branch
    */

   private static String testBranchCreationComment = "Branch for Publishing Server Data Rights Testing";

   /**
    * Prints the currently running test method name followed by the received REST API call results for the test.
    *
    * @param dataRightsResult the {@link DataRightResult} returned from the REST API call.
    */

   private void printDocument(DataRightResult dataRightResult) {
      if (PublishingDataRightsTest.printDocuments) {
         System.out.println("=============================================================");
         System.out.println("Test Name: " + this.testName.getMethodName());
         System.out.println("-------------------------------------------------------------");
         System.out.println(dataRightResult.toString());
         System.out.println("=============================================================");
      }
   }

   @BeforeClass
   public static void testSetup() {

      /*
       * When the test suite is run directly it will be in Database Initialization mode.
       */

      if (OseeProperties.isInDbInit()) {

         /*
          * Get out of database initialization mode and re-authenticate as the test user
          */

         OseeProperties.setInDbInit(false);
         ClientSessionManager.releaseSession();
         ClientSessionManager.getSession();
      }

      /*
       * Setup XML utils
       */

      PublishingDataRightsTest.publishingXmlUtils = new PublishingXmlUtils();

      /*
       * Create the Test Artifacts
       */

      var testDocumentBuilder = new TestDocumentBuilder(PublishingDataRightsTest.setValues);

      testDocumentBuilder.buildDocument(PublishingDataRightsTest.artifactInfoRecords,
         PublishingDataRightsTest.testBranchName, PublishingDataRightsTest.testBranchCreationComment);

      /*
       * Save identifiers of test document root
       */

      PublishingDataRightsTest.rootBranchId = testDocumentBuilder.getRootBranchId();
      PublishingDataRightsTest.rootArtifactId = testDocumentBuilder.getRootArtifactId();

      //@formatter:off
      PublishingDataRightsTest.builderRecordMap =
         PublishingDataRightsTest.artifactInfoRecords.stream()
            .map( BuilderRecord::getIdentifier )
            .collect( Collectors.toMap( Function.identity(), testDocumentBuilder::getArtifactIdByBuilderRecordId ) )
            ;

      PublishingDataRightsTest.artifactIdMap =
         PublishingDataRightsTest.builderRecordMap.entrySet().stream()
            .collect( Collectors.toMap( ( e ) -> e.getValue().get(), ( e ) -> e.getKey() ) )
            ;

      PublishingDataRightsTest.builderRecordByIdentifierMap =
         PublishingDataRightsTest.artifactInfoRecords.stream()
            .collect( Collectors.toMap( BuilderRecord::getIdentifier, Function.identity() ))
            ;
      //@formatter:on

      /*
       * Get services
       */

      var oseeClient = OsgiUtil.getService(DemoChoice.class, OseeClient.class);

      PublishingDataRightsTest.dataRightsEndpoint = oseeClient.getDataRightsEndpoint();

      /*
       * Load the expected classification names and footers
       */

      PublishingDataRightsTest.classificationMap = new HashMap<String, Classification>();

      PublishingDataRightsTest.loadClassification("Default.xml");
      PublishingDataRightsTest.loadClassification("GovernmentPurposeRights.xml");
      PublishingDataRightsTest.loadClassification("RestrictedRights.xml");
      PublishingDataRightsTest.loadClassification("Unspecified.xml");
   }

   private static class ExpectedFlags {
      private final boolean newFooter;
      private final boolean isContinuous;

      ExpectedFlags(boolean newFooter, boolean isContinuous) {
         this.newFooter = newFooter;
         this.isContinuous = isContinuous;
      }

      boolean getNewFooter() {
         return this.newFooter;
      }

      boolean getIsContinuous() {
         return this.isContinuous;
      }
   }

   private static class Classification {
      private final String classification;
      private final String footer;

      Classification(String classification, String footer) {
         this.classification = classification;
         this.footer = footer;
      }

      String getClassification() {
         return this.classification;
      }

      String getFooter() {
         return this.footer;
      }
   }

   private static Map<String, Classification> classificationMap;

   static void loadClassification(String classificationFileName) {

      var contents = OseeInf.getResourceContents(classificationFileName, Artifacts.class);
      var splitContents = contents.split("\\R", 2);
      var classification = splitContents[0];
      var footer = splitContents[1];

      PublishingDataRightsTest.classificationMap.put(classification, new Classification(classification, footer));
   }

   @Test
   public void testA() {

      //@formatter:off
      var artifactIds =
         List.of
            (
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  2 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  3 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  4 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  5 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  6 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  7 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  8 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  9 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get( 10 ).get() )
            );

      var expectedFlagsList =
         List.of
            (
               /*                 newFooter  isContinuous */
               new ExpectedFlags( true,      true  ), /*  2 -> Heading A       <- start of sequence        */
               new ExpectedFlags( false,     true  ), /*  3 -> Requirement A-1                             */
               new ExpectedFlags( false,     true  ), /*  4 -> Requirement A-2                             */
               new ExpectedFlags( false,     false ), /*  5 -> Requirement A-3                             */
               new ExpectedFlags( true,      true  ), /*  6 -> Requirement A-4 <- classification changes   */
               new ExpectedFlags( false,     false ), /*  7 -> Requirement A-5                             */
               new ExpectedFlags( false,     true  ), /*  8 -> Requirement A-6 <- page orientation changes */
               new ExpectedFlags( false,     true  ), /*  9 -> Requirement A-7                             */
               new ExpectedFlags( false,     false )  /* 10 -> Requirement A-8                             */
            );

      var dataRightResult =
         PublishingDataRightsTest.dataRightsEndpoint.getDataRights
            (
               PublishingDataRightsTest.rootBranchId,
               artifactIds
            );

      this.printDocument( dataRightResult );

      var dataRightAnchorMap = dataRightResult.getMap();

      int i = 0;

      for( var artifactId : artifactIds ) {

         Assert.assertTrue
            (
               "[" + i + "] Data Right Anchors Map does not contain expected key.",
               dataRightAnchorMap.containsKey( artifactId )
            );

         var dataRightAnchor = dataRightAnchorMap.get( artifactId );
         var expectedFlags   = expectedFlagsList.get(i);

         Assert.assertEquals
            (
               "[" + i + "] NewFooter flag is not as expected.",
               dataRightAnchor.isSetDataRightFooter(),
               expectedFlags.getNewFooter()
            );

         Assert.assertEquals
            (
               "[" + i + "] IsContinuous flag is not as expected.",
               dataRightAnchor.isContinuous(),
               expectedFlags.getIsContinuous()
            );


         var builderRecordIdentifier = PublishingDataRightsTest.artifactIdMap.get( artifactId.getId() );
         var builderRecord = PublishingDataRightsTest.builderRecordByIdentifierMap.get( builderRecordIdentifier );

         var expectedClassification =
            builderRecord.getAttributeSpecifications().stream()
               .filter( ( attributeSpecification ) -> CoreAttributeTypes.DataRightsClassification.equals( attributeSpecification.getAttributeType() ) )
               .findFirst()
               .get()
               .getAttributeValues()
               .get( 0 );

         var expectedContent = PublishingDataRightsTest.classificationMap.get( expectedClassification ).getFooter();
         var content         = dataRightAnchor.getDataRight().getContent();

         Assert.assertEquals
            (
               "[" + i + "] the footer content is not as expected.",
               expectedContent,
               content
            );

         i++;
      }
   }

   @Test
   public void testSequenceAWithOverride() {

      //@formatter:off
      var artifactIds =
         List.of
            (
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  2 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  3 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  4 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  5 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  6 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  7 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  8 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  9 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get( 10 ).get() )
            );

      /**
       * <pre>
       * The override will prevent data right classification changes in the sequence.
       *
       * The override will default all page orientations to Portrait and prevent page orientation changes in the sequence.
       * </pre>
       */

      var expectedFlagsList =
         List.of
            (
               /*                 newFooter  isContinuous */
               new ExpectedFlags( true,      true  ), /*  2 -> Heading A       <- start of sequence        */
               new ExpectedFlags( false,     true  ), /*  3 -> Requirement A-1                             */
               new ExpectedFlags( false,     true  ), /*  4 -> Requirement A-2                             */
               new ExpectedFlags( false,     true  ), /*  5 -> Requirement A-3                             */
               new ExpectedFlags( false,     true  ), /*  6 -> Requirement A-4                             */
               new ExpectedFlags( false,     true  ), /*  7 -> Requirement A-5                             */
               new ExpectedFlags( false,     true  ), /*  8 -> Requirement A-6                             */
               new ExpectedFlags( false,     true  ), /*  9 -> Requirement A-7                             */
               new ExpectedFlags( false,     false )  /* 10 -> Requirement A-8                             */
            );

      var dataRightResult =
         PublishingDataRightsTest.dataRightsEndpoint.getDataRights
            (
               PublishingDataRightsTest.rootBranchId,
               "Government Purpose Rights",
               artifactIds
            );

      this.printDocument( dataRightResult );

      var dataRightAnchorMap = dataRightResult.getMap();

      int i = 0;

      for( var artifactId : artifactIds ) {

         Assert.assertTrue
            (
               "[" + i + "] Data Right Anchors Map does not contain expected key.",
               dataRightAnchorMap.containsKey( artifactId )
            );

         var dataRightAnchor = dataRightAnchorMap.get( artifactId );
         var expectedFlags   = expectedFlagsList.get(i);

         Assert.assertEquals
            (
               "[" + i + "] NewFooter flag is not as expected.",
               dataRightAnchor.isSetDataRightFooter(),
               expectedFlags.getNewFooter()
            );

         Assert.assertEquals
            (
               "[" + i + "] IsContinuous flag is not as expected.",
               dataRightAnchor.isContinuous(),
               expectedFlags.getIsContinuous()
            );

         var expectedClassification = "Government Purpose Rights";

         var expectedContent = PublishingDataRightsTest.classificationMap.get( expectedClassification ).getFooter();
         var content         = dataRightAnchor.getDataRight().getContent();

         Assert.assertEquals
            (
               "[" + i + "] the footer content is not as expected.",
               expectedContent,
               content
            );

         i++;
      }
   }

   @Test
   public void testSequenceAWithUnknownClassificationOverride() {

      //@formatter:off
      var artifactIds =
         List.of
            (
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  2 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  3 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  4 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  5 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  6 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  7 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  8 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get(  9 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get( 10 ).get() )
            );

      var expectedFlagsList =
         List.of
            (
               /*                 newFooter  isContinuous */
               new ExpectedFlags( true,      true  ), /*  2 -> Heading A       <- start of sequence        */
               new ExpectedFlags( false,     true  ), /*  3 -> Requirement A-1                             */
               new ExpectedFlags( false,     true  ), /*  4 -> Requirement A-2                             */
               new ExpectedFlags( false,     false ), /*  5 -> Requirement A-3                             */
               new ExpectedFlags( true,      true  ), /*  6 -> Requirement A-4 <- classification changes   */
               new ExpectedFlags( false,     false ), /*  7 -> Requirement A-5                             */
               new ExpectedFlags( false,     true  ), /*  8 -> Requirement A-6 <- page orientation changes */
               new ExpectedFlags( false,     true  ), /*  9 -> Requirement A-7                             */
               new ExpectedFlags( false,     false )  /* 10 -> Requirement A-8                             */
            );

      var dataRightResult =
         PublishingDataRightsTest.dataRightsEndpoint.getDataRights
            (
               PublishingDataRightsTest.rootBranchId,
               "Zoo Creatures",
               artifactIds
            );

      this.printDocument( dataRightResult );

      var dataRightAnchorMap = dataRightResult.getMap();

      int i = 0;

      for( var artifactId : artifactIds ) {

         Assert.assertTrue
            (
               "[" + i + "] Data Right Anchors Map does not contain expected key.",
               dataRightAnchorMap.containsKey( artifactId )
            );

         var dataRightAnchor = dataRightAnchorMap.get( artifactId );
         var expectedFlags   = expectedFlagsList.get(i);

         Assert.assertEquals
            (
               "[" + i + "] NewFooter flag is not as expected.",
               dataRightAnchor.isSetDataRightFooter(),
               expectedFlags.getNewFooter()
            );

         Assert.assertEquals
            (
               "[" + i + "] IsContinuous flag is not as expected.",
               dataRightAnchor.isContinuous(),
               expectedFlags.getIsContinuous()
            );

         var builderRecordIdentifier = PublishingDataRightsTest.artifactIdMap.get( artifactId.getId() );
         var builderRecord           = PublishingDataRightsTest.builderRecordByIdentifierMap.get( builderRecordIdentifier );

         var expectedClassification =
            builderRecord.getAttributeSpecifications().stream()
               .filter( ( attributeSpecification ) -> CoreAttributeTypes.DataRightsClassification.equals( attributeSpecification.getAttributeType() ) )
               .findFirst()
               .get()
               .getAttributeValues()
               .get( 0 );

         var expectedContent = PublishingDataRightsTest.classificationMap.get( expectedClassification ).getFooter();
         var content         = dataRightAnchor.getDataRight().getContent();

         Assert.assertEquals
            (
               "[" + i + "] the footer content is not as expected.",
               expectedContent,
               content
            );

         i++;
      }
   }

   @Test
   public void testSequenceB() {

      //@formatter:off
      var artifactIds =
         List.of
            (
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get( 11 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get( 12 ).get() ),
               ArtifactId.valueOf( PublishingDataRightsTest.builderRecordMap.get( 13 ).get() )
            );

      var expectedFlagsList =
         List.of
            (
               /*                 newFooter  isContinuous */
               new ExpectedFlags( true,      true  ), /*  2 -> Heading B       <- start of sequence        */
               new ExpectedFlags( false,     true  ), /*  3 -> Requirement B-1                             */
               new ExpectedFlags( false,     false )  /*  4 -> Requirement B-2                             */
            );

      var dataRightResult =
         PublishingDataRightsTest.dataRightsEndpoint.getDataRights
            (
               PublishingDataRightsTest.rootBranchId,
               artifactIds
            );

      this.printDocument( dataRightResult );

      var dataRightAnchorMap = dataRightResult.getMap();

      int i = 0;

      for( var artifactId : artifactIds ) {

         Assert.assertTrue
            (
               "[" + i + "] Data Right Anchors Map does not contain expected key.",
               dataRightAnchorMap.containsKey( artifactId )
            );

         var dataRightAnchor = dataRightAnchorMap.get( artifactId );
         var expectedFlags   = expectedFlagsList.get(i);

         Assert.assertEquals
            (
               "[" + i + "] NewFooter flag is not as expected.",
               dataRightAnchor.isSetDataRightFooter(),
               expectedFlags.getNewFooter()
            );

         Assert.assertEquals
            (
               "[" + i + "] IsContinuous flag is not as expected.",
               dataRightAnchor.isContinuous(),
               expectedFlags.getIsContinuous()
            );

         var builderRecordIdentifier = PublishingDataRightsTest.artifactIdMap.get( artifactId.getId() );
         var builderRecord           = PublishingDataRightsTest.builderRecordByIdentifierMap.get( builderRecordIdentifier );

         var expectedClassification = "Unspecified";

         var expectedContent = PublishingDataRightsTest.classificationMap.get( expectedClassification ).getFooter();
         var content         = dataRightAnchor.getDataRight().getContent();

         Assert.assertEquals
            (
               "[" + i + "] the footer content is not as expected.",
               expectedContent,
               content
            );

         i++;
      }
   }

   @Test
   public void testEmptySequence() {

      //@formatter:off
      List<ArtifactId> artifactIds =
         List.of
            (
            );

      var dataRightResult =
         PublishingDataRightsTest.dataRightsEndpoint.getDataRights
            (
               PublishingDataRightsTest.rootBranchId,
               artifactIds
            );

      this.printDocument( dataRightResult );

      var dataRightAnchorMap = dataRightResult.getMap();

      Assert.assertEquals
         (
            "Returned data right anchor map is expected to be empty.",
            0,
            dataRightAnchorMap.size()
         );
   }

}

/* EOF */
