package ru.ispras.modis.NetBlox.parser.xmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * CommonXMLParser is a SAX Handler which is able to select processor 
 * object for a defined type of XML elements    
 *  
 * @author epsilon
 *
 */
public class CommonXMLParser extends DefaultHandler {
	protected XMLElementProcessor processor;
	protected final ParserContext context = new ParserContext();
	protected Map<String, XMLElementProcessor> processorMap = new HashMap<String, XMLElementProcessor>(32);
	
	public CommonXMLParser() {
	}
	
	/**
	 * Parses the input stream, creating parser from the given parser factory
	 *   
	 * @param stream input data
	 * @param saxfactory SAX parser factory
	 */
	public void parse(InputStream stream, SAXParserFactory saxfactory) 
			throws SAXException, IOException, ParserConfigurationException 
	{
		saxfactory.setNamespaceAware(true);
		saxfactory.newSAXParser().parse(stream, this);
	}

	/**
	 * Maps an XML element name to a processor
	 * @param key element name
	 * @param p processor
	 */
	public void add(String key, XMLElementProcessor p) {
		processorMap.put(key, p);
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);

		if (processor != null) {
			processor.characters(ch, start, length); 
		};
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);

		context.elementStack.pop();
		
		if (processor != null) { 
			processor = processor.endElement(localName.intern());
		};
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if (processor != null) { 
			processor = processor.startElement(localName.intern(), attributes);
		} else {
			processor = processorMap.get(localName);
			if (processor != null) {
				processor.createElement(null, localName.intern(), attributes, context);
			}
		}

		context.elementStack.push(localName.intern());
	}
	
}
