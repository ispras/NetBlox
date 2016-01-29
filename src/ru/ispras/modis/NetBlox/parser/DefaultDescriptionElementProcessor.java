package ru.ispras.modis.NetBlox.parser;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IDescriptionElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementWithChildrenProcessor;

public abstract class DefaultDescriptionElementProcessor extends XMLElementWithChildrenProcessor implements IDescriptionElementProcessor {

	public boolean getBooleanAttribute(Attributes attributes, String attributeName)	{
		boolean value = false;

		String attributeString = attributes.getValue(attributeName);
		if (attributeString != null)	{
			value = Boolean.parseBoolean(attributeString);
		}

		return value;
	}
}
