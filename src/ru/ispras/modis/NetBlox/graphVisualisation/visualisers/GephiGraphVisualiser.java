package ru.ispras.modis.NetBlox.graphVisualisation.visualisers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.NodeData;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageWriter;
import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.IGroupOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.IPackOfGraphStructures;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.PackOfGraphs;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.graphVisualisation.MinedDataDealer;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription.FinalPresentationType;
import ru.ispras.modis.NetBlox.utils.MiningJobBase;

/**
 * Uses Gephi toolkit to visualise a graph (or some part of it) with a force directed layout.
 * 
 * @author ilya
 */
public class GephiGraphVisualiser extends GraphVisualiser {
	protected ProjectController gephiProjectController;
	protected LayoutBuilder layoutBuilder;

	private float colourWeightStep = 1;

	private static final String EMPTY_NODE_LABEL_PREFIX = "";
	private static final int RED_COLOUR_POSITION = 1;
	private static final int GREEN_COLOUR_POSITION = 2;
	private static final int BLUE_COLOUR_POSITION = 3;


	public GephiGraphVisualiser(GraphVisualisationDescription visualisationDescription, MiningJobBase.JobBase minedDataType) {
		super(visualisationDescription, minedDataType);

		gephiProjectController = Lookup.getDefault().lookup(ProjectController.class);
		setLayoutBuilder();
	}

	/**
	 * These two methods should be overridden in children to specify which layout is to be used.
	 */
	protected void setLayoutBuilder()	{
		//layoutBuilder = new FruchtermanReingoldBuilder();
		//layoutBuilder = new ForceAtlas();
		layoutBuilder = new ForceAtlas2Builder();
		//layoutBuilder = new CircleLayoutBuilder();
	}
	protected Layout makeLayout()	{
		//return new FruchtermanReingold(layoutBuilder);
		//return new ForceAtlasLayout(layoutBuilder);
		return new ForceAtlas2((ForceAtlas2Builder) layoutBuilder);
		//return new CircleLayout(layoutBuilder, 1, false);
	}


	@Override
	public void visualise(IGraph graph, GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters, Integer timeSlice)
			throws VisualisationException {
		visualise(graph, null, initialGraphParameters, miningParameters, timeSlice);
	}

	/**
	 * Draw a graph or multiple subgraphs for groups of nodes on one canvas (if present) for the fixed time slice.
	 */
	@Override
	public void visualise(IGraph graph, IPackOfGraphStructures<?> packOfGraphStructures, GraphParametersSet initialGraphParameters,
			ExtendedMiningParameters extendedMiningParameters, Integer timeSlice) throws VisualisationException {
		gephiProjectController.newProject();	//gephiProjectController.cleanWorkspace(gephiWorkspace) doesn't work.

		formLayout(graph, packOfGraphStructures);

		export(initialGraphParameters, extendedMiningParameters, timeSlice);

		gephiProjectController.closeCurrentProject();
	}

	/**
	 * Everything is drawn over one same graph layout.
	 */
	public void visualiseGroupsOverGraph(IGraph graph, GraphOnDriveHandler initialGraphHandler, ExtendedMiningParameters extendedMiningParameters)
			throws VisualisationException {
		gephiProjectController.newProject();	//gephiProjectController.cleanWorkspace(gephiWorkspace) doesn't work.

		GraphModel graphModel = formLayout(graph, null);

		List<ISetOfGroupsOfNodes> setsOfFinalGroups = prepareGroupStructures(graph, initialGraphHandler, extendedMiningParameters, null);
		for (ISetOfGroupsOfNodes finalGroupsOfNodes : setsOfFinalGroups)	{
			List<IGroupOfNodes> sortedFinalGroups = MinedDataDealer.sortGroupsBySizeInDescendingOrder(finalGroupsOfNodes);
	
			GraphParametersSet initialGraphParameters = initialGraphHandler.getGraphParameters();
	
			if (extendedMiningParameters != null  &&  extendedMiningParameters.getMiningParameters().considerTimeSlices())	{
				for (Integer timeSlice : extendedMiningParameters.getMiningParameters().getTimeSlices())	{
					ISetOfGroupsOfNodes groupsForTimeSlice = (ISetOfGroupsOfNodes) MinedDataDealer.getMined(
							minedDataType, initialGraphHandler, extendedMiningParameters, timeSlice);
					if (groupsForTimeSlice == null)	{	//There were no results for this timeSlice.
						continue;
					}
					paintGroups(graphModel, graph, groupsForTimeSlice, sortedFinalGroups, initialGraphParameters, extendedMiningParameters, timeSlice);
				}
			}
			else	{
				paintGroups(graphModel, graph, finalGroupsOfNodes, sortedFinalGroups, initialGraphParameters, extendedMiningParameters, null);
			}
		}

		gephiProjectController.closeCurrentProject();
	}

