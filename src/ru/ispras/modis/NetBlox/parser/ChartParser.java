package ru.ispras.modis.NetBlox.parser;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.basicParsersAndUtils.Utils;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement;

public class ChartParser extends DataArrangementParser {
	private static final String TAG_DIMENSION = "dimension";
	private static final String TAG_VALUES = "values";

	private final DimensionParser dimensionProcessor;
	private final FixedVaryingParametersSetParser valuesProcessor;

	public ChartParser()	{
		addTaggedParser(TAG_DIMENSION, dimensionProcessor = new DimensionParser());
		addTaggedParser(TAG_VALUES, valuesProcessor = new FixedVaryingParametersSetParser());
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		dimensionProcessor.setStorage(currentArrangementDescription);
		valuesProcessor.setStorage(currentArrangementDescription);
	}


	@Override
	public void closeElement()	{
		super.closeElement();

		taskStorage.addChartDescription(currentArrangementDescription);
	}



	class DimensionParser extends XMLStringValueProcessor	{
		private static final String ATTRIBUTE_DIMENSION_NUMBER = "number";

		private int currentDimensionNumber;
		private DescriptionDataArrangement dataArrangementDescription;

		public void setStorage(DescriptionDataArrangement arrangementDescription)	{
			dataArrangementDescription = arrangementDescription;
		}


		@Override
		public void createElement(XMLElementProcessor aparent, String tagName,
				Attributes attributes, ParserContext acontext) {
			super.createElement(aparent, tagName, attributes, acontext);

			String dimensionNumberString = attributes.getValue(ATTRIBUTE_DIMENSION_NUMBER);
			if (dimensionNumberString == null  ||  dimensionNumberString.isEmpty())	{
				throw new ScenarioException("The dimension number must be specified.");
			}
			currentDimensionNumber = Integer.parseInt(dimensionNumberString);
		}


		@Override
		public void closeElement()	{
			super.closeElement();

			String dimensionVariationID = getText();
			if (dimensionVariationID == null  ||  dimensionVariationID.isEmpty())	{
				throw new ScenarioException("The variation ID specified for dimension #"+currentDimensionNumber+" must be unempty.");
			}
			Utils.checkWhetherIsWordInScenario(dimensionVariationID, TAG_DIMENSION, arrangementTag);

			dataArrangementDescription.addDimension(currentDimensionNumber, dimensionVariationID);
		}
	}
}
