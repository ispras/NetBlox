package ru.ispras.modis.NetBlox.graphAlgorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageWriter;
import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.SetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.GraphMiningException;
import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.exceptions.SetOfGroupsException;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.AGraphMiner;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphMinerExtensionRegistry;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphOnDrive;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphWithSetsOfGroupsOfNodesMiner;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphWithSingleGroupsOfNodesSetMiner;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.MinerResults;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.SupplementaryData;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;

/**
 * The class that serves as an extension point for graph mining process.
 * 
 * @author ilya
 */
public class MiningDriver {
	private static final GraphMinerExtensionRegistry minersRegistry = new GraphMinerExtensionRegistry();


	public static boolean canMinerBeGettingGroupsToMineFromPreliminaryComputations(GraphMiningParametersSet miningParameters)	{
		String algorithmName = miningParameters.getAlgorithmName();
		GraphMiner miner = minersRegistry.getGraphMiner(algorithmName);

		switch (miningParameters.getJobBase())	{
		case NODES_GROUPS_SET:
			GraphWithSingleGroupsOfNodesSetMiner groupsOfNodesMiner = (GraphWithSingleGroupsOfNodesSetMiner) miner;
			return groupsOfNodesMiner.canGetGroupsOfNodesToMineFromPreliminaryComputations();
		case MULTIPLE_SETS_OF_GROUPS_OF_NODES:
			GraphWithSetsOfGroupsOfNodesMiner setsOfGroupsMiner = (GraphWithSetsOfGroupsOfNodesMiner) miner;
			return setsOfGroupsMiner.canGetSetsOfGroupsToMineFromPreliminaryComputations();
		}

		return false;
	}


	/**
	 * @return	total work time in milliseconds.
	 * @throws SourceGraphException 
	 * @throws GraphMiningException 
	 */
	public static long mineGraph(GraphOnDriveHandler graphHandler, SupplementaryData supplementaryData, ExtendedMiningParameters extendedMiningParameters)
			throws SourceGraphException, GraphMiningException	{
		GraphMiningParametersSet miningParameters = extendedMiningParameters.getMiningParameters();
		String algorithmName = miningParameters.getAlgorithmName();
		GraphMiner miner = minersRegistry.getGraphMiner(algorithmName);
		boolean isSourcePassedInFiles = minersRegistry.isSourcePassedInFiles(algorithmName);

		MinerResults minedResults = null;
		long timeStart = System.currentTimeMillis();
		switch (miningParameters.getJobBase())	{
		case GRAPH:
			AGraphMiner graphMiner = (AGraphMiner) miner;
			minedResults = isSourcePassedInFiles ?
					graphMiner.mine(new GraphOnDrive(graphHandler.getAbsoluteGraphPathString(), graphHandler.getGraphParameters()),
							supplementaryData, miningParameters) :
					graphMiner.mine(graphHandler.getGraph(), supplementaryData, miningParameters);	
			break;
		//case MULTIPLE_GRAPHS:	XXX Implement in outer loop.
		//	//The same principal question as below: where to get those multiple graphs (paths in graphs)?
		//	break;
		case NODES_GROUPS_SET:
			GraphWithSingleGroupsOfNodesSetMiner groupsOfNodesMiner = (GraphWithSingleGroupsOfNodesSetMiner) miner;
			minedResults = mineSetOfGroups(groupsOfNodesMiner, isSourcePassedInFiles, graphHandler,
					extendedMiningParameters.getAbsoluteExternalFilename(), supplementaryData, miningParameters);
			break;
		case MULTIPLE_SETS_OF_GROUPS_OF_NODES:
			GraphWithSetsOfGroupsOfNodesMiner setsOfGroupsMiner = (GraphWithSetsOfGroupsOfNodesMiner) miner;
			minedResults = mineSets(setsOfGroupsMiner, isSourcePassedInFiles, graphHandler,
					extendedMiningParameters.getAbsoluteExternalFilenames(), supplementaryData, miningParameters);
			break;
		default:
			throw new PluginException("Wrong plug-in callback type for graph mining extension point: the job base is specified to be "+
					miningParameters.getJobBase());
		}
		long timeStop = System.currentTimeMillis();

		if (minedResults == null)	{
			throw new PluginException("The plug-in for "+algorithmName+" returned _null_ instead of mined data.");
		}

		putMinedDataToStorage(minedResults, graphHandler, extendedMiningParameters);

		long graphMiningTime = timeStop - timeStart;
		StorageWriter.savePerformanceStatistic(graphMiningTime, graphHandler, extendedMiningParameters);

		return graphMiningTime;
	}


