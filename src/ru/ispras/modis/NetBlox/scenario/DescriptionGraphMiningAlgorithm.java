package ru.ispras.modis.NetBlox.scenario;

import java.util.Collection;
import java.util.LinkedList;


/**
 * The container for general features of all graph mining algorithms as described in the scenario.
 * 
 * @author ilya
 */
public abstract class DescriptionGraphMiningAlgorithm extends AlgorithmDescription {
	protected ScenarioTask scenarioTask = null;

	protected Collection<String> supplementaryAlgorithmsIDs = null;

	//protected List<Integer> timeSlices = null;	//This range contains information about intermediate steps in time, iterations, time slices.
	//Left For FUTURE_WORK . RangeOfValues?

	public DescriptionGraphMiningAlgorithm()	{
		supplementaryAlgorithmsIDs = new LinkedList<String>();
	}


	public void addSupplementaryAlgorithmId(String id)	{
		supplementaryAlgorithmsIDs.add(id);
	}

	public void setScenarioTask(ScenarioTask scenarioTask)	{
		this.scenarioTask = scenarioTask;
	}

	/*	Left For FUTURE_WORK . RangeOfValues?
	 * Set the time slices requested in scenario for one (each) launch of the algorithm.
	 * @param timeSlices	- a range of intermediate steps in time, iterations, time slices that are requested in scenario for this algorithm.
	 *
	public void setTimeSlices(List<Integer> timeSlices)	{
		this.timeSlices = timeSlices;
	}*/
}
