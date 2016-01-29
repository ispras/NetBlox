package ru.ispras.modis.NetBlox.graphAlgorithms.graphProvision;

import ru.ispras.modis.NetBlox.exceptions.GraphGenerationException;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;

/**
 * The interface that is to be implemented by callback classes of plug-ins that provide graphs
 * described in <graphs/> section of NetBlox scenario.
 * 
 * @author ilya
 */
public interface IGraphProvider {
	public ProvidedGraph getGraph(GraphParametersSet parameters) throws GraphGenerationException;
}
