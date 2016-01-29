package ru.ispras.modis.NetBlox.parser;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription.Method;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public class VisualisationSectionParser extends DefaultDescriptionElementProcessor {
	class MinimalOverlapVisualisedProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			int minimalNumberOfNodes = Integer.parseInt(getText());
			currentVisualisationDescription.setMinimalNumberOfNodesInOverlapToVisualiseIt(minimalNumberOfNodes);
		}
	}

	class NodesSizeCorrectionCoefficientProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			float coefficient = Float.parseFloat(getText());
			currentVisualisationDescription.setNodesSizeCorrectionCoefficient(coefficient);
		}
	}



	private static final String TAG_EXPORT_FILENAME = "exportFilename";
	private static final String TAG_MINIMAL_OVERLAP_VISUALISED = "minimalOverlapVisualised";
	private static final String TAG_NODES_SIZE_CORRECTION_COEFFICIENT = "nodesSizeCorrectionCoefficient";

	private final XMLStringValueProcessor exportFilenameProcessor;

	private static final String ATTRIBUTE_VISUALISE_GRAPH = "graph";
	private static final String ATTRIBUTE_VISUALISE_GROUPS_OF_NODES = "nodesGroups";

	private static final String ATTRIBUTE_VISUALISATION_METHOD = "method";
	private static final String ATTRIBUTE_VISUALISATION_METHOD_MATRIX = "matrix";
	private static final String ATTRIBUTE_VISUALISATION_METHOD_CLUSTERS_GRAPH = "clustersGraph";
	//private static final String ATTRIBUTE_VISUALISATION_METHOD_CIRCULAR = "circular";
	private static final String ATTRIBUTE_VISUALISATION_METHOD_FORCE_DIRECTED = "force";

	private ScenarioTask taskStorage;

	private GraphVisualisationDescription currentVisualisationDescription;


	public VisualisationSectionParser()	{
		super();

		addTaggedParser(TAG_EXPORT_FILENAME, exportFilenameProcessor = new XMLStringValueProcessor());
		addTaggedParser(TAG_MINIMAL_OVERLAP_VISUALISED, new MinimalOverlapVisualisedProcessor());
		addTaggedParser(TAG_NODES_SIZE_CORRECTION_COEFFICIENT, new NodesSizeCorrectionCoefficientProcessor());
	}


	public void setStorage(ScenarioTask task)	{
		taskStorage = task;
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		boolean visualiseGraph = getBooleanAttribute(attributes, ATTRIBUTE_VISUALISE_GRAPH);
		boolean visualiseGroupsOfNodes = getBooleanAttribute(attributes, ATTRIBUTE_VISUALISE_GROUPS_OF_NODES);
		currentVisualisationDescription = new GraphVisualisationDescription(visualiseGraph, visualiseGroupsOfNodes);

		String visualisationMethodString = attributes.getValue(ATTRIBUTE_VISUALISATION_METHOD);
		if (visualisationMethodString != null)	{
			if (visualisationMethodString.equalsIgnoreCase(ATTRIBUTE_VISUALISATION_METHOD_MATRIX))	{
				currentVisualisationDescription.setVisualisationMethod(Method.MATRIX);
			}
			else if (visualisationMethodString.equalsIgnoreCase(ATTRIBUTE_VISUALISATION_METHOD_CLUSTERS_GRAPH))	{
				currentVisualisationDescription.setVisualisationMethod(Method.CLUSTERS_GRAPH);
			}
			else if (visualisationMethodString.equalsIgnoreCase(ATTRIBUTE_VISUALISATION_METHOD_FORCE_DIRECTED))	{
				currentVisualisationDescription.setVisualisationMethod(Method.FORCE_DIRECTED);
			}
			/*else if (visualisationMethodString.equalsIgnoreCase(ATTRIBUTE_VISUALISATION_METHOD_CIRCULAR))	{
				currentVisualisationDescription.setVisualisationMethod(Method.CIRCULAR);
			}*/
		}
	}


	@Override
	public void closeElement()	{
		super.closeElement();

		String exportFilename = exportFilenameProcessor.getText();
		currentVisualisationDescription.setExportFilename(exportFilename);

		taskStorage.addGraphVisualisationDescription(currentVisualisationDescription);
	}




	@Override
	public AlgorithmDescription getParsedDescription() {
		//FUTURE_WORK - reserved for it.
		throw new UnsupportedOperationException();
	}
}
