package ru.ispras.modis.NetBlox.dataManagement;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataStructures.Graph;
import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.SetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.AnalysedDataIdentifier;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.SetOfGroupsException;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.exceptions.StorageException;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.SupplementaryData;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;
import ru.ispras.modis.NetBlox.scenario.ParametersSet;
import ru.ispras.modis.NetBlox.scenario.SupplementaryAlgorithmParameters;

public class StorageScanner extends StorageHandler {
	/**
	 * Get the results of preliminary computations.
	 */
	public static SupplementaryData getSupplementaryAlgorithmsResults(GraphOnDriveHandler graphHandler, Collection<ParametersSet> fixedPreliminarySet)	{
		SupplementaryData supplementaryData = new SupplementaryData();
		for (ParametersSet parametersSet : fixedPreliminarySet)	{
			SupplementaryAlgorithmParameters supplementaryAlgorithmParameters = (SupplementaryAlgorithmParameters) parametersSet;

			String pathToFileInStorage = getPathToSupplementaryDataStorageFile(graphHandler, supplementaryAlgorithmParameters);
			File fileInStorage = new File(pathToFileInStorage);
			if (fileInStorage.exists())	{
				//TODO Implement. See Monolith.
			}
			else	{
				//TODO Implement. See Monolith.
			}
		}

		return supplementaryData;
	}


	public static boolean containsMinedData(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)	{
		//TODO What about SupplementaryData? Check inside getPathStringToStoredMinedData(...) as well.
		//XXX The case of multiple mined graphs hasn't been considered.
		return containsMinedGraph(graphHandler, miningParameters) ||
				containsMinedGroupsOfNodes(graphHandler, miningParameters) ||
				containsMinedCharacteristic(graphHandler, miningParameters) ||
				containsMinedMultipleGraphs(graphHandler, miningParameters);
	}

	public static boolean containsMinedGraph(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)	{
		return containsMinedData(graphHandler, miningParameters, ContentType.GRAPH_EDGES);
	}
	public static boolean containsMinedGroupsOfNodes(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)	{
		return containsMinedData(graphHandler, miningParameters, ContentType.NODES_GROUPS);
	}
	public static boolean containsMinedCharacteristic(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)	{
		return containsMinedData(graphHandler, miningParameters, ContentType.CHARACTERISTIC);
	}
	private static boolean containsMinedData(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters, ContentType contentType)	{
		GraphMiningParametersSet algorithmParameters = miningParameters.getMiningParameters();
		if (algorithmParameters.considerTimeSlices())	{
			boolean result = true;
			for (Integer timeSlice : algorithmParameters.getTimeSlices())	{
				String pathToStoredForTimeSlice = getPathStringToStoredMinedData(graphHandler, miningParameters, contentType, timeSlice);
				if (!(new File(pathToStoredForTimeSlice)).exists())	{
					//For now let's consider this as not all possible iterations were recorded, so it's possible to get the requested after new launch.
					//FUTURE_WORK Find out how many iterations had been run really and estimate whether there's sense to try more iterations.
					//FUTURE_WORK Another solution, a worse one, is to compare the last iterated file with absolute result.
					result = false;
					break;
				}
			}
			return result;
		}
		else	{
			String pathToStoredData = getPathStringToStoredMinedData(graphHandler, miningParameters, contentType, null);
			File storageFile = new File(pathToStoredData);
			return storageFile.exists();
		}
	}

	public static boolean containsMinedMultipleGraphs(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)	{
		String storageDirectoryPathString = getPathStringToMinedDataDirectory(graphHandler, miningParameters);
		File storageDirectory = new File(storageDirectoryPathString);
		if (!storageDirectory.exists())	{
			return false;
		}

		boolean result = false;

		GraphMiningParametersSet algorithmParameters = miningParameters.getMiningParameters();
		if (algorithmParameters.considerTimeSlices())	{
			for (Integer timeSlice : algorithmParameters.getTimeSlices())	{
				boolean existForTimeSlice = false;

				String storageFilenameBase = ContentType.GRAPH_EDGES.toString() + timeSlice + "_";
				for (String resultFileName : storageDirectory.list())	{
					if (resultFileName.startsWith(storageFilenameBase))	{
						existForTimeSlice = true;
						break;
					}
				}

				if (!existForTimeSlice)	{
					//As in case of private containsMinedData(...) consider this as not all possible iterations were recorded,
					//so it's possible to get the requested after new launch.	//FUTURE_WORK See above, private containsMinedData(...).
					return false;
				}
			}
			result = true;
		}
		else	{
			String storageFilenameBase = ContentType.GRAPH_EDGES.toString() + "_";
			for (String resultFileName : storageDirectory.list())	{
				if (resultFileName.startsWith(storageFilenameBase))	{
					return true;
				}
			}
		}

		return result;
	}


