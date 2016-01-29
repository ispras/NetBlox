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

	public DescriptionGraphMiningAlgorithm()	{
		supplementaryAlgorithmsIDs = new LinkedList<String>();
	}


	public void addSupplementaryAlgorithmId(String id)	{
		supplementaryAlgorithmsIDs.add(id);
	}

	public void setScenarioTask(ScenarioTask scenarioTask)	{
		this.scenarioTask = scenarioTask;
	}
}
