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
package org.eclipse.osee.framework.skynet.core.artifact;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.skynet.core.exception.MultipleArtifactsExist;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * The <code>ArtifactTransfer</code> class is used to transfer an array of <code>Artifact</code>s from one part to
 * another in a drag and drop operation or a cut, copy, paste action.
 * <p>
 * In every drag and drop operation there is a <code>DragSource</code> and a <code>DropTarget</code>. When a drag
 * occurs a <code>Transfer</code> is used to marshall the drag data from the source into a byte array. If a drop
 * occurs another <code>Transfer</code> is used to marshall the byte array into drop data for the target.
 * </p>
 * <p>
 * When a <code>CutAction</code> or a <code>CopyAction</code> is performed, this transfer is used to place
 * references to the selected resources on the <code>Clipboard</code>. When a <code>PasteAction</code> is
 * performed, the references on the clipboard are used to move or copy the resources to the selected destination.
 * </p>
 * <p>
 * This class can be used for a <code>Viewer<code> or an SWT component directly.
 * A singleton is provided which may be serially reused (see <code>getInstance</code>).  
 * It is not intended to be subclassed.
 * </p>
 *
 * @see org.eclipse.jface.viewers.StructuredViewer
 * @see org.eclipse.swt.dnd.DropTarget
 * @see org.eclipse.swt.dnd.DragSource
 * 
 * @author Robert A. Fisher
 */

public class ArtifactTransfer extends ByteArrayTransfer {

   /**
    * Singleton instance.
    */
   private static final ArtifactTransfer instance = new ArtifactTransfer();

   // Create a unique ID to make sure that different Eclipse
   // applications use different "types" of <code>ResourceTransfer</code>
   private static final String TYPE_NAME =
         "artifact-transfer-format:" + System.currentTimeMillis() + ":" + instance.hashCode(); //$NON-NLS-2$//$NON-NLS-1$

   private static final int TYPEID = registerType(TYPE_NAME);

   /**
    * Creates a new transfer object.
    */
   private ArtifactTransfer() {
   }

   /**
    * Returns the singleton instance.
    * 
    * @return the singleton instance
    */
   public static ArtifactTransfer getInstance() {
      return instance;
   }

   /*
    * (non-Javadoc) Method declared on Transfer.
    */
   protected int[] getTypeIds() {
      return new int[] {TYPEID};
   }

   /*
    * (non-Javadoc) Returns the type names. @return the list of type names
    */
   protected String[] getTypeNames() {
      return new String[] {TYPE_NAME};
   }

   /*
    * (non-Javadoc) Method declared on Transfer.
    */
   protected void javaToNative(Object data, TransferData transferData) {
      if (!(data instanceof ArtifactData)) {
         return;
      }

      ArtifactData artData = (ArtifactData) data;
      /**
       * The resource serialization format is: (int) number of artifacts Then, the following for each resource: (int)
       * artID (int) tagID Then the following (int) urlLength (int) sourceLength (chars) url (chars) source
       */

      try {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         DataOutputStream dataOut = new DataOutputStream(out);

         // write the number of resources
         dataOut.writeInt(artData.getArtifacts().length);

         for (Artifact artifact : artData.getArtifacts()) {
            writeArtifact(dataOut, artifact);
         }
         dataOut.writeInt(artData.getUrl().length());
         dataOut.writeInt(artData.getSource().length());
         dataOut.writeChars(artData.getUrl());
         dataOut.writeChars(artData.getSource());

         // cleanup
         dataOut.close();
         out.close();
         byte[] bytes = out.toByteArray();
         super.javaToNative(bytes, transferData);
      } catch (IOException e) {
         // it's best to send nothing if there were problems
      }
   }

   /*
    * (non-Javadoc) Method declared on Transfer.
    */
   public ArtifactData nativeToJava(TransferData transferData) {
      /**
       * The resource serialization format is: (int) number of artifacts Then, the following for each resource: (int)
       * artID (int) tagID
       */

      byte[] bytes = (byte[]) super.nativeToJava(transferData);
      if (bytes == null) return null;
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
      try {
         int count = in.readInt();
         Artifact[] artifacts = new Artifact[count];
         for (int i = 0; i < count; i++)
            artifacts[i] = readArtifact(in);
         int urlLength = in.readInt();
         int sourceLength = in.readInt();
         String url = "";
         for (int x = 0; x < urlLength; x++)
            url += in.readChar();
         String source = "";
         for (int x = 0; x < sourceLength; x++)
            source += in.readChar();
         return new ArtifactData(artifacts, url, source);
      } catch (Exception ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.toString(), ex);
         return null;
      }
   }

   /**
    * Reads a resource from the given stream.
    * 
    * @param dataIn the input stream
    * @return the resource
    * @throws SQLException
    * @throws IllegalArgumentException
    * @throws MultipleArtifactsExist
    * @throws ArtifactDoesNotExist
    * @throws IOException
    */
   private Artifact readArtifact(DataInputStream dataIn) throws IllegalArgumentException, SQLException, ArtifactDoesNotExist, MultipleArtifactsExist, IOException {
      int artID = dataIn.readInt();
      int branchId = dataIn.readInt();
      return ArtifactQuery.getArtifactFromId(artID, BranchPersistenceManager.getInstance().getBranch(branchId));
   }

   /**
    * Writes the given resource to the given stream.
    * 
    * @param dataOut the output stream
    * @param artifact the artifact
    * @throws IOException if there is a problem writing to the stream
    */
   private void writeArtifact(DataOutputStream dataOut, Artifact artifact) throws IOException {
      dataOut.writeInt(artifact.getArtId());
      dataOut.writeInt(artifact.getBranch().getBranchId());
   }
}