	public static String getMinedDataFilePathstring(GraphOnDriveHandler graphHandler, ExtendedMiningParameters extendedMiningParameters,
			ContentType contentType) throws StorageException	{
		//TODO What about SupplementaryData? Check inside getPathStringToStoredMinedData(...) as well.

		String pathToStoredData = getPathStringToStoredMinedData(graphHandler, extendedMiningParameters, contentType, null);

		File storageFile = new File(pathToStoredData);
		if (!storageFile.exists())	{
			GraphMiningParametersSet miningParameters = extendedMiningParameters.getMiningParameters();
			if (!miningParameters.considerTimeSlices())	{	//No time slices, only one result could exist.
				throw new StorageException("Could not get requested mined data.");
			}

			//There're time slices.
			pathToStoredData = null;
			List<Integer> timeSlices = miningParameters.getTimeSlices();
			for (int i = timeSlices.size()-1 ; i>=0 ; i--)	{
				String pathForTimeSlice = getPathStringToStoredMinedData(graphHandler, extendedMiningParameters, contentType, timeSlices.get(i));
				if ((new File(pathToStoredData)).exists())	{	//Found the file for the last time slice for which there's a result.
					pathToStoredData = pathForTimeSlice;
					//FUTURE_WORK It is also possible to remove all the later time slices from the list in miningParameters,
					//but that doesn't seem to be necessary for now. Consider it later?
					break;
				}
			}
			if (pathToStoredData == null)	{	//Means we could find no results for any time slice.
				throw new StorageException("Could not get requested mined data.");
			}
		}

		return pathToStoredData;
	}

	public static IGraph getMinedGraphStructure(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)	throws StorageException	{
		//TODO What about SupplementaryData?
		String pathInStorage = getMinedDataFilePathstring(graphHandler, miningParameters, ContentType.GRAPH_EDGES);

		GraphParametersSet originalGraphParameters = graphHandler.getGraphParameters();
		IGraph minedGraph = new Graph(pathInStorage, originalGraphParameters.isDirected(), originalGraphParameters.isWeighted());
		return minedGraph;
	}
	/**
	 * @return	mined graph for <code>timeSlice</code> (parameters considered). <code>null</code> if there're no results
	 * 			for this <code>timeSlice</code>.
	 */
	public static IGraph getMinedGraphStructure(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters, Integer timeSlice)
			throws StorageException	{
		//TODO What about SupplementaryData?
		String pathInStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.GRAPH_EDGES, timeSlice);

