/*
* generated by Xtext
*/
grammar InternalOseeTypes;

options {
	superClass=AbstractInternalAntlrParser;
	
}

@lexer::header {
package org.eclipse.osee.framework.parser.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;
}

@parser::header {
package org.eclipse.osee.framework.parser.antlr.internal; 

import java.io.InputStream;
import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.xtext.parsetree.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.osee.framework.services.OseeTypesGrammarAccess;

}

@parser::members {
 
 	private OseeTypesGrammarAccess grammarAccess;
 	
    public InternalOseeTypesParser(TokenStream input, IAstFactory factory, OseeTypesGrammarAccess grammarAccess) {
        this(input);
        this.factory = factory;
        registerRules(grammarAccess.getGrammar());
        this.grammarAccess = grammarAccess;
    }
    
    @Override
    protected InputStream getTokenFile() {
    	ClassLoader classLoader = getClass().getClassLoader();
    	return classLoader.getResourceAsStream("org/eclipse/osee/framework/parser/antlr/internal/InternalOseeTypes.tokens");
    }
    
    @Override
    protected String getFirstRuleName() {
    	return "Model";	
   	} 
}

@rulecatch { 
    catch (RecognitionException re) { 
        recover(input,re); 
        appendSkippedTokens();
    } 
}




// Entry rule entryRuleModel
entryRuleModel returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getModelRule(), currentNode); }
	 iv_ruleModel=ruleModel 
	 { $current=$iv_ruleModel.current; } 
	 EOF 
;

// Rule Model
ruleModel returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
((	
	
	    
	    { 
	        currentNode=createCompositeNode(grammarAccess.getModelAccess().getImportsImportParserRuleCall_0_0(), currentNode); 
	    }
	    lv_imports_0=ruleImport 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getModelRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode.getParent(), $current);
	        }
	        
	        try {
	       		add($current, "imports", lv_imports_0, "Import", currentNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	        currentNode = currentNode.getParent();
	    }
	
)*(	
	
	    
	    { 
	        currentNode=createCompositeNode(grammarAccess.getModelAccess().getElementsTypeParserRuleCall_1_0(), currentNode); 
	    }
	    lv_elements_1=ruleType 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getModelRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode.getParent(), $current);
	        }
	        
	        try {
	       		add($current, "elements", lv_elements_1, "Type", currentNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	        currentNode = currentNode.getParent();
	    }
	
)*);





// Entry rule entryRuleImport
entryRuleImport returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getImportRule(), currentNode); }
	 iv_ruleImport=ruleImport 
	 { $current=$iv_ruleImport.current; } 
	 EOF 
;

// Rule Import
ruleImport returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
('import' 
    {
        createLeafNode(grammarAccess.getImportAccess().getImportKeyword_0(), null); 
    }
(	
	
	    lv_importURI_1=	RULE_STRING
	{
		createLeafNode(grammarAccess.getImportAccess().getImportURISTRINGTerminalRuleCall_1_0(), "importURI"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getImportRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "importURI", lv_importURI_1, "STRING", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
));





// Entry rule entryRuleType
entryRuleType returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getTypeRule(), currentNode); }
	 iv_ruleType=ruleType 
	 { $current=$iv_ruleType.current; } 
	 EOF 
;

// Rule Type
ruleType returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
(
    { 
        currentNode=createCompositeNode(grammarAccess.getTypeAccess().getArtifactTypeParserRuleCall_0(), currentNode); 
    }
    this_ArtifactType_0=ruleArtifactType
    { 
        $current = $this_ArtifactType_0.current; 
        currentNode = currentNode.getParent();
    }

    |
    { 
        currentNode=createCompositeNode(grammarAccess.getTypeAccess().getRelationTypeParserRuleCall_1(), currentNode); 
    }
    this_RelationType_1=ruleRelationType
    { 
        $current = $this_RelationType_1.current; 
        currentNode = currentNode.getParent();
    }

    |
    { 
        currentNode=createCompositeNode(grammarAccess.getTypeAccess().getAttributeTypeParserRuleCall_2(), currentNode); 
    }
    this_AttributeType_2=ruleAttributeType
    { 
        $current = $this_AttributeType_2.current; 
        currentNode = currentNode.getParent();
    }
);





// Entry rule entryRuleArtifactType
entryRuleArtifactType returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getArtifactTypeRule(), currentNode); }
	 iv_ruleArtifactType=ruleArtifactType 
	 { $current=$iv_ruleArtifactType.current; } 
	 EOF 
