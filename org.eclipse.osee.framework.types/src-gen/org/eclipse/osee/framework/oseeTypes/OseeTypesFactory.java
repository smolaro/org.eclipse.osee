/**
 * <copyright>
 * </copyright>
 *

 */
package org.eclipse.osee.framework.oseeTypes;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.osee.framework.oseeTypes.OseeTypesPackage
 * @generated
 */
public interface OseeTypesFactory extends EFactory
{
  /**
   * The singleton instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  OseeTypesFactory eINSTANCE = org.eclipse.osee.framework.oseeTypes.impl.OseeTypesFactoryImpl.init();

  /**
   * Returns a new object of class '<em>Model</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Model</em>'.
   * @generated
   */
  Model createModel();

  /**
   * Returns a new object of class '<em>Import</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Import</em>'.
   * @generated
   */
  Import createImport();

  /**
   * Returns a new object of class '<em>Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Type</em>'.
   * @generated
   */
  Type createType();

  /**
   * Returns a new object of class '<em>Artifact Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Artifact Type</em>'.
   * @generated
   */
  ArtifactType createArtifactType();

  /**
   * Returns a new object of class '<em>XRef</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>XRef</em>'.
   * @generated
   */
  XRef createXRef();

  /**
   * Returns a new object of class '<em>Relation Type Ref</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Relation Type Ref</em>'.
   * @generated
   */
  RelationTypeRef createRelationTypeRef();

  /**
   * Returns a new object of class '<em>Attribute Type Ref</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Attribute Type Ref</em>'.
   * @generated
   */
  AttributeTypeRef createAttributeTypeRef();

  /**
   * Returns a new object of class '<em>Attribute Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Attribute Type</em>'.
   * @generated
   */
  AttributeType createAttributeType();

  /**
   * Returns a new object of class '<em>XAttribute</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>XAttribute</em>'.
   * @generated
   */
  XAttribute createXAttribute();

  /**
   * Returns a new object of class '<em>Relation Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Relation Type</em>'.
   * @generated
   */
  RelationType createRelationType();

  /**
   * Returns a new object of class '<em>XRelation</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>XRelation</em>'.
   * @generated
   */
  XRelation createXRelation();

  /**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
  OseeTypesPackage getOseeTypesPackage();

} //OseeTypesFactory
