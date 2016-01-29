package ru.ispras.modis.NetBlox.dataManagement;

import java.util.List;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.AnalysedDataIdentifier;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.AnalysedDataIdentifier.Type;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;
import ru.ispras.modis.NetBlox.scenario.ParametersSet;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.scenario.SupplementaryAlgorithmParameters;
import ru.ispras.modis.NetBlox.scenario.performanceStats.PerformanceStatisticParameters;
import ru.ispras.modis.NetBlox.utils.Pair;

public class StorageHandler {
	public enum ContentType	{GRAPH_EDGES, NODES_GROUPS, CHARACTERISTIC}

	private static final String MINED_FOLDER_SUFFIX = "_mined";
	private static final String STATS_FOLDER_SUFFIX = "_stats";


	public static String getPathToSupplementaryDataStorageFile(GraphOnDriveHandler graphHandler,
			SupplementaryAlgorithmParameters supplementaryAlgorithmParameters)	{
		//TODO Implement.
		return null;
	}


	protected static String getPathStringToStoredMinedData(GraphOnDriveHandler graphHandler, ExtendedMiningParameters extendedMiningParameters,
			ContentType contentType)	{
		StringBuilder pathBuilder = makePathToStorageDirectory(graphHandler, extendedMiningParameters);
		pathBuilder.append(contentType.toString());
		return pathBuilder.toString();
	}

	protected static String getPathStringToMinedDataDirectory(GraphOnDriveHandler graphHandler, ExtendedMiningParameters extendedMiningParameters)	{
		return makePathToStorageDirectory(graphHandler, extendedMiningParameters).toString();
	}

	protected static String getPathToMeasureStorageFile(GraphOnDriveHandler originalGraphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet characteristicParameters)	{
		//XXX The case of characteristics computed on multiple graphs or sets of groups of nodes hasn't been considered here.
		StringBuilder pathBuilder = null;

		if (analysedDataIdentifier == null)	{	// We're dealing with a graph statistic computed for the original graph.
			pathBuilder = new StringBuilder(originalGraphHandler.getAbsoluteGraphDirectoryPathString()).append(STATS_FOLDER_SUFFIX);
		}
		else if (analysedDataIdentifier.type() == Type.MINED)	{	// A statistic for graph mining results.
			ExtendedMiningParameters extendedMiningParameters = analysedDataIdentifier.getMiningParameters();
			pathBuilder = makePathToStorageDirectory(originalGraphHandler, extendedMiningParameters);
		}
		else if (analysedDataIdentifier.type() == Type.EXTERNAL)	{	// A statistic for externally provided data (see <graphs/>).
			String externalFileAbsolutePath = originalGraphHandler.getAbsolutePathPossiblyWithGraphDirectory(
					analysedDataIdentifier.getExternalFilepathAsInScenario());
			pathBuilder = new StringBuilder(externalFileAbsolutePath).append(STATS_FOLDER_SUFFIX);
		}
		pathBuilder.append(SystemConfiguration.FILES_SEPARATOR);

		pathBuilder.append(characteristicParameters.getCharacteristicNameInScenario()).append(SystemConfiguration.FILES_SEPARATOR);
		//TODO Use rather the unique plug-in ID? Or both together? Or add data about the implementor? Similar to makePathToStorageDirectory(...).

		pathBuilder.append(assembleParametersValuesIntoSingleString(characteristicParameters));

		pathBuilder.append(ContentType.CHARACTERISTIC);	//XXX Better options?
		return pathBuilder.toString();
	}

	protected static String getPathStringToPerformanceStat(GraphOnDriveHandler graphHandler, ExtendedMiningParameters extendedMiningParameters)	{
		StringBuilder pathBuilder = null;
		if (extendedMiningParameters == null)	{	// We're dealing with a statistic for graph generation process.
			pathBuilder = new StringBuilder(graphHandler.getAbsoluteGraphDirectoryPathString()).append(STATS_FOLDER_SUFFIX);
		}
		else	{
			pathBuilder = makePathToStorageDirectory(graphHandler, extendedMiningParameters);
		}
		pathBuilder.append(SystemConfiguration.FILES_SEPARATOR).append(
				PerformanceStatisticParameters.PerformanceStatType.EXEC_TIME);	//ATTENTION We deal now only with one statistic: execution time.
		return pathBuilder.toString();
	}


	private static StringBuilder makePathToStorageDirectory(GraphOnDriveHandler graphHandler, ExtendedMiningParameters extendedMiningParameters)	{
		//TODO What about SupplementaryData?
		GraphMiningParametersSet miningParameters = extendedMiningParameters.getMiningParameters();

		StringBuilder pathBuilder = new StringBuilder();
		if (extendedMiningParameters.getAbsoluteExternalFilename() != null)	{
			pathBuilder.append(extendedMiningParameters.getAbsoluteExternalFilename()).append(MINED_FOLDER_SUFFIX);
		}
		else	{
			pathBuilder.append(graphHandler.getAbsoluteGraphDirectoryPathString());
		}
		pathBuilder.append(SystemConfiguration.FILES_SEPARATOR).append(miningParameters.getAlgorithmName());
		//TODO Use rather the unique plug-in ID? Or both together? Or add data about the implementor? Similar to getPathToMeasureStorageFile(...).

		if (miningParameters.useSupplementaryData())	{
			//TODO Implement.
		}
		pathBuilder.append(SystemConfiguration.FILES_SEPARATOR);

		RangeOfValues<String> relativeExternalSetsOfGroupsFilenames = extendedMiningParameters.getRelativeExternalFilenames();
		if (relativeExternalSetsOfGroupsFilenames != null  &&  !relativeExternalSetsOfGroupsFilenames.isEmpty())	{
			//Graph mining was performed over a collection of external files.
			StringBuilder allPathsStringBuilder = new StringBuilder();
			for (String relativeExternalPath : relativeExternalSetsOfGroupsFilenames)	{
				allPathsStringBuilder.append(relativeExternalPath);
			}
			pathBuilder.append("hash").append(allPathsStringBuilder.toString().hashCode()).append(SystemConfiguration.FILES_SEPARATOR);
		}

		pathBuilder.append(assembleParametersValuesIntoSingleString(miningParameters));

		if (miningParameters.useMultipleLaunches())	{
			pathBuilder.append("launch").append(miningParameters.getLaunchNumber()).append(SystemConfiguration.FILES_SEPARATOR);
		}

		return pathBuilder;
	}

	private static String assembleParametersValuesIntoSingleString(ParametersSet parameters)	{
		StringBuilder builder = new StringBuilder();

		List<List<Pair<String, String>>> miningParametersAsGroupsOfPairsOfUniqueKeysAndValues =
				parameters.getSpecifiedParametersAsGroupsOfPairsOfUniqueKeysAndValues();
		if (miningParametersAsGroupsOfPairsOfUniqueKeysAndValues != null)	{
			for (List<Pair<String, String>> groupOfPairs : miningParametersAsGroupsOfPairsOfUniqueKeysAndValues)	{
				for (Pair<String, String> keyValue : groupOfPairs)	{
					builder.append(keyValue.get1st()).append(keyValue.get2nd());
				}
				builder.append(SystemConfiguration.FILES_SEPARATOR);
			}
		}

		return builder.toString();
	}
}