	@Override
	public void visualiseSubgraphsOverGraph(IGraph graph, GraphOnDriveHandler initialGraphHandler, ExtendedMiningParameters extendedMiningParameters)
			throws VisualisationException	{
		gephiProjectController.newProject();

		GraphModel graphModel = formLayout(graph, null);	//Draw original graph (visualisation base).

		List<IGraph> finalSubgraphs = prepareGraphStructures(initialGraphHandler, extendedMiningParameters, null);
		//XXX Sort them?

		GraphParametersSet initialGraphParameters = initialGraphHandler.getGraphParameters();

		if (extendedMiningParameters != null  &&  extendedMiningParameters.getMiningParameters().considerTimeSlices())	{
			for (Integer timeSlice : extendedMiningParameters.getMiningParameters().getTimeSlices())	{
				PackOfGraphs subgraphsForTimeSlice = (PackOfGraphs) MinedDataDealer.getMined(
						minedDataType, initialGraphHandler, extendedMiningParameters, timeSlice);
				if (subgraphsForTimeSlice==null || subgraphsForTimeSlice.isEmpty())	{	//There were no results for this timeSlice.
					continue;
				}

				paintSubgraphs(graphModel, subgraphsForTimeSlice.getList(), finalSubgraphs, initialGraphParameters, extendedMiningParameters, timeSlice);
			}
		}
		else	{
			paintSubgraphs(graphModel, finalSubgraphs, finalSubgraphs, initialGraphParameters, extendedMiningParameters, null);
		}

		gephiProjectController.closeCurrentProject();
	}


	public GraphModel formLayout(IGraph graph, IPackOfGraphStructures<?> packOfGraphStructures)	{
		if (graph.isWeighted())	{
			colourWeightStep = 2f / graph.getMaxEdgeWeight();
		}
		else	{
			colourWeightStep = 1;
		}

		GraphModel graphModel = produceGraphModel(graph, packOfGraphStructures);

		buildLayout(graphModel);

		tweakAppearance();

		return graphModel;
	}


	private GraphModel produceGraphModel(IGraph graph, IPackOfGraphStructures<?> packOfGraphStructures)	{
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

		org.gephi.graph.api.Graph gephiGraph;
		if (graph.isDirected())	{
			gephiGraph = graphModel.getDirectedGraph();
		}
		else	{
			gephiGraph = graphModel.getUndirectedGraph();
		}

		int subgraphsCounter = 0;
		if (packOfGraphStructures != null)	{
			switch (minedDataType)	{
			case NODES_GROUPS_SET:
				for (IGroupOfNodes community : (ISetOfGroupsOfNodes)packOfGraphStructures)	{
					IGraph communitySubgraph = graph.getSubgraphForGroup(community);
					chargeGephiGraph(communitySubgraph, String.valueOf(++subgraphsCounter), gephiGraph, graphModel);
				}
				break;
			default:
				for (Object subgraph : packOfGraphStructures)	{
					chargeGephiGraph((IGraph)subgraph, String.valueOf(++subgraphsCounter), gephiGraph, graphModel);
				}
				break;
			}
		}
		else	{
			chargeGephiGraph(graph, EMPTY_NODE_LABEL_PREFIX, gephiGraph, graphModel);
		}

		return graphModel;
	}

