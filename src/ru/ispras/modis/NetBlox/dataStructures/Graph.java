package ru.ispras.modis.NetBlox.dataStructures;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.utils.Pair;

/**
 * The inner representation of a graph.
 * 
 * @author ilya
 */
public class Graph implements IGraph {	//FUTURE_WORK Separate into DirectedGraph and UndirectedGraph?
	public class Node implements INode	{
		private Integer id;
		private Set<INode> outcomingEdgesTo;	//FUTURE_WORK Should we consider multiple edges between the same two nodes?
		private Map<INode, Float> outcomingWeightedEdgesTo;
		private Set<INode> incomingEdgesFrom;

		private Map<String, String> nodeAttributes;

		public Node(Integer id)	{
			this.id = id;

			outcomingEdgesTo = new HashSet<INode>();
			outcomingWeightedEdgesTo = new HashMap<INode, Float>();
			//FUTURE_WORK Check whether a graph is weighted? Or do we consider graphs that contain both weighted and unweighted edges?

			if (directed)	{
				incomingEdgesFrom = new HashSet<INode>();
			}
		}


		public Integer getId()	{
			return id;
		}

		public void setAttribute(String attributeName, String attributeValue)	{
			if (nodeAttributes == null)	{
				nodeAttributes = new HashMap<String, String>();
			}
			nodeAttributes.put(attributeName, attributeValue);
		}
		public String getAttribute(String attributeName)	{
			if (nodeAttributes == null)	{
				return null;
			}
			return nodeAttributes.get(attributeName);
		}


		public void addEdgeTo(INode neighbour)	{
			outcomingEdgesTo.add(neighbour);
		}
		public void addEdgeTo(INode neighbour, Float edgeWeight)	{
			if (weighted  &&  edgeWeight != null)	{
				outcomingWeightedEdgesTo.put(neighbour, edgeWeight);
			}
			else	{
				addEdgeTo(neighbour);
			}
		}
		public void addEdgeFrom(INode neighbour)	{
			incomingEdgesFrom.add(neighbour);
		}

		public boolean hasEdgeTo(INode neighbour)	{
			return outcomingEdgesTo.contains(neighbour) || outcomingWeightedEdgesTo.containsKey(neighbour);
		}

		public float getWeightOfEdgeTo(INode neighbour)	{
			return outcomingWeightedEdgesTo.get(neighbour);
		}


		/**
		 * @return	the overall degree of the node. For directed graphs it is a sum of in-degree and out-degree.
		 */
		public int getDegree()	{
			int degree = outcomingEdgesTo.size() + outcomingWeightedEdgesTo.size();
			if (directed)	{
				degree += incomingEdgesFrom.size();
			}
			return degree;
		}


		/**
		 * @return	a set of all neighbours, both for in- and out-coming edges.
		 */
		public Set<INode> getNeighbours()	{
			Set<INode> neighbours = new HashSet<INode>(getOutcomingNeighbours());
			if (directed)	{
				neighbours.addAll(incomingEdgesFrom);
			}
			return neighbours;
		}

		public Collection<INode> getIncomingNeighbours()	{
			Collection<INode> neighbours;
			if (directed)	{
				neighbours = new ArrayList<INode>(incomingEdgesFrom);
			}
			else	{
				neighbours = getOutcomingNeighbours();
			}
			return neighbours;
		}

		public Collection<INode> getOutcomingNeighbours()	{
			Collection<INode> neighbours;
			if (!outcomingWeightedEdgesTo.isEmpty())	{
				neighbours = new ArrayList<INode>(outcomingWeightedEdgesTo.keySet());
			}
			else	{
				neighbours = new ArrayList<INode>(outcomingEdgesTo);
			}
			return neighbours;
		}


		@Override
		public boolean equals(Object obj)	{
			if (!(obj instanceof Node))	{
				return super.equals(obj);
			}
			return id.equals(((Node)obj).id);
		}

		@Override
		public int hashCode()	{
			return id.hashCode();
		}
	}


	private static final String WHITESPACE_CHARACTER_REGEX = "\\s";
	private static final int FIRST_NODE_POSITION = 0;
	private static final int SECOND_NODE_POSITION = 1;
	private static final int WEIGHT_POSITION = 2;

	public static final String ATTRIBUTES_NAMES_LINE_PREFIX = "%";
	public static final String ATTRIBUTES_VALUES_DELIMITER_REGEX = ",";

