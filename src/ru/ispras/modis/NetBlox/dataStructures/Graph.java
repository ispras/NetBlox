package ru.ispras.modis.NetBlox.dataStructures;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.modis.NetBlox.utils.Pair;

/**
 * The inner representation of a graph.
 * 
 * @author ilya
 */
public class Graph implements IGraph {
	public class Node implements INode	{
		private Integer id;
		private Set<INode> outcomingEdgesTo;	//FUTURE_WORK Should we consider multiple edges between the same two nodes?
		private Map<INode, Float> outcomingWeightedEdgesTo;

		public Node(Integer id)	{
			this.id = id;
			outcomingEdgesTo = new HashSet<INode>();
			outcomingWeightedEdgesTo = new HashMap<INode, Float>();
		}


		@Override
		public Integer getId()	{
			return id;
		}


		public void addEdgeTo(INode neighbour)	{
			outcomingEdgesTo.add(neighbour);
		}
		public void addEdgeTo(INode neighbour, Float edgeWeight)	{
			if (edgeWeight != null)	{
				outcomingWeightedEdgesTo.put(neighbour, edgeWeight);
			}
			else	{
				addEdgeTo(neighbour);
			}
		}

		public boolean hasEdgeTo(INode neighbour)	{
			return outcomingEdgesTo.contains(neighbour) || outcomingWeightedEdgesTo.containsKey(neighbour);
		}

		public float getWeightOfEdgeTo(INode neighbour)	{
			return outcomingWeightedEdgesTo.get(neighbour);
		}


		public int getDegree()	{
			return outcomingEdgesTo.size() + outcomingWeightedEdgesTo.size();
		}

		public Collection<INode> getNeighbours()	{
			if (!outcomingWeightedEdgesTo.isEmpty())	{
				return Collections.unmodifiableCollection(outcomingWeightedEdgesTo.keySet());
			}
			return Collections.unmodifiableCollection(outcomingEdgesTo);
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

	private Map<Integer, Node> nodes;
	private boolean directed;
	private boolean weighted;


	public Graph(boolean directed, boolean weighted)	{
		nodes = new HashMap<Integer, Node>();
		this.directed = directed;
		this.weighted = weighted;
	}

	public Graph(String pathToGraph, boolean directed, boolean weighted)	{
		this(directed, weighted);
		parseGraph(pathToGraph);
	}


	private void parseGraph(String pathToGraph)	{
		try {
			List<String> graphFileLines = Files.readAllLines(Paths.get(pathToGraph), Charset.defaultCharset());
			Node[] edgeNodes = new Node[2];
			for (String edgeLine : graphFileLines)	{
				Float edgeWeight = parseNodesFromEdgeLine(edgeNodes, edgeLine);
				edgeNodes[0].addEdgeTo(edgeNodes[1], edgeWeight);
				if (!directed)	{
					edgeNodes[1].addEdgeTo(edgeNodes[0], edgeWeight);
				}
			}
		} catch (IOException e) {	//We assume that we had already checked the existence of the graph.
			//XXX Throw additional exception?
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
			if (edgeNodes.length == 2)	{
				weight = 1f;
			}
			else	{
				weight = Float.parseFloat(idStrings[WEIGHT_POSITION]);
			}
		}
		return weight;
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

		if (!directed)	{
			result = result / 2;
		}

		return result;
	}

	public double getAverageDegree()	{
		int cumulativeDegree = 0;

		for (Node node : nodes.values())	{
			cumulativeDegree += node.getDegree();
		}

		double averageDegree = cumulativeDegree / nodes.size();
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

		if (isWeighted())	{
			node1.addEdgeTo(node2, weight);
		}
		else	{
			node1.addEdgeTo(node2);
		}
	}


	@Override
	public Collection<INode> getNeighbours(INode node)	{
		return nodes.get(node.getId()).getNeighbours();
	}

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

		for (Map.Entry<Integer, Node> nodeEntry : nodes.entrySet())	{
			Node node = nodeEntry.getValue();
			if (group.contains(node))	{
				result.nodes.put(nodeEntry.getKey(), node);	//The node keeps the same edges it had!
			}
		}

		return result;
	}


	/*public List<String> getLinesOfEdgesAsNodePairs()	{
		List<String> linesWithEdges = new LinkedList<String>();

		Collection<Node> nodesCollection = nodes.values();
		for (Node node1 : nodesCollection)	{
			for (Node node2 : nodesCollection)	{
				if (node1.equals(node2))	{
					continue;
				}
				if (hasEdge(node1, node2))	{
					String edgeInLine = node1.getId().toString() + "\t" + node2.getId().toString() + "\n";
					linesWithEdges.add(edgeInLine);
				}
			}
		}

		return linesWithEdges;
	}*/

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
		if (isWeighted())	{
			weight = sourceNode.getWeightOfEdgeTo(node2);
		}
		if (weight == null)	{
			weight = 1f;
		}

		return weight;
	}
}
