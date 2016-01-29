package ru.ispras.modis.NetBlox.graphAlgorithms.graphMining;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.exceptions.GraphMiningException;
import ru.ispras.modis.NetBlox.graphAlgorithms.GraphMiner;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;

/**
 * The children of this class mine just single graphs, passed to them as paths to files or in internal representation.
 * 
 * TODO Do we really need this class when we have <code>GraphWithSingleGroupsOfNodesSetMiner</code> and sometimes
 * provide <code>null</code> instead of giving the groups of nodes?
 * 
 * XXX Extract interface?
 * 
 * @author ilya
 */
public abstract class AGraphMiner extends GraphMiner {
	public MinerResults mine(GraphOnDrive graphOnDrive, SupplementaryData supplementaryData, GraphMiningParametersSet miningParameters)
			throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}
	public MinerResults mine(IGraph graph, SupplementaryData supplementaryData, GraphMiningParametersSet miningParameters) throws GraphMiningException	{
		throwUnimplementedException();
		return null;
	}
}
