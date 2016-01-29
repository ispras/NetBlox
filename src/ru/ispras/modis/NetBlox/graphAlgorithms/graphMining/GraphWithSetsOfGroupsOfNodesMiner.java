package ru.ispras.modis.NetBlox.graphAlgorithms.graphMining;

import java.util.List;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.exceptions.GraphMiningException;
import ru.ispras.modis.NetBlox.graphAlgorithms.GraphMiner;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;

/**
 * A parent class for callback classes for graph mining algorithms that deal with the graph
 * and several sets of groups of its nodes at once.
 * 
 * @author ilya
 */
public abstract class GraphWithSetsOfGroupsOfNodesMiner extends GraphMiner {	//TODO Extract interface?
	public abstract boolean canGetSetsOfGroupsToMineFromPreliminaryComputations();

	public MinerResults mine(GraphOnDrive graphOnDrive, List<String> groupsOfNodesSetsFilesPathStrings,
			SupplementaryData supplementaryData, GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}
	public MinerResults mineFromPreliminaryComputationsResults(GraphOnDrive graphOnDrive, SupplementaryData supplementaryData,
			GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}

	public MinerResults mine(IGraph graph, List<ISetOfGroupsOfNodes> setsOfGroupsOfNodes,
			SupplementaryData supplementaryData, GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}
	public MinerResults mineFromPreliminaryComputationsResults(IGraph graph, SupplementaryData supplementaryData,
			GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}
}
