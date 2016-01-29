package ru.ispras.modis.NetBlox.utils;

/**
 * A container for a pair of values.
 * 
 * @author ilya
 */
public class Pair<T1, T2> {
	private T1 key;
	private T2 value;

	public Pair(T1 key, T2 value)	{
		this.key = key;
		this.value = value;
	}


	public T1 getKey()	{
		return key;
	}
	public T1 get1st()	{
		return getKey();
	}

	public T2 getValue()	{
		return value;
	}
	public T2 get2nd()	{
		return getValue();
	}

	public String toString()	{
		StringBuilder builder = new StringBuilder().
				append("[").append(key).append(" : ").append(value).append("]");
		return builder.toString();
	}
}
