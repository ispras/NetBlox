package ru.ispras.modis.NetBlox.parser.xmlParser;

public class XMLStringValueProcessor extends XMLElementProcessor {
	private String text;
	private StringBuilder builder = new StringBuilder();
	
	public static XMLElementProcessorFactory whare;
	
	public static XMLStringValueProcessor getStringProcessor() {
		if (whare == null) {
			whare = new XMLElementProcessorFactory();
		}
		
		XMLStringValueProcessor result;
		
		if ((result = (XMLStringValueProcessor) whare.pop(XMLStringValueProcessor.class)) == null) {
			(result = new XMLStringValueProcessor()).wharehouse = whare;
		}		
		
		return result;
	}

	public String getText() {
		return text;
	}

	@Override
	public void closeElement() {
		text = builder.toString();
		builder.setLength(0);
	}
	
	@Override
	public void characters(char[] ch, int start, int length) {
		builder.append(ch, start, length);
	}
}
