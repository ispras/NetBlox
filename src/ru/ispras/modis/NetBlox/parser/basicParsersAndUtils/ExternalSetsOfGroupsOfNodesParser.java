package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementWithChildrenProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;

/**
 * Parses the lists of names/paths of external sets of groups of nodes (<external/> and
 * <externalForMining/> tags of a <graph/>).
 * 
 * @author ilya
 */
public class ExternalSetsOfGroupsOfNodesParser extends XMLElementWithChildrenProcessor {
	private static final String TAG_SET_OF_GROUPS_OF_NODES = "setOfGroupsFile";

	private final SetOfGroupsOfNodesFileNameParser externalSetProcessor;

	private static final String ATTRIBUTE_VARIATION_ID = XMLRangedStringValueProcessor.ATTRIBUTE_VARIATION_ID;

	private RangeOfValues<String> externalSetsOfGroupsOfNodesFilesNames = null;


	public ExternalSetsOfGroupsOfNodesParser()	{
		super();
		addTaggedParser(TAG_SET_OF_GROUPS_OF_NODES, externalSetProcessor = new SetOfGroupsOfNodesFileNameParser());
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		String variationIdString = attributes.getValue(ATTRIBUTE_VARIATION_ID);
		if (variationIdString == null)	{
			throw new ScenarioException("There must be a variation ID specified for the collection of external sets of groups of nodes.");
		}
		Utils.checkWhetherIsWordInScenario(variationIdString, ATTRIBUTE_VARIATION_ID, tagName);

		externalSetsOfGroupsOfNodesFilesNames = new RangeOfValues<String>(variationIdString, tagName);
		externalSetProcessor.setStorage(externalSetsOfGroupsOfNodesFilesNames);
	}


	public RangeOfValues<String> getSetsOfGroupsFilenames()	{
		return externalSetsOfGroupsOfNodesFilesNames;
	}
}



/**
 * Parses the names of external communities (community covers files) provided for the graph.
 * 
 * @author ilya
 */
class SetOfGroupsOfNodesFileNameParser extends XMLStringValueProcessor	{
	private RangeOfValues<String> setsOfGroupsOfNodesFilesNames;

	public void setStorage(RangeOfValues<String> storage)	{
		setsOfGroupsOfNodesFilesNames = storage;
	}

	@Override
	public void closeElement()	{
		super.closeElement();
		setsOfGroupsOfNodesFilesNames.addValue(getText());
	}
}
