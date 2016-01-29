package ru.ispras.modis.NetBlox.parser.extensionInterfaces;

import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;

/**
 * An interface for elements of parser that process some element of scenario description
 * (for one algorithm description, both graph, mining and characteristics algorithms).
 * 
 * @author ilya
 */
public interface IDescriptionElementProcessor {
	//FUTURE_WORK Ideally it should be extending an interface extracted from XMLElementProcessor in modis-lib...

	public AlgorithmDescription getParsedDescription();
}
