package ru.ispras.modis.NetBlox.parser;

import java.util.Collection;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.ispras.modis.NetBlox.parser.xmlParser.CommonXMLParser;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;


/**
 * The main class to parse XML files with scenarios for NetBlox.
 * 
 * @author ilya
 */
public class ScenarioParser extends CommonXMLParser {
	private final TaskParser taskProcessor;

	private Collection<ScenarioTask> tasksStorage;

	private static final String TAG_TASK = "task";

	private boolean doWriteTagsDown = false;


	public ScenarioParser() {
		ScenarioParserExtensionsRegistry extensionsRegistry = new ScenarioParserExtensionsRegistry();

        add(TAG_TASK, taskProcessor = new TaskParser(this, extensionsRegistry));
    }


	public void setStorage(Collection<ScenarioTask> tasksStorage)	{
		this.tasksStorage = tasksStorage;
	}

	public void setWriteTagsDown(boolean b)	{
		doWriteTagsDown = b;
	}


	@Override
	public void startDocument() throws SAXException {
		super.startDocument();

		if (tasksStorage == null) {
			tasksStorage = new LinkedList<ScenarioTask>();
		}
		taskProcessor.setStorage(tasksStorage);
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if (processor != null  &&  doWriteTagsDown)	{
			StringBuilder tagStringBuilder = new StringBuilder("<").append(localName);
			for (int i=0 ; i<attributes.getLength() ; i++)	{
				tagStringBuilder.append(' ').append(attributes.getLocalName(i)).append("=\"").append(attributes.getValue(i)).append('"');
			}
			tagStringBuilder.append('>');

			String tagString = tagStringBuilder.toString();
			processor.characters(tagString.toCharArray(), 0, tagString.length());
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (processor != null  &&  doWriteTagsDown)	{
			String tagString = "</" + localName + ">";

			processor.characters(tagString.toCharArray(), 0, tagString.length());
		}

		super.endElement(uri, localName, qName);
	}
}
