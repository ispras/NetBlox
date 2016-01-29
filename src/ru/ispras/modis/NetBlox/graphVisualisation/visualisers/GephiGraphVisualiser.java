package ru.ispras.modis.NetBlox.graphVisualisation.visualisers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;

/**
 * Uses Gephi toolkit to visualise a graph (or some part of it) with a force directed layout.
 * 
 * @author ilya
 */
public class GephiGraphVisualiser extends GraphVisualiser {
	protected ProjectController gephiProjectController;
	protected LayoutBuilder layoutBuilder;


	public GephiGraphVisualiser(GraphVisualisationDescription visualisationDescription) {
		super(visualisationDescription);

		gephiProjectController = Lookup.getDefault().lookup(ProjectController.class);
		gephiProjectController.newProject();
		setLayoutBuilder();
	}

	/**
	 * These two methods should be overridden in children to specify which layout is to be used.
	 */
	protected void setLayoutBuilder()	{
		layoutBuilder = new FruchtermanReingoldBuilder();
	}
	protected Layout makeLayout()	{
		return new FruchtermanReingold(layoutBuilder);
	}


	@Override
	public void visualise(IGraph graph, GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters)
			throws VisualisationException {
		Workspace gephiWorkspace = gephiProjectController.getCurrentWorkspace();
		gephiProjectController.cleanWorkspace(gephiWorkspace);

		GraphModel graphModel = produceGraphModel(graph);

		buildLayout(graphModel);

		export(initialGraphParameters, miningParameters);
	}


	@Override
	public void visualise(IGraph graph, ISetOfGroupsOfNodes setOfGroupsOfNodes, GraphParametersSet initialGraphParameters,
			ExtendedMiningParameters miningParameters) throws VisualisationException {
		// TODO Auto-generated method stub
		
	}


	private GraphModel produceGraphModel(IGraph graph)	{
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

		org.gephi.graph.api.Graph gephiGraph;
		if (graph.isDirected())	{
			gephiGraph = graphModel.getDirectedGraph();
		}
		else	{
			gephiGraph = graphModel.getUndirectedGraph();
		}

		for (IGraph.INode node : graph.getNodes())	{
			
			org.gephi.graph.api.Node gephiNode = obtainGephiNode(node, gephiGraph, graphModel);

			Collection<IGraph.INode> neighbours = graph.getNeighbours(node);
			for (IGraph.INode neighbour : neighbours)	{
				org.gephi.graph.api.Node gephiNeighbour = obtainGephiNode(neighbour, gephiGraph, graphModel);

				Float edgeWeight = graph.getEdgeWeight(node, neighbour);
				obtainGephiEdge(gephiNode, gephiNeighbour, edgeWeight, graph.isDirected(), gephiGraph, graphModel);
			}
		}

		return graphModel;
	}

	private org.gephi.graph.api.Node obtainGephiNode(IGraph.INode node, org.gephi.graph.api.Graph gephiGraph, GraphModel graphModel)	{
		String nodeId = node.getId().toString();

		org.gephi.graph.api.Node gephiNode = gephiGraph.getNode(nodeId);
		if (gephiNode == null)	{
			gephiNode = graphModel.factory().newNode(nodeId);

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


	protected void buildLayout(GraphModel graphModel)	{
		Layout layout = makeLayout();
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();

		layout.initAlgo();

		int maximalNumberOfIterations = graphModel.getGraph().getNodeCount();
		for (int i=0 ; i<maximalNumberOfIterations && layout.canAlgo() ; i++)	{
			layout.goAlgo();
		}

		layout.endAlgo();
	}


	protected void export(GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters) throws VisualisationException	{
		ExportController exportController = Lookup.getDefault().lookup(ExportController.class);

		try {
			String exportFilename = makePdfExportFilePathname(initialGraphParameters, miningParameters);
			exportController.exportFile(new File(exportFilename));
		} catch (IOException e) {
			throw new VisualisationException(e);
		}
	}
}
