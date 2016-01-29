package ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics;

import java.util.List;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.exceptions.MeasureComputationException;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphOnDrive;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;

/**
 * Parent class for callback classes in plug-ins that compute characteristics for multiple sets of groups of nodes (a whole bunch at a time).
 * 
 * @author ilya
 */
public abstract class SetsOfGroupsOfNodesCharacteristicComputer extends CharacteristicComputer {
	/**
	 * Run the computation of characteristic.
	 * @param graphOnDrive	- contains path to the file with graph and some additional data.
	 * @param groupsOfNodesSetsFilesPathStrings	- paths to the files with sets of groups of nodes for which the characteristic is computed.
	 * @param referenceGroupsOfNodesFilePathString	- path to file with reference groups of nodes (communities) provided with the graph.
	 * @param parameters	- parameters of the characteristic being computed.
	 * @return	the computed characteristic.
	 */
	public NumericCharacteristic run(GraphOnDrive graphOnDrive, List<String> groupsOfNodesSetsFilesPathStrings,
			String referenceGroupsOfNodesFilePathString, MeasureParametersSet parameters) throws MeasureComputationException	{
		throwUnimplementedException();
		return null;
	}

	/**
	 * Run the computation of characteristic.
	 * @param graph	- the inner representation of a graph (for which all the computations are done).
	 * @param groupsOfNodesSets	- a list of inner representations for groups of nodes for which the characteristic is computed.
	 * @param referenceGroupsOfNodes	- the inner representation of reference groups of nodes provided with the graph.
	 * @param parameters	- the parameters of the characteristic being computed.
	 * @return	the computed characteristic.
	 */
	public NumericCharacteristic run(IGraph graph, List<ISetOfGroupsOfNodes> groupsOfNodesSets,
			ISetOfGroupsOfNodes referenceGroupsOfNodes, MeasureParametersSet parameters)
					throws MeasureComputationException	{	//XXX Or is there a link to Graph from ISetOfGroupsOfNodes?
		throwUnimplementedException();
		return null;
	}
}
