package ru.ispras.modis.NetBlox.graphVisualisation.visualisers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageWriter;
import ru.ispras.modis.NetBlox.dataStructures.Graph;
import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.IGroupOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.IPackOfGraphStructures;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.PackOfGraphs;
import ru.ispras.modis.NetBlox.dataStructures.SetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.SetOfGroupsException;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.graphVisualisation.MinedDataDealer;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.scenario.ParametersSet;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.utils.MiningJobBase;
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
	protected MiningJobBase.JobBase minedDataType;


	public GraphVisualiser(GraphVisualisationDescription visualisationDescription, MiningJobBase.JobBase minedDataType)	{
		this.visualisationDescription = visualisationDescription;
		this.minedDataType = minedDataType;
	}


	/**
	 * Visualise graph for fixed time slice.
	 */
	public abstract void visualise(IGraph graph, GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters, Integer timeSlice)
			throws VisualisationException;

	/**
	 * Visualise graph with groups of nodes / subgraphs for fixed time slice.
	 */
	public abstract void visualise(IGraph graph, IPackOfGraphStructures<?> packOfGraphStructures, GraphParametersSet initialGraphParameters,
			ExtendedMiningParameters extendedMiningParameters, Integer timeSlice) throws VisualisationException;

	/**
	 * Visualise graph with groups of nodes / subgraphs (potentially for a range of time slices) drawn over the original graph (if method allows).
	 */
	public abstract void visualiseGroupsOverGraph(IGraph graph, GraphOnDriveHandler initialGraphHandler, ExtendedMiningParameters miningParameters)
			throws VisualisationException;

	/**
	 * Visualise graph with substructures in it (potentially for a range of time slices). Draw everything over original graph (if method allows).
	 */
	public void visualiseSubgraphsOverGraph(IGraph graph, GraphOnDriveHandler initialGraphHandler, ExtendedMiningParameters miningParameters)
			throws VisualisationException	{
		System.out.println("WARNING: Visualising graph substructure is not supported in "+visualisationDescription.getMethod()+" visualisation mode.");
	}


	protected void callVisualisationForTimeSlices(IGraph graph, GraphOnDriveHandler initialGraphHandler,
			ExtendedMiningParameters extendedMiningParameters) throws VisualisationException	{
		if (extendedMiningParameters != null  &&  extendedMiningParameters.getMiningParameters().considerTimeSlices())	{
			for (Integer timeSlice : extendedMiningParameters.getMiningParameters().getTimeSlices())	{
				ISetOfGroupsOfNodes groupsForTimeSlice = (ISetOfGroupsOfNodes) MinedDataDealer.getMined(
						minedDataType, initialGraphHandler, extendedMiningParameters, timeSlice);
				if (groupsForTimeSlice == null)	{	//There were no results for this timeSlice.
					continue;
				}
				visualise(graph, groupsForTimeSlice, initialGraphHandler.getGraphParameters(), extendedMiningParameters, timeSlice);
			}
		}
		else	{
			List<ISetOfGroupsOfNodes> setsOfGroups = prepareGroupStructures(graph, initialGraphHandler, extendedMiningParameters, null);
			if (setsOfGroups == null) {
				return;
			}
			for (ISetOfGroupsOfNodes groupsOfNodes : setsOfGroups)	{
				visualise(graph, groupsOfNodes, initialGraphHandler.getGraphParameters(), extendedMiningParameters, null);
			}
		}
	}

	protected List<ISetOfGroupsOfNodes> prepareGroupStructures(IGraph initialGraph, GraphOnDriveHandler initialGraphHandler,
			ExtendedMiningParameters extendedMiningParameters, Integer timeSlice) throws VisualisationException	{
		List<ISetOfGroupsOfNodes> data = new LinkedList<ISetOfGroupsOfNodes>();

		if (extendedMiningParameters != null)	{
			ISetOfGroupsOfNodes minedData = (ISetOfGroupsOfNodes) MinedDataDealer.getMined(
					minedDataType, initialGraphHandler, extendedMiningParameters, timeSlice);
			if (minedData != null)	{
				data.add(minedData);
			}
		}
		else	{
			RangeOfValues<String> pathsToDataFiles = initialGraphHandler.getGraphParameters().getProvidedForCharacterizationExternalFilenames();
			if (pathsToDataFiles == null)	{
				return null;
			}

			for (String relativePathToFile : pathsToDataFiles)	{
				String path = initialGraphHandler.getAbsolutePathPossiblyWithGraphDirectory(relativePathToFile);
				try {
					ISetOfGroupsOfNodes setOfGroupsOfNodes = new SetOfGroupsOfNodes(path, initialGraph);
					data.add(setOfGroupsOfNodes);
				} catch (SourceGraphException | SetOfGroupsException e) {
					throw new VisualisationException(e);
				}
			}
		}

		return data;
	}

	protected List<IGraph> prepareGraphStructures(GraphOnDriveHandler initialGraphHandler, ExtendedMiningParameters extendedMiningParameters,
			Integer timeSlice) throws VisualisationException	{
		List<IGraph> data = null;

		if (extendedMiningParameters != null)	{
			PackOfGraphs minedData = (PackOfGraphs) MinedDataDealer.getMined(minedDataType, initialGraphHandler, extendedMiningParameters, timeSlice);
			if (minedData != null)	{
				data = minedData.getList();
			}
		}
		else	{
			RangeOfValues<String> pathsToDataFiles = initialGraphHandler.getGraphParameters().getProvidedForCharacterizationExternalFilenames();
			if (pathsToDataFiles == null)	{
				return null;
			}

			GraphParametersSet initialGraphParameters = initialGraphHandler.getGraphParameters();
			data = new ArrayList<IGraph>(pathsToDataFiles.size());
			for (String relativePathToFile : pathsToDataFiles)	{
				String path = initialGraphHandler.getAbsolutePathPossiblyWithGraphDirectory(relativePathToFile);
				IGraph graphStructure = new Graph(path, initialGraphParameters.isDirected(), initialGraphParameters.isWeighted());
				data.add(graphStructure);
			}
		}

		return data;
	}


	private String structureID = "";
	private int graphsCounter = 0;

	public <GraphStructure> void setStructureID(List<?> sortedStructures, GraphStructure structure)	{
		if (structure == null)	{
			structureID = "";
			return;
		}

		StringBuilder idBuilder = new StringBuilder();

		if (sortedStructures != null)	{
			int index = sortedStructures.indexOf(structure);
			if (index >= 0)	{
				int numberOfDigits = (int) Math.ceil(Math.log10(sortedStructures.size()));
				if (numberOfDigits > 0)	{
					idBuilder.append(String.format("%0"+numberOfDigits+"d", index));
				}
			}
			else	{
				idBuilder.append("faded");
			}
		}

		if (structure instanceof IGroupOfNodes)	{
			Integer id = ((IGroupOfNodes) structure).getID();
			if (id != null)	{	idBuilder.append("_id").append(id);	}
		}
		else if (structure instanceof IGraph)	{
			/*TODO This solution doesn't allow to track the development of a single graph substructure.
			To make it better we need to introduce unique graph IDs. Needs to be done, but separately.*/
			idBuilder.append("_abstractID").append(graphsCounter++);
		}

		structureID = idBuilder.toString();
	}

	private boolean workWithOriginalGraph = false;
	public void workWithOriginalGraph(boolean b)	{
		workWithOriginalGraph = b;
	}
	public boolean workWithOriginalGraph()	{
		return workWithOriginalGraph;
	}

	protected String makePDFExportFilePathname(GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters, Integer timeSlice)	{
		StringBuilder exportFilePathBuilder = buildPrincipalPathName(initialGraphParameters, miningParameters, timeSlice);
		exportFilePathBuilder.append(".pdf");
		return exportFilePathBuilder.toString();
	}
	protected String makePNGExportFilePathname(GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters, Integer timeSlice) {
		StringBuilder exportFilePathBuilder = buildPrincipalPathName(initialGraphParameters, miningParameters, timeSlice);
		exportFilePathBuilder.append(".png");
		return exportFilePathBuilder.toString();
	}

	private StringBuilder buildPrincipalPathName(GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters,
			Integer timeSlice)	{
		//TODO Consider AnalysedDataIdentifier if passing the specific external files names is necessary. Also consider it in connection with #5474.

		StringBuilder filePathBuilder = new StringBuilder(OUTPUT_DIRECTORY).append(visualisationDescription.getExportFilename()).
				append(makeExportFileNameBase(initialGraphParameters, miningParameters));

		boolean areExternalGraphStructuresProbable = !workWithOriginalGraph  &&  (miningParameters == null)  &&
				(initialGraphParameters.getProvidedForCharacterizationExternalFilenames() != null)  &&
				!initialGraphParameters.getProvidedForCharacterizationExternalFilenames().isEmpty();

		if (!structureID.isEmpty() || timeSlice!=null || areExternalGraphStructuresProbable)	{
			filePathBuilder.append(SystemConfiguration.FILES_SEPARATOR);
		}

		filePathBuilder.append(structureID);

		if (timeSlice != null)	{
			int numberOfDigits = (int) Math.ceil(Math.log10(miningParameters.getMiningParameters().getTimeSlices().size()));
			filePathBuilder.append(String.format("_t%0"+numberOfDigits+"d", timeSlice));
		}

		if (areExternalGraphStructuresProbable)	{
			filePathBuilder.append("_").append(graphsCounter++);
			//XXX Make a distinct counter? Make a more sophisticated distinction?
		}

		return filePathBuilder;
	}

	private String makeExportFileNameBase(GraphParametersSet initialGraphParameters, ExtendedMiningParameters extendedMiningParameters)	{
		StringBuilder nameBuilder = new StringBuilder().append('_').append(initialGraphParameters.getGraphDescriptionId());

		appendParametersValues(nameBuilder, initialGraphParameters);

		RangeOfValues<String> pathsToExternalDataFiles = initialGraphParameters.getProvidedForCharacterizationExternalFilenames();

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
		else if (pathsToExternalDataFiles != null  &&  !pathsToExternalDataFiles.isEmpty())	{
			int hashForPaths = initialGraphParameters.getTypeOfProvidedForCharacterizationExternalData().ordinal();	//XXX Consider long?
			for (String externalPath : pathsToExternalDataFiles)	{
				hashForPaths += externalPath.hashCode();
			}
			nameBuilder.append('_').append(hashForPaths);
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
