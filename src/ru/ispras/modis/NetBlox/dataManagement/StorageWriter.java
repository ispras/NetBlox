package ru.ispras.modis.NetBlox.dataManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.IGroupOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.AnalysedDataIdentifier;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;
import ru.ispras.modis.NetBlox.utils.Pair;

public class StorageWriter extends StorageHandler {
	public static void makeSureDirectoryExists(Path directoryPath) throws IOException	{
		if (!Files.exists(directoryPath))	{
			Files.createDirectories(directoryPath);
		}
	}

	public static void move(String sourcePathString, String targetPathString) throws IOException	{
		Path source = Paths.get(sourcePathString);
		Path target = Paths.get(targetPathString);

		if (!Files.exists(source))	{
			//XXX Do something?
		}

		makeSureDirectoryExists(target.getParent());

		Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void save(List<String> dataLines, String targetPathString) throws IOException	{
		if (dataLines == null  ||  dataLines.isEmpty())	{
			return;
			//TODO Throw an exception?
		}

		Path target = Paths.get(targetPathString);
		makeSureDirectoryExists(target.getParent());

		Files.write(target, dataLines, Charset.defaultCharset());
	}

	public static void save(InputStream dataStream, String targetPathString) throws IOException	{
		InputStreamReader dataStreamReader = new InputStreamReader(dataStream);
		BufferedReader bufferedReader = new BufferedReader(dataStreamReader);

		List<String> dataLines = new LinkedList<String>();
		String line;
		while ((line=bufferedReader.readLine()) != null)	{
			dataLines.add(line);
		}

		save(dataLines, targetPathString);
	}

	public static void save(IGraph graph, String targetPathString) throws IOException	{
		Collection<Pair<IGraph.INode, IGraph.INode>> edges = graph.getEdges();

		//System.out.println("\t\tDiscovered graph:");

		List<String> linesOfEdges = new ArrayList<String>(edges.size());
		for (Pair<IGraph.INode, IGraph.INode> edge : edges)	{
			StringBuilder edgeLineBuilder = new StringBuilder().append(edge.get1st().getId()).append("\t").append(edge.get2nd().getId());
			linesOfEdges.add(edgeLineBuilder.toString());

			//System.out.println("\t\t\t"+edgeLineBuilder.toString());
		}

		save(linesOfEdges, targetPathString);
	}

	public static void save(ISetOfGroupsOfNodes setOfGroupsOfNodes, String targetPathString) throws IOException	{
		List<String> linesOfGroupsOfNodes = new ArrayList<String>(setOfGroupsOfNodes.size());

		for (IGroupOfNodes groupOfNodes : setOfGroupsOfNodes)	{
			StringBuilder groupLineBuilder = new StringBuilder();
			for (IGraph.INode node : groupOfNodes)	{
				groupLineBuilder.append(node.getId()).append(" ");
			}

			String groupLine = groupLineBuilder.toString().trim();
			linesOfGroupsOfNodes.add(groupLine);
		}

		save(linesOfGroupsOfNodes, targetPathString);
	}


	public static void saveMined(String dataFilePathString, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters,
			ContentType contentType)	throws IOException	{
		//TODO What about SupplementaryData?
		String pathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, contentType);
		move(dataFilePathString, pathToStorage);
	}

