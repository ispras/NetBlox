package ru.ispras.modis.NetBlox.parser.extensionInterfaces;

import java.io.InputStream;

import ru.ispras.modis.NetBlox.scenario.DescriptionMeasure;

public interface IMeasureDescriptionParser {
	public DescriptionMeasure parseMeasureDescription(InputStream tagContent);
}
