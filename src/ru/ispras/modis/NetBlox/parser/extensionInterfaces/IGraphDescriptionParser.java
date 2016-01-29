package ru.ispras.modis.NetBlox.parser.extensionInterfaces;

import java.io.InputStream;

import ru.ispras.modis.NetBlox.scenario.DescriptionGraphsOneType;

public interface IGraphDescriptionParser {
	public DescriptionGraphsOneType parse(InputStream tagContent);
}
