package ru.ispras.modis.NetBlox.parser.basicParsersAndUtils;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;

/**
 * Is used to parse xml string values that can contain a range of float values.
 * 
 * TODO Add checks for correctness of ranges. See #3605.
 * 
 * @author ilya
 */
public class XMLFloatRangeStringProcessor extends XMLRangedStringValueProcessor {
	private List<Float> values;

	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		values = new LinkedList<Float>();
		super.createElement(aparent, tagName, attributes, acontext);
	}

	protected int extractValuesFromRange(String[] range, String stepString)	{
		float rangeStart = Float.parseFloat(range[0]);
		float rangeEnd = Float.parseFloat(range[1]);
		float step = Float.parseFloat(stepString);

		float upperBorder = (float) (rangeEnd + 0.01*step);
		for (float value=rangeStart ; value < upperBorder ; value+=step)	{
			values.add(value);
		}

		return values.size();
	}

	protected void addValue(String value)	{
		values.add(Float.valueOf(value));
	}

	public RangeOfValues<Float> getValues()	{
		return new RangeOfValues<Float>(variationId, variationTag, values);
	}

	public RangeOfValues<Float> getValues(float lowerLimit, float upperLimit)	{
		RangeOfValues<Float> rangeOfValues = new RangeOfValues<Float>(variationId, variationTag);
		float comparedUpperLimit = 1.000001f*upperLimit;
		for (Float value : values)	{
			if (value < lowerLimit  ||  value > comparedUpperLimit)	{
				System.out.println("WARNING: "+variationTag+" value "+value+" is beyond the permitted range: ["+
						lowerLimit+"; "+upperLimit+"]. Will be excluded.");
			}
			else	{
				rangeOfValues.addValue(value);
			}
		}
		return rangeOfValues;
	}
}