	private static MinerResults mineSetOfGroups(GraphWithSingleGroupsOfNodesSetMiner groupsOfNodesMiner, boolean isSourcePassedInFiles,
			GraphOnDriveHandler graphHandler, String setOfGroupsFilePathname,
			SupplementaryData supplementaryData, GraphMiningParametersSet miningParameters) throws GraphMiningException, SourceGraphException	{
		MinerResults result = null;
		if (isSourcePassedInFiles)	{
			GraphOnDrive graphOnDrive = new GraphOnDrive(graphHandler.getAbsoluteGraphPathString(), graphHandler.getGraphParameters());
			if (setOfGroupsFilePathname != null)	{
				result = groupsOfNodesMiner.mine(graphOnDrive, setOfGroupsFilePathname, supplementaryData, miningParameters);
			}
			else	{
				result = groupsOfNodesMiner.mineFromPreliminaryComputationsResults(graphOnDrive, supplementaryData, miningParameters);
			}
		}
		else	{
			IGraph graph = graphHandler.getGraph();
			if (setOfGroupsFilePathname != null)	{
				try	{
					SetOfGroupsOfNodes setOfGroupsOfNodes = new SetOfGroupsOfNodes(setOfGroupsFilePathname, graph);
					result = groupsOfNodesMiner.mine(graph, setOfGroupsOfNodes, supplementaryData, miningParameters);
				}
				catch (SetOfGroupsException e)	{
					throw new GraphMiningException(e);
				}
			}
			else	{
				result = groupsOfNodesMiner.mineFromPreliminaryComputationsResults(graph, supplementaryData, miningParameters);
			}
		}
		return result;
	}

	private static MinerResults mineSets(GraphWithSetsOfGroupsOfNodesMiner setsOfGroupsMiner, boolean isSourcePassedInFiles,
			GraphOnDriveHandler graphHandler, List<String> absoluteExternalFilenames,
			SupplementaryData supplementaryData, GraphMiningParametersSet miningParameters) throws GraphMiningException, SourceGraphException	{
		MinerResults minedResults = null;

		if (isSourcePassedInFiles)	{
			GraphOnDrive graphOnDrive = new GraphOnDrive(graphHandler.getAbsoluteGraphPathString(), graphHandler.getGraphParameters());
			if (absoluteExternalFilenames != null  &&  !absoluteExternalFilenames.isEmpty())	{
				minedResults = setsOfGroupsMiner.mine(graphOnDrive, absoluteExternalFilenames, supplementaryData, miningParameters);
			}
			else	{
				minedResults = setsOfGroupsMiner.mineFromPreliminaryComputationsResults(graphOnDrive, supplementaryData, miningParameters);
			}
		}
		else	{
			IGraph graph = graphHandler.getGraph();
			if (absoluteExternalFilenames != null  &&  !absoluteExternalFilenames.isEmpty())	{
				List<ISetOfGroupsOfNodes> setsOfGroups = new ArrayList<ISetOfGroupsOfNodes>(absoluteExternalFilenames.size());
				for (String externalPathstring : absoluteExternalFilenames)	{
					try	{
						SetOfGroupsOfNodes setOfGroupsOfNodes = new SetOfGroupsOfNodes(externalPathstring, graph);
						setsOfGroups.add(setOfGroupsOfNodes);
					}
					catch (SetOfGroupsException e)	{
						throw new GraphMiningException(e);
					}
				}
				minedResults = setsOfGroupsMiner.mine(graph, setsOfGroups, supplementaryData, miningParameters);
			}
			else	{
				minedResults = setsOfGroupsMiner.mineFromPreliminaryComputationsResults(graph, supplementaryData, miningParameters);
			}
		}
		return minedResults;
	}


