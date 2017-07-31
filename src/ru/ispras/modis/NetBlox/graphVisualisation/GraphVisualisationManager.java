package ru.ispras.modis.NetBlox.graphVisualisation;

import java.util.Collection;
import java.util.List;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageScanner;
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
import ru.ispras.modis.NetBlox.exceptions.StorageException;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.ClustersGraphVisualiser;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.GephiGraphVisualiser;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.GraphVisualiser;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.MatrixGraphVisualiser;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.utils.MiningJobBase;

public class GraphVisualisationManager {
	private static final int MINIMAL_COMMUNITY_SIZE_FOR_VISUALISATION = 1;

	public static void visualise(GraphVisualisationDescription visualisationDescription, GraphOnDriveHandler graphHandler)
			throws VisualisationException	{
		IGraph graph = null;
		try {
			graph = graphHandler.getGraph();
		} catch (SourceGraphException e) {
			throw new VisualisationException(e);
		}

		GraphParametersSet graphParameters = graphHandler.getGraphParameters();
		MiningJobBase.JobBase minedDataType = graphParameters.getTypeOfProvidedForCharacterizationExternalData();
		GraphVisualiser visualiser = getVisualiser(visualisationDescription, minedDataType);

		if (visualisationDescription.visualiseGraph())	{
			switch (visualisationDescription.getSubstructuresFinalPresentationType())	{
			case NO:	//Visualise the original graph.
				visualiser.workWithOriginalGraph(true);
				visualiser.visualise(graph, graphParameters, null, null);
				visualiser.workWithOriginalGraph(false);
				break;
			case ONE_CANVAS: 		//Draw the provided external graph substructures over the original graph on one canvas (if there're any).
			case MULTIPLE_CANVAS:	//Draw the provided external graph substructures over the original graph on distinct canvas (if there're any).
				RangeOfValues<String> pathsToDataFiles = graphParameters.getProvidedForCharacterizationExternalFilenames();
				if (pathsToDataFiles==null || pathsToDataFiles.isEmpty())	{
					System.out.println("WARNING: No substructures to visualise on graph.");
					return;
				}

				if (minedDataType == MiningJobBase.JobBase.NODES_GROUPS_SET)	{
					visualiser.visualiseGroupsOverGraph(graph, graphHandler, null);
				}
				else if (minedDataType == MiningJobBase.JobBase.GRAPH  ||  minedDataType == MiningJobBase.JobBase.MULTIPLE_GRAPHS)	{
					visualiser.visualiseSubgraphsOverGraph(graph, graphHandler, null);
				}
				break;
			}
		}
		else	{
			RangeOfValues<String> pathsToDataFiles = graphParameters.getProvidedForCharacterizationExternalFilenames();
			if (pathsToDataFiles==null || pathsToDataFiles.isEmpty())	{
				System.out.println("WARNING: No substructures to visualise.");
				return;
			}
			
			switch (visualisationDescription.getSubstructuresFinalPresentationType())	{
			case ONE_CANVAS: //Visualise the provided external graph substructures on one canvas (if there're any).
				drawDisjointStructuresOnOneCanvas(visualiser, graph, graphHandler, minedDataType, pathsToDataFiles);
				break;
			case MULTIPLE_CANVAS: //Visualise the provided external graph substructures on distinct canvas (if there're any).
				drawDisjointStructuresInSeparateFiles(visualiser, graph, graphHandler, minedDataType, pathsToDataFiles);
				break;
			}
		}
	}
	private static void drawDisjointStructuresOnOneCanvas(GraphVisualiser visualiser, IGraph graph, GraphOnDriveHandler graphHandler,
			MiningJobBase.JobBase minedDataType, RangeOfValues<String> pathsToDataFiles) throws VisualisationException	{
		GraphParametersSet graphParameters = graphHandler.getGraphParameters();
		PackOfGraphs packOfGraphStructures = new PackOfGraphs();
		for (String relativePathToFile : pathsToDataFiles)	{
			String path = graphHandler.getAbsolutePathPossiblyWithGraphDirectory(relativePathToFile);
			switch (minedDataType)	{
			case NODES_GROUPS_SET:
				try {
					ISetOfGroupsOfNodes setOfGroupsOfNodes = new SetOfGroupsOfNodes(path, graph);
					visualiser.visualise(graph, setOfGroupsOfNodes, graphParameters, null, null);
				} catch (SourceGraphException | SetOfGroupsException e) {
					throw new VisualisationException(e);
				}
				break;
			case GRAPH:
			case MULTIPLE_GRAPHS:
				IGraph graphStructure = new Graph(path, graph.isDirected(), graph.isWeighted());
				packOfGraphStructures.add(graphStructure);
				break;
			}
		}
		if (!packOfGraphStructures.isEmpty())	{
			visualiser.visualise(graph, packOfGraphStructures, graphParameters, null, null);
		}
	}
	private static void drawDisjointStructuresInSeparateFiles(GraphVisualiser visualiser, IGraph graph, GraphOnDriveHandler graphHandler,
			MiningJobBase.JobBase minedDataType, RangeOfValues<String> pathsToDataFiles) throws VisualisationException	{
		for (String relativePathToFile : pathsToDataFiles)	{
			String path = graphHandler.getAbsolutePathPossiblyWithGraphDirectory(relativePathToFile);
			switch (minedDataType)	{
			case NODES_GROUPS_SET:
				try {
					ISetOfGroupsOfNodes setOfGroupsOfNodes = new SetOfGroupsOfNodes(path, graph);
					List<IGroupOfNodes> sortedGroups = MinedDataDealer.sortGroupsBySizeInDescendingOrder(setOfGroupsOfNodes);
					drawSubgraphsSeparately(visualiser, graph, graphHandler, null, sortedGroups, minedDataType, null);
				} catch (SourceGraphException | SetOfGroupsException e) {
					throw new VisualisationException(e);
				}
				break;
			case GRAPH:
			case MULTIPLE_GRAPHS:
				IGraph graphStructure = new Graph(path, graph.isDirected(), graph.isWeighted());
				visualiser.setStructureID(null, graphStructure);
				visualiser.visualise(graphStructure, graphHandler.getGraphParameters(), null, null);
				break;
			}
		}
		visualiser.setStructureID(null, null);
	}

