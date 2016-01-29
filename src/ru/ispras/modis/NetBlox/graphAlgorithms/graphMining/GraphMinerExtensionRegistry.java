package ru.ispras.modis.NetBlox.graphAlgorithms.graphMining;

import org.eclipse.core.runtime.IConfigurationElement;

import ru.ispras.modis.NetBlox.graphAlgorithms.GraphAlgorithmExtensionRegistry;
import ru.ispras.modis.NetBlox.graphAlgorithms.GraphMiner;


/**
 * The registry for the extensions of graph.miners extension point.
 * 
 * @author ilya
 */
public class GraphMinerExtensionRegistry extends GraphAlgorithmExtensionRegistry<GraphMiner> {
	private static final String EXTENSION_POINT_ID = "graph.miners";

	private static final String ATTRIBUTE_ALGORITHM_SCENARY_NAME = "algorithmNameInScenary";
	private static final String ATTRIBUTE_IS_SOURCE_IN_FILES = "isSourceInFiles";


	public GraphMinerExtensionRegistry()	{
		super(GraphMiner.class, EXTENSION_POINT_ID, ATTRIBUTE_ALGORITHM_SCENARY_NAME);
	}


	public GraphMiner getGraphMiner(String algorithmName)	{
		return getCallbackObject(algorithmName);
	}


	public boolean isSourcePassedInFiles(String algorithmName)	{
		IConfigurationElement minerConfiguration = getConfigurationElement(algorithmName);

		String stringValue = minerConfiguration.getAttribute(ATTRIBUTE_IS_SOURCE_IN_FILES);
		if (stringValue == null  ||  stringValue.isEmpty())	{
			return true;
		}
		return Boolean.parseBoolean(stringValue);
	}
}