;

// Rule ArtifactType
ruleArtifactType returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
('artifactType' 
    {
        createLeafNode(grammarAccess.getArtifactTypeAccess().getArtifactTypeKeyword_0(), null); 
    }
(	
	
	    lv_name_1=	RULE_ID
	{
		createLeafNode(grammarAccess.getArtifactTypeAccess().getNameIDTerminalRuleCall_1_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getArtifactTypeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_1, "ID", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
)('extends' 
    {
        createLeafNode(grammarAccess.getArtifactTypeAccess().getExtendsKeyword_2_0(), null); 
    }
(	
	
		
		{
			if ($current==null) {
	            $current = factory.create(grammarAccess.getArtifactTypeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
        }
	RULE_ID
	{
		createLeafNode(grammarAccess.getArtifactTypeAccess().getSuperEntityArtifactTypeCrossReference_2_1_0(), "superEntity"); 
	}

		// TODO assign feature to currentNode
	
))?'{' 
    {
        createLeafNode(grammarAccess.getArtifactTypeAccess().getLeftCurlyBracketKeyword_3(), null); 
    }
(	
	
	    
	    { 
	        currentNode=createCompositeNode(grammarAccess.getArtifactTypeAccess().getAttributesXRefParserRuleCall_4_0(), currentNode); 
	    }
	    lv_attributes_5=ruleXRef 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getArtifactTypeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode.getParent(), $current);
	        }
	        
	        try {
	       		add($current, "attributes", lv_attributes_5, "XRef", currentNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	        currentNode = currentNode.getParent();
	    }
	
)*'}' 
    {
        createLeafNode(grammarAccess.getArtifactTypeAccess().getRightCurlyBracketKeyword_5(), null); 
    }
);





// Entry rule entryRuleXRef
entryRuleXRef returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getXRefRule(), currentNode); }
	 iv_ruleXRef=ruleXRef 
	 { $current=$iv_ruleXRef.current; } 
	 EOF 
;

// Rule XRef
ruleXRef returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
(
    { 
        currentNode=createCompositeNode(grammarAccess.getXRefAccess().getRelationTypeRefParserRuleCall_0(), currentNode); 
    }
    this_RelationTypeRef_0=ruleRelationTypeRef
    { 
        $current = $this_RelationTypeRef_0.current; 
        currentNode = currentNode.getParent();
    }

    |
    { 
        currentNode=createCompositeNode(grammarAccess.getXRefAccess().getAttributeTypeRefParserRuleCall_1(), currentNode); 
    }
    this_AttributeTypeRef_1=ruleAttributeTypeRef
    { 
        $current = $this_AttributeTypeRef_1.current; 
        currentNode = currentNode.getParent();
    }
);





// Entry rule entryRuleRelationTypeRef
entryRuleRelationTypeRef returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getRelationTypeRefRule(), currentNode); }
	 iv_ruleRelationTypeRef=ruleRelationTypeRef 
	 { $current=$iv_ruleRelationTypeRef.current; } 
	 EOF 
;

// Rule RelationTypeRef
ruleRelationTypeRef returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
('relation' 
    {
        createLeafNode(grammarAccess.getRelationTypeRefAccess().getRelationKeyword_0(), null); 
    }
(	
	
		
		{
			if ($current==null) {
	            $current = factory.create(grammarAccess.getRelationTypeRefRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
        }
	RULE_ID
	{
		createLeafNode(grammarAccess.getRelationTypeRefAccess().getTypeRelationTypeCrossReference_1_0(), "type"); 
	}

		// TODO assign feature to currentNode
	
));





// Entry rule entryRuleAttributeTypeRef
entryRuleAttributeTypeRef returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getAttributeTypeRefRule(), currentNode); }
	 iv_ruleAttributeTypeRef=ruleAttributeTypeRef 
	 { $current=$iv_ruleAttributeTypeRef.current; } 
	 EOF 
;

// Rule AttributeTypeRef
ruleAttributeTypeRef returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
('attribute' 
    {
        createLeafNode(grammarAccess.getAttributeTypeRefAccess().getAttributeKeyword_0(), null); 
    }
(	
	
		
		{
			if ($current==null) {
	            $current = factory.create(grammarAccess.getAttributeTypeRefRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
        }
	RULE_ID
	{
		createLeafNode(grammarAccess.getAttributeTypeRefAccess().getTypeAttributeTypeCrossReference_1_0(), "type"); 
	}

		// TODO assign feature to currentNode
	
));





// Entry rule entryRuleAttributeType
entryRuleAttributeType returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getAttributeTypeRule(), currentNode); }
	 iv_ruleAttributeType=ruleAttributeType 
	 { $current=$iv_ruleAttributeType.current; } 
	 EOF 
