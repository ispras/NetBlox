package ru.ispras.modis.NetBlox.dataStructures;

import java.util.Collection;
import java.util.List;

import ru.ispras.modis.NetBlox.utils.Pair;

/**
 * The interface for all internal representations of graphs.
 * 
 * @author ilya
 */
public interface IGraph {
	/**
	 * The interface for all internal representations of nodes.
	 */
	public interface INode	{
		public Integer getId();	//XXX Make String ID?

		public void setAttribute(String attributeName, String attributeValue);
		public String getAttribute(String attributeName);
	}



	/**
	 * Get node with ID equal <code>id</code>.
	 * @param id	- the ID of the node.
	 * @return	INode
	 */
	public INode getNode(Integer id);

	public Collection<INode> getNodes();

	/**
	 * Get all the edges of the graph as pairs of nodes.
	 * @return
	 */
	public Collection<Pair<INode, INode>> getEdges();

	public Collection<INode> getNeighbours(INode node);

	public Collection<INode> getNeighboursInGroup(INode node, IGroupOfNodes group);

	public IGraph getSubgraphForGroup(IGroupOfNodes group);


	/**
	 * Check whether the edges between the nodes node1 and node2 exists in this graph.
	 * @param node1
	 * @param node2
	 * @return
	 */
	public boolean hasEdge(INode node1, INode node2);

	/**
	 * Get the weight of an edge between node1 and node2. 
	 * @param node1
	 * @param node2
	 * @return	the weight of the edge between the nodes. Equals 1 if the graph is unweighted. null if there's no such edge.
	 */
	public Float getEdgeWeight(INode node1, INode node2);


	/**
	 * Number of nodes.
	 */
	public int size();

	public boolean isDirected();

	public boolean isWeighted();


	public boolean hasNodeAttributes();

	public List<String> getNodeAttributesNames();
}