	public static void visualise(GraphVisualisationDescription visualisationDescription, GraphOnDriveHandler graphHandler,
			ExtendedMiningParameters extendedMiningParameters) throws VisualisationException	{
		MiningJobBase.JobBase minedDataType = null;
		if (StorageScanner.containsMinedGroupsOfNodes(graphHandler, extendedMiningParameters))	{
			minedDataType = MiningJobBase.JobBase.NODES_GROUPS_SET;
		}
		else if (StorageScanner.containsMinedGraph(graphHandler, extendedMiningParameters))	{
			minedDataType = MiningJobBase.JobBase.GRAPH;
		}
		else if (StorageScanner.containsMinedMultipleGraphs(graphHandler, extendedMiningParameters))	{
			minedDataType = MiningJobBase.JobBase.MULTIPLE_GRAPHS;
		}

		IGraph graph = null;
		try {
			graph = graphHandler.getGraph();
		} catch (SourceGraphException e) {
			throw new VisualisationException(e);
		}

		GraphVisualiser visualiser = getVisualiser(visualisationDescription, minedDataType);

		if (visualisationDescription.visualiseGraph())	{
			switch (visualisationDescription.getSubstructuresFinalPresentationType())	{
			case ONE_CANVAS:		//Layout a graph. Paint its nodes in multiple colours (for gephi force directed layout).
			case MULTIPLE_CANVAS:	//Layout a graph, then paint it differently for each community and put into separate files.
				if (minedDataType == MiningJobBase.JobBase.NODES_GROUPS_SET)	{
					visualiser.visualiseGroupsOverGraph(graph, graphHandler, extendedMiningParameters);
				}
				else if (minedDataType == MiningJobBase.JobBase.GRAPH  ||  minedDataType == MiningJobBase.JobBase.MULTIPLE_GRAPHS)	{
					visualiser.visualiseSubgraphsOverGraph(graph, graphHandler, extendedMiningParameters);
				}
				break;
			}
		}
		else	{
			GraphMiningParametersSet miningParameters = extendedMiningParameters.getMiningParameters();

			switch (visualisationDescription.getSubstructuresFinalPresentationType())	{
			case ONE_CANVAS:		//Layout groups and subgraphs as separate graphs on one canvas. Different canvas for different time slices.
				if (miningParameters.considerTimeSlices())	{
					for (Integer timeSlice : miningParameters.getTimeSlices())	{
						IPackOfGraphStructures<?> minedGraphStructures = MinedDataDealer.getMined(
								minedDataType, graphHandler, extendedMiningParameters, timeSlice);
						visualiser.visualise(graph, minedGraphStructures, graphHandler.getGraphParameters(), extendedMiningParameters, timeSlice);
					}
				}
				else	{
					IPackOfGraphStructures<?> minedGraphStructures = MinedDataDealer.getMined(
							minedDataType, graphHandler, extendedMiningParameters, null);
					visualiser.visualise(graph, minedGraphStructures, graphHandler.getGraphParameters(), extendedMiningParameters, null);
				}
				break;

			case MULTIPLE_CANVAS:	//Layout groups and subgraphs as separate graphs, one graph per canvas (file).
				List<?> finalGraphStructures = MinedDataDealer.getOrderedMined(minedDataType, graphHandler, extendedMiningParameters);
				if (finalGraphStructures==null || finalGraphStructures.isEmpty())	{
					return;
				}

				if (miningParameters.considerTimeSlices())	{
					for (Integer timeSlice : miningParameters.getTimeSlices())	{
						drawSubgraphsSeparately(visualiser, graph, graphHandler, extendedMiningParameters, finalGraphStructures, minedDataType, timeSlice);
					}
				}
				else	{
					drawSubgraphsSeparately(visualiser, graph, graphHandler, extendedMiningParameters, finalGraphStructures, minedDataType, null);
				}
				break;
			}
		}
	}