;

// Rule AttributeType
ruleAttributeType returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
('attributeType' 
    {
        createLeafNode(grammarAccess.getAttributeTypeAccess().getAttributeTypeKeyword_0(), null); 
    }
(	
	
	    lv_name_1=	RULE_ID
	{
		createLeafNode(grammarAccess.getAttributeTypeAccess().getNameIDTerminalRuleCall_1_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getAttributeTypeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_1, "ID", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
)('extends' 
    {
        createLeafNode(grammarAccess.getAttributeTypeAccess().getExtendsKeyword_2_0(), null); 
    }
(	
	
		
		{
			if ($current==null) {
	            $current = factory.create(grammarAccess.getAttributeTypeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
        }
	RULE_ID
	{
		createLeafNode(grammarAccess.getAttributeTypeAccess().getSuperEntityAttributeTypeCrossReference_2_1_0(), "superEntity"); 
	}

		// TODO assign feature to currentNode
	
))?'{' 
    {
        createLeafNode(grammarAccess.getAttributeTypeAccess().getLeftCurlyBracketKeyword_3(), null); 
    }
(	
	
	    
	    { 
	        currentNode=createCompositeNode(grammarAccess.getAttributeTypeAccess().getAttributesXAttributeParserRuleCall_4_0(), currentNode); 
	    }
	    lv_attributes_5=ruleXAttribute 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getAttributeTypeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode.getParent(), $current);
	        }
	        
	        try {
	       		add($current, "attributes", lv_attributes_5, "XAttribute", currentNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	        currentNode = currentNode.getParent();
	    }
	
)'}' 
    {
        createLeafNode(grammarAccess.getAttributeTypeAccess().getRightCurlyBracketKeyword_5(), null); 
    }
);





// Entry rule entryRuleXAttribute
entryRuleXAttribute returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getXAttributeRule(), currentNode); }
	 iv_ruleXAttribute=ruleXAttribute 
	 { $current=$iv_ruleXAttribute.current; } 
	 EOF 
;

// Rule XAttribute
ruleXAttribute returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
('dataProvider' 
    {
        createLeafNode(grammarAccess.getXAttributeAccess().getDataProviderKeyword_0(), null); 
    }
(	
	
	    lv_name_1=	RULE_ID
	{
		createLeafNode(grammarAccess.getXAttributeAccess().getNameIDTerminalRuleCall_1_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getXAttributeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_1, "ID", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
)'taggerId' 
    {
        createLeafNode(grammarAccess.getXAttributeAccess().getTaggerIdKeyword_2(), null); 
    }
(	
	
	    lv_name_3=	RULE_ID
	{
		createLeafNode(grammarAccess.getXAttributeAccess().getNameIDTerminalRuleCall_3_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getXAttributeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_3, "ID", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
)('defaultValue' 
    {
        createLeafNode(grammarAccess.getXAttributeAccess().getDefaultValueKeyword_4_0(), null); 
    }
(	
	
	    lv_name_5=	RULE_STRING
	{
		createLeafNode(grammarAccess.getXAttributeAccess().getNameSTRINGTerminalRuleCall_4_1_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getXAttributeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_5, "STRING", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
))?);





// Entry rule entryRuleRelationType
entryRuleRelationType returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getRelationTypeRule(), currentNode); }
	 iv_ruleRelationType=ruleRelationType 
	 { $current=$iv_ruleRelationType.current; } 
	 EOF 
;

// Rule RelationType
ruleRelationType returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
('relationType' 
    {
        createLeafNode(grammarAccess.getRelationTypeAccess().getRelationTypeKeyword_0(), null); 
    }
(	
	
	    lv_name_1=	RULE_ID
	{
		createLeafNode(grammarAccess.getRelationTypeAccess().getNameIDTerminalRuleCall_1_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getRelationTypeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_1, "ID", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
)'{' 
    {
        createLeafNode(grammarAccess.getRelationTypeAccess().getLeftCurlyBracketKeyword_2(), null); 
    }
(	
	
	    
	    { 
	        currentNode=createCompositeNode(grammarAccess.getRelationTypeAccess().getAttributesXRelationParserRuleCall_3_0(), currentNode); 
	    }
	    lv_attributes_3=ruleXRelation 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getRelationTypeRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode.getParent(), $current);
	        }
	        
	        try {
	       		add($current, "attributes", lv_attributes_3, "XRelation", currentNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	        currentNode = currentNode.getParent();
	    }
	
)'}' 
    {
        createLeafNode(grammarAccess.getRelationTypeAccess().getRightCurlyBracketKeyword_4(), null); 
    }
);





// Entry rule entryRuleXRelation
entryRuleXRelation returns [EObject current=null] :
	{ currentNode = createCompositeNode(grammarAccess.getXRelationRule(), currentNode); }
	 iv_ruleXRelation=ruleXRelation 
	 { $current=$iv_ruleXRelation.current; } 
	 EOF 
;

// Rule XRelation
ruleXRelation returns [EObject current=null] 
    @init { EObject temp=null; setCurrentLookahead(); resetLookahead(); 
    }
    @after { resetLookahead(); 
    	lastConsumedNode = currentNode;
    }:
('sideAName' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getSideANameKeyword_0(), null); 
    }
(	
	
	    lv_name_1=	RULE_STRING
	{
		createLeafNode(grammarAccess.getXRelationAccess().getNameSTRINGTerminalRuleCall_1_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getXRelationRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_1, "STRING", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
)'sideAArtifactType' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getSideAArtifactTypeKeyword_2(), null); 
    }
(	
	
		
		{
			if ($current==null) {
	            $current = factory.create(grammarAccess.getXRelationRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
        }
	RULE_ID
	{
		createLeafNode(grammarAccess.getXRelationAccess().getTypeArtifactTypeCrossReference_3_0(), "type"); 
	}

		// TODO assign feature to currentNode
	
)'sideBName' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getSideBNameKeyword_4(), null); 
    }
(	
	
	    lv_name_5=	RULE_STRING
	{
		createLeafNode(grammarAccess.getXRelationAccess().getNameSTRINGTerminalRuleCall_5_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getXRelationRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_5, "STRING", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
)'sideBArtifactType' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getSideBArtifactTypeKeyword_6(), null); 
    }
(	
	
		
		{
			if ($current==null) {
	            $current = factory.create(grammarAccess.getXRelationRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
        }
	RULE_ID
	{
		createLeafNode(grammarAccess.getXRelationAccess().getTypeArtifactTypeCrossReference_7_0(), "type"); 
	}

		// TODO assign feature to currentNode
	
)'defaultOrderType' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getDefaultOrderTypeKeyword_8(), null); 
    }
(	
	
	    lv_name_9=	RULE_STRING
	{
		createLeafNode(grammarAccess.getXRelationAccess().getNameSTRINGTerminalRuleCall_9_0(), "name"); 
	}
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getXRelationRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", lv_name_9, "STRING", lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
)'multiplicity' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getMultiplicityKeyword_10(), null); 
    }
(	
	
	    lv_name_11=('one-to-many' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getNameOneToManyKeyword_11_0_0(), "name"); 
    }


    |'many-to-many' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getNameManyToManyKeyword_11_0_1(), "name"); 
    }


    |'many-to-one' 
    {
        createLeafNode(grammarAccess.getXRelationAccess().getNameManyToOneKeyword_11_0_2(), "name"); 
    }

)
 
	    {
	        if ($current==null) {
	            $current = factory.create(grammarAccess.getXRelationRule().getType().getClassifier());
	            associateNodeWithAstElement(currentNode, $current);
	        }
	        
	        try {
	       		set($current, "name", /* lv_name_11 */ input.LT(-1), null, lastConsumedNode);
	        } catch (ValueConverterException vce) {
				handleValueConverterException(vce);
	        }
	    }
	
));





RULE_ID : '^'? ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

RULE_INT : ('0'..'9')+;

RULE_STRING : ('"' ('\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')|~(('\\'|'"')))* '"'|'\'' ('\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')|~(('\\'|'\'')))* '\'');

RULE_ML_COMMENT : '/*' ( options {greedy=false;} : . )*'*/';

RULE_SL_COMMENT : '//' ~(('\n'|'\r'))* ('\r'? '\n')?;

RULE_WS : (' '|'\t'|'\r'|'\n')+;

RULE_ANY_OTHER : .;


