package ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics;


/**
 * A base class for all the callback classes for characteristics.computers extension point.
 * 
 * @author ilya
 */
//TODO How about turning this (and children) into interfaces?
public abstract class CharacteristicComputer {
	protected void throwUnimplementedException()	{	//TODO Regarding ^, should we extract this to somewhere?
		throw new UnsupportedOperationException("The method hasn't been implemented in plug-in.");
	}
}
