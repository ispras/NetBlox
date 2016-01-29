package ru.ispras.modis.NetBlox.parser.extensionInterfaces;

import java.io.InputStream;

import ru.ispras.modis.NetBlox.scenario.DescriptionGraphMiningAlgorithm;

public interface IGraphMiningDescriptionParser {
	public DescriptionGraphMiningAlgorithm parseMiningDescription(InputStream tagContent);
}
