package ru.ispras.modis.NetBlox.graphAlgorithms;

import java.io.IOException;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageHandler.ContentType;
import ru.ispras.modis.NetBlox.dataManagement.StorageScanner;
import ru.ispras.modis.NetBlox.dataManagement.StorageWriter;
import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.SetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.AnalysedDataIdentifier;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.MeasureComputationException;
import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.exceptions.SetOfGroupsException;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.exceptions.StorageException;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.GraphOnDrive;
import ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics.CharacteristicComputer;
import ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics.CharacteristicEvaluator;
import ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics.CharacteristicsExtensionRegistry;
import ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics.GraphCharacteristicComputer;
import ru.ispras.modis.NetBlox.graphAlgorithms.numericCharacteristics.GroupsOfNodesSetCharacteristicComputer;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;

/**
 * The class that serves as an extension point for characteristics computation plug-ins.
 * 
 * @author ilya
 */
public class CharacteristicsDriver {
	private static final CharacteristicsExtensionRegistry characteristicsRegistry = new CharacteristicsExtensionRegistry();

	/**
	 * Compute the characteristics that are based on single-source data (single graph structures, single sets of groups of nodes, mined characteristics).
	 * @param graphHandler
	 * @param analysedDataIdentifier
	 * @param characteristicParameters
	 * @return
	 * @throws SourceGraphException
	 * @throws StorageException
	 * @throws MeasureComputationException 
	 */
	public static NumericCharacteristic computeCharacteristic(GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet characteristicParameters)	throws SourceGraphException, StorageException, MeasureComputationException	{
		String characteristicName = characteristicParameters.getCharacteristicNameInScenario();
		CharacteristicComputer characteristicComputer = characteristicsRegistry.getCharacteristicComputer(characteristicName);
		boolean isSourcePassedInFiles = characteristicsRegistry.isSourcePassedInFiles(characteristicName);

		NumericCharacteristic result = null;
		switch (characteristicParameters.getJobBase())	{
		case GRAPH:
			result = computeGraphStatistic((GraphCharacteristicComputer) characteristicComputer, isSourcePassedInFiles,
					graphHandler, analysedDataIdentifier, characteristicParameters);
			break;
		case NODES_GROUPS_SET:
			result = computeGroupsOfNodesStatistic((GroupsOfNodesSetCharacteristicComputer) characteristicComputer, isSourcePassedInFiles,
					graphHandler, analysedDataIdentifier, characteristicParameters);
			break;
		case NUMERIC_CHARACTERISTIC:
			CharacteristicEvaluator characteristicEvaluator = (CharacteristicEvaluator) characteristicComputer;
			NumericCharacteristic minedCharacteristic = StorageScanner.getMinedCharacteristic(graphHandler, analysedDataIdentifier.getMiningParameters());
			result = characteristicEvaluator.run(minedCharacteristic, characteristicParameters);
			break;
		default:	//XXX Is there any sense in this last check, considering all other possibilities?
			throw new PluginException("Wrong plug-in callback type for characteristics computation extension point: the job base is specified to be "+
					characteristicParameters.getJobBase());
		}

		if (result == null)	{
			throw new PluginException("The plug-in for "+characteristicName+" returned _null_ instead of computed characteristic.");
		}

		try {
			StorageWriter.saveStatistic(result, graphHandler, analysedDataIdentifier, characteristicParameters);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

			//case MULTIPLE_GRAPHS:
			//	MultipleGraphsCharacteristicComputer graphsCharacteristicComputer = (MultipleGraphsCharacteristicComputer) characteristicComputer;
			//	result = isSourcePassedInFiles ? graphsCharacteristicComputer.run(null /*TODO paths to files with graphs*/, characteristicParameters) :
			//		graphsCharacteristicComputer.runWithInternalRepresentation(null /*TODO List<Graph>*/, characteristicParameters);
			//	//TODO OK, where do we get those (^) multiple graphs?
			//case MULTIPLE_SETS_OF_GROUPS_OF_NODES:
			//	SetsOfGroupsOfNodesCharacteristicComputer setsOfGroupsCharacteristicComputer =
			//		(SetsOfGroupsOfNodesCharacteristicComputer) characteristicComputer;
			//	//TODO Now, how and where am I going to get multiple sets of groups of nodes? Or is it an unnecessary case for characteristics computing?
			//	result = isSourcePassedInFiles ? setsOfGroupsCharacteristicComputer.run(
			//				graphHandler.getAbsoluteGraphPathString(), null /*TODO list of paths to files with groups of nodes*/,
			//				graphHandler.getAbsoluteReferenceCoverPathString(), characteristicParameters) :
			//			setsOfGroupsCharacteristicComputer.run(graphHandler.getGraph(), null /*TODO List<ISetOfGroupsOfNodes>*/,
			//				null /*TODO ISetOfGroupsOfNodes for reference cover*/, characteristicParameters);
			//	break;
	}

	private static NumericCharacteristic computeGraphStatistic(GraphCharacteristicComputer computer, boolean isSourcePassedInFiles,
			GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier, MeasureParametersSet characteristicParameters)
					throws SourceGraphException, StorageException, MeasureComputationException	{
		NumericCharacteristic result = null;
		if (analysedDataIdentifier == null)	{	// We're dealing with the original graph.
			result = isSourcePassedInFiles ?
				computer.run(new GraphOnDrive(graphHandler.getAbsoluteGraphPathString(), graphHandler.getGraphParameters()), characteristicParameters) :
				computer.run(graphHandler.getGraph(), characteristicParameters);
		}
		else	{	// Dealing with mined graphs.
			ExtendedMiningParameters miningParameters = analysedDataIdentifier.getMiningParameters();
			if (isSourcePassedInFiles)	{
				String minedGraphPath = StorageScanner.getMinedDataFilePathstring(graphHandler, miningParameters, ContentType.GRAPH_EDGES);
				result = computer.run(new GraphOnDrive(minedGraphPath, graphHandler.getGraphParameters()), characteristicParameters);
			}
			else	{
				IGraph minedGraphStructure = StorageScanner.getMinedGraphStructure(graphHandler, miningParameters);
				result = computer.run(minedGraphStructure, characteristicParameters);
			}
		}
		return result;
	}

	private static NumericCharacteristic computeGroupsOfNodesStatistic(GroupsOfNodesSetCharacteristicComputer computer, boolean isSourcePassedInFiles,
			GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier, MeasureParametersSet characteristicParameters)
					throws SourceGraphException, StorageException, MeasureComputationException	{
		NumericCharacteristic result = null;
		switch (analysedDataIdentifier.type())	{
		case MINED:
			ExtendedMiningParameters miningParameters = analysedDataIdentifier.getMiningParameters();
			if (isSourcePassedInFiles)	{
				String minedDataPathString = StorageScanner.getMinedDataFilePathstring(graphHandler, miningParameters, ContentType.NODES_GROUPS);
				result = computer.run(new GraphOnDrive(graphHandler.getAbsoluteGraphPathString(), graphHandler.getGraphParameters()), minedDataPathString,
						graphHandler.getAbsoluteReferenceCoverPathString(), characteristicParameters);
			}
			else	{
				ISetOfGroupsOfNodes minedData = StorageScanner.getMinedGroupsOfNodes(graphHandler, miningParameters);
				ISetOfGroupsOfNodes referenceData = graphHandler.doesReferenceSetOfGroupsOfNodesExist() ? graphHandler.getReference() : null;
				result = computer.run(graphHandler.getGraph(), minedData, referenceData, characteristicParameters);
			}
			break;
		case EXTERNAL:
			String pathToExternalFileWithGroupsOfNodes = graphHandler.getAbsolutePathPossiblyWithGraphDirectory(
					analysedDataIdentifier.getExternalFilepathAsInScenario());
			if (isSourcePassedInFiles)	{
				result = computer.run(new GraphOnDrive(graphHandler.getAbsoluteGraphPathString(), graphHandler.getGraphParameters()),
						pathToExternalFileWithGroupsOfNodes, graphHandler.getAbsoluteReferenceCoverPathString(), characteristicParameters);
			}
			else	{
				IGraph originalGraph = graphHandler.getGraph();
				try {
					SetOfGroupsOfNodes externalSetOfGroups = new SetOfGroupsOfNodes(pathToExternalFileWithGroupsOfNodes, originalGraph);
					ISetOfGroupsOfNodes referenceData = graphHandler.doesReferenceSetOfGroupsOfNodesExist() ? graphHandler.getReference() : null;
					result = computer.run(originalGraph, externalSetOfGroups, referenceData, characteristicParameters);
				} catch (SetOfGroupsException e) {
					throw new MeasureComputationException(e);
				}
			}
			break;
		}
		return result;
	}
}
