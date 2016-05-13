package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;

/**
 * Parses xml string values that can contain a range of string values.
 * 
 * @author ilya
 */
public class XMLStringValuesRangeProcessor extends XMLRangedStringValueProcessor {
	private List<String> values = new LinkedList<String>();


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName, Attributes attributes, ParserContext acontext) {
		values = new LinkedList<String>();

		String valuesRepresentation = attributes.getValue(ATTRIBUTE_EXPLICIT_OR_RANGE);
		if (valuesRepresentation != null  &&  !valuesRepresentation.equalsIgnoreCase(ATTRIBUTE_PRESENTATION_VALUES))	{
			StringBuilder messageBuilder = new StringBuilder("String values must be specified explicitly (").
					append(ATTRIBUTE_EXPLICIT_OR_RANGE).append("='").append(ATTRIBUTE_PRESENTATION_VALUES).append("'). Check tag: ").
					append(tagName);
			throw new ScenarioException(messageBuilder.toString());
		}

		super.createElement(aparent, tagName, attributes, acontext);
	}


	@Override
	protected int extractValuesFromRange(String[] range, String stepString) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void addValue(String value) {
		values.add(value);
	}

	public RangeOfValues<String> getValues()	{
		return new RangeOfValues<String>(variationId, variationTag, values);
	}
}
