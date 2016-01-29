package ru.ispras.modis.NetBlox.parser.xmlParser;

import org.xml.sax.Attributes;

public abstract class XMLElementProcessor {
	private XMLElementProcessor parent;
	public XMLElementProcessorFactory wharehouse;
	protected ParserContext context;
	protected String tName;
	private int depth;
	private OnElementListener onElementListener;

	public void setOnElementListener(OnElementListener onElementListener) {
		this.onElementListener = onElementListener;
	}
	
	public static interface OnElementListener {
		public void trigger(XMLElementProcessor sender);
	}
	
	public String getName() {
		return tName;
	}

	public void characters(char[] ch, int start, int length) {}

	/**
	 * Called when the new element of this type is created. 
	 * 
	 * @param aparent parent XML processor
	 * @param tagName element tag name
	 * @param attributes attributes of this element
	 * @param acontext parsing context
	 */
	public void createElement(XMLElementProcessor aparent, String tagName, Attributes attributes, ParserContext acontext) {
		parent = aparent;
		context = acontext;
		tName = tagName.intern();
		depth = 1;
	}
	
	/**
	 * Called finally when the element if closed 
	 */	
	public void closeElement() {
		if (onElementListener != null) { onElementListener.trigger(this); }
	}

	/**
	 * Called when child element is found 
	 * 
	 * @param tagName child name
	 * @param attributes child element attributes
	 * @return returns new processor, that will process new element 
	 */
	public XMLElementProcessor startElement(String tagName, Attributes attributes) {
		depth++;
		return this;
	}

	/**
	 * Child element is closed
	 * @param tagName element name
	 * @return an element processor for further parsing
	 */
	public XMLElementProcessor endElement(String tagName) {
		XMLElementProcessor result;
		
		depth--;
		if ((tagName.intern() == tName) && (depth == 0)) {
			closeElement();
			
			if (parent != null) {
				result = parent.endElement(tagName, this);
			} else {
				result = null;
			}
			
			if (wharehouse != null) { 
				wharehouse.push(this); 
			}
		} else {
			result = this;
		}
		
		return result;
	}

	public XMLElementProcessor endElement(String tagName, XMLElementProcessor element) {
		return endElement(tagName);
	}
}
