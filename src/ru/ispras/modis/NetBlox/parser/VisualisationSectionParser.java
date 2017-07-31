package ru.ispras.modis.NetBlox.parser;

import java.awt.Color;

import org.xml.sax.Attributes;

import ru.ispras.modis.NetBlox.exceptions.ScenarioException;
import ru.ispras.modis.NetBlox.parser.xmlParser.ParserContext;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLElementProcessor;
import ru.ispras.modis.NetBlox.parser.xmlParser.XMLStringValueProcessor;
import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription.FinalPresentationType;
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

	class RepulsionCoefficientProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			float coefficient = Float.parseFloat(getText());
			currentVisualisationDescription.setRepulsionCoefficient(coefficient);
		}
	}

	class GravityCoefficientProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			float coefficient = Float.parseFloat(getText());
			currentVisualisationDescription.setGravityCoefficient(coefficient);
		}
	}

	class NormalisedEdgeWeightInfluenceProcessor extends XMLStringValueProcessor	{
		@Override
		public void closeElement()	{
			super.closeElement();
			int coefficient = Integer.parseInt(getText());
			currentVisualisationDescription.setNormalisedEdgeWeightInfluence(coefficient);
		}
	}

	private class BackgroundColourProcessor extends XMLStringValueProcessor	{
		private static final String BLACK = "black";
		private static final String DARKGRAY = "darkgray";
		private static final String DARKGREY = "darkgrey";
		private static final String GRAY = "gray";
		private static final String GREY = "grey";
		private static final String WHITE = "white";

		@Override
		public void closeElement()	{
			super.closeElement();
			String stringValue = getText();
			if (stringValue == null  ||  stringValue.isEmpty())	{
				return;
			}

			
			Color value = Color.BLACK;
			if (stringValue.equalsIgnoreCase(DARKGRAY) || stringValue.equalsIgnoreCase(DARKGREY))	{
				value = Color.DARK_GRAY;
			}
			else if (stringValue.equalsIgnoreCase(GRAY) || stringValue.equalsIgnoreCase(GREY))	{
				value = Color.GRAY;
			}
			else if (stringValue.equalsIgnoreCase(WHITE))	{
				value = Color.WHITE;
			}
			else if (!stringValue.equalsIgnoreCase(BLACK))	{
				System.out.println("WARNING: Unsupported color was specified for background. Default color will be used.");
			}
			currentVisualisationDescription.setBackgroundColour(value);
		}
	}



	private static final String TAG_EXPORT_FILENAME = "exportFilename";
	private static final String TAG_MINIMAL_OVERLAP_VISUALISED = "minimalOverlapVisualised";
	private static final String TAG_NODES_SIZE_CORRECTION_COEFFICIENT = "nodesSizeCorrectionCoefficient";
	private static final String TAG_REPULSION_COEFFICIENT = "repulsionCoefficient";
	private static final String TAG_GRAVITY_COEFFICIENT = "gravityCoefficient";
	private static final String TAG_NORMALISED_EDGE_WEIGHT_INFLUENCE = "normalisedEdgeWeightInfluence";

	private static final String TAG_BACKGROUND_COLOUR = "background";

	private final XMLStringValueProcessor exportFilenameProcessor;

	private static final String ATTRIBUTE_VISUALISE_GRAPH = "graph";
	private static final String ATTRIBUTE_VISUALISE_SUBSTRUCTURES = "substructures";

	private static final String ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_NO = "no";
	private static final String ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_FALSE = "false";
	private static final String ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_ONE_CANVAS = "oneFile";
	private static final String ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_MULTIPLE_CANVAS = "multiFiles";
	private static final String ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_TRUE = "true";

	private static final String ATTRIBUTE_VISUALISATION_METHOD = "method";
	private static final String ATTRIBUTE_VISUALISATION_METHOD_MATRIX = "matrix";
	private static final String ATTRIBUTE_VISUALISATION_METHOD_CLUSTERS_GRAPH = "clustersGraph";
	private static final String ATTRIBUTE_VISUALISATION_METHOD_FORCE_DIRECTED = "force";

	private ScenarioTask taskStorage;

	private GraphVisualisationDescription currentVisualisationDescription;


	public VisualisationSectionParser()	{
		super();

		addTaggedParser(TAG_EXPORT_FILENAME, exportFilenameProcessor = new XMLStringValueProcessor());
		addTaggedParser(TAG_MINIMAL_OVERLAP_VISUALISED, new MinimalOverlapVisualisedProcessor());
		addTaggedParser(TAG_NODES_SIZE_CORRECTION_COEFFICIENT, new NodesSizeCorrectionCoefficientProcessor());
		addTaggedParser(TAG_REPULSION_COEFFICIENT, new RepulsionCoefficientProcessor());
		addTaggedParser(TAG_GRAVITY_COEFFICIENT, new GravityCoefficientProcessor());
		addTaggedParser(TAG_NORMALISED_EDGE_WEIGHT_INFLUENCE, new NormalisedEdgeWeightInfluenceProcessor());
		addTaggedParser(TAG_BACKGROUND_COLOUR, new BackgroundColourProcessor());
	}


	public void setStorage(ScenarioTask task)	{
		taskStorage = task;
	}


	@Override
	public void createElement(XMLElementProcessor aparent, String tagName,
			Attributes attributes, ParserContext acontext) {
		super.createElement(aparent, tagName, attributes, acontext);

		boolean visualiseGraph = getBooleanAttribute(attributes, ATTRIBUTE_VISUALISE_GRAPH);
		FinalPresentationType visualiseSubstructures = getFinalPresentationAttribute(attributes, ATTRIBUTE_VISUALISE_SUBSTRUCTURES);
		if (!visualiseGraph && visualiseSubstructures==FinalPresentationType.NO)	{
			throw new ScenarioException("Incompatible negative values for "+ATTRIBUTE_VISUALISE_GRAPH+" and "+ATTRIBUTE_VISUALISE_SUBSTRUCTURES);
		}
		currentVisualisationDescription = new GraphVisualisationDescription(visualiseGraph, visualiseSubstructures);

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
		}
	}

	private FinalPresentationType getFinalPresentationAttribute(Attributes attributes, String attributeName)	{
		FinalPresentationType value = FinalPresentationType.NO;

		String attributeString = attributes.getValue(attributeName);
		if (attributeString != null)	{
			if (attributeString.equalsIgnoreCase(ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_ONE_CANVAS) ||
					attributeString.equalsIgnoreCase(ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_TRUE))	{
				value = FinalPresentationType.ONE_CANVAS;
			}
			else if (attributeString.equalsIgnoreCase(ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_MULTIPLE_CANVAS))	{
				value = FinalPresentationType.MULTIPLE_CANVAS;
			}
			else if (!attributeString.equalsIgnoreCase(ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_NO) &&
					attributeString.equalsIgnoreCase(ATTRIBUTE_VISUALISE_PRESENTATION_TYPE_FALSE))	{
				throw new ScenarioException("Wrong value for "+attributeName+" attribute: "+attributeString);
			}
		}

		return value;
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