		if (!(new File(pathInStorage)).exists())	{	//In case the results for this or that time slice are absent for some reason.
			return null;
		}
		GraphParametersSet originalGraphParameters = graphHandler.getGraphParameters();
		IGraph minedGraph = new Graph(pathInStorage, originalGraphParameters.isDirected(), originalGraphParameters.isWeighted());
		return minedGraph;
	}

	public static ISetOfGroupsOfNodes getMinedGroupsOfNodes(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)
			throws StorageException, SourceGraphException	{
		//TODO What about SupplementaryData?
		String pathInStorage = getMinedDataFilePathstring(graphHandler, miningParameters, ContentType.NODES_GROUPS);

		try	{
			ISetOfGroupsOfNodes minedGroupsOfNodes = new SetOfGroupsOfNodes(pathInStorage, graphHandler.getGraph());
			return minedGroupsOfNodes;
		}
		catch (SetOfGroupsException e)	{
			throw new StorageException(e);
		}
	}
	/**
	 * @return	groups of nodes for <code>timeSlice</code> (parameters considered). <code>null</code> if there're no results
	 * 			for this <code>timeSlice</code>.
	 */
	public static ISetOfGroupsOfNodes getMinedGroupsOfNodes(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters,
			Integer timeSlice)	throws StorageException, SourceGraphException	{
		//TODO What about SupplementaryData?
		String pathInStorage = getPathStringToStoredMinedData(graphHandler, miningParameters, ContentType.NODES_GROUPS, timeSlice);

		if (!(new File(pathInStorage)).exists())	{	//In case the results for this or that time slice are absent for some reason.
			return null;
		}
		try	{
			ISetOfGroupsOfNodes minedGroupsOfNodes = new SetOfGroupsOfNodes(pathInStorage, graphHandler.getGraph());
			return minedGroupsOfNodes;
		}
		catch (SetOfGroupsException e)	{
			throw new StorageException(e);
		}
	}

	public static NumericCharacteristic getMinedCharacteristic(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)
			throws StorageException	{
		String pathInStorage = getMinedDataFilePathstring(graphHandler, miningParameters, ContentType.CHARACTERISTIC);
		return retrieveCharacteristic(pathInStorage);
	}

	@SuppressWarnings("unchecked")
	public static <IT> List<IT> getMinedMultipleGraphStructures(Class<IT> itemClass,
			GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters, Integer timeSlice)	{
		GraphParametersSet originalGraphParameters = graphHandler.getGraphParameters();

		List<IT> pathsToMined = new LinkedList<IT>();	//XXX Is it better to return null if no result files are found?

		String storageFilenameBase = ContentType.GRAPH_EDGES.toString() + ((timeSlice==null)?"":timeSlice) + "_";

		String storageDirectoryPathString = getPathStringToMinedDataDirectory(graphHandler, miningParameters);
		File storageDirectory = new File(storageDirectoryPathString);
		for (String resultFileName : storageDirectory.list())	{
			if (!resultFileName.startsWith(storageFilenameBase))	{
				continue;
			}

			String storageFilename = storageDirectoryPathString + SystemConfiguration.FILES_SEPARATOR + resultFileName;
			if (itemClass.isAssignableFrom(IGraph.class))	{
				IGraph graphStructure = new Graph(storageFilename, originalGraphParameters.isDirected(), originalGraphParameters.isWeighted());
				pathsToMined.add((IT) graphStructure);
			}
			else if (itemClass.isAssignableFrom(String.class))	{
				pathsToMined.add((IT) storageFilename);
			}
		}

		return pathsToMined;
	}

	/*public static List<IGraph> getMinedMultipleGraphStructures(GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)	{
		List<IGraph> graphStructures = null;

		return graphStructures;
	}*/


	/**
	 * Get the value of statistic computed over some data.
	 * @param graphHandler	- the handler for the graph to which corresponds all the data that is being analysed.
	 * @param analysedDataIdentifier	- the data on which the characteristic must have been computed (its identifier).
	 * 									_null_ for statistics on original graphs.
	 * @param characteristicParameters	- the parameters of the characteristic statistic that must have been computed.
	 * @return	the value of characteristic statistic.
	 * @throws StorageException 
	 */
	public static NumericCharacteristic getStatisticValue(GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet characteristicParameters) throws StorageException	{
		String storageFilePathString = getPathToMeasureStorageFile(graphHandler, analysedDataIdentifier, characteristicParameters);
		return retrieveCharacteristic(storageFilePathString);
	}


	private static NumericCharacteristic retrieveCharacteristic(String pathToStorageFileString) throws StorageException	{
		Path pathToFile = Paths.get(pathToStorageFileString);
		if (!Files.exists(pathToFile))	{
			return null;
		}

		NumericCharacteristic result = null;
		try {
			List<String> strings = Files.readAllLines(pathToFile, Charset.defaultCharset());
			Iterator<String> stringsIterator = strings.iterator();

			String line = stringsIterator.next();
			if (line.equalsIgnoreCase(NumericCharacteristic.Type.LIST_OF_VALUES.toString()))	{
				result = retrieveListOfValues(stringsIterator);
			}
			else if (line.equalsIgnoreCase(NumericCharacteristic.Type.DISTRIBUTION.toString()))	{
				result = retrieveDistribution(stringsIterator);
			}
			else if (line.equalsIgnoreCase(NumericCharacteristic.Type.FUNCTION.toString()))	{
				result = retrieveFunction(stringsIterator);
			}
			else if (line.equalsIgnoreCase(NumericCharacteristic.Type.SINGLE_VALUE.toString())  ||  strings.size() == 1)	{
				result = retrieveSingleFloatValue(stringsIterator, line);
			}

		} catch (IOException e) {
			throw new StorageException("Couldn't get characteristic from storage: " + e.getMessage());
		}

		return result;
	}

	private static NumericCharacteristic retrieveSingleFloatValue(Iterator<String> stringsIterator, String line)	{
		if (line.equalsIgnoreCase(NumericCharacteristic.Type.SINGLE_VALUE.toString()))	{
			line = stringsIterator.next();
		}
		Float measureValue = Float.parseFloat(line);
		return new NumericCharacteristic(NumericCharacteristic.Type.SINGLE_VALUE, measureValue);
	}

	private static NumericCharacteristic retrieveListOfValues(Iterator<String> stringsIterator)	{
		NumericCharacteristic result = new NumericCharacteristic(NumericCharacteristic.Type.LIST_OF_VALUES);
		while (stringsIterator.hasNext())	{
			String line = stringsIterator.next();
			if (!line.isEmpty())	{
				result.addValue(Double.parseDouble(line));
			}
		}

		return result;
	}

	private static NumericCharacteristic retrieveDistribution(Iterator<String> stringsIterator)	{
		NumericCharacteristic result = new NumericCharacteristic(NumericCharacteristic.Type.DISTRIBUTION);
		while (stringsIterator.hasNext())	{
			String line = stringsIterator.next();
			if (!line.isEmpty())	{
				String[] dataElementsFromLine = line.split("\\s");

				Double value = Double.parseDouble(dataElementsFromLine[0]);
				int numberOfOccurences = Integer.parseInt(dataElementsFromLine[1]);

				if (value.equals(Double.NaN) || value.equals(Double.NEGATIVE_INFINITY) || value.equals(Double.POSITIVE_INFINITY) ||
						!value.equals(Math.rint(value)))	{
					result.addToDistribution(value, numberOfOccurences);
				}
				else	{
					result.addToDistribution(value.intValue(), numberOfOccurences);
				}
			}
		}

		return result;
	}

	private static NumericCharacteristic retrieveFunction(Iterator<String> stringsIterator)	{
		NumericCharacteristic result = new NumericCharacteristic(NumericCharacteristic.Type.FUNCTION);
		while (stringsIterator.hasNext())	{
			String line = stringsIterator.next();
			if (!line.isEmpty())	{
				String[] dataElementsFromLine = line.split("\\s");

				Double parameter = Double.parseDouble(dataElementsFromLine[0]);
				Double value = Double.parseDouble(dataElementsFromLine[1]);

				result.putToFunction(parameter, value);
			}
		}

		return result;
	}


	/**
	 * Get the time of the graph mining process.
	 * TODO How about the case of supplementary algorithms (and parameters for them)?
	 * 
	 * @param originalGraphHandler
	 * @param extendedMiningParameters
	 * @return
	 */
	public static Long getPerformanceTime(GraphOnDriveHandler originalGraphHandler, ExtendedMiningParameters extendedMiningParameters)	{
		Long time = null;

		String storageFilePathString = getPathStringToPerformanceStat(originalGraphHandler, extendedMiningParameters);
		Path pathToFile = Paths.get(storageFilePathString);
		if (!Files.exists(pathToFile))	{
			return time;
		}

		try {
			List<String> strings = Files.readAllLines(pathToFile, Charset.defaultCharset());
			String line = strings.iterator().next();

			time = Long.parseLong(line);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return time;
	}

	/**
	 * Get the time of generation for a graph specified in the handler.
	 * @param originalGraphHandler
	 * @return
	 */
	public static Long getPerformanceTime(GraphOnDriveHandler originalGraphHandler)	{
		return getPerformanceTime(originalGraphHandler, null);
	}

	/**
	 * Gets the running time over the whole run of graph mining process, described by <code>extendedMiningParameters</code>.
	 * @return	milliseconds (or null if there's no such statistic in storage).
	 */
	public static Long getOverallPerformanceTime(GraphOnDriveHandler originalGraphHandler, ExtendedMiningParameters extendedMiningParameters)	{
		Long totalWorkTimeMillis = 0l;
		Long singleAlgorithmComputationTime = null;

		GraphMiningParametersSet miningParameters = extendedMiningParameters.getMiningParameters();
		if (miningParameters.useSupplementaryData())	{
			/*List<ParametersSet> listOfPreliminaryCalculationsParametersSets = miningParameters.getPreliminaryCalculationsParametersSets();
			for (ParametersSet preliminaryCalculationsParameters : listOfPreliminaryCalculationsParametersSets)	{
				//TODO Retrieve preliminary calculations time.
				//TODO Check it is not null, otherwise return null.
				//TODO Add retrieved time to totalWorkTimeMillis .
			}*/
		}

		singleAlgorithmComputationTime = getPerformanceTime(originalGraphHandler, extendedMiningParameters);
		if (singleAlgorithmComputationTime == null)	{
			return null;
		}

		totalWorkTimeMillis += singleAlgorithmComputationTime;
		return totalWorkTimeMillis;
	}
}
