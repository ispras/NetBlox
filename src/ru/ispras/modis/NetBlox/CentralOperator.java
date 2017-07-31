package ru.ispras.modis.NetBlox;

import java.util.Collection;
import java.util.Iterator;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageCleaner;
import ru.ispras.modis.NetBlox.dataManagement.StorageScanner;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.AnalysedDataIdentifier;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ArrangedData;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.DataArrangementException;
import ru.ispras.modis.NetBlox.exceptions.GraphGenerationException;
import ru.ispras.modis.NetBlox.exceptions.GraphMiningException;
import ru.ispras.modis.NetBlox.exceptions.MeasureComputationException;
import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.exceptions.StorageException;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.graphAlgorithms.CharacteristicsDriver;
import ru.ispras.modis.NetBlox.graphAlgorithms.GraphsObtainer;
import ru.ispras.modis.NetBlox.graphAlgorithms.MiningDriver;
import ru.ispras.modis.NetBlox.graphAlgorithms.graphMining.SupplementaryData;
import ru.ispras.modis.NetBlox.graphVisualisation.GraphVisualisationManager;
import ru.ispras.modis.NetBlox.numericResultsPresentation.PlotsDrawer;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;
import ru.ispras.modis.NetBlox.scenario.ParametersSet;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;
import ru.ispras.modis.NetBlox.scenario.UploadedGraphDataParametersSet;
import ru.ispras.modis.NetBlox.scenario.ValueFromRange;
import ru.ispras.modis.NetBlox.scenario.performanceStats.PerformanceStatisticParameters;

/**
 * Directs the whole scenario execution process.
 * 
 * @author ilya
 */
public class CentralOperator {
	private Collection<ScenarioTask> scenario;


	public CentralOperator(Collection<ScenarioTask> scenario)	{
		this.scenario = scenario;
	}

	public void executeScenario()	{
		int executedTaskNumber = 0;
		for (ScenarioTask task : scenario)	{
			executedTaskNumber++;

			if (task.getGoal() == ScenarioTask.Goal.NONE)	{
				continue;
			}

			StringBuilder startTaskMessageBuilder = new StringBuilder("Starting task number ").append(executedTaskNumber).
					append(" - ").append(task.getGoal());
			if (task.recompute() != ScenarioTask.Recompute.NO)	{
				startTaskMessageBuilder.append(" (recompute ").append(task.recompute()).append(")");
			}
			System.out.println(startTaskMessageBuilder.toString());
			long taskStartTime = System.currentTimeMillis();

			executeTask(task);

			double executionTimeSeconds = 0.001  *  (System.currentTimeMillis() - taskStartTime);
			System.out.println("Finished task number "+executedTaskNumber+":\t"+executionTimeSeconds+" seconds");
		}
	}


	private void executeTask(ScenarioTask scenarioTask)	{
		ArrangedData arrangedData = null;
		if (scenarioTask.doNeedArrangedData())	{
			arrangedData = new ArrangedData(scenarioTask);
		}

		GraphParametersSet.resetNumberOfGraphParametersSets();
		for (Iterator<ParametersSet> graphsParametersIterator = scenarioTask.getGraphsIterator() ;
				graphsParametersIterator.hasNext() ; )	{
			GraphParametersSet fixedGraphParameters = (GraphParametersSet) graphsParametersIterator.next();
			System.out.println("\tGraph: "+fixedGraphParameters.toString());

			try {
				GraphOnDriveHandler graphHandler = makeSureThereIsTheGraph(fixedGraphParameters, scenarioTask);

				if (scenarioTask.getGoal()==ScenarioTask.Goal.CLEAR && !graphHandler.doesGraphExistOnDisk())	{
					//Nothing needs to be done.
					continue;
				}

				if (scenarioTask.doRunGraphMining())	{
					executeTaskWithGraphMining(scenarioTask, graphHandler, arrangedData);
				}
				else	{
					executeTaskNoGraphMining(scenarioTask, graphHandler, arrangedData);
				}

				if (scenarioTask.getGoal() == ScenarioTask.Goal.GRAPH_VISUALISATION)	{
					//Try to visualise the original graph and provided substructures in either case.
					accomplishGraphVisualisation(scenarioTask, graphHandler);
				}
			} catch (GraphGenerationException | SourceGraphException | PluginException e) {
				e.printStackTrace();
				//Tell the user about the problems with the concrete graph and go on with the next one.
			}

			System.out.println("\tDone with graph.");
		}

		if (scenarioTask.doRunGraphMining())	{
			//TODO If we have graph mining algorithms that work with several graphs at once then run them now (MULTIPLE_GRAPHS).
		}

		//TODO If we have the "measures" goal and some multisource measures have been specified (MULTIPLE_GRAPHS, MULTIPLE_SETS_OF_GROUPS_OF_NODES)
		// then compute them now.

		presentResultsToUser(scenarioTask, arrangedData);
	}