	private Map<Integer, Node> nodes;
	private boolean directed;
	private boolean weighted;
	private Float maxEdgeWeight = null;
	private List<String> nodeAttributesNames;

	private Integer id;	//FUTURE_WORK The IDs of graphs are not stored or used anywhere outside the class. This part needs to be developed.


	public Graph(boolean directed, boolean weighted)	{
		nodes = new HashMap<Integer, Node>();
		this.directed = directed;
		this.weighted = weighted;
	}

	public Graph(String pathToGraph, boolean directed, boolean weighted)	{
		this(directed, weighted);
		parseGraph(pathToGraph);
	}

	public Graph(String pathToGraph, String pathToNodeAttributesFile, boolean directed, boolean weighted) throws SourceGraphException	{
		this(pathToGraph, directed, weighted);
		parseNodesAttributesFile(pathToNodeAttributesFile);
	}


	private void parseGraph(String pathToGraph)	{
		try {
			List<String> graphFileLines = Files.readAllLines(Paths.get(pathToGraph), Charset.defaultCharset());
			Node[] edgeNodes = new Node[2];
			for (String edgeLine : graphFileLines)	{
				Float edgeWeight = parseNodesFromEdgeLine(edgeNodes, edgeLine);
				addEdge(edgeNodes[0], edgeNodes[1], edgeWeight);
			}
		} catch (IOException e) {	//We assume that we have already checked the existence of the graph.
			e.printStackTrace();
		}
	}

	private Float parseNodesFromEdgeLine(Node[] edgeNodes, String edgeLine)	{
		String[] idStrings = edgeLine.split(WHITESPACE_CHARACTER_REGEX);
		for (int position=FIRST_NODE_POSITION ; position<=SECOND_NODE_POSITION ; position++)	{
			Integer id = Integer.parseInt(idStrings[position]);
			if (nodes.containsKey(id))	{
				edgeNodes[position] = nodes.get(id);
			}
			else	{
				edgeNodes[position] = new Node(id);
				nodes.put(id, edgeNodes[position]);
			}
		}

		Float weight = null;
		if (weighted)	{
			if (idStrings.length == 2)	{
				weight = 1f;
			}
			else	{
				weight = Float.parseFloat(idStrings[WEIGHT_POSITION]);
			}
		}
		return weight;
	}

	private void parseNodesAttributesFile(String pathToAttributes) throws SourceGraphException	{
		Path path = Paths.get(pathToAttributes);
		if (!Files.exists(path))	{
			throw new SourceGraphException("No attributes file when expected.");
		}

		try {
			List<String> graphFileLines = Files.readAllLines(path, Charset.defaultCharset());
			if (graphFileLines.isEmpty())	{
				throw new SourceGraphException("Empty attributes file.");
			}
			Iterator<String> linesIterator = graphFileLines.iterator();

			nodeAttributesNames = parseAttributesNames(linesIterator.next());

			int numberOfUnconnectedNodes = 0;
			while (linesIterator.hasNext())	{
				numberOfUnconnectedNodes += parseNodeAttributesValues(linesIterator.next());
			}
			if (numberOfUnconnectedNodes > 0)	{
				System.out.println("\r\nWARNING: "+numberOfUnconnectedNodes+" nodes from attributes file do not have in-/out-coming edges.");
			}
		} catch (IOException e) {	//We have checked it exists.
			String errorMessage = "Couldn't read attributes file: "+e.getMessage();
			throw new SourceGraphException(errorMessage);
		}
	}

	private List<String> parseAttributesNames(String firstLine) throws SourceGraphException	{
		if (!firstLine.startsWith(ATTRIBUTES_NAMES_LINE_PREFIX))	{
			StringBuilder messageBuilder = new StringBuilder("The first line of attributes file must start with '").
					append(ATTRIBUTES_NAMES_LINE_PREFIX).append("' and contain attributes names, separated with '").
					append(ATTRIBUTES_VALUES_DELIMITER_REGEX).append("'.");
			throw new SourceGraphException(messageBuilder.toString());
		}
		firstLine = firstLine.substring(1);
		String[] names = firstLine.split(ATTRIBUTES_VALUES_DELIMITER_REGEX);
		return Arrays.asList(names);
	}

