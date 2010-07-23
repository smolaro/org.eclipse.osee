//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.07.23 at 07:00:27 AM MST 
//

package org.eclipse.osee.framework.messaging.event.res.msgs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.osee.framework.messaging.event.res.RemoteEvent;

/**
 * <p>
 * Java class for RemoteBasicGuidRelationReorder1 complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RemoteBasicGuidRelationReorder1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modTypeGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="branchGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="relTypeGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parentArt" type="{}RemoteBasicGuidArtifact1"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoteBasicGuidRelationReorder1", propOrder = {"modTypeGuid", "branchGuid", "relTypeGuid", "parentArt"})
public class RemoteBasicGuidRelationReorder1 extends RemoteEvent {

   @XmlElement(required = true)
   protected String modTypeGuid;
   @XmlElement(required = true)
   protected String branchGuid;
   @XmlElement(required = true)
   protected String relTypeGuid;
   @XmlElement(required = true)
   protected RemoteBasicGuidArtifact1 parentArt;

   /**
    * Gets the value of the modTypeGuid property.
    * 
    * @return possible object is {@link String }
    */
   public String getModTypeGuid() {
      return modTypeGuid;
   }

   /**
    * Sets the value of the modTypeGuid property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setModTypeGuid(String value) {
      this.modTypeGuid = value;
   }

   /**
    * Gets the value of the branchGuid property.
    * 
    * @return possible object is {@link String }
    */
   public String getBranchGuid() {
      return branchGuid;
   }

   /**
    * Sets the value of the branchGuid property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setBranchGuid(String value) {
      this.branchGuid = value;
   }

   /**
    * Gets the value of the relTypeGuid property.
    * 
    * @return possible object is {@link String }
    */
   public String getRelTypeGuid() {
      return relTypeGuid;
   }

   /**
    * Sets the value of the relTypeGuid property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setRelTypeGuid(String value) {
      this.relTypeGuid = value;
   }

   /**
    * Gets the value of the parentArt property.
    * 
    * @return possible object is {@link RemoteBasicGuidArtifact1 }
    */
   public RemoteBasicGuidArtifact1 getParentArt() {
      return parentArt;
   }

   /**
    * Sets the value of the parentArt property.
    * 
    * @param value allowed object is {@link RemoteBasicGuidArtifact1 }
    */
   public void setParentArt(RemoteBasicGuidArtifact1 value) {
      this.parentArt = value;
   }

}
