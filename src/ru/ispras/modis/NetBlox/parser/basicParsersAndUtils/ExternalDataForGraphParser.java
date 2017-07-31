package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementWithChildrenProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.utils.MiningJobBase;
import ru.ispras.modis.NetBlox.utils.MiningJobBase.JobBase;

/**
 * Parses the lists of names/paths of external sets of groups of nodes, graph substructures, etc.
 * (<external/> and <externalForMining/> tags of a <graph/>).
 * 
 * @author ilya
 */
public class ExternalDataForGraphParser extends XMLElementWithChildrenProcessor {
	private static final String TAG_DATA_FILE = "dataFile";

	private final DataFileNameParser externalDataProcessor;

	private static final String ATTRIBUTE_VARIATION_ID = XMLRangedStringValueProcessor.ATTRIBUTE_VARIATION_ID;

	private static final String ATTRIBUTE_DATA_TYPE = "type";
	private static final String ATTRIBUTE_TYPE_GROUPS = "setsOfGroups";
	private static final String ATTRIBUTE_TYPE_GRAPH = "graphs";
	private static final String ATTRIBUTE_TYPE_PACK_OF_GRAPHS = "packOfGraphs";

	private RangeOfValues<String> externalDataFilesNames = null;
	private MiningJobBase.JobBase externalDataType = null;


	public ExternalDataForGraphParser()	{
		super();
		addTaggedParser(TAG_DATA_FILE, externalDataProcessor = new DataFileNameParser());
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

		String dataTypeString = attributes.getValue(ATTRIBUTE_DATA_TYPE);
		if (dataTypeString==null || dataTypeString.isEmpty() || dataTypeString.equalsIgnoreCase(ATTRIBUTE_TYPE_GROUPS))	{
			externalDataType = JobBase.NODES_GROUPS_SET;
		}
		else if (dataTypeString.equalsIgnoreCase(ATTRIBUTE_TYPE_GRAPH))	{
			externalDataType = JobBase.GRAPH;
		}
		else if (dataTypeString.equalsIgnoreCase(ATTRIBUTE_TYPE_PACK_OF_GRAPHS))	{
			externalDataType = JobBase.MULTIPLE_GRAPHS;
		}
		else	{
			System.out.println("WARNING: Unknown external data type. Nodes groups will be supposed by default.");
			externalDataType = JobBase.NODES_GROUPS_SET;
		}

		externalDataFilesNames = new RangeOfValues<String>(variationIdString, tagName);
		externalDataProcessor.setStorage(externalDataFilesNames);
	}


	public RangeOfValues<String> getExternalFilenames()	{
		return externalDataFilesNames;
	}
	public MiningJobBase.JobBase getExternalDataType()	{
		return externalDataType;
	}
}



/**
 * Parses the names of external data (community covers, graph substructures, etc.) files provided for the graph.
 * 
 * @author ilya
 */
class DataFileNameParser extends XMLStringValueProcessor	{
	private RangeOfValues<String> dataFilesNames;

	public void setStorage(RangeOfValues<String> storage)	{
		dataFilesNames = storage;
	}

	@Override
	public void closeElement()	{
		super.closeElement();
		dataFilesNames.addValue(getText());
	}
}
