package ru.ispras.modis.NetBlox.scenario;

import java.util.Iterator;

public abstract class IteratorForParametersProbablyMissing implements Iterator<ParametersSet> {
	protected boolean mayInitiateValues = false;
	protected boolean hasSingleIteration = false;	//For the case when no variations have been described but at least a single iteration is necessary.

	protected <T> T initiateValue(Iterator<T> iterator)	{
		T result = null;
		if (iterator != null)	{
			if (mayInitiateValues)	{
				result = iterator.next();
			}
			else	{
				mayInitiateValues = true;
			}
		}
		return result;
	}

	protected <T> Iterator<T> getIterator(RangeOfValues<T> range)	{
		return (range==null) ? null : range.iterator();
	}

	protected <T> T getNext(Iterator<T> iterator)	{
		return (iterator==null) ? null : iterator.next();
	}

	protected <T> boolean hasNext(Iterator<T> iterator)	{
		return (iterator==null) ? false : iterator.hasNext();
	}

	protected <T> ValueFromRange<T> makeValueFromRangeInstance(RangeOfValues<T> range, T value)	{
		if (value == null)	{
			return null;
		}
		String rangeId = (range==null) ? RangeOfValues.NO_RANGE_ID : range.getRangeId();
		return new ValueFromRange<T>(rangeId, value);
	}
}
