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

package org.eclipse.osee.framework.ui.skynet.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.db.connection.exception.OseeArgumentException;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.StaticIdManager;
import org.eclipse.osee.framework.skynet.core.artifact.WordArtifact;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.skynet.core.linking.LinkType;
import org.eclipse.osee.framework.skynet.core.linking.WordMlLinkHandler;
import org.eclipse.osee.framework.skynet.core.word.WordAnnotationHandler;
import org.eclipse.osee.framework.skynet.core.word.WordUtil;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.plugin.util.Jobs;
import org.eclipse.osee.framework.ui.plugin.util.OseeData;
import org.eclipse.osee.framework.ui.skynet.ArtifactExplorer;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.preferences.DiffPreferencePage;
import org.eclipse.osee.framework.ui.skynet.render.word.AttributeElement;
import org.eclipse.osee.framework.ui.skynet.render.word.Producer;
import org.eclipse.osee.framework.ui.skynet.render.word.WordMLProducer;
import org.eclipse.osee.framework.ui.skynet.render.word.WordTemplateProcessor;
import org.eclipse.osee.framework.ui.skynet.templates.TemplateManager;
import org.w3c.dom.Element;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Renders WordML content.
 * 
 * @author Jeff C. Phillips
 */
public class WordTemplateRenderer extends WordRenderer implements ITemplateRenderer {

