package ru.ispras.modis.NetBlox.scenario;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.utils.MiningJobBase;

/**
 * The basic class for the descriptions of measures (numeric characteristics) in scenario
 * (<measures/> section) that will be computed over graph mining results.
 * 
 * @author ilya
 */
public abstract class DescriptionMeasure extends AlgorithmDescription {
	private static final String ARGUMENT_KEY_SUFFIX = "_argument";

	protected MiningJobBase jobBase;

	public DescriptionMeasure(MiningJobBase.JobBase jobBase)	{
		this.jobBase = new MiningJobBase(jobBase);
	}

	@Override
	public Iterator<ParametersSet> iterator() {
		MeasureParametersSet measureParameters = new MeasureParametersSet(getNameInScenario(), jobBase);
		ArrayList<ParametersSet> oneItemArray = new ArrayList<ParametersSet>(1);
		oneItemArray.add(measureParameters);

		return oneItemArray.iterator();
	}


	/**
	 * Does the measure parameters set belong to this measure description.
	 * @param measureParametersSet
	 * @return
	 */
	public boolean doesBelong(MeasureParametersSet measureParametersSet)	{
		return getNameInScenario().equals(measureParametersSet.getCharacteristicNameInScenario());
		//XXX Add information about implementor or use some more unique ID.
	}


	/**
	 * Get the name (label) for the values of this characteristic (measure).
	 * @return	the string label to be used on plots, etc.
	 */
	public String getValuesName()	{
		URL resourcesURL = getLanguageResourcesURL();
		LanguagesConfiguration languageConfiguration = LanguagesConfiguration.getConfiguration(resourcesURL);
		String valuesName = languageConfiguration.getLabel(getNameInScenario());
		return valuesName;
	}

	/**
	 * For functional measure types. Returns the name of the argument of the function.
	 */
	public String getFunctionArgumentName()	{
		URL resourcesURL = getLanguageResourcesURL();
		LanguagesConfiguration languageConfiguration = LanguagesConfiguration.getConfiguration(resourcesURL);
		String key = getNameInScenario() + ARGUMENT_KEY_SUFFIX;
		String argumentName = languageConfiguration.getLabel(key);
		return argumentName;
	}
}
