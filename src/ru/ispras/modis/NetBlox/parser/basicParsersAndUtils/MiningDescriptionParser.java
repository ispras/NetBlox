package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.parser.DefaultDescriptionElementProcessor;
import ru.ispras.modis.NetBlox.parser.GraphMiningSectionParser;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.DescriptionGraphMiningAlgorithm;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;

/**
 * The common class for the parsers of graph mining algorithm descriptions of all types in the scenario file.
 * 
 * @author ilya
 */
public abstract class MiningDescriptionParser extends DefaultDescriptionElementProcessor {
	class SupplementaryAlgosIdsProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			String stringOfIds = getText();
			String[] ids = stringOfIds.split(Utils.DELIMITER);
			for (String stringId : ids)	{
				Utils.checkWhetherIsWordInScenario(stringId, TAG_SUPPLEMENTARY_ALGOS_IDS, thisTag);

				currentMinerDescription.addSupplementaryAlgorithmId(stringId);
			}
		}
	}

	class LaunchesProcessor extends XMLIntegerRangeStringProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();

			RangeOfValues<Integer> launchNumbers = getValues();
			if (launchNumbers != null  &&  !launchNumbers.isEmpty())	{
				currentMinerDescription.setLaunchNumbers(launchNumbers);
			}
		}
	}


	private static final String TAG_SUPPLEMENTARY_ALGOS_IDS = "supplementaryAlgosIds";
	private static final String TAG_LAUNCH_NUMBERS = "launchNumbers";

	private DescriptionGraphMiningAlgorithm currentMinerDescription;
	private String thisTag;


	public MiningDescriptionParser()	{
		super();

		addTaggedParser(TAG_SUPPLEMENTARY_ALGOS_IDS, new SupplementaryAlgosIdsProcessor());
		addTaggedParser(TAG_LAUNCH_NUMBERS, new LaunchesProcessor());
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);
		thisTag = tagName;

		String id = Utils.getId(attributes, tagName, GraphMiningSectionParser.ATTRIBUTE_ALGORITHM_ID);

		currentMinerDescription = createMinerDescription();
		currentMinerDescription.setId(id);
	}

	protected abstract DescriptionGraphMiningAlgorithm createMinerDescription();


	@Override
	public AlgorithmDescription getParsedDescription() {
		return currentMinerDescription;
	}
}
