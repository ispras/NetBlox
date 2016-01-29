package ru.ispras.modis.NetBlox.parser;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementWithChildrenProcessor;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public class DataArrangementParser extends XMLElementWithChildrenProcessor {
	private static final String ATTRIBUTE_NAME = "name";

	protected String arrangementTag;

	protected ScenarioTask taskStorage;
	protected DescriptionDataArrangement currentArrangementDescription;

	public void setStorage(ScenarioTask task)	{
		taskStorage = task;
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);
		arrangementTag = tagName;

		String name = attributes.getValue(ATTRIBUTE_NAME);
		if (name == null)	{
			name = "";
		}

		currentArrangementDescription = new DescriptionDataArrangement(name);
	}
}
