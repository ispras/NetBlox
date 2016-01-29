package ru.ispras.modis.NetBlox.scenario;


/**
 * Contains the value with the ID of the range (parameter variation) it comes from.
 * @author ilya
 *
 * @param <T> - the type of the value itself.
 */
public class ValueFromRange <T> {
	private String rangeId = RangeOfValues.NO_RANGE_ID;
	private T value;

	public ValueFromRange(String rangeId, T value)	{
		this.rangeId = rangeId;
		this.value = value;
	}


	public String getRangeId()	{
		return rangeId;
	}

	public T getValue()	{
		return value;
	}

	public void setValue(T value)	{
		this.value = value;
	}


	@Override
	public boolean equals(Object obj)	{
		if (obj instanceof ValueFromRange)	{
			ValueFromRange<?> compared = (ValueFromRange<?>) obj;

			return rangeId.equals(compared.rangeId) &&
					value.equals(compared.value);
		}
		else	{
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode()	{
		return rangeId.hashCode() + value.hashCode();
	}

	@Override
	public ValueFromRange<T> clone()	{
		return new ValueFromRange<T>(rangeId, value);
	}
}
