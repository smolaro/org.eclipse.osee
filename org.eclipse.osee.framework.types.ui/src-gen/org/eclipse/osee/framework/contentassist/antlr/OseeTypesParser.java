/*
* generated by Xtext
*/
package org.eclipse.osee.framework.contentassist.antlr;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.ui.common.editor.contentassist.antlr.AbstractContentAssistParser;
import org.eclipse.xtext.ui.common.editor.contentassist.antlr.FollowElement;
import org.eclipse.xtext.ui.common.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;

import com.google.inject.Inject;

import org.eclipse.osee.framework.services.OseeTypesGrammarAccess;

public class OseeTypesParser extends AbstractContentAssistParser {
	
	@Inject
	private OseeTypesGrammarAccess grammarAccess;
	
	private Map<AbstractElement, String> nameMappings;
	
	@Override
	protected org.eclipse.osee.framework.contentassist.antlr.internal.InternalOseeTypesLexer createLexer(CharStream stream) {
		return new org.eclipse.osee.framework.contentassist.antlr.internal.InternalOseeTypesLexer(stream);
	}
	
	@Override
	protected org.eclipse.osee.framework.contentassist.antlr.internal.InternalOseeTypesParser createParser() {
		org.eclipse.osee.framework.contentassist.antlr.internal.InternalOseeTypesParser result = new org.eclipse.osee.framework.contentassist.antlr.internal.InternalOseeTypesParser(null);
		result.setGrammarAccess(grammarAccess);
		return result;
	}
	
