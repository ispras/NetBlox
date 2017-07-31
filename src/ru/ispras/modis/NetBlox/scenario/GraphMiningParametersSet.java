package ru.ispras.modis.NetBlox.scenario;

import java.util.Collections;
import java.util.List;

import ru.ispras.modis.NetBlox.utils.MiningJobBase;

/**
 * A fixed set of parameters for a graph mining algorithm (for one launch; basic class for such set).
 * 
 * @author ilya
 */
public abstract class GraphMiningParametersSet extends ParametersSet {
	private MiningJobBase.JobBase jobBase;

	private String algorithmNameInScenario;
	private String algorithmDescriptionId;

	private List<ParametersSet> listOfPreliminaryCalculationsParametersSets = null;
	protected ValueFromRange<Integer> launchNumber = null;

	//timeSlices list contains information about intermediate steps in time, iterations, time slices in _one_launch_ of an algorithm.
	private List<Integer> timeSlices = null;


	public GraphMiningParametersSet(MiningJobBase.JobBase jobBase, String algorithmNameInScenario, String algorithmDescriptionID)	{
		this.jobBase = jobBase;
		this.algorithmNameInScenario = algorithmNameInScenario;
		this.algorithmDescriptionId = algorithmDescriptionID;
	}

	public GraphMiningParametersSet(MiningJobBase.JobBase jobBase, String algorithmNameInScenario, String algorithmDescriptionID,
			List<ParametersSet> listOfPreliminaryCalculationsParametersSets)	{
		this(jobBase, algorithmNameInScenario, algorithmDescriptionID);
		this.listOfPreliminaryCalculationsParametersSets = listOfPreliminaryCalculationsParametersSets;
	}


	public String getAlgorithmName()	{
		return algorithmNameInScenario;
	}

	public String getAlgorithmDescriptionId()	{
		return algorithmDescriptionId;
	}

	public MiningJobBase.JobBase getJobBase()	{
		return jobBase;
	}


	public boolean useSupplementaryData()	{
		return (listOfPreliminaryCalculationsParametersSets != null)  &&
				!listOfPreliminaryCalculationsParametersSets.isEmpty();
	}

	public List<ParametersSet> getPreliminaryCalculationsParametersSets()	{
		return listOfPreliminaryCalculationsParametersSets;
	}


	/**
	 * Is for the case when the algorithm is launched multiple times with same set of parameters. 
	 * @param launchNumber
	 */
	public void setLaunchNumber(ValueFromRange<Integer> launchNumber)	{
		this.launchNumber = launchNumber;
	}

	public boolean useMultipleLaunches()	{
		return launchNumber != null;
	}

	public Integer getLaunchNumber()	{
		return (launchNumber==null) ? null : launchNumber.getValue();
	}


	public boolean hasParametersFromSomeRange() {
		return (launchNumber != null)  &&  (!launchNumber.getRangeId().equals(RangeOfValues.NO_RANGE_ID));
	}

	@Override
	public Object getValueForVariationId(String id) {
		Object result = null;
		if ((launchNumber != null)  &&  (id.equals(launchNumber.getRangeId())))	{
			result = getLaunchNumber();
		}

		return result;
	}

	public boolean hasVariationBeenFixedConstant(String variationId)	{
		return false;
	}


	/**
	 * Set the time slices requested in scenario for one (each) launch of the algorithm.
	 * @param timeSlices	- a range of intermediate steps in time, iterations, time slices that are requested in scenario for this algorithm.
	 */
	public void setTimeSlices(List<Integer> timeSlices)	{
		Collections.sort(timeSlices);
		this.timeSlices = timeSlices;
	}

	/**
	 * Are there time slices for the algorithm for this set of parameters?
	 */
	public boolean considerTimeSlices()	{
		return timeSlices!=null && !timeSlices.isEmpty();
	}

	public List<Integer> getTimeSlices()	{
		return timeSlices;
	}
}
