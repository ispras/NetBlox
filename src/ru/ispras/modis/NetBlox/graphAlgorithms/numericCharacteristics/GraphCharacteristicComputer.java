package ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.exceptions.MeasureComputationException;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphOnDrive;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;

/**
 * Parent class for callback classes in plug-ins that compute characteristic statistics on graphs.
 * 
 * @author ilya
 */
public abstract class GraphCharacteristicComputer extends CharacteristicComputer	{
	public NumericCharacteristic run(GraphOnDrive graphOnDrive, MeasureParametersSet parameters) throws MeasureComputationException	{
		throwUnimplementedException();
		return null;
	}

	public NumericCharacteristic run(IGraph graph, MeasureParametersSet parameters) throws MeasureComputationException	{
		throwUnimplementedException();
		return null;
	}
}
