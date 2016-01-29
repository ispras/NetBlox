package ru.ispras.modis.NetBlox.scenario;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.osgi.framework.Bundle;

import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;

/**
 * Graphs, supplementary algorithms, GCD algorithms, Quality Measures and graph&community Characteristic Statistics
 * can all be described by the name of the algorithm (its type) and the set of parameters used to generate/compute
 * the concrete instances.
 * 
 * @author ilya
 */
public abstract class AlgorithmDescription implements Iterable<ParametersSet> {
	public static final String NO_ID = "__NO_ID";	//-1;

	private String id = NO_ID;
	private String algorithmNameInScenario = null;

	protected RangeOfValues<Integer> launchNumbers = null;	//In some cases we might want to explore several _launches_ of same algorithm with exactly the same parameters.


	public void setId(String id)	{
		this.id = id;
	}

	public String getId()	{
		return id;
	}


	public void setAlgorithmNameInScenario(String name)	{
		this.algorithmNameInScenario = name;
	}

	public String getNameInScenario()	{
		return algorithmNameInScenario;
	}


	public void setLaunchNumbers(RangeOfValues<Integer> launchNumbers)	{
		this.launchNumbers = launchNumbers;
	}

	protected boolean doLaunchSeveralTimes()	{
		return launchNumbers != null;
	}


	/**
	 * This method is to assemble all variations that play part of <code>AlgorithmDescription</code>
	 * in one Collection. It is to be overridden in children.
	 * @return
	 */
	public Collection<RangeOfValues<?>> getAllVariations()	{
		Collection<RangeOfValues<?>> variations = new LinkedList<RangeOfValues<?>>();
		addNonNullVariation(launchNumbers, variations);
		return variations;
	}

	protected void addNonNullVariation(RangeOfValues<?> variation, Collection<RangeOfValues<?>> allVariations)	{
		if (variation != null)	{
			allVariations.add(variation);
		}
	}


	/**
	 * Get the URL for language resources directory in the plug-in that implements this specific algorithm description (and perhaps algorithm itself).
	 * @return
	 */
	public URL getLanguageResourcesURL()	{
		Bundle bundle = getImplementingPluginBundle();
		URL resourcesURL = bundle.getEntry(LanguagesConfiguration.DEFAULT_RELATIVE_LOCATION_OF_RESOURCES+SystemConfiguration.FILES_SEPARATOR);
		return resourcesURL;
	}

	protected abstract Bundle getImplementingPluginBundle();


	/**
	 * Superclass for algorithms parameters iterators.
	 */
	protected abstract class AlgorithmParametersIterator extends IteratorForParametersProbablyMissing	{
		protected Iterator<Integer> launchesIterator = getIterator(launchNumbers);

		protected Integer launchNumber = null;

		protected boolean resolveValues()	{
			if (!hasNext(launchesIterator))	{
				if (hasSingleIteration)	{
					hasSingleIteration = false;
					return true;
				}
				return false;
			}
			launchNumber = getNext(launchesIterator);
			return true;
		}


		@Override
		public boolean hasNext() {
			return hasNext(launchesIterator) || hasSingleIteration;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
