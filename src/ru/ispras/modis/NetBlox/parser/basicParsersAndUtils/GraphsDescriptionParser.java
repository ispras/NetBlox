package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.parser.DefaultDescriptionElementProcessor;
import ru.ispras.modis.NetBlox.parser.GraphsSectionParser;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.DescriptionGraphsOneType;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;

/**
 * This class is proposed as the base class for graphs sections elements (<graph/>s) parsers.
 * Plug-in developers are free to use it or write their parsers from scratch (they must extend
 * <code>XMLElementProcessor</code> from <u>modis-lib</u> and implement <code>IDescriptionElementProcessor</code>.
 * 
 * @author ilya
 */
public abstract class GraphsDescriptionParser extends DefaultDescriptionElementProcessor {
	class LaunchesProcessor extends XMLIntegerRangeStringProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();

			RangeOfValues<Integer> launchNumbers = getValues();
			if (launchNumbers != null  &&  !launchNumbers.isEmpty())	{
				graphsDescription.setLaunchNumbers(launchNumbers);
			}
		}
	}


	private static final String TAG_REFERENCE = "reference";
	private static final String TAG_EXTERNAL_FOR_MINING = "externalForMining";
	private static final String TAG_EXTERNAL_COVERS = "external";
	private static final String TAG_ATTRIBUTES = "attributes";
	private static final String TAG_GENERATION_NUMBERS = "generationNumbers";

	private final XMLStringValueProcessor referenceCoverFileNameProcessor;
	private final ExternalSetsOfGroupsOfNodesParser externalForMiningProcessor;
	private final ExternalSetsOfGroupsOfNodesParser externalCoversProcessor;
	private final XMLStringValueProcessor attributesFileNameProcessor;

	protected DescriptionGraphsOneType graphsDescription;


	public GraphsDescriptionParser() {
		super();

		addTaggedParser(TAG_REFERENCE, referenceCoverFileNameProcessor = new XMLStringValueProcessor());
		addTaggedParser(TAG_EXTERNAL_FOR_MINING, externalForMiningProcessor = new ExternalSetsOfGroupsOfNodesParser());
		addTaggedParser(TAG_EXTERNAL_COVERS, externalCoversProcessor = new ExternalSetsOfGroupsOfNodesParser());
		addTaggedParser(TAG_ATTRIBUTES, attributesFileNameProcessor = new XMLStringValueProcessor());
		addTaggedParser(TAG_GENERATION_NUMBERS, new LaunchesProcessor());
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		String id = Utils.getId(attributes, tagName, GraphsSectionParser.ATTRIBUTE_GRAPH_ID);
		boolean directed = getBooleanAttribute(attributes, GraphsSectionParser.ATTRIBUTE_GRAPH_DIRECTED);
		boolean weighted = getBooleanAttribute(attributes, GraphsSectionParser.ATTRIBUTE_GRAPH_WEIGHTED);

		graphsDescription = createGraphsDescription();

		graphsDescription.setId(id);
		graphsDescription.setDirectedWeighted(directed, weighted);
	}


	@Override
	public void closeElement()	{
		super.closeElement();

		graphsDescription.setReferenceCommunitiesRelativeFileName(referenceCoverFileNameProcessor.getText());

		String attributesFileName = attributesFileNameProcessor.getText();
		if (attributesFileName != null  &&  !attributesFileName.isEmpty())	{
			graphsDescription.setAttributesFileName(attributesFileName);
		}

		RangeOfValues<String> setsOfGroupsFilenames = externalForMiningProcessor.getSetsOfGroupsFilenames();
		if (setsOfGroupsFilenames != null)	{
			graphsDescription.setExternalForMiningFiles(setsOfGroupsFilenames);
		}
		setsOfGroupsFilenames = externalCoversProcessor.getSetsOfGroupsFilenames();
		if (setsOfGroupsFilenames != null)	{
			graphsDescription.setExternalCoversFiles(setsOfGroupsFilenames);
		}
	}


	protected abstract DescriptionGraphsOneType createGraphsDescription();


	public AlgorithmDescription getParsedDescription()	{
		return graphsDescription;
	}
}