	private GraphOnDriveHandler makeSureThereIsTheGraph(GraphParametersSet fixedGraphParameters, ScenarioTask scenarioTask)
			throws GraphGenerationException, SourceGraphException	{
		GraphOnDriveHandler graphHandler = new GraphOnDriveHandler(fixedGraphParameters);

		switch (scenarioTask.getGoal())	{
		case CLEAR:		//No need to generate the graph in case it does not exist.
		case CLEARALL:
			return graphHandler;
		}

		if (scenarioTask.recompute() == ScenarioTask.Recompute.GRAPHS)	{	//Delete old results in case of recompute, they are irrelative.
			runCleaning(ScenarioTask.Goal.CLEAR, graphHandler, null, null);
		}

		if (!graphHandler.doesGraphExistOnDisk())	{
			System.out.println("\t\tNeed to generate the graph.");
			GraphsObtainer.obtainGraph(graphHandler);
			System.out.println("\t\tHave generated the graph.");
		}

		return graphHandler;
	}


	/**
	 * Here can be statistics for graphs or groups of nodes, performance statistics or graph/groups visualisation.
	 * The latter doesn't need any computations to be run.
	 * @throws GraphMiningException 
	 * @throws GraphGenerationException 
	 * @throws SourceGraphException 
	 */
	private void executeTaskNoGraphMining(ScenarioTask scenarioTask, GraphOnDriveHandler graphHandler, ArrangedData arrangedData)
			throws SourceGraphException, GraphGenerationException	{
		ScenarioTask.Goal goal = scenarioTask.getGoal();
		try {
			switch (goal)	{
			case PERFORMANCE:
				accomplishPerformanceGoal(scenarioTask, graphHandler, null, arrangedData);
				break;
			case MEASURES:
				accomplishMeasuresGoalOnSolitarySources(scenarioTask, graphHandler, null, arrangedData);
				break;
			/*case GRAPH_VISUALISATION:
				accomplishGraphVisualisation(scenarioTask, graphHandler, null);
				break;*/
			case CLEAR:
			case CLEARALL:
				accomplishClear(scenarioTask, graphHandler, null);
				break;
			}
		} catch (GraphMiningException e) {	//An impossible situation. This exception exists here just for the sake of compatibility.
			e.printStackTrace();
		}
	}

	/**
	 * All the spectrum of tasks.
	 * @param scenarioTask
	 * @param graphHandler
	 * @param arrangedData
	 * @throws SourceGraphException
	 * @throws GraphGenerationException
	 */
	private void executeTaskWithGraphMining(ScenarioTask scenarioTask, GraphOnDriveHandler graphHandler, ArrangedData arrangedData)
			throws SourceGraphException, GraphGenerationException	{
		for (Iterator<ParametersSet> graphMinersIterator = scenarioTask.getGraphMinersIterator() ;
				graphMinersIterator.hasNext() ; )	{
			GraphMiningParametersSet fixedGraphMiningParameters = (GraphMiningParametersSet) graphMinersIterator.next();

			System.out.println("\t\tGraph mining algorithm: "+fixedGraphMiningParameters.getAlgorithmName()+"; "+
					fixedGraphMiningParameters.getSpecifiedParametersAsGroupsOfPairsOfUniqueKeysAndValues());

			RangeOfValues<String> externalSetsOfGroupsFilenames =
					graphHandler.getGraphParameters().getProvidedForMiningExternalDataFilenames();
			//FUTURE_WORK Can there be not only sets of groups in those external files provided for mining (in addition to graph)?
			try {
				switch (fixedGraphMiningParameters.getJobBase())	{
				case GRAPH:
					achieveTheGoal(scenarioTask, graphHandler, null, null, fixedGraphMiningParameters, arrangedData);
					break;
				case NODES_GROUPS_SET:
					if (externalSetsOfGroupsFilenames != null)	{
						String rangeId = externalSetsOfGroupsFilenames.getRangeId();
						for (String relativeExternalPathstring : externalSetsOfGroupsFilenames)	{
							achieveTheGoal(scenarioTask, graphHandler, new ValueFromRange<String>(rangeId, relativeExternalPathstring), null,
									fixedGraphMiningParameters, arrangedData);
						}
					}
					else if (MiningDriver.canMinerBeGettingGroupsToMineFromPreliminaryComputations(fixedGraphMiningParameters))	{
						achieveTheGoal(scenarioTask, graphHandler, null, null, fixedGraphMiningParameters, arrangedData);
					}
					else	{
						System.out.println("\t\t\tWARNING\tThere's no data (no nodes groups sets) on which the computations can be done.");
					}
					break;
				case MULTIPLE_SETS_OF_GROUPS_OF_NODES:
					if (externalSetsOfGroupsFilenames != null)	{
						achieveTheGoal(scenarioTask, graphHandler, null, externalSetsOfGroupsFilenames, fixedGraphMiningParameters, arrangedData);
					}
					else if (MiningDriver.canMinerBeGettingGroupsToMineFromPreliminaryComputations(fixedGraphMiningParameters))	{
						achieveTheGoal(scenarioTask, graphHandler, null, null, fixedGraphMiningParameters, arrangedData);
					}
					else	{
						System.out.println("\t\t\tWARNING\tThere's no data (no collections of nodes groups sets) on which the computations can be done.");
					}
					break;
				default:	//Other variants of job base are skipped in this method.
					System.out.println("\t\t\tSkipping as "+fixedGraphMiningParameters.getJobBase());
					return;
				}
			} catch (GraphMiningException e) {
				// Tell the user about the problems with a concrete graph mining algorithm launch and go on.
				e.printStackTrace();
			}
		}
	}


