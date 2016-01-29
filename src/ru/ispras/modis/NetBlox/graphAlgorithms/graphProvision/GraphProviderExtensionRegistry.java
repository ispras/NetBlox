package ru.ispras.modis.NetBlox.graphAlgorithms.graphProvision;

import ru.ispras.modis.NetBlox.graphAlgorithms.GraphAlgorithmExtensionRegistry;

/**
 * A registry for extensions of graph.providers extension point.
 * 
 * @author ilya
 */
public class GraphProviderExtensionRegistry extends GraphAlgorithmExtensionRegistry<IGraphProvider> {
	private static final String EXTENSION_POINT_ID = "graph.providers";

	private static final String ATTRIBUTE_GRAPH_TYPE_SCENARY_NAME = "graphTypeScenaryName";


	public GraphProviderExtensionRegistry()	{
		super(IGraphProvider.class, EXTENSION_POINT_ID, ATTRIBUTE_GRAPH_TYPE_SCENARY_NAME);
	}


	public IGraphProvider getGraphProvider(String graphTypeName)	{
		return getCallbackObject(graphTypeName);
	}
}
