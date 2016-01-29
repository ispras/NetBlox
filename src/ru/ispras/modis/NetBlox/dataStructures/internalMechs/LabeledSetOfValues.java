package ru.ispras.modis.NetBlox.dataStructures.internalMechs;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import ru.ispras.modis.NetBlox.scenario.AlgorithmDescription;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;

public class LabeledSetOfValues	{
	private String label = "";
	private Map<String, String> values = null;	// Map<ID: String, value: String>

	private String graphDescriptionId;
	private String graphMinerDescriptionId;

	private Collection<String> averageByVariationsIds = null;


	/**
	 * Constructor.
	 * @param label		- String label for the set of values;
	 * @param values	- values arranged in map; keys are String IDs, values are also represented in <code>String</code>s;
	 * @param graphId	- ID of a graph;
	 * @param graphMinerId		- ID of a graph mining algorithm;
	 * @param averageByVariationsIds	- the IDs of variations along which the the results are to be averaged.
	 */
	public LabeledSetOfValues(String label, Map<String, String> values, String graphId, String graphMinerId, Collection<String> averageByVariationsIds)	{
		this.label = label;
		this.values = values;

		graphDescriptionId = graphId;
		graphMinerDescriptionId = graphMinerId;

		this.averageByVariationsIds = averageByVariationsIds;
	}


	public String getLabel()	{
		return label;
	}

	/**
	 * @return	values arranged in map; keys are String IDs, values are also represented in <code>String</code>s.
	 */
	public Map<String, String> getValuesForIds()	{
		return Collections.unmodifiableMap(values);
	}


	/**
	 * Checks whether we have <code>graphParameters</code> fixed from the graph description specified
	 * for this set of fixed values in data arrangement description in scenario.
	 * @param graphParameters
	 * @return
	 */
	public boolean doesGraphDescriptionFit(GraphParametersSet graphParameters)	{
		return graphDescriptionId.equals(graphParameters.getGraphDescriptionId());
	}

	public boolean doesGraphMinerDescriptionFit(GraphMiningParametersSet graphMiningParameters)	{
		return graphMinerDescriptionId.equals(graphMiningParameters.getAlgorithmDescriptionId());
	}

	public boolean isGraphMinerDescriptionSpecified()	{
		return !graphMinerDescriptionId.equals(AlgorithmDescription.NO_ID);
	}


	public boolean doAverageAlongVariation()	{
		return averageByVariationsIds != null  &&  !averageByVariationsIds.isEmpty();
	}

	public Collection<String> getAverageByVariationsIds()	{
		return averageByVariationsIds;
	}
}