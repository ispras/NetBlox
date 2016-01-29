package ru.ispras.modis.NetBlox.graphVisualisation;

import java.util.Collection;
import java.util.LinkedList;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.IGroupOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.SetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.ClustersGraphVisualiser;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.GephiGraphVisualiser;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.GraphVisualiser;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.MatrixGraphVisualiser;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public class GraphVisualisationManager {
	private static final int MINIMAL_COMMUNITY_SIZE_FOR_VISUALISATION = 1;

	private ScenarioTask scenarioTask;

	public GraphVisualisationManager(ScenarioTask task)	{
		scenarioTask = task;
	}


	public void visualise(IGraph graph, GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters)
			throws VisualisationException	{	//Parameters are passed for result name generation.
		for (GraphVisualisationDescription visualisationDescription : scenarioTask.getGraphVisualisationDescriptions())	{
			if (!visualisationDescription.visualiseGraph() && !visualisationDescription.visualiseGroupsOfNodes())	{
				throw new VisualisationException("Description tells to visualise neither graph nor groups of its nodes.");
			}
	
			GraphVisualiser visualiser = getVisualiser(visualisationDescription);
			visualiser.visualise(graph, initialGraphParameters, miningParameters);
		}
	}

	public void visualise(IGraph graph, ISetOfGroupsOfNodes setOfGroupsOfNodes, GraphParametersSet initialGraphParameters,
			ExtendedMiningParameters miningParameters) throws VisualisationException	{	//Parameters are passed for result name generation.
		for (GraphVisualisationDescription visualisationDescription : scenarioTask.getGraphVisualisationDescriptions())	{
			if (!visualisationDescription.visualiseGraph() && !visualisationDescription.visualiseGroupsOfNodes())	{
				throw new VisualisationException("Description tells to visualise neither graph nor groups of its nodes.");
			}
	
			GroupsOfNodesSelectionCriteria communitiesSelectionCriteria = getCommunitiesSelectionCriteria(visualisationDescription);
	
			setOfGroupsOfNodes = filterCommunities(setOfGroupsOfNodes, communitiesSelectionCriteria);
	
			visualise(graph, setOfGroupsOfNodes, visualisationDescription, initialGraphParameters, miningParameters);
		}
	}


	private GroupsOfNodesSelectionCriteria getCommunitiesSelectionCriteria(GraphVisualisationDescription visualisationDescription)	{
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

		return new SetOfGroupsOfNodes(filteredCommunities);	//, communities.getOriginalGraph());
	}


	private void visualise(IGraph graph, ISetOfGroupsOfNodes setOfGroupsOfNodes, GraphVisualisationDescription visualisationDescription,
			GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters)	{
		GraphVisualiser visualiser = getVisualiser(visualisationDescription);

		if (visualisationDescription.visualiseGraph() && visualisationDescription.visualiseGroupsOfNodes())	{
			try {
				visualiser.visualise(graph, setOfGroupsOfNodes, initialGraphParameters, miningParameters);
			} catch (VisualisationException e) {
				e.printStackTrace();
			}
		}
		else if (visualisationDescription.visualiseGraph())	{
			try {
				visualiser.visualise(graph, initialGraphParameters, miningParameters);
			} catch (VisualisationException e) {
				e.printStackTrace();
			}
		}
		else if (visualisationDescription.visualiseGroupsOfNodes())	{
			for (IGroupOfNodes community : setOfGroupsOfNodes)	{
				if (community.size() < MINIMAL_COMMUNITY_SIZE_FOR_VISUALISATION)	{
					continue;
				}

				IGraph communitySubgraph = graph.getSubgraphForGroup(community);
				try {
					visualiser.visualise(communitySubgraph, initialGraphParameters, miningParameters);
				} catch (VisualisationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private GraphVisualiser getVisualiser(GraphVisualisationDescription visualisationDescription)	{
		GraphVisualiser visualiser = null;
		switch (visualisationDescription.getMethod())	{
		case MATRIX:
			visualiser = new MatrixGraphVisualiser(visualisationDescription);
			break;
		case CLUSTERS_GRAPH:
			visualiser = new ClustersGraphVisualiser(visualisationDescription);
			break;
		/*case CIRCULAR:
			visualiser = new CircularGradientGraphVisualiser(visualisationDescription);
			break;*/
		case FORCE_DIRECTED:
			visualiser = new GephiGraphVisualiser(visualisationDescription);
			break;
		}

		return visualiser;
	}
}
