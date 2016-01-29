package ru.ispras.modis.NetBlox.parser;

import java.io.InputStream;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.basicParsersAndUtils.Utils;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IGraphMiningDescriptionParser;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.DescriptionGraphMiningAlgorithm;

public class GraphMiningSectionParser extends ScenarioSectionParser {
	private static final String TAG_ALGORITHM = "algorithm";

	private static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_ALGORITHM_ID = "id";


	public GraphMiningSectionParser(ScenarioParser scenarioParser, ScenarioParserExtensionsRegistry extensionsRegistry, String scenarioSectionTag)	{
		super(extensionsRegistry, scenarioSectionTag);
		defaultSectionElementProcessor = new AlgorithmTagProcessor(scenarioParser);
	}


	/**
	 * Called when child element is found.
	 */
	@Override
	public XMLElementProcessor startElement(String tagName, Attributes attributes) {
		if (!tagName.equalsIgnoreCase(TAG_ALGORITHM))	{
			throw new ScenarioException("Graph mining section must consist of <algorithm/>s.");
		}

		return startElement(tagName, attributes, ATTRIBUTE_NAME);
	}

	@Override
	protected void checkCallbackSuitsSection(Object callback)	{
		if (!(callback instanceof IGraphMiningDescriptionParser))	{
			String errorMessage = "Graph mining section parser must implement "+IGraphMiningDescriptionParser.class.getName()+", while "
					+callback.getClass().getName()+" does not.";	//XXX Give more information about plug-in?
			throw new PluginException(errorMessage);
		}
	}


	@Override
	protected void putToTask(AlgorithmDescription parsedDescription, String currentAlgorithmName)	{
		if (! (parsedDescription instanceof DescriptionGraphMiningAlgorithm))	{
			String errorMessage = "The parser of graph mining section elements for "+currentAlgorithmName+
					" algorithm has put its result to a wrong container (not "+DescriptionGraphMiningAlgorithm.class.getName()+" descendant).";
			throw new PluginException(errorMessage);
		}

		getTaskStorage().addGraphMiningAlgorithmDescription((DescriptionGraphMiningAlgorithm) parsedDescription);
	}



	private class AlgorithmTagProcessor extends SectionElementProcessor	{
		private IGraphMiningDescriptionParser descriptionParserCallback;

		private String algorithmId;


		public AlgorithmTagProcessor(ScenarioParser overallScenarioParser)	{
			super(overallScenarioParser);
		}


		@Override
		public void setExtensionCallback(Object callback)	{
			descriptionParserCallback = (IGraphMiningDescriptionParser) callback;
		}


		/**
		 * Called when the new element of this type is created.
		 */
		@Override
		public void createElement(XMLElementProcessor aparent, String tagName,
				Attributes attributes, ParserContext acontext) {
			super.createElement(aparent, tagName, attributes, acontext);

			algorithmId = Utils.getId(attributes, tagName, ATTRIBUTE_ALGORITHM_ID);
		}


		/**
		 * Called finally when the element is closed.
		 */
		@Override
		public void closeElement()	{
			InputStream contentStream = closeAndGetContent();

			DescriptionGraphMiningAlgorithm algorithmDescription = descriptionParserCallback.parseMiningDescription(contentStream);
			setAlgorithmDescription(algorithmDescription);
			algorithmDescription.setId(algorithmId);
		}
	}
}