	private void achieveTheGoal(ScenarioTask task, GraphOnDriveHandler graphHandler, ValueFromRange<String> relativeExternalPathstring,
			RangeOfValues<String> externalSetsOfGroupsFilenames, GraphMiningParametersSet fixedGraphMiningParameters, ArrangedData arrangedData)
			throws SourceGraphException, GraphMiningException, GraphGenerationException	{
		ExtendedMiningParameters miningParameters = new ExtendedMiningParameters(
				fixedGraphMiningParameters, relativeExternalPathstring, externalSetsOfGroupsFilenames, graphHandler);

		ScenarioTask.Goal goal = task.getGoal();
		switch (goal)	{
		case MINING:
			accomplishMiningGoal(task, graphHandler, miningParameters);
			break;
		case PERFORMANCE:
			accomplishPerformanceGoal(task, graphHandler, miningParameters, arrangedData);
			break;
		case MEASURES:
			accomplishMeasuresGoalOnSolitarySources(task, graphHandler, miningParameters, arrangedData);
			break;
		case GRAPH_VISUALISATION:
			accomplishGraphVisualisation(task, graphHandler, miningParameters);
			break;
		case CLEAR:
		case CLEARALL:
			accomplishClear(task, graphHandler, miningParameters);
			break;
		}
	}

	/**
	 * The aims of graph mining section should be simply achieved (communities discovered, etc.). We only need to know it has been done.
	 * @throws SourceGraphException 
	 * @throws GraphMiningException 
	 */
	private void accomplishMiningGoal(ScenarioTask task, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)
			throws SourceGraphException, GraphMiningException	{
		if (task.recompute() == ScenarioTask.Recompute.MINING)	{	//Delete old results in case of recompute, they are irrelative.
			runCleaning(ScenarioTask.Goal.CLEAR, graphHandler, new AnalysedDataIdentifier(miningParameters), null);
		}

		if (!StorageScanner.containsMinedData(graphHandler, miningParameters))	{
			runGraphMiner(graphHandler, miningParameters, false);
		}

		//FUTURE_WORK: Tell the user everything is Ok (if it is, if no exceptions have been thrown).
	}

