package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;

/**
 * The parent class for parsers of tags that can contain not just a value, but a range of values.
 * 
 * @author ilya
 */
abstract class XMLRangedStringValueProcessor extends XMLStringValueProcessor	{
	protected static final String ATTRIBUTE_EXPLICIT_OR_RANGE = "explicitValuesOrRange";
	protected static final String ATTRIBUTE_PRESENTATION_VALUES = "values";
	private static final String ATTRIBUTE_PRESENTATION_RANGE = "range";
	private static final String ATTRIBUTE_RANGE_STEP = "step";

	public static final String ATTRIBUTE_VARIATION_ID = "varId";

	private boolean haveExplicitValues = false;

	protected String variationId = RangeOfValues.NO_RANGE_ID;
	protected String variationTag = "";


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		variationTag = tagName;
		variationId = getVariationId(attributes);

		String valuesRepresentation = attributes.getValue(ATTRIBUTE_EXPLICIT_OR_RANGE);
		if (valuesRepresentation == null)	{	//By default we expect explicit value.
			haveExplicitValues = true;
			return;
		}

		if (valuesRepresentation.equalsIgnoreCase(ATTRIBUTE_PRESENTATION_VALUES))	{
			haveExplicitValues = true;
		}
		else if (valuesRepresentation.equalsIgnoreCase(ATTRIBUTE_PRESENTATION_RANGE))	{
			haveExplicitValues = false;
			int numberOfValues = getValuesFromRange(attributes);
			if (numberOfValues > 1  &&  variationId.equals(RangeOfValues.NO_RANGE_ID))	{
				throw new ScenarioException("Incorrect number of elements in variation description.");
			}
		}
		else	{
			throw new ScenarioException("Incorrect type of variation in scenario."); 
		}
	}

	/**
	 * Values of parameter are presented as a range (including start and end) and a step between values.
	 * @param attributes
	 * @return the number of values in range.
	 */
	private int getValuesFromRange(Attributes attributes)	{
		String rangeString = attributes.getValue(ATTRIBUTE_PRESENTATION_RANGE);
		if (rangeString == null)	{
			throw new ScenarioException("The range of variation hasn't been described.");
		}

		String[] range = rangeString.split(Utils.DELIMITER);
		if (range.length != 2)	{
			throw new ScenarioException("There must be 2 elements specifying the range of variation.");
		}

		String step = attributes.getValue(ATTRIBUTE_RANGE_STEP);
		if (step == null)	{
			throw new ScenarioException("The step of variation hasn't been specified.");
		}

		return extractValuesFromRange(range, step);
	}

	protected abstract int extractValuesFromRange(String[] range, String stepString);

	private String getVariationId(Attributes attributes)	{
		String variationIdString = attributes.getValue(ATTRIBUTE_VARIATION_ID);
		if (variationIdString == null)	{
			return RangeOfValues.NO_RANGE_ID;
		}

		Utils.checkWhetherIsWordInScenario(variationIdString, ATTRIBUTE_VARIATION_ID, variationTag);

		return variationIdString;
	}


	@Override
	public void closeElement()	{
		super.closeElement();

		if (haveExplicitValues)	{
			int numberOfValues = parseExplicitValues();
			if (numberOfValues > 1  &&  variationId.equals(RangeOfValues.NO_RANGE_ID))	{
				throw new ScenarioException("Too many elements when there's no variation ID to describe the variation.");
			}
		}
	}

	/**
	 * Parses the explicit values for parameter.
	 * @return the number of provided values.
	 */
	private int parseExplicitValues()	{
		String[] valuesStrings = getText().split(Utils.DELIMITER);
		if (valuesStrings.length == 0)	{
			throw new ScenarioException("Elements of variation are missing.");
		}

		for (String value : valuesStrings)	{
			addValue(value);
		}

		return valuesStrings.length;
	}

	protected abstract void addValue(String value);
}