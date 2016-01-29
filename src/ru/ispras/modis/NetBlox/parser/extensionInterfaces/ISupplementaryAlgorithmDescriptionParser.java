package ru.ispras.modis.NetBlox.parser.extensionInterfaces;

import java.io.InputStream;

import ru.ispras.modis.NetBlox.scenario.DescriptionPreliminaryAlgorithm;

public interface ISupplementaryAlgorithmDescriptionParser {
	public DescriptionPreliminaryAlgorithm parse(InputStream tagContent);
}