	/**
	 * The graphs should be generated, the graph mining aims should be achieved. We need performance statistics gathered during these processes.
	 * @param miningParameters	- if is not null, get performance statistics for specified graph mining process;
	 * 							get the statistic for graph generation otherwise. 
	 * @throws GraphGenerationException 
	 * @throws SourceGraphException 
	 * @throws GraphMiningException 
	 */
	private void accomplishPerformanceGoal(ScenarioTask task, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters,
			ArrangedData arrangedData)	throws SourceGraphException, GraphGenerationException, GraphMiningException	{
		Long workTimeMillis = null;
		if (miningParameters == null)	{	//Need performance statistics for graph generation.
			workTimeMillis = StorageScanner.getPerformanceTime(graphHandler);

			if (workTimeMillis == null)	{
				GraphParametersSet graphParameters = graphHandler.getGraphParameters();
				if (graphParameters instanceof UploadedGraphDataParametersSet)	{
					String message = "The uncategorised graph cannot be generated, no performance stats for this. Graph: "+graphParameters.toString();
					throw new SourceGraphException(message);
				}
				workTimeMillis = GraphsObtainer.obtainGraph(graphHandler);
			}
		}
		else	{							//Need performance statistics for graph mining.
			if (task.recompute() == ScenarioTask.Recompute.MINING)	{	//Delete old results in case of recompute, they are irrelative.
				runCleaning(ScenarioTask.Goal.CLEAR, graphHandler, new AnalysedDataIdentifier(miningParameters), null);
			}

			workTimeMillis = StorageScanner.getOverallPerformanceTime(graphHandler, miningParameters);
			if (workTimeMillis==null)	{
				workTimeMillis = runGraphMiner(graphHandler, miningParameters, true);
			}
		}

		if (arrangedData != null)	{
			float workTimeSeconds = (float)workTimeMillis / 1000;
			NumericCharacteristic statisticValue = new NumericCharacteristic(NumericCharacteristic.Type.SINGLE_VALUE, workTimeSeconds);

			AnalysedDataIdentifier analysedDataIdentifier = (miningParameters==null) ? null : new AnalysedDataIdentifier(miningParameters);
			MeasureParametersSet statisticParameters = new PerformanceStatisticParameters();
			try {
				arrangedData.putComputedStatistic(graphHandler, analysedDataIdentifier, statisticParameters, statisticValue);
			} catch (DataArrangementException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * We need measures computed over mined data.
	 * @throws GraphMiningException 
	 * @throws SourceGraphException 
	 */
	private void accomplishMeasuresGoalOnSolitarySources(ScenarioTask task, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters,
			ArrangedData arrangedData)	throws SourceGraphException, GraphMiningException	{
		if (miningParameters != null)	{
			//The mining data must be computed to run the computations of characteristics.
			accomplishMiningGoal(task, graphHandler, miningParameters);
		}

		RangeOfValues<String> providedForMeasuresSetsOfGroupsOfNodesFilenames =
				graphHandler.getGraphParameters().getProvidedForCharacterizationExternalFilenames();
		//FUTURE_WORK #5474 Those can be not only sets of groups of nodes in those files.

		for (Iterator<ParametersSet> measuresIterator = task.getMeasuresIterator() ;
				measuresIterator.hasNext() ; )	{
			MeasureParametersSet characteristicParameters = (MeasureParametersSet) measuresIterator.next();

			System.out.print("\t\t\tMeasure: "+characteristicParameters.getJobBase()+"\t"+
					characteristicParameters.getCharacteristicNameInScenario()+"\t"+
					characteristicParameters.getSpecifiedParametersAsGroupsOfPairsOfUniqueKeysAndValues());

			//FUTURE_WORK Rewrite so that the process for graph mining results and for externally provided data will be divided. #5474 ?
			// (But they must be both attempted in this method in case of graph mining section present in scenario.)

			AnalysedDataIdentifier minedDataIdentifier = new AnalysedDataIdentifier(miningParameters);
			boolean couldComputeCharacteristicOnMiningResults = false;
			switch (characteristicParameters.getJobBase())	{
			case GRAPH:
				if (miningParameters != null  &&  StorageScanner.containsMinedGraph(graphHandler, miningParameters))	{
					computeCharacteristic(task, graphHandler, minedDataIdentifier, characteristicParameters, arrangedData);
					couldComputeCharacteristicOnMiningResults = true;
				}

				//TODO Check whether we do run characteristic computations on original graphs. Requires adding parameters to measure description.
				//TODO Check whether there're only some specific graphs on which computations can be run and whether the current graph suits.
				computeCharacteristic(task, graphHandler, null, characteristicParameters, arrangedData);	//XXX Run only if responds to the condition above^.

				//TODO Can there be externally provided graphs on which characteristics should be computed?	#5474
				break;
			case NODES_GROUPS_SET:
				if (miningParameters != null  &&  StorageScanner.containsMinedGroupsOfNodes(graphHandler, miningParameters))	{
					computeCharacteristic(task, graphHandler, minedDataIdentifier, characteristicParameters, arrangedData);
					couldComputeCharacteristicOnMiningResults = true;
				}

				if (providedForMeasuresSetsOfGroupsOfNodesFilenames != null)	{
					//FIXME These computations will be run as many times as there're combinations of mining parameters. #5474
					for (String setOfGroupsOfNodesFilename : providedForMeasuresSetsOfGroupsOfNodesFilenames)	{
						AnalysedDataIdentifier externalGroupsIdentifier = new AnalysedDataIdentifier(setOfGroupsOfNodesFilename);
						computeCharacteristic(task, graphHandler, externalGroupsIdentifier, characteristicParameters, arrangedData);
					}
				}
				break;
			case NUMERIC_CHARACTERISTIC:
				if (miningParameters != null  &&  StorageScanner.containsMinedCharacteristic(graphHandler, miningParameters))	{
					computeCharacteristic(task, graphHandler, minedDataIdentifier, characteristicParameters, arrangedData);
					couldComputeCharacteristicOnMiningResults = true;
				}
				break;
			}

			//#4689. Sometimes mining methods could have got no results. But it may be necessary to plot this absence of result.
			if (miningParameters != null  &&  !couldComputeCharacteristicOnMiningResults  &&  arrangedData != null)	{
				try {
					arrangedData.putComputedStatistic(graphHandler, minedDataIdentifier, characteristicParameters, null);
				} catch (DataArrangementException e) {
					e.printStackTrace();
				}
			}

			System.out.println();
		}
	}

	/**
	 * Visualise mined graph structures or sets of groups of nodes.
	 * @throws GraphMiningException 
	 * @throws SourceGraphException 
	 */
	private void accomplishGraphVisualisation(ScenarioTask task, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)
			throws SourceGraphException, GraphMiningException	{
		//The mining data must be computed for the results to be visualised.
		accomplishMiningGoal(task, graphHandler, miningParameters);

		for (GraphVisualisationDescription visualisationDescription : task.getGraphVisualisationDescriptions())	{
			if (visualisationDescription.getSubstructuresFinalPresentationType() == GraphVisualisationDescription.FinalPresentationType.NO)	{
				continue;	//This visualisation description says to show no substructures, either mined or externally provided.
			}

			try {
				GraphVisualisationManager.visualise(visualisationDescription, graphHandler, miningParameters);
			} catch (VisualisationException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Visualise the original graph or provided external sets of groups of nodes (covers).
	 * @throws GraphMiningException 
	 * @throws SourceGraphException 
	 */
	private void accomplishGraphVisualisation(ScenarioTask task, GraphOnDriveHandler graphHandler)	{
		for (GraphVisualisationDescription visualisationDescription : task.getGraphVisualisationDescriptions())	{
			try {
				GraphVisualisationManager.visualise(visualisationDescription, graphHandler);
			} catch (VisualisationException e) {
				e.printStackTrace();
			}
		}
	}

	private void accomplishClear(ScenarioTask task, GraphOnDriveHandler graphHandler, ExtendedMiningParameters miningParameters)	{
		if (!task.doDealWithStatsNMeasures())	{	//No measures section in scenario task.
			AnalysedDataIdentifier analysedDataIdentifier = (miningParameters==null) ? null : new AnalysedDataIdentifier(miningParameters);
			runCleaning(task.getGoal(), graphHandler, analysedDataIdentifier, null);
			return;
		}

		RangeOfValues<String> providedForMeasuresDataFilenames =
				graphHandler.getGraphParameters().getProvidedForCharacterizationExternalFilenames();

		for (Iterator<ParametersSet> measuresIterator = task.getMeasuresIterator() ;
				measuresIterator.hasNext() ; )	{
			MeasureParametersSet characteristicParameters = (MeasureParametersSet) measuresIterator.next();

			System.out.print("\t\t\tMeasure: "+characteristicParameters.getJobBase()+"\t"+
					characteristicParameters.getCharacteristicNameInScenario()+"\t"+
					characteristicParameters.getSpecifiedParametersAsGroupsOfPairsOfUniqueKeysAndValues());

			//Clear the measures that were computed over the mining results (or over the graph itself - if no mining was done).
			AnalysedDataIdentifier analysedDataIdentifier = (miningParameters==null) ? null : new AnalysedDataIdentifier(miningParameters);
			runCleaning(task.getGoal(), graphHandler, analysedDataIdentifier, characteristicParameters);

			if (providedForMeasuresDataFilenames != null)	{
				//FIXME How many times will this thing be run? Considering the case when the graph mining section is present in the scenario.	#5474?
				for (String setOfGroupsOfNodesFilename : providedForMeasuresDataFilenames)	{
					//Clear the measures that were computed over the externally provided data sets.
					analysedDataIdentifier = new AnalysedDataIdentifier(setOfGroupsOfNodesFilename);
					runCleaning(task.getGoal(), graphHandler, analysedDataIdentifier, characteristicParameters);
				}
			}

			System.out.println();
		}
	}


	/**
	 * @return	total work time in milliseconds.
	 * @throws SourceGraphException 
	 * @throws GraphMiningException 
	 */
	private long runGraphMiner(GraphOnDriveHandler graphHandler, ExtendedMiningParameters extendedMiningParameters, boolean requestPerformanceStatistics)
			throws SourceGraphException, GraphMiningException	{
		GraphMiningParametersSet miningParameters = extendedMiningParameters.getMiningParameters();
		//XXX Will requestPerformanceStatistics be necessary?

		long totalWorkTimeMillis = 0;

		SupplementaryData supplementaryData = null;
		if (miningParameters.useSupplementaryData())	{
			Collection<ParametersSet> fixedPreliminarySet = miningParameters.getPreliminaryCalculationsParametersSets();

			supplementaryData = StorageScanner.getSupplementaryAlgorithmsResults(graphHandler, fixedPreliminarySet);
			//TODO If not all preliminary results are present or performance statistic is required then run computations.
		}

		totalWorkTimeMillis += MiningDriver.mineGraph(graphHandler, supplementaryData, extendedMiningParameters);
		return totalWorkTimeMillis;
	}


	private void computeCharacteristic(ScenarioTask task, GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet characteristicParameters, ArrangedData arrangedData) throws SourceGraphException	{
		if (task.recompute() == ScenarioTask.Recompute.MEASURES)	{	//Delete old results in case of recompute, they are irrelative.
			runCleaning(ScenarioTask.Goal.CLEAR, graphHandler, analysedDataIdentifier, characteristicParameters);
		}

		NumericCharacteristic value = null;
		try {
			value = StorageScanner.getStatisticValue(graphHandler, analysedDataIdentifier, characteristicParameters);
		} catch (StorageException e1) {
			e1.printStackTrace();
		}

		try {
			if (value == null)	{
				long timeStart = System.currentTimeMillis();
	
				try {
					value = CharacteristicsDriver.computeCharacteristic(graphHandler, analysedDataIdentifier, characteristicParameters);
				} catch (StorageException e) {
					e.printStackTrace();
				}
	
				System.out.print("\t"+getTimeString(System.currentTimeMillis()-timeStart));
			}
	
			if (arrangedData != null)	{
				try {
					arrangedData.putComputedStatistic(graphHandler, analysedDataIdentifier, characteristicParameters, value);
				} catch (DataArrangementException e) {
					e.printStackTrace();
				}
			}

		} catch (MeasureComputationException e) {
			e.printStackTrace();
		}
	}

	private String getTimeString(long timeMilliseconds)	{	//FUTURE_WORK Extract to some external class?
		float allSeconds = (float) (timeMilliseconds*0.001);

		int hours = (int) ((allSeconds<3600) ? 0 : (allSeconds/3600));
		float seconds = allSeconds - hours*3600;

		int minutes = (int) ((seconds<60) ? 0 : (seconds/60));
		seconds -= minutes*60;

		StringBuilder stringBuilder = new StringBuilder();
		if (hours > 0)	{
			stringBuilder.append(hours).append("h ");
		}
		if (minutes > 0)	{
			stringBuilder.append(minutes).append("m ");
		}
		stringBuilder.append(seconds).append("s");

		return stringBuilder.toString();
	}


	private void presentResultsToUser(ScenarioTask task, ArrangedData arrangedData)	{
		//if (task.doNeedCharts())	{	FUTURE_WORK
		//	System.out.println("\tDrawing charts.");
		//	ChartsDrawer.drawCharts(task, arrangedData);
		//}

		if (task.doNeedPlots())	{
			System.out.println("\tDrawing plots.");
			PlotsDrawer.drawPlots(task, arrangedData);
		}
	}


	private void runCleaning(ScenarioTask.Goal goal, GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet characteristicParameters)	{
		switch (goal)	{
		case CLEAR:
			StorageCleaner.clear(graphHandler, analysedDataIdentifier, characteristicParameters);
			break;
		case CLEARALL:
			StorageCleaner.clearall(graphHandler, analysedDataIdentifier, characteristicParameters);
			break;
		}
	}
}