	//TODO This version doesn't consider attributes that consist of several words including commas (and other punctuation delimiters).
	private int parseNodeAttributesValues(String lineWithValues)	{
		String[] idAndValues = lineWithValues.split(WHITESPACE_CHARACTER_REGEX);
		if (idAndValues==null || idAndValues.length==0)	{
			return 0;
		}

		int numberOfUnconnectedNodes = 0;

		Integer id = Integer.parseInt(idAndValues[0]);
		Node node = nodes.get(id);
		if (node == null)	{
			//System.out.println("WARNING: Node "+idAndValues[0]+" does not have in-/out-coming edges.");
			numberOfUnconnectedNodes = 1;
			node = new Node(id);
			nodes.put(id, node);
		}

		if (idAndValues.length==1)	{
			System.out.println("WARNING: Node "+idAndValues[0]+" is listed in attributes file with NO attributes.");
			return numberOfUnconnectedNodes;
		}
		Iterator<String> attributesNamesIterator = nodeAttributesNames.iterator();
		String[] attributesValues = idAndValues[1].split(ATTRIBUTES_VALUES_DELIMITER_REGEX);
		for (int i=0 ; i<attributesValues.length ; i++)	{
			if (!attributesNamesIterator.hasNext())	{
				break;
			}
			String name = attributesNamesIterator.next();
			String value = attributesValues[i];
			node.setAttribute(name, value);
		}

		return numberOfUnconnectedNodes;
	}


	private void addEdge(Node node1, Node node2, Float weight)	{
		node1.addEdgeTo(node2, weight);
		if (directed)	{
			node2.addEdgeFrom(node1);
		}
		else	{
			node2.addEdgeTo(node1, weight);
		}

		if (weighted)	{
			if (weight == null)	{
				return;
			}
			maxEdgeWeight = (maxEdgeWeight==null) ? weight : Math.max(maxEdgeWeight, weight);
		}
		else	{
			maxEdgeWeight = 1f;
		}
	}

	@Override
	public INode getNode(Integer id)	{
		return nodes.get(id);
	}

	public Collection<INode> getNodes()	{
		Collection<INode> nodesForExport = new ArrayList<INode>(nodes.size());
		for (Node node : nodes.values())	{
			nodesForExport.add(node);
		}
		return nodesForExport;
	}

	public int size()	{
		return nodes.size();
	}

	public int getNumberOfEdges()	{
		int result = 0;
		for (Node node : nodes.values())	{
			result += node.getDegree();
		}
		result /= 2;	//Each edge is counted twice, both for directed and undirected graphs.
		return result;
	}

	public double getAverageDegree()	{
		int cumulativeDegree = 0;
		for (Node node : nodes.values())	{
			cumulativeDegree += node.getDegree();
		}

		double averageDegree = ((float)cumulativeDegree) / nodes.size();
		return averageDegree;
	}


	@Override
	public boolean isDirected()	{
		return directed;
	}

	@Override
	public boolean isWeighted()	{
		return weighted;
	}


	@Override
	public boolean hasEdge(INode iNode1, INode iNode2)	{
		Node node1 = nodes.get(iNode1.getId());
		if (node1==null)	{
			return false;
		}
		return nodes.containsKey(iNode2.getId()) && node1.hasEdgeTo(iNode2);
	}

	public void addEdge(INode iNode1, INode iNode2, Float weight)	{
		Integer id1 = iNode1.getId();
		Integer id2 = iNode2.getId();

		Node node1 = nodes.get(id1);
		if (node1 == null)	{
			node1 = new Node(id1);
			nodes.put(id1, node1);
		}
		Node node2 = nodes.get(id2);
		if (node2 == null)	{
			node2 = new Node(id2);
			nodes.put(id2, node2);
		}

		addEdge(node1, node2, weight);
	}


	/**
	 * All unique neighbours. Self-loops are possible.
	 */
	@Override
	public Collection<INode> getNeighbours(INode node)	{
		return nodes.get(node.getId()).getNeighbours();
	}
	@Override
	public Collection<INode> getIncomingNeighbours(INode node)	{
		return nodes.get(node.getId()).getIncomingNeighbours();
	}
	@Override
	public Collection<INode> getOutcomingNeighbours(INode node)	{
		return nodes.get(node.getId()).getOutcomingNeighbours();
	}


	/**
	 * Unique neighbours that belong to the group. Self-loops are possible.
	 */
	@Override
	public Collection<INode> getNeighboursInGroup(INode node, IGroupOfNodes group)	{
		Collection<INode> allNeighbours = getNeighbours(node);
		Collection<INode> innerNeighbours = new LinkedList<INode>();
		for (INode neighbour : allNeighbours)	{
			if (group.contains(neighbour))	{
				innerNeighbours.add(neighbour);
			}
		}

		return innerNeighbours;
	}

