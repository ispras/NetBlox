package ru.ispras.modis.NetBlox.parser.xmlParser;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * This extension of <code>XMLElementProcessor</code> class contains a map
 * to specify which children tags are to be parsed with which parser class.
 * 
 * @author ilya
 *
 */
public class XMLElementWithChildrenProcessor extends XMLElementProcessor {
	private Map<String, XMLElementProcessor> parserMap;


	public XMLElementWithChildrenProcessor()	{
		super();

		parserMap = new HashMap<String, XMLElementProcessor>();
	}


	public void addTaggedParser(String tag, XMLElementProcessor elementProcessor)	{
		parserMap.put(tag, elementProcessor);
	}


	@Override
	public XMLElementProcessor startElement(String tagName,
			Attributes attributes) {
		XMLElementProcessor p;
		super.startElement(tagName, attributes);
		
		p = parserMap.get(tagName);
		
		if (p == null) { 
			p = this; 
		} else {
			p.createElement(this, tagName, attributes, context);
		}
		
		return p;
	}
}
