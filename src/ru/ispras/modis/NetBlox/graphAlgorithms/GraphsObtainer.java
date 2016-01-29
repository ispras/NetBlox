package ru.ispras.modis.NetBlox.graphAlgorithms;

import java.io.IOException;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageWriter;
import ru.ispras.modis.NetBlox.exceptions.GraphGenerationException;
import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphProvision.GraphProviderExtensionRegistry;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphProvision.IGraphProvider;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphProvision.ProvidedGraph;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;

public class GraphsObtainer {
	private static final GraphProviderExtensionRegistry providersRegistry = new GraphProviderExtensionRegistry();

	public static long obtainGraph(GraphOnDriveHandler writtenGraphsHandler) throws GraphGenerationException	{
		GraphParametersSet parameters = writtenGraphsHandler.getGraphParameters();

		String graphTypeName = parameters.getGraphTypeName();
		IGraphProvider provider = providersRegistry.getGraphProvider(graphTypeName);

		long timeStart = System.currentTimeMillis();
		ProvidedGraph providedGraph = provider.getGraph(parameters);
		long runningTime = System.currentTimeMillis() - timeStart;

		if (providedGraph == null)	{
			throw new PluginException("The plug-in for "+graphTypeName+" graph type returned _null_ instead of graph.");
		}

		putObtainedGraphToStorage(providedGraph, writtenGraphsHandler);

		StorageWriter.savePerformanceStatistic(runningTime, writtenGraphsHandler);
		return runningTime;
	}


	private static void putObtainedGraphToStorage(ProvidedGraph providedGraph, GraphOnDriveHandler writtenGraphsHandler)
			throws GraphGenerationException	{
		try {
			switch (providedGraph.getProvisionFormat())	{
			case FILE_PATH_STRING:
				StorageWriter.move(providedGraph.getGraphFilePathString(), writtenGraphsHandler.getAbsoluteGraphPathString());
				StorageWriter.move(providedGraph.getCoverFilePathString(), writtenGraphsHandler.getAbsoluteReferenceCoverPathString());
				break;
			case INTERNAL:
				StorageWriter.save(providedGraph.getGraph(), writtenGraphsHandler.getAbsoluteGraphPathString());
				StorageWriter.save(providedGraph.getCover(), writtenGraphsHandler.getAbsoluteReferenceCoverPathString());
				break;
			case LIST_OF_STRINGS:
				StorageWriter.save(providedGraph.getGraphStrings(), writtenGraphsHandler.getAbsoluteGraphPathString());
				StorageWriter.save(providedGraph.getCoverStrings(), writtenGraphsHandler.getAbsoluteReferenceCoverPathString());
				break;
			case STREAM:
				StorageWriter.save(providedGraph.getGraphStream(), writtenGraphsHandler.getAbsoluteGraphPathString());
				StorageWriter.save(providedGraph.getCoverStream(), writtenGraphsHandler.getAbsoluteReferenceCoverPathString());
				break;
			}
		} catch (IOException e) {
			String errorMessage = "Could not put provided graph (or reference cover) to storage: "+e.getMessage();
			throw new GraphGenerationException(errorMessage);
		}
	}
}
