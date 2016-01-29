package ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics;

import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.exceptions.MeasureComputationException;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;

/**
 * The children of this class evaluate the numeric characteristics discovered
 * by graph mining algorithms.
 * 
 * @author ilya
 */
public abstract class CharacteristicEvaluator extends CharacteristicComputer {
	public abstract NumericCharacteristic run(NumericCharacteristic characteristic, MeasureParametersSet parameters) throws MeasureComputationException;
	//TODO Or a list of characteristics?
}
