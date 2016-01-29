package ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.exceptions.MeasureComputationException;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphOnDrive;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;

/**
 * Parent class for callback classes in plug-ins that compute characteristics for groups of nodes (a set of them per launch).
 * 
 * @author ilya
 */
public abstract class GroupsOfNodesSetCharacteristicComputer extends CharacteristicComputer {
	/**
	 * Run the computation of characteristic.
	 * @param graphOnDrive	- contains path to the file with graph and some additional data.
	 * @param groupsOfNodesFilePathString	- path to the file with groups of nodes for which the characteristic is computed.
	 * @param referenceGroupsOfNodesFilePathString	- path to file with reference groups of nodes (communities) provided with the graph.
	 * @param parameters	- parameters of the characteristic being computed.
	 * @return	the computed characteristic.
	 * @throws MeasureComputationException 
	 */
	public NumericCharacteristic run(GraphOnDrive graphOnDrive, String groupsOfNodesFilePathString, String referenceGroupsOfNodesFilePathString,
			MeasureParametersSet parameters) throws MeasureComputationException	{
		throwUnimplementedException();
		return null;
	}

	/**
	 * Run the computation of characteristic.
	 * @param graph	- the inner representation of a graph (for which all the computations are done).
	 * @param groupsOfNodes	- the inner representation for groups of nodes for which the characteristic is computed.
	 * @param referenceGroupsOfNodes	- the inner representation of reference groups of nodes provided with the graph.
	 * @param parameters	- the parameters of the characteristic being computed.
	 * @return	the computed characteristic.
	 */
	public NumericCharacteristic run(IGraph graph, ISetOfGroupsOfNodes groupsOfNodes, ISetOfGroupsOfNodes referenceGroupsOfNodes,
			MeasureParametersSet parameters) throws MeasureComputationException	{	//XXX Or is there a link to Graph from ISetOfGroupsOfNodes?
		throwUnimplementedException();
		return null;
	}
}