   private static final Pattern pattern =
         Pattern.compile("<v:imagedata[^>]*src=\"wordml://(\\d+\\.\\w+)\"[^>]*>",
               Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
   public static final String RENDERER_EXTENSION = "org.eclipse.osee.framework.ui.skynet.word";
   public static final String DEFAULT_SET_NAME = "Default";
   public static final String ARTIFACT_NAME = "Word Renderer";
   public static final String TEMPLATE_ATTRIBUTE = "Word Template";
   public static final String ARTIFACT_SCHEMA = "http://eclipse.org/artifact.xsd";
   private static final String EMBEDDED_OBJECT_NO = "w:embeddedObjPresent=\"no\"";
   private static final String EMBEDDED_OBJECT_YES = "w:embeddedObjPresent=\"yes\"";
   private static final String STYLES_END = "</w:styles>";
   private static final String OLE_START = "<w:docOleData>";
   private static final String OLE_END = "</w:docOleData>";
   private static final QName fo = new QName("ns0", "unused_localname", ARTIFACT_SCHEMA);
   public static final String UPDATE_PARAGRAPH_NUMBER_OPTION = "updateParagraphNumber";

   private final WordTemplateProcessor templateProcessor = new WordTemplateProcessor(this);

   /**
    * @param rendererId
    */
   public WordTemplateRenderer() {
      super();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.render.DefaultArtifactRenderer#commandId()
    */
   @Override
   public List<String> getCommandId(PresentationType presentationType) {
      ArrayList<String> commandIds = new ArrayList<String>(2);

      if (presentationType == PresentationType.SPECIALIZED_EDIT) {
         commandIds.add("org.eclipse.osee.framework.ui.skynet.wordeditor.command");
      } else if (presentationType == PresentationType.PREVIEW) {
         commandIds.add("org.eclipse.osee.framework.ui.skynet.wordpreview.command");
         commandIds.add("org.eclipse.osee.framework.ui.skynet.wordpreviewChildren.command");
      }

      return commandIds;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.render.IRenderer#newInstance()
    */
   @Override
   public WordTemplateRenderer newInstance() throws OseeCoreException {
      return new WordTemplateRenderer();
   }

   public void publish(VariableMap variableMap, Artifact masterTemplateArtifact, Artifact slaveTemplateArtifact, List<Artifact> artifacts) throws OseeCoreException {
      templateProcessor.publishWithExtensionTemplates(variableMap, masterTemplateArtifact, slaveTemplateArtifact,
            artifacts);
   }

   /**
    * Creates a difference report for each artifact between baseArtifact and newerArtifact. Then produces a single
    * report by combining each of the difference reports together for a single report.
    */
   @Override
   public void compareArtifacts(final List<Artifact> baseArtifacts, final List<Artifact> newerArtifact, IProgressMonitor monitor, final Branch branch, PresentationType presentationType) throws OseeCoreException {
      if (branch == null) {
         throw new OseeArgumentException("Branch can not be null");
      }

      if (baseArtifacts.size() != newerArtifact.size()) {
         throw new OseeArgumentException(
               "base artifacts size: " + baseArtifacts.size() + " must match newer artifacts size: " + newerArtifact.size() + ".");
      }

      Jobs.startJob(new Job("Word Change Report") {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               String fileName = getStringOption("fileName");

               //if the file name is null we will give it a GUID
               if (fileName == null) {
                  fileName = GUID.generateGuidStr() + ".xml";
               }

               monitor.beginTask("Word Change Report ", newerArtifact.size() * 2);
               ArrayList<String> fileNames = new ArrayList<String>(newerArtifact.size());
               IFolder baseFolder = getRenderFolder(branch, PresentationType.DIFF);
               IFolder changeReportFolder = OseeData.getFolder(".diff/" + GUID.generateGuidStr());
               String baseFileStr = "c:/UserData";
               String localFileName = null;

               VbaWordDiffGenerator generator = new VbaWordDiffGenerator();
               generator.initialize(false, false);
               for (int i = 0; i < newerArtifact.size(); i++) {
                  try {

                     //Remove tracked changes and display image diffs
                     Pair<String, Boolean> originalValue = null;
                     Pair<String, Boolean> newAnnotationValue = null;
                     Pair<String, Boolean> oldAnnotationValue = null;
                     if (!StaticIdManager.hasValue(UserManager.getUser(), DiffPreferencePage.REMOVE_TRACKED_CHANGES)) {
                        Attribute attribute;
                        if (baseArtifacts.get(i) != null) {
                           attribute = baseArtifacts.get(i).getSoleAttribute(WordAttribute.WORD_TEMPLATE_CONTENT);
                           if (attribute != null) {
                              String value = attribute.getValue().toString();
                              if (WordAnnotationHandler.containsWordAnnotations(value)) {
                                 oldAnnotationValue = new Pair<String, Boolean>(value, attribute.isDirty());
                                 attribute.setValue(WordAnnotationHandler.removeAnnotations(value));
                              }
                           }
                        }
                        if (newerArtifact.get(i) != null) {
                           attribute = newerArtifact.get(i).getSoleAttribute(WordAttribute.WORD_TEMPLATE_CONTENT);
                           if (attribute != null) {
                              String value = attribute.getValue().toString();
                              if (WordAnnotationHandler.containsWordAnnotations(value)) {
                                 newAnnotationValue = new Pair<String, Boolean>(value, attribute.isDirty());
                                 attribute.setValue(WordAnnotationHandler.removeAnnotations(value));
                              }
                           }
                        }
                     }
                     if (!StaticIdManager.hasValue(UserManager.getUser(), DiffPreferencePage.IDENTFY_IMAGE_CHANGES)) {
                        originalValue =
                              WordImageChecker.checkForImageDiffs(
                                    baseArtifacts.get(i) != null ? baseArtifacts.get(i).getSoleAttribute(
                                          WordAttribute.WORD_TEMPLATE_CONTENT) : null,
                                    newerArtifact.get(i) != null ? newerArtifact.get(i).getSoleAttribute(
                                          WordAttribute.WORD_TEMPLATE_CONTENT) : null);
                     }
                     IFile baseFile =
                           renderToFile(baseFolder, getFilenameFromArtifact(null, PresentationType.DIFF), branch,
                                 getRenderInputStream(baseArtifacts.get(i), PresentationType.DIFF),
                                 PresentationType.DIFF);
                     IFile newerFile =
                           renderToFile(baseFolder, getFilenameFromArtifact(null, PresentationType.DIFF), branch,
                                 getRenderInputStream(newerArtifact.get(i), PresentationType.DIFF),
                                 PresentationType.DIFF);
                     WordImageChecker.restoreOriginalValue(
                           baseArtifacts.get(i) != null ? baseArtifacts.get(i).getSoleAttribute(
                                 WordAttribute.WORD_TEMPLATE_CONTENT) : null,
                           oldAnnotationValue != null ? oldAnnotationValue : originalValue);
                     WordImageChecker.restoreOriginalValue(
                           newerArtifact.get(i) != null ? newerArtifact.get(i).getSoleAttribute(
                                 WordAttribute.WORD_TEMPLATE_CONTENT) : null, newAnnotationValue);
                     baseFileStr = changeReportFolder.getLocation().toOSString();
                     localFileName = baseFileStr + "/" + GUID.generateGuidStr() + ".xml";
                     fileNames.add(localFileName);

                     monitor.setTaskName("Adding to Diff Script: " + (newerArtifact.get(i) == null ? "Unnamed Artifact" : newerArtifact.get(
                           i).getDescriptiveName()));
                     monitor.worked(1);

                     if (monitor.isCanceled()) {
                        monitor.done();
                        return Status.CANCEL_STATUS;
                     }
                     generator.addComparison(baseFile, newerFile, localFileName, false);
                  } catch (OseeCoreException ex) {
                     OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
                  }

               }
               monitor.setTaskName("Running Diff Script");
               generator.finish(baseFileStr + "/compareDocs.vbs", true);
            } catch (OseeCoreException ex) {
               return new Status(Status.ERROR, SkynetGuiPlugin.PLUGIN_ID, Status.OK, ex.getLocalizedMessage(), ex);
            }
            return Status.OK_STATUS;
         }
      });
   }

   @Override
   public String compare(Artifact baseVersion, Artifact newerVersion, IProgressMonitor monitor, PresentationType presentationType, boolean show) throws OseeCoreException {
      if (baseVersion == null && newerVersion == null) throw new OseeArgumentException(
            "baseVersion and newerVersion can't both be null.");

      Branch branch = (baseVersion != null ? baseVersion.getBranch() : newerVersion.getBranch());
      IFile baseFile;
      IFile newerFile;
      Pair<String, Boolean> originalValue = null;
      Pair<String, Boolean> newAnnotationValue = null;
      Pair<String, Boolean> oldAnnotationValue = null;

      if (!StaticIdManager.hasValue(UserManager.getUser(), DiffPreferencePage.REMOVE_TRACKED_CHANGES)) {
         Attribute attribute = null;

         if (baseVersion != null) {
            attribute = baseVersion.getSoleAttribute(WordAttribute.WORD_TEMPLATE_CONTENT);
            if (attribute != null) {
               String value = attribute.getValue().toString();
               if (WordAnnotationHandler.containsWordAnnotations(value)) {
                  oldAnnotationValue = new Pair<String, Boolean>(value, attribute.isDirty());
                  attribute.setValue(WordAnnotationHandler.removeAnnotations(value));
               }
            }
         }
         if (newerVersion != null) {
            attribute = newerVersion.getSoleAttribute(WordAttribute.WORD_TEMPLATE_CONTENT);
            if (attribute != null) {
               String value = attribute.getValue().toString();
               if (WordAnnotationHandler.containsWordAnnotations(value)) {
                  newAnnotationValue = new Pair<String, Boolean>(value, attribute.isDirty());
                  attribute.setValue(WordAnnotationHandler.removeAnnotations(value));
               }
            }
         }
      }

      if (!StaticIdManager.hasValue(UserManager.getUser(), DiffPreferencePage.IDENTFY_IMAGE_CHANGES)) {
         originalValue =
               WordImageChecker.checkForImageDiffs(
                     baseVersion != null ? baseVersion.getSoleAttribute(WordAttribute.WORD_TEMPLATE_CONTENT) : null,
                     newerVersion != null ? newerVersion.getSoleAttribute(WordAttribute.WORD_TEMPLATE_CONTENT) : null);
      }
      if (baseVersion != null) {
         if (presentationType == PresentationType.MERGE || presentationType == PresentationType.MERGE_EDIT) {
            baseFile = renderForMerge(monitor, baseVersion, presentationType);
         } else {
            baseFile = renderForDiff(monitor, baseVersion);
         }
      } else {
         baseFile = renderForDiff(monitor, branch);
      }

      if (newerVersion != null) {
         if (presentationType == PresentationType.MERGE || presentationType == PresentationType.MERGE_EDIT) {
            newerFile = renderForMerge(monitor, newerVersion, presentationType);
         } else {
            newerFile = renderForDiff(monitor, newerVersion);
         }
      } else {
         newerFile = renderForDiff(monitor, branch);
      }
      WordImageChecker.restoreOriginalValue(
            baseVersion != null ? baseVersion.getSoleAttribute(WordAttribute.WORD_TEMPLATE_CONTENT) : null,
            oldAnnotationValue != null ? oldAnnotationValue : originalValue);
      WordImageChecker.restoreOriginalValue(
            newerVersion != null ? newerVersion.getSoleAttribute(WordAttribute.WORD_TEMPLATE_CONTENT) : null,
            newAnnotationValue);
      return compare(baseVersion, newerVersion, baseFile, newerFile, presentationType, show);
   }

   @Override
   public String compare(Artifact baseVersion, Artifact newerVersion, IFile baseFile, IFile newerFile, PresentationType presentationType, boolean show) throws OseeCoreException {
      String diffPath;

      String fileName = getStringOption("fileName");
      if (fileName == null || fileName.equals("")) {
         if (baseVersion != null) {
            String baseFileStr = baseFile.getLocation().toOSString();
            diffPath =
                  baseFileStr.substring(0, baseFileStr.lastIndexOf(')') + 1) + " to " + (newerVersion != null ? newerVersion.getTransactionNumber() : " deleted") + baseFileStr.substring(baseFileStr.lastIndexOf(')') + 1);
         } else {
            String baseFileStr = newerFile.getLocation().toOSString();
            diffPath =
                  baseFileStr.substring(0, baseFileStr.lastIndexOf('(') + 1) + "new " + baseFileStr.substring(baseFileStr.lastIndexOf('(') + 1);
         }
      } else {
         diffPath =
               getRenderFolder(baseVersion.getBranch(), PresentationType.SPECIALIZED_EDIT).getLocation().toOSString() + '\\' + fileName;
      }

      VbaWordDiffGenerator diffGenerator = new VbaWordDiffGenerator();
      diffGenerator.initialize(presentationType == PresentationType.DIFF,
            presentationType == PresentationType.MERGE_EDIT);

      if (presentationType == PresentationType.MERGE_EDIT && baseVersion != null) {
         addFileToWatcher(getRenderFolder(baseVersion.getBranch(), PresentationType.SPECIALIZED_EDIT),
               diffPath.substring(diffPath.lastIndexOf('\\') + 1));
         diffGenerator.addComparison(baseFile, newerFile, diffPath, true);
         diffGenerator.finish(diffPath.substring(0, diffPath.lastIndexOf('\\')) + "mergeDocs.vbs", show);
      } else {
         diffGenerator.addComparison(baseFile, newerFile, diffPath, false);
         diffGenerator.finish(diffPath.substring(0, diffPath.lastIndexOf('\\')) + "/compareDocs.vbs", show);
      }

      return diffPath;
   }

   /**
    * Displays a list of artifacts in the Artifact Explorer that could not be multi edited because they contained
    * artifacts that had an OLEData attribute.
    * 
    * @param artifacts
    */
   private void displayNotMultiEditArtifacts(final Collection<Artifact> artifacts) {
      if (!artifacts.isEmpty()) {
         Displays.ensureInDisplayThread(new Runnable() {

            public void run() {
               ArtifactExplorer.explore(artifacts);
            }
         });
      }
   }

   public static QName getFoNamespace() {
      return fo;
   }

   public static byte[] getFormattedContent(Element formattedItemElement) {
      ByteArrayOutputStream data = new ByteArrayOutputStream((int) Math.pow(2, 10));
      OutputFormat format = Jaxp.getCompactFormat(formattedItemElement.getOwnerDocument());
      format.setOmitDocumentType(true);
      format.setOmitXMLDeclaration(true);
      XMLSerializer serializer = new XMLSerializer(data, format);

      try {
         for (Element e : Jaxp.getChildDirects(formattedItemElement))
            serializer.serialize(e);
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }

      return data.toByteArray();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.ui.skynet.render.IRenderer#isValidFor(org.eclipse.osee.framework.skynet.core.artifact.Artifact)
    */
   @Override
   public int getApplicabilityRating(PresentationType presentationType, Artifact artifact) {
      if (!artifact.isOfType(WordArtifact.WHOLE_WORD) && !artifact.isOfType("Native")) {
         if (presentationType == PresentationType.DIFF || presentationType == PresentationType.PREVIEW) {
            return WORD_PUBLICATION;
         }
      }

      if (artifact.isOfType(WordArtifact.WORD_TEMPLATE)) {
         if (presentationType != PresentationType.GENERALIZED_EDIT) {
            return PRESENTATION_SUBTYPE_MATCH;
         }
      }
      return NO_MATCH;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.ui.skynet.render.FileRenderer#getRenderInputStream(org.eclipse.core.runtime.IProgressMonitor,
    *      org.eclipse.osee.framework.skynet.core.artifact.Artifact, java.lang.String,
    *      org.eclipse.osee.framework.ui.skynet.render.PresentationType)
    */
   @Override
   public InputStream getRenderInputStream(Artifact artifact, PresentationType presentationType) throws OseeCoreException {
      ArrayList<Artifact> artifacts = new ArrayList<Artifact>(1);
      if (artifact != null) {
         artifacts.add(artifact);
      }
      return getRenderInputStream(artifacts, presentationType);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.render.WordRenderer#renderAttribute(java.lang.String, org.eclipse.osee.framework.skynet.core.artifact.Artifact)
    */
   @Override
   public void renderAttribute(String attributeTypeName, Artifact artifact, PresentationType presentationType, Producer producer, VariableMap map, AttributeElement attributeElement) throws OseeCoreException {
      String value = "";
      WordMLProducer wordMl = (WordMLProducer) producer;

      if (attributeTypeName.equals(WordAttribute.WORD_TEMPLATE_CONTENT)) {
         Attribute<?> wordTempConAttr = artifact.getSoleAttribute(attributeTypeName);
         String data = (String) wordTempConAttr.getValue();

         if (attributeElement.getLabel().length() > 0) {
            wordMl.addParagraph(attributeElement.getLabel());
         }

         if (data != null) {
            value = WordUtil.stripSpellCheck(data);//TODO what is the best way to get at unknown attribute types? (because this isn't it)
            //Change the BinData Id so images do not get overridden by the other images
            value = WordUtil.reassignBinDataID(value);

            LinkType linkType = (LinkType) map.getValue("linkType");
            value = WordMlLinkHandler.link(linkType, artifact, value);
         }

         if (presentationType == PresentationType.SPECIALIZED_EDIT) {
            WordTemplateProcessor.writeXMLMetaDataWrapper(wordMl,
                  WordTemplateProcessor.elementNameFor(attributeTypeName), "ns0:guid=\"" + artifact.getGuid() + "\"",
                  "ns0:attrId=\"" + wordTempConAttr.getAttributeType().getAttrTypeId() + "\"", value);
         } else {
            wordMl.addWordMl(value);
         }
         wordMl.resetListValue();

      } else {
         super.renderAttribute(attributeTypeName, artifact, PresentationType.SPECIALIZED_EDIT, wordMl, map,
               attributeElement);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.ui.skynet.render.FileRenderer#getRenderInputStream(org.eclipse.core.runtime.IProgressMonitor,
    *      org.eclipse.osee.framework.skynet.core.artifact.Artifact, java.lang.String,
    *      org.eclipse.osee.framework.ui.skynet.render.FileSystemRenderer.PresentationType)
    */
   @Override
   public InputStream getRenderInputStream(List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      final List<Artifact> notMultiEditableArtifacts = new LinkedList<Artifact>();
      String template;

      if (artifacts.isEmpty()) {
         //  Still need to get a default template with a null artifact list
         template = getTemplate(null, presentationType);
      } else {
         Artifact firstArtifact = artifacts.iterator().next();
         template = getTemplate(firstArtifact, presentationType);
         if (presentationType == PresentationType.SPECIALIZED_EDIT && artifacts.size() > 1) {
            // currently we can't support the editing of multiple artifacts with OLE data
            for (Artifact artifact : artifacts) {
               if (!artifact.getSoleAttributeValue(WordAttribute.OLE_DATA_NAME, "").equals("") && presentationType == PresentationType.GENERALIZED_EDIT) {
                  notMultiEditableArtifacts.add(artifact);
               }
            }
            displayNotMultiEditArtifacts(notMultiEditableArtifacts);
            artifacts.removeAll(notMultiEditableArtifacts);
         } else { // support OLE data when appropriate
            if (!firstArtifact.getSoleAttributeValue(WordAttribute.OLE_DATA_NAME, "").equals("")) {
               template = template.replaceAll(EMBEDDED_OBJECT_NO, EMBEDDED_OBJECT_YES);
               template =
                     template.replaceAll(STYLES_END, STYLES_END + OLE_START + firstArtifact.getSoleAttributeValue(
                           WordAttribute.OLE_DATA_NAME, "") + OLE_END);
            }
         }
      }

      template = WordUtil.removeGUIDFromTemplate(template);
      return templateProcessor.applyTemplate(getOptions(), artifacts, template, null, null, null, presentationType);
   }

   protected String getTemplate(Artifact artifact, PresentationType presentationType) throws OseeCoreException {
      return TemplateManager.getTemplate(this, artifact, presentationType.name(), getStringOption(TEMPLATE_OPTION)).getSoleAttributeValue(
            WordAttribute.WHOLE_WORD_CONTENT);
   }

}