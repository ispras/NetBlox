package ru.ispras.modis.NetBlox.scenario;

import java.util.List;

import ru.ispras.modis.NetBlox.utils.MiningJobBase;
import ru.ispras.modis.NetBlox.utils.Pair;

/**
 * The basic class for the sets of parameters for measures (numeric characteristics) as
 * described in scenario (<measures/> section) that will be computed over graph mining
 * results.
 * 
 * XXX How about making this class abstract, so that the authors of plug-ins would always
 * remember to create children, implementing the methods in case of varying parameters?
 * 
 * @author ilya
 */
public class MeasureParametersSet extends ParametersSet {

	private String numericCharacteristicNameInScenario;

	private MiningJobBase jobBase;

	public MeasureParametersSet(String numericCharacteristicNameInScenario, MiningJobBase jobBase)	{
		this.numericCharacteristicNameInScenario = numericCharacteristicNameInScenario;
		this.jobBase = jobBase;
	}


	public String getCharacteristicNameInScenario()	{
		return numericCharacteristicNameInScenario;
	}

	public MiningJobBase.JobBase getJobBase()	{
		return jobBase.getJobBase();
	}


	@Override
	public boolean hasParametersFromSomeRange() {
		return false;
	}

	@Override
	public Object getValueForVariationId(String id) {
		return null;
	}

	@Override
	public List<Pair<String, String>> getSpecifiedParametersAsPairsOfUniqueKeysAndValues() {
		return null;
	}

}