	private static void putMinedDataToStorage(MinerResults minedResults, GraphOnDriveHandler graphHandler,
			ExtendedMiningParameters extendedMiningParameters)	throws GraphMiningException	{
		try {
			switch (minedResults.getResultType())	{
			case NODES_GROUPS:
				saveMinedNodesGroups(minedResults, graphHandler, extendedMiningParameters);
				break;
			case GRAPH_EDGES:
				saveMinedGraphStructure(minedResults, graphHandler, extendedMiningParameters);
				break;
			case SETS_OF_GRAPH_EDGES:
				saveMultipleGraphStructures(minedResults, graphHandler, extendedMiningParameters);
				break;
			case CHARACTERISTIC:
				StorageWriter.saveMined(minedResults.getCharacteristic(), graphHandler, extendedMiningParameters);
				break;
			case MULTIRESULT:
				List<MinerResults> multipleResults = minedResults.getMultipleResults();
				for (MinerResults singleResultsSet : multipleResults)	{
					ExtendedMiningParameters extendedParameters = new ExtendedMiningParameters(
							singleResultsSet.getParametersOfAlgorithm(), extendedMiningParameters);
					putMinedDataToStorage(singleResultsSet, graphHandler, extendedParameters);
				}
				break;
			}
		} catch (IOException e) {
			String message = "Could not put the mined results to storage: "+e.getMessage();
			throw new GraphMiningException(message);
		}
	}

	private static void saveMinedNodesGroups(MinerResults minedResults, GraphOnDriveHandler graphHandler,
			ExtendedMiningParameters extendedMiningParameters)	throws IOException	{
		switch (minedResults.getProvisionFormat())	{
		case FILE_PATH_STRING:
			StorageWriter.saveMined(minedResults.getNodesGroupsFilePathString(), graphHandler, extendedMiningParameters,
					StorageHandler.ContentType.NODES_GROUPS);
			break;
		case INTERNAL:
			StorageWriter.saveMined(minedResults.getNodesGroups(), graphHandler, extendedMiningParameters);
			break;
		case LIST_OF_STRINGS:
			StorageWriter.saveMined(minedResults.getNodesGroupsStrings(), graphHandler, extendedMiningParameters,
					StorageHandler.ContentType.NODES_GROUPS);
			break;
		case STREAM:
			StorageWriter.saveMined(minedResults.getNodesGroupsStream(), graphHandler, extendedMiningParameters,
					StorageHandler.ContentType.NODES_GROUPS);
			break;
		}
	}

	private static void saveMinedGraphStructure(MinerResults minedResults, GraphOnDriveHandler graphHandler,
			ExtendedMiningParameters extendedMiningParameters)	throws IOException	{
		switch (minedResults.getProvisionFormat())	{
		case FILE_PATH_STRING:
			StorageWriter.saveMined(minedResults.getMinedGraphStructureFilePathString(), graphHandler, extendedMiningParameters,
					StorageHandler.ContentType.GRAPH_EDGES);
			break;
		case INTERNAL:
			StorageWriter.saveMined(minedResults.getMinedGraphStructure(), graphHandler, extendedMiningParameters);
			break;
		case LIST_OF_STRINGS:
			StorageWriter.saveMined(minedResults.getMinedGraphStrings(), graphHandler, extendedMiningParameters,
					StorageHandler.ContentType.GRAPH_EDGES);
			break;
		case STREAM:
			StorageWriter.saveMined(minedResults.getMinedGraphStream(), graphHandler, extendedMiningParameters,
					StorageHandler.ContentType.GRAPH_EDGES);
			break;
		}
	}

	private static void saveMultipleGraphStructures(MinerResults minedResults, GraphOnDriveHandler graphHandler,
			ExtendedMiningParameters extendedMiningParameters)	throws IOException	{
		switch (minedResults.getProvisionFormat())	{
		case FILE_PATH_STRING:
			StorageWriter.saveMinedMultipleFiles(minedResults.getMultipleGraphStructuresFilePathStrings(), graphHandler, extendedMiningParameters);
			break;
		case INTERNAL:
			StorageWriter.saveMinedMultipleGraphs(minedResults.getMultipleGraphStructures(), graphHandler, extendedMiningParameters);
			break;
		case LIST_OF_STRINGS:
			StorageWriter.saveMinedMultipleListsOfStrings(minedResults.getStringsForMultipleGraphs(), graphHandler, extendedMiningParameters);
			break;
		case STREAM:
			StorageWriter.saveMinedMultipleStreams(minedResults.getMultipleGraphsStreams(), graphHandler, extendedMiningParameters);
			break;
		}
	}
}
