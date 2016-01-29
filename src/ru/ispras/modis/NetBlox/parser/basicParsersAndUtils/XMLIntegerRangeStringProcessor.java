package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;

/**
 * Is used to parse xml string values that can contain a range of integer values.
 * 
 * TODO Add checks for correctness of ranges. See #3605.
 * 
 * @author ilya
 */

public class XMLIntegerRangeStringProcessor extends XMLRangedStringValueProcessor {
	private List<Integer> values = new LinkedList<Integer>();

	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		values = new LinkedList<Integer>();
		super.createElement(aparent, tagName, attributes, acontext);
	}

	protected int extractValuesFromRange(String[] range, String stepString)	{
		int rangeStart = Integer.parseInt(range[0]);
		int rangeEnd = Integer.parseInt(range[1]);
		int step = Integer.parseInt(stepString);

		for (int value=rangeStart ; value <= rangeEnd ; value+=step)	{
			values.add(value);
		}

		return values.size();
	}

	protected void addValue(String value)	{
		values.add(Integer.valueOf(value));
	}

	public RangeOfValues<Integer> getValues()	{
		return new RangeOfValues<Integer>(variationId, variationTag, values);
	}
}
