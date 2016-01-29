package ru.ispras.modis.NetBlox.graphVisualisation.visualisers;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.plugin.circularlayout.circlelayout.CircleLayout;
import org.gephi.layout.plugin.circularlayout.circlelayout.CircleLayoutBuilder;
import org.gephi.layout.spi.Layout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.IGroupOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;

/**
 * <p>This visualiser presents known graph clusters as nodes in a new metagraph, that are
 * connected by edges when clusters overlap.</p>
 * <p>The size of a node corresponds to the size of cluster (community, group of nodes),
 * and the weight of an edge between two nodes reflects the degree of overlap of the
 * clusters.</p>
 * 
 * @author ilya
 */
public class ClustersGraphVisualiser extends GephiGraphVisualiser {

	public ClustersGraphVisualiser(GraphVisualisationDescription visualisationDescription) {
		super(visualisationDescription);
	}

	protected void setLayoutBuilder()	{
		//layoutBuilder = new ForceAtlas();
		//layoutBuilder = new ForceAtlas2Builder();
		layoutBuilder = new CircleLayoutBuilder();
	}
	protected Layout makeLayout()	{
		//return new ForceAtlasLayout(layoutBuilder);
		//return new ForceAtlas2((ForceAtlas2Builder) layoutBuilder);
		return new CircleLayout(layoutBuilder, 1, false);
	}


	@Override
	public void visualise(IGraph graph, ISetOfGroupsOfNodes setOfGroupsOfNodes, GraphParametersSet initialGraphParameters,
			ExtendedMiningParameters miningParameters) throws VisualisationException {
		Workspace gephiWorkspace = gephiProjectController.getCurrentWorkspace();
		gephiProjectController.cleanWorkspace(gephiWorkspace);

		GraphModel graphModel = produceGraphModel(graph, setOfGroupsOfNodes);

		buildLayout(graphModel);

		changeColorOfEdges();

		export(initialGraphParameters, miningParameters);
	}


	private GraphModel produceGraphModel(IGraph graph, ISetOfGroupsOfNodes setOfGroupsOfNodes)	{
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

		org.gephi.graph.api.Graph gephiGraph = graphModel.getUndirectedGraph();

		for (IGroupOfNodes community1 : setOfGroupsOfNodes)	{
			for (IGroupOfNodes community2 : setOfGroupsOfNodes)	{
				if (community1.equals(community2))	{
					continue;
				}

				org.gephi.graph.api.Node gephiNode1 = obtainGephiNode(community1, gephiGraph, graphModel);
				org.gephi.graph.api.Node gephiNode2 = obtainGephiNode(community2, gephiGraph, graphModel);

				float edgeWeight = countCommonNodes(community1, community2);
				if (edgeWeight >= visualisationDescription.getMinimalNumberOfNodesInOverlapToVisualiseIt())	{
					obtainGephiEdge(gephiNode1, gephiNode2, (edgeWeight/visualisationDescription.getMinimalNumberOfNodesInOverlapToVisualiseIt()),
							graph.isDirected(), gephiGraph, graphModel);
				}
			}
		}

		return graphModel;
	}

	private int countCommonNodes(IGroupOfNodes group1, IGroupOfNodes group2)	{
		int commonNodesCounter = 0;
		for (IGraph.INode node1 : group1)	{
			for (IGraph.INode node2 : group2)	{
				if (node1.equals(node2))	{
					commonNodesCounter++;
				}
			}
		}
		return commonNodesCounter;
	}


	private org.gephi.graph.api.Node obtainGephiNode(IGroupOfNodes community, org.gephi.graph.api.Graph gephiGraph, GraphModel graphModel)	{
		String communityId = String.valueOf(community.hashCode());

		org.gephi.graph.api.Node gephiNode = gephiGraph.getNode(communityId);
		if (gephiNode == null)	{
			gephiNode = graphModel.factory().newNode(communityId);

			NodeData nodeData = gephiNode.getNodeData();
			float nodeSize = community.size() * visualisationDescription.getNodesSizeCorrectionCoefficient();
			nodeData.setSize(nodeSize);
			nodeData.setColor(0, 0, 1);	//blue

			gephiGraph.addNode(gephiNode);
		}

		return gephiNode;
	}


	/**
	 * The <code>PreviewController</code> is the class that is responsible for how the graph data image will be rendered for output.
	 */
	private void changeColorOfEdges()	{
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		EdgeColor edgeColor = new EdgeColor(EdgeColor.Mode.ORIGINAL);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, edgeColor);
	}
}
