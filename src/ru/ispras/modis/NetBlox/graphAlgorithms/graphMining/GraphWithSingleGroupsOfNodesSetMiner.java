package ru.ispras.modis.NetBlox.graphAlgorithms.graphMining;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.exceptions.GraphMiningException;
import ru.ispras.modis.NetBlox.graphAlgorithms.GraphMiner;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;

/**
 * The children of this class mine data from a graph with a set of groups of its nodes (like a cover by communities).
 * 
 * @author ilya
 */
public abstract class GraphWithSingleGroupsOfNodesSetMiner extends GraphMiner {	//TODO Extract interface?
	public abstract boolean canGetGroupsOfNodesToMineFromPreliminaryComputations();


	/**
	 * Mines the data that is passed in files (inside <code>GraphOnDrive</code> and in the file with address <code>groupsOfNodesFilePathString</code>).
	 * @param graphOnDrive	- a container for the path to the file that keeps the graph (with the information about it being directed/weighted).
	 * @param groupsOfNodesFilePathString	- the path to the file with groups of nodes in NetBlox format. Can be <code>null</code>.
	 * @param supplementaryData	- data computed by supplementary algorithms. See the documentation for the plug-in that provides those computations.
	 * @param miningParameters	- the parameters of the mining algorithm.
	 * @return	a container with mined results.
	 * @throws GraphMiningException
	 */
	public MinerResults mine(GraphOnDrive graphOnDrive, String groupsOfNodesFilePathString,
			SupplementaryData supplementaryData, GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}

	/**
	 * The groups of nodes are provided not via NetBlox, but by the preliminary computations algorithms.
	 * The rest is like in the previous method.
	 */
	public MinerResults mineFromPreliminaryComputationsResults(GraphOnDrive graphOnDrive, SupplementaryData supplementaryData,
			GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}


	/**
	 * Mines the data that is passed in internal representation.
	 * @param graph	- the original graph in which we're interested.
	 * @param groupsOfNodes	- groups of nodes from which we want to mine some data. Can be <code>null</code>.
	 * @param supplementaryData	- data computed by supplementary algorithms. See the documentation for the plug-in that provides those computations.
	 * @param miningParameters	- the parameters of the mining algorithm.
	 * @return	a container with mined results.
	 * @throws GraphMiningException
	 */
	public MinerResults mine(IGraph graph, ISetOfGroupsOfNodes groupsOfNodes,
			SupplementaryData supplementaryData, GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}

	/**
	 * The groups of nodes are provided not via NetBlox, but by the preliminary computations algorithms.
	 * The rest is like in the previous method.
	 */
	public MinerResults mineFromPreliminaryComputationsResults(IGraph graph, SupplementaryData supplementaryData,
			GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}
}
