package ru.ispras.modis.NetBlox.scenario;

import java.util.Collection;
import java.util.Iterator;

/**
 * Iterates consecutively over parameters sets described in several algorithm descriptions
 * equal and absolutely interchangeable from the point of view of the scenario.
 * 
 * @author ilya
 */
public class ParametersSetsIterator implements Iterator<ParametersSet>	{
	private Iterator<AlgorithmDescription> descriptionsIterator;
	private Iterator<ParametersSet> parameterSetsIterator;

	public ParametersSetsIterator(Collection<AlgorithmDescription> algoritmsDescriptions)	{
		descriptionsIterator = algoritmsDescriptions.iterator();
		parameterSetsIterator = descriptionsIterator.next().iterator();
	}


	@Override
	public boolean hasNext() {
		if (descriptionsIterator.hasNext() || parameterSetsIterator.hasNext())	{
			return true;
		}

		return false;
	}

	@Override
	public ParametersSet next() {
		if (parameterSetsIterator.hasNext())	{
			return parameterSetsIterator.next();
		}

		while (descriptionsIterator.hasNext())	{
			parameterSetsIterator = descriptionsIterator.next().iterator();
			if (parameterSetsIterator.hasNext())	{
				return parameterSetsIterator.next();
			}
		}

		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
