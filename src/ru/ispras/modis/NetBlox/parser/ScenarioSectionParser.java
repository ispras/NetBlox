package ru.ispras.modis.NetBlox.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.ScenarioParserExtensionsRegistry.ScenarioNamedElementsParsers;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IDescriptionElementProcessor;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IParserSupplier;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public abstract class ScenarioSectionParser extends XMLElementProcessor {
	private ScenarioParserExtensionsRegistry extensionsRegistry;
	private ScenarioNamedElementsParsers elementsParsers = null;
	private String scenarioSectionTag;

	protected SectionElementProcessor defaultSectionElementProcessor;

	private ScenarioTask taskStorage;

	private String currentAlgorithmName;


	public ScenarioSectionParser(ScenarioParserExtensionsRegistry extensionsRegistry, String scenarioSectionTag)	{
		this.extensionsRegistry = extensionsRegistry;
		this.scenarioSectionTag = scenarioSectionTag;
	}

	public void setStorage(ScenarioTask task)	{
		taskStorage = task;
	}

	protected ScenarioTask getTaskStorage()	{
		return taskStorage;
	}


	protected XMLElementProcessor startElement(String tagName, Attributes attributes, String algorithmNameAttribute) {
		if (elementsParsers == null)	{
			elementsParsers = extensionsRegistry.getParsersForSection(scenarioSectionTag);
		}

		super.startElement(tagName, attributes);

		currentAlgorithmName = attributes.getValue(algorithmNameAttribute);
		if (currentAlgorithmName == null  ||  currentAlgorithmName.isEmpty())	{
			throw new ScenarioException("The '"+algorithmNameAttribute+"' attribute must be specified for a <"+tagName+"/>.");
		}

		XMLElementProcessor childProcessor = getXMLElementProcessor(currentAlgorithmName);
		if (childProcessor != this)	{
			childProcessor.createElement(this, tagName, attributes, context);
		}

		return childProcessor;
	}

	private XMLElementProcessor getXMLElementProcessor(String graphTypeInText)	{
		XMLElementProcessor childProcessor = this;

		if (elementsParsers.isToBeParsedInPlugin(graphTypeInText))	{
			Object callback = elementsParsers.getElementParser(graphTypeInText);
			checkCallbackSuitsSection(callback);
			defaultSectionElementProcessor.setExtensionCallback(callback);
			childProcessor = defaultSectionElementProcessor;
		}
		else	{
			IParserSupplier parserPluginCallback = elementsParsers.getParserSupplier(graphTypeInText);
			childProcessor = parserPluginCallback.getXMLElementParser();
		}

		return childProcessor;
	}

	protected abstract void checkCallbackSuitsSection(Object callback);


	/**
	 * Child element is closed.
	 * @param tagName				- child element tag
	 * @param childElementProcessor	- child element parser
	 * @return an element processor for further parsing
	 */
	public XMLElementProcessor endElement(String tagName, XMLElementProcessor childElementProcessor) {
		XMLElementProcessor result = super.endElement(tagName, childElementProcessor);

		if (! (childElementProcessor instanceof IDescriptionElementProcessor))	{
			String errorMessage = "The "+currentAlgorithmName+" scenario element parser must implement "+
					IDescriptionElementProcessor.class.getName()+" interface. "+childElementProcessor.getClass().getName()+" does not.";
			throw new PluginException(errorMessage);
		}

		AlgorithmDescription parsedDescription = ((IDescriptionElementProcessor)childElementProcessor).getParsedDescription();
		parsedDescription.setAlgorithmNameInScenario(currentAlgorithmName);

		putToTask(parsedDescription, currentAlgorithmName);

		return result;
	}

	protected abstract void putToTask(AlgorithmDescription parsedDescription, String algorithmName);



	protected abstract class SectionElementProcessor extends DefaultDescriptionElementProcessor	{
		private ScenarioParser overallScenarioParser;

		private StringBuilder contentStringBuilder;

		private AlgorithmDescription algorithmDescription = null;


		public SectionElementProcessor(ScenarioParser overallScenarioParser)	{
			super();
			this.overallScenarioParser = overallScenarioParser;
		}


		public abstract void setExtensionCallback(Object callback);


		@Override
		public void createElement(XMLElementProcessor aparent, String tagName,
				Attributes attributes, ParserContext acontext) {
			super.createElement(aparent, tagName, attributes, acontext);

			contentStringBuilder = new StringBuilder();
			overallScenarioParser.setWriteTagsDown(true);

			algorithmDescription = null;
		}


		@Override
		public void characters(char[] ch, int start, int length) {
			contentStringBuilder.append(ch, start, length);
		}


		protected InputStream closeAndGetContent()	{
			super.closeElement();
			overallScenarioParser.setWriteTagsDown(false);

			String tagContent = contentStringBuilder.toString();

			InputStream contentStream = new ByteArrayInputStream(tagContent.getBytes());
			return contentStream;
		}


		protected void setAlgorithmDescription(AlgorithmDescription description)	{
			algorithmDescription = description;
		}

		@Override
		public AlgorithmDescription getParsedDescription() {
			return algorithmDescription;
		}
	}
}