	public IGraph getSubgraphForGroup(IGroupOfNodes group)	{
		Graph result = new Graph(directed, weighted);

		result.nodeAttributesNames = nodeAttributesNames;

		//XXX Is computation of max edge weights for each subgraph necessary?
		//For now let's just keep the parent's maximal edge weight. This is better for the existing visualisation process (force-directed layout).
		result.maxEdgeWeight = maxEdgeWeight;

		for (Map.Entry<Integer, Node> nodeEntry : nodes.entrySet())	{
			Node node = nodeEntry.getValue();
			if (group.contains(node))	{
				result.nodes.put(nodeEntry.getKey(), node);	//The node keeps the same edges it had!

				/*if (weighted)	{	//Need to set the maximal edge weight of the new graph.
					for (INode neighbour : node.getOutcomingNeighbours())	{
						if (!group.contains(neighbour))	{	//Consider only inner edges.
							continue;
						}
						float edgeWeight = node.getWeightOfEdgeTo(neighbour);
						result.maxEdgeWeight = (result.maxEdgeWeight==null) ? edgeWeight : Math.max(result.maxEdgeWeight, edgeWeight);
					}
				}*/
			}
		}

		/*if (!weighted)	{	//Also need to set the maximal edge weight of the new graph if it has edges at all.
			if (result.getEdges().size() > 0)	{
				//FUTURE_WORK Can't use getNumberOfEdges() as the nodes keep their old edges, so we won't see the picture for internal edges. Fix that?
				result.maxEdgeWeight = 1f;
			}
		}*/

		return result;
	}


	@Override
	public Collection<Pair<INode, INode>> getEdges()	{
		Collection<Pair<INode, INode>> edges = new LinkedList<Pair<INode, INode>>();

		Collection<Node> nodesCollection = nodes.values();
		for (Node node1 : nodesCollection)	{
			for (Node node2 : nodesCollection)	{
				/*if (node1.equals(node2))	{
					continue;
				}*/
				if (node1.hasEdgeTo(node2))	{
					Pair<INode, INode> edge = new Pair<INode, INode>(node1, node2);
					edges.add(edge);
				}
			}
		}

		return edges;
	}

	@Override
	public Float getEdgeWeight(INode node1, INode node2) {
		if (!hasEdge(node1, node2))	{
			return null;
		}

		Node sourceNode = nodes.get(node1.getId());
		Float weight = null;
		if (weighted)	{
			weight = sourceNode.getWeightOfEdgeTo(node2);
		}
		if (weight == null)	{
			weight = 1f;
		}

		return weight;
	}

	@Override
	public Float getMaxEdgeWeight()	{
		return maxEdgeWeight;
	}


	public boolean hasNodeAttributes()	{
		return nodeAttributesNames != null  &&  !nodeAttributesNames.isEmpty();
	}

	public List<String> getNodeAttributesNames()	{
		return Collections.unmodifiableList(nodeAttributesNames);
	}


	@Override
	public boolean equals(Object obj)	{
		if (!(obj instanceof Graph))	{
			return super.equals(obj);
		}
		Graph other = (Graph)obj;

		if (id!=null && other.id!=null)	{
			return id.equals(other.id);
		}

		if (directed!=other.directed || weighted!=other.weighted || maxEdgeWeight!=other.maxEdgeWeight)	{	//FUTURE_WORK nodeAttributesNames - ?
			return false;
		}
		if (!nodes.equals(other.nodes))	{
			return false;
		}

		for (Map.Entry<Integer, Node> idNode : nodes.entrySet())	{
			INode otherNode = other.getNode(idNode.getKey());

			Collection<INode> neighbours = idNode.getValue().getOutcomingNeighbours();
			Collection<INode> otherNeighbours = other.getOutcomingNeighbours(otherNode);
			if (!neighbours.equals(otherNeighbours))	{
				return false;
			}

			if (directed)	{
				neighbours = idNode.getValue().getIncomingNeighbours();
				otherNeighbours = other.getIncomingNeighbours(otherNode);
				if (!neighbours.equals(otherNeighbours))	{
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public int hashCode()	{
		if (id != null)	{
			return id.hashCode();
		}

		int hashResult = nodes.hashCode();
		if (maxEdgeWeight != null)	{
			hashResult += maxEdgeWeight.hashCode();
		}
		if (nodeAttributesNames != null)	{
			hashResult += nodeAttributesNames.hashCode();
		}

		return hashResult;
	}
}