	/*private GroupsOfNodesSelectionCriteria getCommunitiesSelectionCriteria(GraphVisualisationDescription visualisationDescription)	{
		//TODO Implement getting the communities selection criteria.
		return new GroupsOfNodesSelectionCriteria();
	}


	private ISetOfGroupsOfNodes filterCommunities(ISetOfGroupsOfNodes setOfGroupsOfNodes, GroupsOfNodesSelectionCriteria communitiesSelectionCriteria)	{
		Collection<IGroupOfNodes> filteredCommunities = new LinkedList<IGroupOfNodes>();

		for (IGroupOfNodes community : setOfGroupsOfNodes)	{
			if (communitiesSelectionCriteria.meetsCriteria(community))	{
				filteredCommunities.add(community);
			}
		}

		return new SetOfGroupsOfNodes(filteredCommunities);
	}*/


	private static GraphVisualiser getVisualiser(GraphVisualisationDescription visualisationDescription, MiningJobBase.JobBase minedDataType)	{
		GraphVisualiser visualiser = null;
		switch (visualisationDescription.getMethod())	{
		case MATRIX:
			visualiser = new MatrixGraphVisualiser(visualisationDescription, minedDataType);
			break;
		case CLUSTERS_GRAPH:
			visualiser = new ClustersGraphVisualiser(visualisationDescription, minedDataType);
			break;
		case FORCE_DIRECTED:
			visualiser = new GephiGraphVisualiser(visualisationDescription, minedDataType);
			break;
		}

		return visualiser;
	}

	private static void drawSubgraphsSeparately(GraphVisualiser visualiser, IGraph graph, GraphOnDriveHandler initialGraphHandler,
			ExtendedMiningParameters miningParameters, List<?> finalGraphStructures, MiningJobBase.JobBase minedDataType, Integer timeSlice)
					throws VisualisationException	{
		try {
			switch (minedDataType)	{
			case NODES_GROUPS_SET:
				@SuppressWarnings("unchecked")
				ISetOfGroupsOfNodes setOfGroupsOfNodes = (miningParameters==null) ?
						new SetOfGroupsOfNodes((Collection<IGroupOfNodes>) finalGraphStructures) :
							StorageScanner.getMinedGroupsOfNodes(initialGraphHandler, miningParameters, timeSlice);
				if (setOfGroupsOfNodes == null)	{
					return;
				}
				for (IGroupOfNodes group : setOfGroupsOfNodes)	{
					if (group.size() < MINIMAL_COMMUNITY_SIZE_FOR_VISUALISATION)	{
						continue;
					}
					IGraph communitySubgraph = graph.getSubgraphForGroup(group);
					visualiser.setStructureID(finalGraphStructures, group);
					visualiser.visualise(communitySubgraph, initialGraphHandler.getGraphParameters(), miningParameters, timeSlice);
				}
				break;
	
			case GRAPH:
				IGraph minedGraph = StorageScanner.getMinedGraphStructure(initialGraphHandler, miningParameters, timeSlice);
				visualiser.setStructureID(null, minedGraph);
				visualiser.visualise(minedGraph, initialGraphHandler.getGraphParameters(), miningParameters, timeSlice);
				break;
	
			case MULTIPLE_GRAPHS:
				List<IGraph> subgraphs = StorageScanner.getMinedMultipleGraphStructures(IGraph.class, initialGraphHandler, miningParameters, timeSlice);
				if (subgraphs == null)	{
					return;
				}
				for (IGraph subgraph : subgraphs)	{
					visualiser.setStructureID((timeSlice==null)?null:finalGraphStructures, subgraph);
					visualiser.visualise(subgraph, initialGraphHandler.getGraphParameters(), miningParameters, timeSlice);
				}
				break;
			}

			visualiser.setStructureID(null, null);	//Clear that ID.
		} catch (VisualisationException ve)	{
			visualiser.setStructureID(null, null);	//Clear that ID.
			throw ve;
		} catch (StorageException | SourceGraphException e) {
			throw new VisualisationException(e);
		}
	}
}
