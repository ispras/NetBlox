package ru.ispras.modis.NetBlox.parser.extensionInterfaces;

import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;

/**
 * The plug-in callback classes that implement this interface must
 * return <code>XMLElementProcessor</code> objects that NetBlox will
 * use to parse scenario elements by itself.
 * 
 * @author ilya
 */
public interface IParserSupplier {
	public XMLElementProcessor getXMLElementParser();
}