	@Override
	protected String getRuleName(AbstractElement element) {
		if (nameMappings == null) {
			nameMappings = new HashMap<AbstractElement, String>() {
				{
					put(grammarAccess.getDIGITSAccess().getAlternatives(), "rule__DIGITS__Alternatives");
					put(grammarAccess.getOseeTypeAccess().getAlternatives(), "rule__OseeType__Alternatives");
					put(grammarAccess.getAttributeTypeAccess().getDataProviderAlternatives_5_0(), "rule__AttributeType__DataProviderAlternatives_5_0");
					put(grammarAccess.getAttributeTypeAccess().getMaxAlternatives_9_0(), "rule__AttributeType__MaxAlternatives_9_0");
					put(grammarAccess.getAttributeTypeAccess().getTaggerIdAlternatives_10_1_0(), "rule__AttributeType__TaggerIdAlternatives_10_1_0");
					put(grammarAccess.getAttributeBaseTypeAccess().getAlternatives(), "rule__AttributeBaseType__Alternatives");
					put(grammarAccess.getRelationTypeAccess().getDefaultOrderTypeAlternatives_12_0(), "rule__RelationType__DefaultOrderTypeAlternatives_12_0");
					put(grammarAccess.getRelationTypeAccess().getMultiplicityAlternatives_14_0(), "rule__RelationType__MultiplicityAlternatives_14_0");
					put(grammarAccess.getModelAccess().getGroup(), "rule__Model__Group__0");
					put(grammarAccess.getImportAccess().getGroup(), "rule__Import__Group__0");
					put(grammarAccess.getQUALIFIED_NAMEAccess().getGroup(), "rule__QUALIFIED_NAME__Group__0");
					put(grammarAccess.getQUALIFIED_NAMEAccess().getGroup_1(), "rule__QUALIFIED_NAME__Group_1__0");
					put(grammarAccess.getArtifactTypeAccess().getGroup(), "rule__ArtifactType__Group__0");
					put(grammarAccess.getArtifactTypeAccess().getGroup_3(), "rule__ArtifactType__Group_3__0");
					put(grammarAccess.getAttributeTypeRefAccess().getGroup(), "rule__AttributeTypeRef__Group__0");
					put(grammarAccess.getAttributeTypeAccess().getGroup(), "rule__AttributeType__Group__0");
					put(grammarAccess.getAttributeTypeAccess().getGroup_2(), "rule__AttributeType__Group_2__0");
					put(grammarAccess.getAttributeTypeAccess().getGroup_10(), "rule__AttributeType__Group_10__0");
					put(grammarAccess.getAttributeTypeAccess().getGroup_11(), "rule__AttributeType__Group_11__0");
					put(grammarAccess.getAttributeTypeAccess().getGroup_12(), "rule__AttributeType__Group_12__0");
					put(grammarAccess.getAttributeTypeAccess().getGroup_13(), "rule__AttributeType__Group_13__0");
					put(grammarAccess.getAttributeTypeAccess().getGroup_14(), "rule__AttributeType__Group_14__0");
					put(grammarAccess.getOseeEnumTypeAccess().getGroup(), "rule__OseeEnumType__Group__0");
					put(grammarAccess.getOseeEnumAccess().getGroup(), "rule__OseeEnum__Group__0");
					put(grammarAccess.getRelationTypeAccess().getGroup(), "rule__RelationType__Group__0");
					put(grammarAccess.getModelAccess().getImportsAssignment_0(), "rule__Model__ImportsAssignment_0");
					put(grammarAccess.getModelAccess().getTypesAssignment_1(), "rule__Model__TypesAssignment_1");
					put(grammarAccess.getImportAccess().getImportURIAssignment_1(), "rule__Import__ImportURIAssignment_1");
					put(grammarAccess.getArtifactTypeAccess().getNameAssignment_2(), "rule__ArtifactType__NameAssignment_2");
					put(grammarAccess.getArtifactTypeAccess().getSuperArtifactTypeAssignment_3_1(), "rule__ArtifactType__SuperArtifactTypeAssignment_3_1");
					put(grammarAccess.getArtifactTypeAccess().getValidTypesAssignment_5(), "rule__ArtifactType__ValidTypesAssignment_5");
					put(grammarAccess.getAttributeTypeRefAccess().getValidAttributeTypeAssignment_1(), "rule__AttributeTypeRef__ValidAttributeTypeAssignment_1");
					put(grammarAccess.getAttributeTypeAccess().getNameAssignment_1(), "rule__AttributeType__NameAssignment_1");
					put(grammarAccess.getAttributeTypeAccess().getBaseAttributeTypeAssignment_2_1(), "rule__AttributeType__BaseAttributeTypeAssignment_2_1");
					put(grammarAccess.getAttributeTypeAccess().getDataProviderAssignment_5(), "rule__AttributeType__DataProviderAssignment_5");
					put(grammarAccess.getAttributeTypeAccess().getMinAssignment_7(), "rule__AttributeType__MinAssignment_7");
					put(grammarAccess.getAttributeTypeAccess().getMaxAssignment_9(), "rule__AttributeType__MaxAssignment_9");
					put(grammarAccess.getAttributeTypeAccess().getTaggerIdAssignment_10_1(), "rule__AttributeType__TaggerIdAssignment_10_1");
					put(grammarAccess.getAttributeTypeAccess().getEnumTypeAssignment_11_1(), "rule__AttributeType__EnumTypeAssignment_11_1");
					put(grammarAccess.getAttributeTypeAccess().getDescriptionAssignment_12_1(), "rule__AttributeType__DescriptionAssignment_12_1");
					put(grammarAccess.getAttributeTypeAccess().getDefaultValueAssignment_13_1(), "rule__AttributeType__DefaultValueAssignment_13_1");
					put(grammarAccess.getAttributeTypeAccess().getFileExtensionAssignment_14_1(), "rule__AttributeType__FileExtensionAssignment_14_1");
					put(grammarAccess.getOseeEnumTypeAccess().getNameAssignment_1(), "rule__OseeEnumType__NameAssignment_1");
					put(grammarAccess.getOseeEnumTypeAccess().getEnumsAssignment_3(), "rule__OseeEnumType__EnumsAssignment_3");
					put(grammarAccess.getOseeEnumAccess().getNameAssignment_0(), "rule__OseeEnum__NameAssignment_0");
					put(grammarAccess.getOseeEnumAccess().getOrdinalAssignment_1(), "rule__OseeEnum__OrdinalAssignment_1");
					put(grammarAccess.getRelationTypeAccess().getNameAssignment_1(), "rule__RelationType__NameAssignment_1");
					put(grammarAccess.getRelationTypeAccess().getSideANameAssignment_4(), "rule__RelationType__SideANameAssignment_4");
					put(grammarAccess.getRelationTypeAccess().getSideAArtifactTypeAssignment_6(), "rule__RelationType__SideAArtifactTypeAssignment_6");
					put(grammarAccess.getRelationTypeAccess().getSideBNameAssignment_8(), "rule__RelationType__SideBNameAssignment_8");
					put(grammarAccess.getRelationTypeAccess().getSideBArtifactTypeAssignment_10(), "rule__RelationType__SideBArtifactTypeAssignment_10");
					put(grammarAccess.getRelationTypeAccess().getDefaultOrderTypeAssignment_12(), "rule__RelationType__DefaultOrderTypeAssignment_12");
					put(grammarAccess.getRelationTypeAccess().getMultiplicityAssignment_14(), "rule__RelationType__MultiplicityAssignment_14");
				}
			};
		}
		return nameMappings.get(element);
	}
	
	@Override
	protected Collection<FollowElement> getFollowElements(AbstractInternalContentAssistParser parser) {
		try {
			org.eclipse.osee.framework.contentassist.antlr.internal.InternalOseeTypesParser typedParser = (org.eclipse.osee.framework.contentassist.antlr.internal.InternalOseeTypesParser) parser;
			typedParser.entryRuleModel();
			return typedParser.getFollowElements();
		} catch(RecognitionException ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	@Override
	protected String[] getInitialHiddenTokens() {
		return new String[] { "RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT" };
	}
	
	public OseeTypesGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}
	
	public void setGrammarAccess(OseeTypesGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}