	private void chargeGephiGraph(IGraph graph, String nodeLabelPrefix, org.gephi.graph.api.Graph gephiGraph, GraphModel graphModel)	{
		Collection<IGraph.INode> nodes = graph.getNodes();
		for (IGraph.INode node : nodes)	{
			org.gephi.graph.api.Node gephiNode = obtainGephiNode(node, nodeLabelPrefix, gephiGraph, graphModel);

			Collection<IGraph.INode> neighbours = graph.getOutcomingNeighbours(node);
			for (IGraph.INode neighbour : neighbours)	{
				if (!nodes.contains(neighbour))	{	//Exclude node's neighbours that are outside of the current (sub)graph.
					continue;
				}
				org.gephi.graph.api.Node gephiNeighbour = obtainGephiNode(neighbour, nodeLabelPrefix, gephiGraph, graphModel);

				Float edgeWeight = graph.getEdgeWeight(node, neighbour);

				org.gephi.graph.api.Edge edge = obtainGephiEdge(gephiNode, gephiNeighbour, edgeWeight, graph.isDirected(), gephiGraph, graphModel);
				paintEdge(edge, BLUE_COLOUR_POSITION);
			}

			NodeData nodeData = gephiNode.getNodeData();
			float nodeSize = neighbours.size() * visualisationDescription.getNodesSizeCorrectionCoefficient();
			nodeData.setSize(nodeSize);
		}
	}

	private org.gephi.graph.api.Node obtainGephiNode(IGraph.INode node, String labelPrefix, org.gephi.graph.api.Graph gephiGraph, GraphModel graphModel) {
		String nodeID = makeGephiNodeID(node, labelPrefix);

		org.gephi.graph.api.Node gephiNode = gephiGraph.getNode(nodeID);
		if (gephiNode == null)	{
			gephiNode = graphModel.factory().newNode(nodeID);

			gephiNode.getNodeData().setLabel(labelPrefix);

			gephiGraph.addNode(gephiNode);
		}

		return gephiNode;
	}

	protected org.gephi.graph.api.Edge obtainGephiEdge(org.gephi.graph.api.Node node1, org.gephi.graph.api.Node node2, Float weight, boolean directed,
			org.gephi.graph.api.Graph gephiGraph, GraphModel graphModel)	{
		org.gephi.graph.api.Edge edge = gephiGraph.getEdge(node1, node2);
		if (edge == null)	{
			edge = graphModel.factory().newEdge(node1, node2, weight, directed);

			gephiGraph.addEdge(edge);
		}

		return edge;
	}

	private String makeGephiNodeID(IGraph.INode node, String labelPrefix)	{
		String nodeID = labelPrefix + (labelPrefix.isEmpty()?"":"_") + node.getId().toString();
		return nodeID;
	}


	protected void buildLayout(GraphModel graphModel)	{
		Layout layout = makeLayout();
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();

		if (layout instanceof ForceAtlas2)	{
			//((ForceAtlas2)layout).setScalingRatio(1000.0);
			//((ForceAtlas2)layout).setGravity(2000.0);
			//((ForceAtlas2)layout).setScalingRatio(180.0);
			//((ForceAtlas2)layout).setGravity(100.0);
			//((ForceAtlas2)layout).setScalingRatio(10.0);
			if (visualisationDescription.getRepulsionCoefficient() != null)	{
				((ForceAtlas2)layout).setScalingRatio(visualisationDescription.getRepulsionCoefficient().doubleValue());
			}
			if (visualisationDescription.getGravityCoefficient() != null)	{
				((ForceAtlas2)layout).setGravity(visualisationDescription.getGravityCoefficient().doubleValue());
			}
			if (visualisationDescription.getNormalisedEdgeWeightInfluence() != null)	{
				((ForceAtlas2)layout).setEdgeWeightInfluence((double)
						colourWeightStep * 0.5 * visualisationDescription.getNormalisedEdgeWeightInfluence());
			}

			//((ForceAtlas2)layout).setLinLogMode(true);
		}

		layout.initAlgo();

		int maximalNumberOfIterations = graphModel.getGraph().getNodeCount();
		for (int i=0 ; i<maximalNumberOfIterations && layout.canAlgo() ; i++)	{
			layout.goAlgo();
		}

		if (layout instanceof ForceAtlas2)	{
			((ForceAtlas2)layout).setAdjustSizes(true);
			for (int i=0 ; i<5 && layout.canAlgo() ; i++)	{
				layout.goAlgo();
			}
		}

		layout.endAlgo();
	}