	public static void saveMined(List<String> dataLines, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters,
			ContentType contentType)	throws IOException	{
		//TODO What about SupplementaryData?
		String pathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, contentType);
		save(dataLines, pathToStorage);
	}

	public static void saveMined(InputStream dataStream, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters,
			ContentType contentType)	throws IOException	{
		//TODO What about SupplementaryData?

		InputStreamReader dataStreamReader = new InputStreamReader(dataStream);
		BufferedReader bufferedReader = new BufferedReader(dataStreamReader);

		List<String> dataLines = new LinkedList<String>();
		String line;
		while ((line=bufferedReader.readLine()) != null)	{
			dataLines.add(line);
		}

		saveMined(dataLines, graphHandler, miningParameters, contentType);
	}

	public static void saveMined(IGraph graph, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters) throws IOException	{
		String pathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.GRAPH_EDGES);
		save(graph, pathToStorage);
	}

	public static void saveMined(ISetOfGroupsOfNodes setOfGroupsOfNodes, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)
			throws IOException	{
		String pathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.NODES_GROUPS);
		save(setOfGroupsOfNodes, pathToStorage);
	}

	public static void saveMined(NumericCharacteristic characteristic, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)
			throws IOException	{
		String pathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.CHARACTERISTIC);

		List<String> linesWithValues = putCharacteristicIntoLines(characteristic);

		save(linesWithValues, pathToStorage);

		/*for (String line : linesWithValues)	{
			System.out.println(line);
		}*/
	}


	//XXX Extract this set of methods to MiningDriver? Or they (the construction of path in storage) belong completely to storage subsystem?
	//XXX Divide storage writers for saving, e.g., mining results and characteristic statistics?
	public static void saveMinedMultipleFiles(Collection<String> dataFilesPathStrings, GraphOnDriveHandler graphHandler,
			ExtendedMiningParameters miningParameters) throws IOException	{
		String basicPathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.GRAPH_EDGES);
		int structuresCounter = 0;
		for (String singlePathString : dataFilesPathStrings)	{
			structuresCounter++;
			move(singlePathString, basicPathToStorage+structuresCounter);
		}
	}

	public static void saveMinedMultipleListsOfStrings(Collection<List<String>> setsOfDataLines, GraphOnDriveHandler graphHandler,
			ExtendedMiningParameters miningParameters) throws IOException	{
		String basicPathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.GRAPH_EDGES);
		int structuresCounter = 0;
		for (List<String> dataLines : setsOfDataLines)	{
			structuresCounter++;
			save(dataLines, basicPathToStorage+structuresCounter);
		}
	}

	public static void saveMinedMultipleStreams(Collection<InputStream> dataStreams, GraphOnDriveHandler graphHandler,
			ExtendedMiningParameters miningParameters) throws IOException	{
		String basicPathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.GRAPH_EDGES);
		int structuresCounter = 0;
		for (InputStream dataStream : dataStreams)	{
			structuresCounter++;
			save(dataStream, basicPathToStorage+structuresCounter);
		}
	}

	public static void saveMinedMultipleGraphs(Collection<IGraph> graphs,GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)
			throws IOException	{
		String basicPathToStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.GRAPH_EDGES);
		int structuresCounter = 0;
		for (IGraph graph : graphs)	{
			structuresCounter++;
			save(graph, basicPathToStorage+structuresCounter);
		}
	}


	public static void saveStatistic(NumericCharacteristic characteristicMeasure, GraphOnDriveHandler graphHandler,
			AnalysedDataIdentifier analysedDataIdentifier, MeasureParametersSet characteristicParameters) throws IOException	{
		String storageFilePathString = getPathToMeasureStorageFile(graphHandler, analysedDataIdentifier, characteristicParameters);

		List<String> linesWithValues = putCharacteristicIntoLines(characteristicMeasure);

		save(linesWithValues, storageFilePathString);
	}

	/**
	 * Saves the performance statistic (time of execution of the algorithm specified by <code>extendedMiningParameters</code>).
	 * TODO How about the case of supplementary algorithms (and parameters for them)?
	 * 
	 * @param milliseconds
	 * @param originalGraphHandler
	 * @param extendedMiningParameters
	 */
	public static void savePerformanceStatistic(long milliseconds, GraphOnDriveHandler originalGraphHandler,
			ExtendedMiningParameters extendedMiningParameters)	{
		String storageFilePathString = getPathStringToPerformanceStat(originalGraphHandler, extendedMiningParameters);

		List<String> linesWithValues = new ArrayList<String>(1);
		String millisecondsString = String.valueOf(milliseconds);
		linesWithValues.add(millisecondsString);

		try {
			save(linesWithValues, storageFilePathString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save the performance statistic: the time of generation for a graph specified in the handler.
	 * @param milliseconds
	 * @param originalGraphHandler
	 */
	public static void savePerformanceStatistic(long milliseconds, GraphOnDriveHandler originalGraphHandler)	{
		savePerformanceStatistic(milliseconds, originalGraphHandler, null);
	}


	private static List<String> putCharacteristicIntoLines(NumericCharacteristic characteristic)	{
		List<String> linesWithValues = new LinkedList<String>();

		NumericCharacteristic.Type characteristicType = characteristic.getType();
		linesWithValues.add(characteristicType.toString());

		switch (characteristicType)	{
		case SINGLE_VALUE:
			String toBeWrittenDown = String.valueOf(characteristic.getValue());
			linesWithValues.add(toBeWrittenDown);
			break;
		case LIST_OF_VALUES:
			List<Double> values = characteristic.getValues();
			for (Double value : values)	{
				linesWithValues.add(value.toString());
			}
			break;
		case DISTRIBUTION:
			NumericCharacteristic.Distribution distribution = characteristic.getDistribution();
			Set<Number> distributionValues = distribution.getValues();
			for (Number value : distributionValues)	{
				Integer numberOfOccurences = distribution.getNumberOfOccurences(value);

				StringBuilder storageLineBuilder = new StringBuilder(value.toString()).append("\t").append(numberOfOccurences);
				linesWithValues.add(storageLineBuilder.toString());
			}
			break;
		case FUNCTION:
			Map<Double, Double> function = characteristic.getFunction();
			for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
				StringBuilder storageLineBuilder = new StringBuilder(functionEntry.getKey().toString()).append("\t").append(functionEntry.getValue());
				linesWithValues.add(storageLineBuilder.toString());
			}
			break;
		}

		return linesWithValues;
	}
}
