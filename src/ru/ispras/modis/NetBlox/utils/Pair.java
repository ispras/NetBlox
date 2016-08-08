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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
