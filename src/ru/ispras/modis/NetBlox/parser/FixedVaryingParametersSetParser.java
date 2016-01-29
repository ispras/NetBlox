package ru.ispras.modis.NetBlox.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.basicParsersAndUtils.Utils;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementWithChildrenProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement;

public class FixedVaryingParametersSetParser extends XMLElementWithChildrenProcessor {
	class AverageByIdProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();

			String averageString = getText();
			if (averageString != null  &&  !averageString.isEmpty())	{
				String[] idsOfVariations = averageString.split(Utils.DELIMITER);

				currentVariationIdsToAverageAlong = new ArrayList<String>(idsOfVariations.length);
				for (String id : idsOfVariations)	{
					Utils.checkWhetherIsWordInScenario(id, TAG_AVERAGE, thisFixedSetTag);
					currentVariationIdsToAverageAlong.add(id);
				}
			}
		}
	}

	class AlgorithmIdProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			String algorithmIdString = getText();
			if (algorithmIdString != null  &&  !algorithmIdString.isEmpty())	{
				Utils.checkWhetherIsWordInScenario(algorithmIdString, TAG_ALGORITHM, thisFixedSetTag);
				miningAlgorithmId = algorithmIdString;
			}
		}
	}


	private static final String TAG_GRAPH = "graph";
	private static final String TAG_ALGORITHM = "algorithm";
	private static final String TAG_FIX = "fix";
	private static final String TAG_AVERAGE = "average";

	private final XMLStringValueProcessor graphIdProcessor;
	private final FixedValueParser fixedValueProcessor;

	private static final String ATTRIBUTE_LABEL = "label";
	private String setLabel;
	private static int noLabelCounter = 0;

	private DescriptionDataArrangement descriptionDataArrangement;
	private Map<String, String> currentValuesForIdsSet;	// Map<ID: String, value: String>

	String miningAlgorithmId = AlgorithmDescription.NO_ID;
	private Collection<String> currentVariationIdsToAverageAlong = null;

	private String thisFixedSetTag;


	public FixedVaryingParametersSetParser()	{
		addTaggedParser(TAG_GRAPH, graphIdProcessor = new XMLStringValueProcessor());
		addTaggedParser(TAG_ALGORITHM, new AlgorithmIdProcessor());
		addTaggedParser(TAG_FIX, fixedValueProcessor = new FixedValueParser());
		addTaggedParser(TAG_AVERAGE, new AverageByIdProcessor());
	}

	public void setStorage(DescriptionDataArrangement arrangementDescription)	{
		descriptionDataArrangement = arrangementDescription;
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);
		thisFixedSetTag = tagName;

		currentValuesForIdsSet = new HashMap<String, String>();
		fixedValueProcessor.setStorage(currentValuesForIdsSet);

		setLabel = attributes.getValue(ATTRIBUTE_LABEL);
		if (setLabel == null  ||  setLabel.isEmpty())	{
			noLabelCounter++;
			setLabel = String.valueOf(noLabelCounter);
			System.out.println("  WARNING:\tmissing label in plot.");
		}

		miningAlgorithmId = AlgorithmDescription.NO_ID;
		currentVariationIdsToAverageAlong = null;
	}


	@Override
	public void closeElement()	{
		super.closeElement();

		try {
			String graphId = getObligatoryId(graphIdProcessor);
			Utils.checkWhetherIsWordInScenario(graphId, TAG_GRAPH, thisFixedSetTag);

			descriptionDataArrangement.addFixedValuesSet(setLabel, currentValuesForIdsSet, graphId, miningAlgorithmId, currentVariationIdsToAverageAlong);
		} catch (ScenarioException e) {
			e.printStackTrace();
		}
	}

	private String getObligatoryId(XMLStringValueProcessor xmlStringValueProcessor) throws ScenarioException	{
		String stringValue = xmlStringValueProcessor.getText();
		if (stringValue == null)	{
			throw new ScenarioException("Forgot graph ID in data arrangement description.");
		}
		return stringValue;
	}
}



class FixedValueParser extends XMLStringValueProcessor	{
	private static final String ATTRIBUTE_VARIATION_ID = "varId";

	private Map<String, String> fixedValuesContainer;	// Map<ID: String, value: String>
	private String currentVariationId;

	public void setStorage(Map<String, String> valuesContainer)	{
		fixedValuesContainer = valuesContainer;
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		currentVariationId = attributes.getValue(ATTRIBUTE_VARIATION_ID);
		if (currentVariationId == null  ||  currentVariationId.isEmpty())	{
			throw new ScenarioException("Forgot to specify which variation is to be fixed. Check <fix varId=''/> in scenario.");
		}
		Utils.checkWhetherIsWordInScenario(currentVariationId, ATTRIBUTE_VARIATION_ID, tagName);
	}


	@Override
	public void closeElement()	{
		super.closeElement();
		fixedValuesContainer.put(currentVariationId, getText());
	}
}
