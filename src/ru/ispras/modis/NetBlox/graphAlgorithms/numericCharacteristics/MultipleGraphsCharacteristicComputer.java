package ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics;

import java.util.List;

import ru.ispras.modis.NetBlox.dataStructures.Graph;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.exceptions.MeasureComputationException;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphOnDrive;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;

/**
 * Parent class for callback classes in plug-ins that compute characteristic statistics on multiple graphs
 * (for several graphs (graph structures) at once).
 * 
 * @author ilya
 */
public abstract class MultipleGraphsCharacteristicComputer extends CharacteristicComputer {
	public NumericCharacteristic run(List<GraphOnDrive> graphsOnDrive, MeasureParametersSet parameters) throws MeasureComputationException	{
		throwUnimplementedException();
		return null;
	}
	public NumericCharacteristic runWithInternalRepresentation(List<Graph> graphs, MeasureParametersSet parameters) throws MeasureComputationException	{
		throwUnimplementedException();
		return null;
	}
}