	private void paintGroups(GraphModel graphModel, IGraph graph, ISetOfGroupsOfNodes setOfGroups, List<IGroupOfNodes> sortedFinalGroups,
			GraphParametersSet initialGraphParameters, ExtendedMiningParameters extendedMiningParameters, Integer timeSlice)
					throws VisualisationException	{
		for (IGroupOfNodes group : setOfGroups)	{
			if (visualisationDescription.getSubstructuresFinalPresentationType() == FinalPresentationType.MULTIPLE_CANVAS)	{
				paintBasicColour(graphModel);
				paintNodesAndEdges(graphModel, graph, group, EMPTY_NODE_LABEL_PREFIX);

				setStructureID(sortedFinalGroups, group);
				export(initialGraphParameters, extendedMiningParameters, timeSlice);
			}
			else	{
				//TODO This is a temporary solution. Make multicolor painting.
				paintNodesAndEdges(graphModel, graph, group, EMPTY_NODE_LABEL_PREFIX);
			}
		}
		setStructureID(null, null);	//Clear that ID.
		if (visualisationDescription.getSubstructuresFinalPresentationType() == FinalPresentationType.ONE_CANVAS)	{
			export(initialGraphParameters, extendedMiningParameters, timeSlice);
		}
	}
	private void paintSubgraphs(GraphModel graphModel, Collection<IGraph> subgraphs, List<IGraph> finalSubgraphs,
			GraphParametersSet initialGraphParameters, ExtendedMiningParameters extendedMiningParameters, Integer timeSlice)
					throws VisualisationException	{
		for (IGraph subgraph : subgraphs)	{
			if (visualisationDescription.getSubstructuresFinalPresentationType() == FinalPresentationType.MULTIPLE_CANVAS)	{
				paintBasicColour(graphModel);
				paintNodesAndEdges(graphModel, subgraph, EMPTY_NODE_LABEL_PREFIX);

				setStructureID(finalSubgraphs, subgraph);
				export(initialGraphParameters, extendedMiningParameters, timeSlice);
			}
			else	{
				//TODO This is a temporary solution. Make multicolor painting.
				paintNodesAndEdges(graphModel, subgraph, EMPTY_NODE_LABEL_PREFIX);
			}
		}
		setStructureID(null, null);	//Clear that ID.
		if (visualisationDescription.getSubstructuresFinalPresentationType() == FinalPresentationType.ONE_CANVAS)	{
			export(initialGraphParameters, extendedMiningParameters, timeSlice);
		}
	}

