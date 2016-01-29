package ru.ispras.modis.NetBlox.graphVisualisation.visualisers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataManagement.StorageWriter;
import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.scenario.ParametersSet;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.utils.Pair;

public abstract class GraphVisualiser {
	protected static final SystemConfiguration configuration;
	private static final String OUTPUT_DIRECTORY;

	static	{
		configuration = SystemConfiguration.getInstance();
		//labelsBundle = configuration.getLabelsBundle();

		OUTPUT_DIRECTORY = configuration.getGraphFilesRoot() + SystemConfiguration.FILES_SEPARATOR +
				"visualisationResults" + SystemConfiguration.FILES_SEPARATOR;
		try {
			StorageWriter.makeSureDirectoryExists(Paths.get(OUTPUT_DIRECTORY));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected GraphVisualisationDescription visualisationDescription;


	public GraphVisualiser(GraphVisualisationDescription visualisationDescription)	{
		this.visualisationDescription = visualisationDescription;
	}


	public abstract void visualise(IGraph graph, GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters)
			throws VisualisationException;

	public abstract void visualise(IGraph graph, ISetOfGroupsOfNodes setOfGroupsOfNodes, GraphParametersSet initialGraphParameters,
			ExtendedMiningParameters miningParameters) throws VisualisationException;


	protected String makePdfExportFilePathname(GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters)	{
		StringBuilder exportFilePathBuilder = new StringBuilder(OUTPUT_DIRECTORY).append(visualisationDescription.getExportFilename()).
				append(makeExportFileNameBase(initialGraphParameters, miningParameters)).append(".pdf");

		return exportFilePathBuilder.toString();
	}

	protected String makePNGExportFilePathname(GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters) {
		StringBuilder exportFilePathBuilder = new StringBuilder(OUTPUT_DIRECTORY).append(visualisationDescription.getExportFilename()).
				append(makeExportFileNameBase(initialGraphParameters, miningParameters)).append(".png");
	
		return exportFilePathBuilder.toString();
	}

	private String makeExportFileNameBase(GraphParametersSet initialGraphParameters, ExtendedMiningParameters extendedMiningParameters)	{
		StringBuilder nameBuilder = new StringBuilder().append('_').append(initialGraphParameters.getGraphDescriptionId());

		appendParametersValues(nameBuilder, initialGraphParameters);

		if (extendedMiningParameters != null)	{
			nameBuilder.append('_');
			if (extendedMiningParameters.getAbsoluteExternalFilename() != null)	{
				nameBuilder.append(extendedMiningParameters.getRelativeExternalFilename()).append('_');
			}

			RangeOfValues<String> relativeExternalSetsOfGroupsFilenames = extendedMiningParameters.getRelativeExternalFilenames();
			if (relativeExternalSetsOfGroupsFilenames != null  &&  !relativeExternalSetsOfGroupsFilenames.isEmpty())	{
				//Graph mining was performed over a collection of external files.
				nameBuilder.append(relativeExternalSetsOfGroupsFilenames.hashCode()).append('_');
			}

			GraphMiningParametersSet miningParameters = extendedMiningParameters.getMiningParameters();
			nameBuilder.append(miningParameters.getAlgorithmDescriptionId());
			appendParametersValues(nameBuilder, miningParameters);
		}

		//FUTURE_WORK SupplementaryData is left out again.

		String result = nameBuilder.toString().replace('/', '_');	//XXX Replace with FILES_SEPARATOR?
		return result;
	}

	private void appendParametersValues(StringBuilder builder, ParametersSet parameters)	{
		List<Pair<String, String>> parametersAsKeyValuePairs = parameters.getSpecifiedParametersAsPairsOfUniqueKeysAndValues();
		if (parametersAsKeyValuePairs != null)	{
			builder.append('_');
			for (Pair<String, String> graphParameterKeyValue : parametersAsKeyValuePairs)	{
				builder.append(graphParameterKeyValue.get1st()).append(graphParameterKeyValue.get2nd());
			}
		}
	}
}
