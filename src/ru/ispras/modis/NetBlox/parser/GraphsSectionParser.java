package ru.ispras.modis.NetBlox.parser;

import java.io.InputStream;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.basicParsersAndUtils.Utils;
import ru.ispras.modis.NetBlox.parser.extensionInterfaces.IGraphDescriptionParser;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.DescriptionGraphsOneType;

/**
 * This parser processes the <graphs/> section of scenario file.
 * 
 * @author ilya
 */
public class GraphsSectionParser extends ScenarioSectionParser {
	private static final String TAG_GRAPH = "graph";

	private static final String ATTRIBUTE_GRAPH_TYPE = "type";
	public static final String ATTRIBUTE_GRAPH_ID = "id";
	public static final String ATTRIBUTE_GRAPH_DIRECTED = "directed";
	public static final String ATTRIBUTE_GRAPH_WEIGHTED = "weighted";


	public GraphsSectionParser(ScenarioParser scenarioParser, ScenarioParserExtensionsRegistry extensionsRegistry, String scenarioSectionTag)	{
		super(extensionsRegistry, scenarioSectionTag);
		defaultSectionElementProcessor = new GraphTagProcessor(scenarioParser);
	}


	/**
	 * Called when child element is found.
	 */
	@Override
	public XMLElementProcessor startElement(String tagName, Attributes attributes) {
		if (!tagName.equalsIgnoreCase(TAG_GRAPH))	{
			throw new ScenarioException("<graphs/> must consist of <"+TAG_GRAPH+"/>s.");
		}

		return startElement(tagName, attributes, ATTRIBUTE_GRAPH_TYPE);
	}

	@Override
	protected void checkCallbackSuitsSection(Object callback)	{
		if (!(callback instanceof IGraphDescriptionParser))	{
			String errorMessage = "Graphs section parser must implement "+IGraphDescriptionParser.class.getName()+", while "
					+callback.getClass().getName()+" does not.";	//XXX Give more information about plug-in?
			throw new PluginException(errorMessage);
		}
	}


	@Override
	protected void putToTask(AlgorithmDescription parsedDescription, String currentGraphType)	{
		if (! (parsedDescription instanceof DescriptionGraphsOneType))	{
			String errorMessage = "The parser of graphs section elements for "+currentGraphType+
					" graph type has put its result to a wrong container (not "+DescriptionGraphsOneType.class.getName()+" descendant).";
			throw new PluginException(errorMessage);
		}

		getTaskStorage().addGraphTypeDescription((DescriptionGraphsOneType) parsedDescription);
	}



	private class GraphTagProcessor extends SectionElementProcessor	{
		private IGraphDescriptionParser descriptionParserCallback;

		private String graphId;
		private boolean directed;
		private boolean weighted;


		public GraphTagProcessor(ScenarioParser overallScenarioParser)	{
			super(overallScenarioParser);
		}


		@Override
		public void setExtensionCallback(Object callback)	{
			descriptionParserCallback = (IGraphDescriptionParser) callback;
		}


		/**
		 * Called when the new element of this type is created.
		 */
		@Override
		public void createElement(XMLElementProcessor aparent, String tagName,
				Attributes attributes, ParserContext acontext) {
			super.createElement(aparent, tagName, attributes, acontext);

			graphId = Utils.getId(attributes, tagName, ATTRIBUTE_GRAPH_ID);
			directed = getBooleanAttribute(attributes, ATTRIBUTE_GRAPH_DIRECTED);
			weighted = getBooleanAttribute(attributes, ATTRIBUTE_GRAPH_WEIGHTED);
		}


		/**
		 * Called finally when the element is closed.
		 */
		@Override
		public void closeElement()	{
			InputStream contentStream = closeAndGetContent();

			DescriptionGraphsOneType algorithmDescription = descriptionParserCallback.parse(contentStream);
			setAlgorithmDescription(algorithmDescription);
			algorithmDescription.setId(graphId);
			algorithmDescription.setDirectedWeighted(directed, weighted);
		}
	}
}