	private void paintBasicColour(GraphModel graphModel)	{
		org.gephi.graph.api.Graph gephiGraph = graphModel.getGraph();
		for (org.gephi.graph.api.Node gephiNode : gephiGraph.getNodes())	{
			gephiNode.getNodeData().setColor(0.5f, 0.5f, 0.5f);
			for (org.gephi.graph.api.Edge edge : gephiGraph.getEdges(gephiNode))	{
				paintEdge(edge, BLUE_COLOUR_POSITION);
			}
		}
	}
	private void paintNodesAndEdges(GraphModel graphModel, IGraph graph, IGroupOfNodes groupOfNodes, String nodeLabelPrefix)	{
		org.gephi.graph.api.Graph gephiGraph = graphModel.getGraph();
		for (IGraph.INode node : groupOfNodes)	{
			String gephiNodeID = makeGephiNodeID(node, nodeLabelPrefix);
			org.gephi.graph.api.Node gephiNode = gephiGraph.getNode(gephiNodeID);
			gephiNode.getNodeData().setColor(1, 0, 0);

			for (IGraph.INode neighbour : graph.getNeighbours(node))	{	//Paint edges between group nodes and incident edges.
				//if (!groupOfNodes.contains(neighbour))	{
				//	continue;
				//}
				String gephiNeighbourID = makeGephiNodeID(neighbour, nodeLabelPrefix);
				org.gephi.graph.api.Node gephiNeighbour = gephiGraph.getNode(gephiNeighbourID);

				boolean isInnerEdge = groupOfNodes.contains(neighbour);
				paintEdgeInDirection(gephiGraph, gephiNode, gephiNeighbour, isInnerEdge, true);
				paintEdgeInDirection(gephiGraph, gephiNode, gephiNeighbour, isInnerEdge, false);
			}
		}
	}
	private void paintNodesAndEdges(GraphModel graphModel, IGraph subgraph, String nodeLabelPrefix)	{
		org.gephi.graph.api.Graph gephiGraph = graphModel.getGraph();
		for (IGraph.INode node : subgraph.getNodes())	{
			String gephiNodeID = makeGephiNodeID(node, nodeLabelPrefix);
			org.gephi.graph.api.Node gephiNode = gephiGraph.getNode(gephiNodeID);
			gephiNode.getNodeData().setColor(1, 0, 0);

			for (IGraph.INode outNeighbour : subgraph.getOutcomingNeighbours(node))	{	//Paint edges of the subgraph.
				String gephiNeighbourID = makeGephiNodeID(outNeighbour, nodeLabelPrefix);
				org.gephi.graph.api.Node gephiNeighbour = gephiGraph.getNode(gephiNeighbourID);

				paintEdgeInDirection(gephiGraph, gephiNode, gephiNeighbour, true, true);
			}
		}
	}
	private void paintEdgeInDirection(org.gephi.graph.api.Graph graph, org.gephi.graph.api.Node node, org.gephi.graph.api.Node neighbour,
			boolean isInner, boolean straightDirection)	{
		org.gephi.graph.api.Edge edge = straightDirection ?
				graph.getEdge(node, neighbour) :
					graph.getEdge(neighbour, node);	//Check reverse edge, that is necessary for incident edges.
		if (edge == null)	{
			return;
		}
		if (isInner)	{
			paintEdge(edge, RED_COLOUR_POSITION);
		}
		else	{
			paintEdge(edge, GREEN_COLOUR_POSITION);
		}
	}
	private void paintEdge(org.gephi.graph.api.Edge edge, int colourChannel)	{
		float edgeWeight = edge.getWeight();

		float colour = Math.min(edgeWeight*colourWeightStep, 1);
		float not_colour = Math.max(0, edgeWeight*colourWeightStep - 1);

		if (colourChannel == RED_COLOUR_POSITION)	{
			edge.getEdgeData().setColor(colour, not_colour, not_colour);
		}
		else if (colourChannel == GREEN_COLOUR_POSITION)	{
			edge.getEdgeData().setColor(not_colour, colour, not_colour);
		}
		else if (colourChannel == BLUE_COLOUR_POSITION)	{
			edge.getEdgeData().setColor(not_colour, not_colour, colour);
		}
		//edge.getEdgeData().setColor(blue, blue, blue);
		//edge.getEdgeData().setColor(1 - blue, 1 - blue, 1 - not_blue);
		//edge.getEdgeData().setColor(1 - edgeWeight*colourWeightStep, 1 - edgeWeight*colourWeightStep, 1);
		//edge.getEdgeData().setColor(1 - edgeWeight*colourWeightStep, 1 - edgeWeight*colourWeightStep, 1 - edgeWeight*colourWeightStep);
		//edge.getEdgeData().setAlpha(1 - edgeWeight*colourWeightStep);
		//edge.getEdgeData().setAlpha(1);
	}


	private void tweakAppearance()	{
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, false);

		EdgeColor edgeColor = new EdgeColor(EdgeColor.Mode.ORIGINAL);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, edgeColor);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, colourWeightStep*2.5);

		previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, visualisationDescription.getBackgroundColour());

		//previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, true);
	}


	protected void export(GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters, Integer timeSlice)
			throws VisualisationException	{
		ExportController exportController = Lookup.getDefault().lookup(ExportController.class);

		String exportFilename = makePDFExportFilePathname(initialGraphParameters, miningParameters, timeSlice);
		try {
			StorageWriter.makeSureDirectoryExists(Paths.get(exportFilename).getParent());

			exportController.exportFile(new File(exportFilename));
		} catch (IOException e) {
			throw new VisualisationException(e);
		}
	}
}
